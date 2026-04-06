package pbl2.sub119.backend.toss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pbl2.sub119.backend.common.enumerated.BillingKeyStatus;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.party.enumerated.OperationStatus;
import pbl2.sub119.backend.party.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.enumerated.PartyRole;
import pbl2.sub119.backend.party.mapper.PartyMapper;
import pbl2.sub119.backend.party.mapper.PartyMemberMapper;
import pbl2.sub119.backend.party.service.PartyHistoryService;
import pbl2.sub119.backend.payment.dto.PaymentChargeTarget;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;
import pbl2.sub119.backend.payment.mapper.PaymentExecutionQueryMapper;
import pbl2.sub119.backend.settlement.event.SettlementRequestedEvent;
import pbl2.sub119.backend.toss.client.TossPaymentClient;
import pbl2.sub119.backend.toss.dto.request.TossBillingPaymentRequest;

import java.util.List;
import java.util.UUID;

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

    @Transactional
    public void execute(Long partyId, Long partyCycleId) {
        PartyCycle cycle = partyCycleMapper.findById(partyCycleId);
        if (cycle == null) {
            return;
        }

        int claimed = partyCycleMapper.compareAndUpdateStatus(
                partyCycleId,
                PartyCycleStatus.PAYMENT_PENDING,
                PartyCycleStatus.PROCESSING
        );
        if (claimed != 1) {
            log.info("자동결제 선점 실패 또는 이미 처리 중. partyId={}, partyCycleId={}", partyId, partyCycleId);
            return;
        }

        partyMapper.updateOperationStatus(partyId, OperationStatus.ACTIVE);

        PartyMemberStatus targetMemberStatus = resolveTargetMemberStatus(cycle);

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

        if (expectedMemberCount == 0 || targets.size() != expectedMemberCount) {
            failCycle(partyId, partyCycleId, null, 0L, "INVALID_CHARGE_TARGET_COUNT");
            return;
        }

        for (PaymentChargeTarget target : targets) {
            try {
                tossPaymentClient.executeBillingPayment(
                        target.getBillingKey(),
                        new TossBillingPaymentRequest(
                                target.getCustomerKey(),
                                target.getAmount(),
                                createOrderId(target),
                                "Submate 파티 이용료"
                        )
                );

                if (targetMemberStatus == PartyMemberStatus.PENDING) {
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

    private PartyMemberStatus resolveTargetMemberStatus(PartyCycle cycle) {
        if (cycle.getCycleNo() == 1) {
            return PartyMemberStatus.PENDING;
        }
        return PartyMemberStatus.ACTIVE;
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
            log.info("이전 회차 종료 처리 완료. partyId={}, previousCycleId={}, currentCycleId={}",
                    currentCycle.getPartyId(), previousCycle.getId(), currentCycle.getId());
        }
    }

    private void failCycle(
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

        partyMapper.updateOperationStatus(partyId, OperationStatus.WAITING_START);

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
                + "-user-" + target.getUserId()
                + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}