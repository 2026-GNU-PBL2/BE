package pbl2.sub119.backend.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.enumerated.MemberPaymentStatus;
import pbl2.sub119.backend.payment.event.PartyCyclePaymentFailedEvent;
import pbl2.sub119.backend.payment.mapper.PartyCycleMemberPaymentMapper;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeadlinePaymentService {

    private static final long GRACE_HOURS = 24L;

    private final PartyCycleMemberPaymentMapper memberPaymentMapper;
    private final PartyCycleMapper partyCycleMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processExpired() {
        // JVM timezone 기준 (billing_due_at 저장 기준과 동일하게 유지)
        LocalDateTime deadline = LocalDateTime.now().minusHours(GRACE_HOURS);

        List<Long> expiredCycleIds = memberPaymentMapper.findDeadlineExceededCycleIds(deadline);

        for (Long cycleId : expiredCycleIds) {
            handleExpiredCycle(cycleId);
        }
    }

    private void handleExpiredCycle(Long cycleId) {
        PartyCycle cycle = partyCycleMapper.findById(cycleId);
        if (cycle == null) {
            return;
        }

        memberPaymentMapper.bulkMarkFailedForDeadline(
                cycleId, "DEADLINE_EXCEEDED", "DEADLINE_EXCEEDED",
                LocalDateTime.now());

        // PAYMENT_PENDING / PROCESSING 둘 다 FAILED 전이 허용
        int updated = partyCycleMapper.failIfPendingOrProcessing(cycleId);

        if (updated != 1) {
            log.warn("deadline cycle FAILED 전이 실패(이미 처리됨). cycleId={}", cycleId);
            return;
        }

        int failedCount = memberPaymentMapper.countByStatus(cycleId, MemberPaymentStatus.FAILED);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishEvent(new PartyCyclePaymentFailedEvent(
                        cycle.getPartyId(), cycleId, failedCount, 0, "DEADLINE_EXCEEDED"));
            }
        });

        log.info("마감 초과 사이클 실패 처리 완료. cycleId={}, partyId={}", cycleId, cycle.getPartyId());
    }
}
