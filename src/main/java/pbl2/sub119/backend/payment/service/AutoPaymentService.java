package pbl2.sub119.backend.payment.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pbl2.sub119.backend.common.enumerated.BillingKeyStatus;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.common.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;
import pbl2.sub119.backend.party.common.service.PartyHistoryService;
import pbl2.sub119.backend.party.cycle.service.PartyCycleService;
import pbl2.sub119.backend.payment.dto.PaymentChargeTarget;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;
import pbl2.sub119.backend.payment.mapper.PaymentExecutionQueryMapper;
import pbl2.sub119.backend.settlement.event.SettlementRequestedEvent;
import pbl2.sub119.backend.toss.client.TossPaymentClient;
import pbl2.sub119.backend.toss.dto.request.TossBillingPaymentRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoPaymentService {

    private final PartyCycleMapper partyCycleMapper;
    private final PaymentExecutionQueryMapper paymentExecutionQueryMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyMapper partyMapper;
    private final PartyHistoryService partyHistoryService;
    private final TossPaymentClient tossPaymentClient;
    private final ApplicationEventPublisher eventPublisher;
    private final PartyCycleService partyCycleService;

    @Transactional
    public void execute(Long partyId, Long partyCycleId) {
        PartyCycle cycle = partyCycleMapper.findById(partyCycleId);
        if (cycle == null) {
            return;
        }

        if (!claimCycle(partyCycleId)) {
            log.info("자동결제 선점 실패 또는 이미 처리 중. partyId={}, partyCycleId={}", partyId, partyCycleId);
            return;
        }

        partyMapper.updateOperationStatus(partyId, OperationStatus.ACTIVE);

        if (cycle.getCycleNo() > 1) {
            // 반복회차 정책:
            // 1) 상태변이 먼저
            // 2) 상태변이 후 ACTIVE MEMBER 수로 현재 cycle snapshot 재동기화
            // 3) 그 다음 ACTIVE MEMBER만 결제
            partyCycleService.confirmRecurringCycleStart(partyId);
            refreshRecurringSnapshot(partyId, partyCycleId);
        }

        PaymentTargets paymentTargets = resolvePaymentTargets(partyId, partyCycleId, cycle);
        if (paymentTargets.expectedMemberCount() == 0
                || paymentTargets.targets().size() != paymentTargets.expectedMemberCount()) {
            failCycle(cycle, partyId, partyCycleId, null, 0L, "INVALID_CHARGE_TARGET_COUNT");
            return;
        }

        for (PaymentChargeTarget target : paymentTargets.targets()) {
            try {
                tossPaymentClient.executeBillingPayment(
                        target.getBillingKey(),
                        new TossBillingPaymentRequest(
                                target.getCustomerKey(),
                                target.getAmount(),
                                createOrderId(target),
                                "Submate 파티 이용료"
                        ),
                        createIdempotencyKey(target)
                );

                if (paymentTargets.targetMemberStatus() == PartyMemberStatus.PENDING) {
                    partyMemberMapper.updateStatusAndActivatedAt(
                            target.getMemberId(),
                            PartyMemberStatus.ACTIVE
                    );
                }

                partyHistoryService.saveHistory(
                        partyId,
                        target.getMemberId(),
                        PartyHistoryEventType.PAYMENT_EXECUTION_SUCCEEDED,
                        "{\"userId\":" + target.getUserId() + "}",
                        target.getUserId()
                );

            } catch (Exception e) {
                log.error("자동결제 실패. partyId={}, partyCycleId={}, memberId={}, userId={}",
                        partyId, partyCycleId, target.getMemberId(), target.getUserId(), e);

                failCycle(
                        cycle,
                        partyId,
                        partyCycleId,
                        target.getMemberId(),
                        target.getUserId(),
                        "PAYMENT_API_FAILED"
                );
                return;
            }
        }

        int updated = partyCycleMapper.compareAndUpdateStatus(
                partyCycleId,
                PartyCycleStatus.PROCESSING,
                PartyCycleStatus.RUNNING
        );
        if (updated != 1) {
            throw new IllegalStateException("party_cycle RUNNING 상태 전이 실패. partyCycleId=" + partyCycleId);
        }

        closePreviousCycleIfExists(cycle);

        if (cycle.getCycleNo() == 1) {
            partyCycleService.handleCycleStart(partyId);
        }

        partyMapper.updateOperationStatus(partyId, OperationStatus.ACTIVE);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("정산 이벤트 발행(afterCommit). partyId={}, partyCycleId={}", partyId, partyCycleId);
                eventPublisher.publishEvent(new SettlementRequestedEvent(partyId, partyCycleId));
            }
        });

        log.info("자동결제 전원 성공. partyId={}, partyCycleId={}", partyId, partyCycleId);
    }

    private void refreshRecurringSnapshot(Long partyId, Long partyCycleId) {
        int billableMemberCount = partyCycleService.countRecurringBillableMembers(partyId);

        int updated = partyCycleMapper.updateMemberCountSnapshot(
                partyCycleId,
                billableMemberCount,
                PartyCycleStatus.PROCESSING
        );

        if (updated != 1) {
            throw new IllegalStateException("party_cycle snapshot 갱신 실패. partyCycleId=" + partyCycleId);
        }

        log.info("반복회차 snapshot 재동기화 완료. partyId={}, partyCycleId={}, memberCountSnapshot={}",
                partyId, partyCycleId, billableMemberCount);
    }

    private boolean claimCycle(Long partyCycleId) {
        int claimed = partyCycleMapper.compareAndUpdateStatus(
                partyCycleId,
                PartyCycleStatus.PAYMENT_PENDING,
                PartyCycleStatus.PROCESSING
        );
        if (claimed == 1) {
            return true;
        }

        return partyCycleMapper.compareAndUpdateStatus(
                partyCycleId,
                PartyCycleStatus.FAILED,
                PartyCycleStatus.PROCESSING
        ) == 1;
    }

    private PaymentTargets resolvePaymentTargets(Long partyId, Long partyCycleId, PartyCycle cycle) {
        if (cycle.getCycleNo() == 1) {
            PartyMemberStatus targetMemberStatus = PartyMemberStatus.PENDING;
            int expectedMemberCount = paymentExecutionQueryMapper.countMembersByStatus(
                    partyId,
                    PartyRole.MEMBER,
                    targetMemberStatus
            );
            List<PaymentChargeTarget> targets = paymentExecutionQueryMapper.findChargeTargets(
                    partyId,
                    partyCycleId,
                    PartyRole.MEMBER,
                    targetMemberStatus,
                    BillingKeyStatus.ACTIVE
            );
            return new PaymentTargets(targetMemberStatus, expectedMemberCount, targets);
        }

        // 반복회차는 상태변이 이후 ACTIVE인 MEMBER만 결제 대상으로 조회한다.
        int expectedMemberCount = paymentExecutionQueryMapper.countRecurringBillableMembers(
                partyId,
                PartyRole.MEMBER
        );
        List<PaymentChargeTarget> targets = paymentExecutionQueryMapper.findRecurringChargeTargets(
                partyId,
                partyCycleId,
                PartyRole.MEMBER,
                BillingKeyStatus.ACTIVE
        );
        return new PaymentTargets(null, expectedMemberCount, targets);
    }

    private void closePreviousCycleIfExists(PartyCycle currentCycle) {
        if (currentCycle.getCycleNo() <= 1) {
            return;
        }

        PartyCycle previousCycle = partyCycleMapper.findByPartyIdAndCycleNo(
                currentCycle.getPartyId(),
                currentCycle.getCycleNo() - 1
        );

        if (previousCycle == null) {
            return;
        }

        int closed = partyCycleMapper.closeCycle(
                previousCycle.getId(),
                PartyCycleStatus.RUNNING,
                PartyCycleStatus.CLOSED,
                currentCycle.getBillingDueAt()
        );

        if (closed == 1) {
            log.info("이전 cycle 종료 처리 완료. partyId={}, previousCycleId={}, currentCycleId={}",
                    currentCycle.getPartyId(), previousCycle.getId(), currentCycle.getId());
        }
    }

    private void failCycle(
            PartyCycle cycle,
            Long partyId,
            Long partyCycleId,
            Long memberId,
            Long createdBy,
            String reason
    ) {
        int updated = partyCycleMapper.compareAndUpdateStatus(
                partyCycleId,
                PartyCycleStatus.PROCESSING,
                PartyCycleStatus.FAILED
        );
        if (updated != 1) {
            throw new IllegalStateException("party_cycle FAILED 상태 전이 실패. partyCycleId=" + partyCycleId);
        }

        if (cycle.getCycleNo() == 1) {
            partyMapper.updateOperationStatus(partyId, OperationStatus.WAITING_START);
        }

        partyHistoryService.saveHistory(
                partyId,
                memberId,
                PartyHistoryEventType.PAYMENT_EXECUTION_FAILED,
                "{\"reason\":\"" + reason + "\"}",
                createdBy
        );
    }

    private String createOrderId(PaymentChargeTarget target) {
        return "party-" + target.getPartyId()
                + "-cycle-" + target.getPartyCycleId()
                + "-user-" + target.getUserId();
    }

    private String createIdempotencyKey(PaymentChargeTarget target) {
        return "settle:party:" + target.getPartyId()
                + ":cycle:" + target.getPartyCycleId()
                + ":user:" + target.getUserId();
    }

    private record PaymentTargets(
            PartyMemberStatus targetMemberStatus,
            int expectedMemberCount,
            List<PaymentChargeTarget> targets
    ) {
    }
}