package pbl2.sub119.backend.payment.service;

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
import pbl2.sub119.backend.party.common.enumerated.PartyRole;
import pbl2.sub119.backend.payment.dto.PaymentChargeTarget;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.entity.PartyCycleMemberPayment;
import pbl2.sub119.backend.payment.enumerated.MemberPaymentStatus;
import pbl2.sub119.backend.payment.event.PartyCyclePaymentCompletedEvent;
import pbl2.sub119.backend.payment.event.PartyCyclePaymentFailedEvent;
import pbl2.sub119.backend.payment.mapper.PartyCycleMemberPaymentMapper;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;
import pbl2.sub119.backend.payment.mapper.PaymentExecutionQueryMapper;
import pbl2.sub119.backend.toss.client.TossPaymentClient;
import pbl2.sub119.backend.toss.dto.request.TossBillingPaymentRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoPaymentService {

    private final PartyCycleMapper partyCycleMapper;
    private final PaymentExecutionQueryMapper paymentExecutionQueryMapper;
    private final PartyCycleMemberPaymentMapper memberPaymentMapper;
    private final TossPaymentClient tossPaymentClient;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void execute(Long partyId, Long partyCycleId) {
        PartyCycle cycle = partyCycleMapper.findById(partyCycleId);
        if (cycle == null) {
            return;
        }

        if (!claimCycle(partyCycleId)) {
            log.info("자동결제 선점 실패. partyId={}, partyCycleId={}", partyId, partyCycleId);
            return;
        }

        if (cycle.getCycleNo() > 1) {
            refreshRecurringSnapshot(partyId, partyCycleId);
        }

        PaymentTargets paymentTargets = resolvePaymentTargets(partyId, partyCycleId, cycle);

        if (paymentTargets.expectedMemberCount() == 0
                || paymentTargets.targets().size() != paymentTargets.expectedMemberCount()) {
            failCycleAndPublish(partyId, partyCycleId, "INVALID_CHARGE_TARGET_COUNT");
            return;
        }

        initMemberPayments(partyId, partyCycleId, paymentTargets.targets());

        for (PaymentChargeTarget target : paymentTargets.targets()) {
            int claimed = memberPaymentMapper.compareAndUpdateStatus(
                    partyCycleId, target.getMemberId(),
                    MemberPaymentStatus.PAYMENT_PENDING, MemberPaymentStatus.PROCESSING);

            if (claimed == 0) {
                log.warn("멤버 결제 선점 실패(이미 처리됨). partyCycleId={}, memberId={}",
                        partyCycleId, target.getMemberId());
                continue;
            }

            try {
                var response = tossPaymentClient.executeBillingPayment(
                        target.getBillingKey(),
                        new TossBillingPaymentRequest(
                                target.getCustomerKey(),
                                target.getAmount(),
                                createOrderId(target),
                                "Submate 파티 이용료"),
                        createIdempotencyKey(target)
                );

                memberPaymentMapper.markPaid(
                        partyCycleId, target.getMemberId(),
                        response.paymentKey(), LocalDateTime.now());

            } catch (Exception e) {
                log.error("자동결제 실패. partyId={}, partyCycleId={}, memberId={}",
                        partyId, partyCycleId, target.getMemberId(), e);

                memberPaymentMapper.markFailed(
                        partyCycleId, target.getMemberId(),
                        e.getMessage(), e.getClass().getSimpleName(), LocalDateTime.now());

                failCycleAndPublish(partyId, partyCycleId, "PAYMENT_API_FAILED");
                return;
            }
        }

        // DB 기준 최종 완료 판정 (claimed==0 skip 케이스 포함)
        int nonPaidCount = memberPaymentMapper.countNonPaid(partyCycleId);
        if (nonPaidCount > 0) {
            log.warn("미완료 멤버 존재. partyId={}, partyCycleId={}, nonPaidCount={}",
                    partyId, partyCycleId, nonPaidCount);
            failCycleAndPublish(partyId, partyCycleId, "INCOMPLETE_PAYMENT");
            return;
        }

        // DB 기준 실제 PAID 카운트 (targets.size() 대신 DB 재조회)
        int paidCount  = memberPaymentMapper.countByStatus(partyCycleId, MemberPaymentStatus.PAID);
        int totalCount = paymentTargets.expectedMemberCount();
        registerAfterCommit(() ->
                eventPublisher.publishEvent(new PartyCyclePaymentCompletedEvent(
                        partyId, partyCycleId, cycle.getCycleNo(), paidCount, totalCount))
        );

        log.info("자동결제 전원 성공. partyId={}, partyCycleId={}", partyId, partyCycleId);
    }

    private void initMemberPayments(Long partyId, Long partyCycleId,
                                    List<PaymentChargeTarget> targets) {
        LocalDateTime now = LocalDateTime.now();
        for (PaymentChargeTarget target : targets) {
            memberPaymentMapper.insertIfAbsent(
                    PartyCycleMemberPayment.builder()
                            .partyCycleId(partyCycleId)
                            .partyId(partyId)
                            .partyMemberId(target.getMemberId())
                            .userId(target.getUserId())
                            .amount(target.getAmount())
                            .status(MemberPaymentStatus.PAYMENT_PENDING)
                            .idempotencyKey(createIdempotencyKey(target))
                            .createdAt(now)
                            .updatedAt(now)
                            .build()
            );
        }
    }

    private void refreshRecurringSnapshot(Long partyId, Long partyCycleId) {
        int activeMemberCount = paymentExecutionQueryMapper.countActiveMembersByPartyId(partyId);

        int updated = partyCycleMapper.updateMemberCountSnapshot(
                partyCycleId, activeMemberCount, PartyCycleStatus.PROCESSING);

        if (updated != 1) {
            throw new IllegalStateException(
                    "party_cycle snapshot 갱신 실패. partyCycleId=" + partyCycleId);
        }
    }

    // PAYMENT_PENDING → PROCESSING 만 허용 (FAILED → PROCESSING 자동 재시도 금지)
    private boolean claimCycle(Long partyCycleId) {
        return partyCycleMapper.compareAndUpdateStatus(
                partyCycleId,
                PartyCycleStatus.PAYMENT_PENDING,
                PartyCycleStatus.PROCESSING) == 1;
    }

    // CAS 성공(1회) 시에만 afterCommit 이벤트 등록 → 멱등성 보장
    private void failCycleAndPublish(Long partyId, Long partyCycleId, String reason) {
        int updated = partyCycleMapper.compareAndUpdateStatus(
                partyCycleId, PartyCycleStatus.PROCESSING, PartyCycleStatus.FAILED);

        if (updated != 1) {
            log.warn("party_cycle FAILED 전이 실패(이미 처리됨). partyCycleId={}", partyCycleId);
            return;
        }

        // failedCount/pendingCount: DB 재조회 기준 (루프 변수 금지)
        // pendingCount: PAYMENT_PENDING + PROCESSING 합산 (장애 시 PROCESSING 잔존 포함)
        int failedCount  = memberPaymentMapper.countByStatus(partyCycleId, MemberPaymentStatus.FAILED);
        int pendingCount = memberPaymentMapper.countPendingOrProcessing(partyCycleId);

        registerAfterCommit(() ->
                eventPublisher.publishEvent(new PartyCyclePaymentFailedEvent(
                        partyId, partyCycleId, failedCount, pendingCount, reason))
        );
    }

    private PaymentTargets resolvePaymentTargets(Long partyId, Long partyCycleId,
                                                  PartyCycle cycle) {
        if (cycle.getCycleNo() == 1) {
            int expected = paymentExecutionQueryMapper.countMembersByStatus(
                    partyId, PartyRole.MEMBER, PartyMemberStatus.PENDING);
            List<PaymentChargeTarget> targets = paymentExecutionQueryMapper.findChargeTargets(
                    partyId, partyCycleId, PartyRole.MEMBER,
                    PartyMemberStatus.PENDING, BillingKeyStatus.ACTIVE);
            return new PaymentTargets(expected, targets);
        }

        int expected = paymentExecutionQueryMapper.countActiveMembersByPartyId(partyId);
        List<PaymentChargeTarget> targets = paymentExecutionQueryMapper.findRecurringChargeTargets(
                partyId, partyCycleId, PartyRole.MEMBER, BillingKeyStatus.ACTIVE);
        return new PaymentTargets(expected, targets);
    }

    private void registerAfterCommit(Runnable action) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    private String createOrderId(PaymentChargeTarget target) {
        return "party-" + target.getPartyId()
                + "-cycle-" + target.getPartyCycleId()
                + "-user-" + target.getUserId()
                + "-" + UUID.randomUUID();
    }

    private String createIdempotencyKey(PaymentChargeTarget target) {
        return "settle:party:" + target.getPartyId()
                + ":cycle:" + target.getPartyCycleId()
                + ":user:" + target.getUserId();
    }

    private record PaymentTargets(int expectedMemberCount, List<PaymentChargeTarget> targets) {}
}