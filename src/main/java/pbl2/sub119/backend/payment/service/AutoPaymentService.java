package pbl2.sub119.backend.payment.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import pbl2.sub119.backend.common.enumerated.BillingKeyStatus;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;
import pbl2.sub119.backend.payment.dto.PaymentChargeTarget;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.entity.PartyCycleMemberPayment;
import pbl2.sub119.backend.payment.enumerated.MemberPaymentStatus;
import pbl2.sub119.backend.notification.event.event.PaymentFailedEvent;
import pbl2.sub119.backend.notification.event.event.PaymentSucceededEvent;
import pbl2.sub119.backend.payment.event.PartyCyclePaymentCompletedEvent;
import pbl2.sub119.backend.payment.event.PartyCyclePaymentFailedEvent;
import pbl2.sub119.backend.payment.mapper.PartyCycleMemberPaymentMapper;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;
import pbl2.sub119.backend.payment.mapper.PaymentExecutionQueryMapper;
import pbl2.sub119.backend.toss.client.PaymentGatewayClient;
import pbl2.sub119.backend.toss.dto.request.TossBillingPaymentRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoPaymentService {

    private final PartyCycleMapper partyCycleMapper;
    private final PaymentExecutionQueryMapper paymentExecutionQueryMapper;
    private final PartyCycleMemberPaymentMapper memberPaymentMapper;
    private final PaymentGatewayClient paymentGatewayClient;
    private final ApplicationEventPublisher eventPublisher;
    private final PlatformTransactionManager transactionManager;


    private TransactionTemplate requiresNewTx;

    @PostConstruct
    void initTransactionTemplate() {
        this.requiresNewTx = new TransactionTemplate(transactionManager);
        this.requiresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }


    public void execute(Long partyId, Long partyCycleId) {


        final ClaimResult claimResult = requiresNewTx.execute(status -> {

            PartyCycle cycle = partyCycleMapper.findById(partyCycleId);
            if (cycle == null) {
                return null;
            }

            if (!claimCycle(partyCycleId)) {
                log.info("자동결제 선점 실패. partyId={}, partyCycleId={}", partyId, partyCycleId);
                return null;
            }

            if (cycle.getCycleNo() > 1) {
                refreshRecurringSnapshot(partyId, partyCycleId);
            }

            PaymentTargets paymentTargets = resolvePaymentTargets(partyId, partyCycleId, cycle);

            if (paymentTargets.expectedMemberCount() == 0
                    || paymentTargets.targets().size() != paymentTargets.expectedMemberCount()) {
                failCycleAndPublish(partyId, partyCycleId, "INVALID_CHARGE_TARGET_COUNT");
                return null;
            }

            batchInitMemberPayments(partyId, partyCycleId, paymentTargets.targets());

            List<PaymentChargeTarget> claimedTargets = new ArrayList<>();
            for (PaymentChargeTarget target : paymentTargets.targets()) {
                int claimed = memberPaymentMapper.compareAndUpdateStatus(
                        partyCycleId, target.getMemberId(),
                        MemberPaymentStatus.PAYMENT_PENDING, MemberPaymentStatus.PROCESSING);
                if (claimed == 1) {
                    claimedTargets.add(target);
                } else {
                    log.warn("멤버 결제 선점 실패(이미 처리됨). partyCycleId={}, memberId={}",
                            partyCycleId, target.getMemberId());
                }
            }

            return new ClaimResult(cycle, claimedTargets, paymentTargets.expectedMemberCount());
        });

        if (claimResult == null) {

            return;
        }


        final List<MemberPaymentResult> pgResults =
                executePgCalls(partyId, partyCycleId, claimResult.claimedTargets());

        try {
            requiresNewTx.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    recordResultsAndFinalize(partyId, partyCycleId, claimResult, pgResults);
                }
            });
        } catch (Exception e) {
            log.error("Phase3 결제 결과 기록 실패. partyCycleId={}. 마감 처리기에 의해 복구 예정.",
                    partyCycleId, e);
        }
    }


    private List<MemberPaymentResult> executePgCalls(
            Long partyId, Long partyCycleId, List<PaymentChargeTarget> targets) {

        List<MemberPaymentResult> results = new ArrayList<>(targets.size());

        for (PaymentChargeTarget target : targets) {
            try {
                var response = paymentGatewayClient.executeBillingPayment(
                        target.getBillingKey(),
                        new TossBillingPaymentRequest(
                                target.getCustomerKey(),
                                target.getAmount(),
                                createOrderId(target),
                                "Submate 파티 이용료"),
                        createIdempotencyKey(target)
                );
                results.add(MemberPaymentResult.success(target, response.paymentKey(), LocalDateTime.now()));

            } catch (Exception e) {
                log.error("자동결제 PG 호출 실패. partyId={}, partyCycleId={}, memberId={}",
                        partyId, partyCycleId, target.getMemberId(), e);
                results.add(MemberPaymentResult.failure(
                        target, e.getMessage(), e.getClass().getSimpleName(), LocalDateTime.now()));
                // 기존 동작 유지: 첫 실패 시 이후 멤버 호출 중단
                break;
            }
        }

        return results;
    }


    private void recordResultsAndFinalize(
            Long partyId, Long partyCycleId,
            ClaimResult claimResult, List<MemberPaymentResult> pgResults) {

        for (MemberPaymentResult result : pgResults) {
            if (result.success()) {
                memberPaymentMapper.markPaid(
                        partyCycleId, result.target().getMemberId(),
                        result.paymentKey(), result.paidAt());

                final Long succeededUserId = result.target().getUserId();
                registerAfterCommit(() ->
                        eventPublisher.publishEvent(
                                new PaymentSucceededEvent(partyId, partyCycleId, succeededUserId))
                );

            } else {
                memberPaymentMapper.markFailed(
                        partyCycleId, result.target().getMemberId(),
                        result.failureReason(), result.failureCode(), result.failedAt());

                final Long failedUserId = result.target().getUserId();
                registerAfterCommit(() ->
                        eventPublisher.publishEvent(
                                new PaymentFailedEvent(partyId, partyCycleId, failedUserId))
                );

                resetUnprocessedClaimedMembers(partyCycleId, claimResult.claimedTargets(), pgResults);


                failCycleAndPublish(partyId, partyCycleId, "PAYMENT_API_FAILED");
                return;
            }
        }

        int nonPaidCount = memberPaymentMapper.countNonPaid(partyCycleId);
        if (nonPaidCount > 0) {
            log.warn("미완료 멤버 존재. partyId={}, partyCycleId={}, nonPaidCount={}",
                    partyId, partyCycleId, nonPaidCount);
            failCycleAndPublish(partyId, partyCycleId, "INCOMPLETE_PAYMENT");
            return;
        }


        int paidCount = memberPaymentMapper.countByStatus(partyCycleId, MemberPaymentStatus.PAID);
        int totalCount = claimResult.expectedMemberCount();

        registerAfterCommit(() ->
                eventPublisher.publishEvent(new PartyCyclePaymentCompletedEvent(
                        partyId, partyCycleId, claimResult.cycle().getCycleNo(), paidCount, totalCount))
        );

        log.info("자동결제 전원 성공. partyId={}, partyCycleId={}", partyId, partyCycleId);
    }


    private boolean claimCycle(Long partyCycleId) {
        return partyCycleMapper.compareAndUpdateStatus(
                partyCycleId,
                PartyCycleStatus.PAYMENT_PENDING,
                PartyCycleStatus.PROCESSING) == 1;
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


    private PaymentTargets resolvePaymentTargets(Long partyId, Long partyCycleId, PartyCycle cycle) {
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


    private void batchInitMemberPayments(Long partyId, Long partyCycleId,
                                          List<PaymentChargeTarget> targets) {
        if (targets.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        List<PartyCycleMemberPayment> payments = targets.stream()
                .map(target -> PartyCycleMemberPayment.builder()
                        .partyCycleId(partyCycleId)
                        .partyId(partyId)
                        .partyMemberId(target.getMemberId())
                        .userId(target.getUserId())
                        .amount(target.getAmount())
                        .status(MemberPaymentStatus.PAYMENT_PENDING)
                        .idempotencyKey(createIdempotencyKey(target))
                        .createdAt(now)
                        .updatedAt(now)
                        .build())
                .toList();

        memberPaymentMapper.batchInsertIfAbsent(payments);
    }


    private void resetUnprocessedClaimedMembers(
            Long partyCycleId,
            List<PaymentChargeTarget> claimedTargets,
            List<MemberPaymentResult> pgResults) {

        Set<Long> processedIds = pgResults.stream()
                .map(r -> r.target().getMemberId())
                .collect(Collectors.toSet());

        List<Long> unprocessedIds = claimedTargets.stream()
                .map(PaymentChargeTarget::getMemberId)
                .filter(id -> !processedIds.contains(id))
                .toList();

        if (!unprocessedIds.isEmpty()) {
            memberPaymentMapper.resetProcessingToPaymentPending(partyCycleId, unprocessedIds);
            log.info("fail-fast 미처리 멤버 PAYMENT_PENDING 복구. partyCycleId={}, memberIds={}",
                    partyCycleId, unprocessedIds);
        }
    }


    private void failCycleAndPublish(Long partyId, Long partyCycleId, String reason) {
        int updated = partyCycleMapper.compareAndUpdateStatus(
                partyCycleId, PartyCycleStatus.PROCESSING, PartyCycleStatus.FAILED);

        if (updated != 1) {
            log.warn("party_cycle FAILED 전이 실패(이미 처리됨). partyCycleId={}", partyCycleId);
            return;
        }

        int failedCount  = memberPaymentMapper.countByStatus(partyCycleId, MemberPaymentStatus.FAILED);
        int pendingCount = memberPaymentMapper.countPendingOrProcessing(partyCycleId);

        registerAfterCommit(() ->
                eventPublisher.publishEvent(new PartyCyclePaymentFailedEvent(
                        partyId, partyCycleId, failedCount, pendingCount, reason))
        );
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


    private record ClaimResult(
            PartyCycle cycle,
            List<PaymentChargeTarget> claimedTargets,
            int expectedMemberCount
    ) {}


    private record MemberPaymentResult(
            PaymentChargeTarget target,
            boolean success,
            String paymentKey,
            LocalDateTime paidAt,
            String failureReason,
            String failureCode,
            LocalDateTime failedAt
    ) {
        static MemberPaymentResult success(PaymentChargeTarget t, String pk, LocalDateTime paidAt) {
            return new MemberPaymentResult(t, true, pk, paidAt, null, null, null);
        }

        static MemberPaymentResult failure(PaymentChargeTarget t,
                                           String reason, String code, LocalDateTime failedAt) {
            return new MemberPaymentResult(t, false, null, null, reason, code, failedAt);
        }
    }

    private record PaymentTargets(int expectedMemberCount, List<PaymentChargeTarget> targets) {}
}
