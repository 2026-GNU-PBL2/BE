package pbl2.sub119.backend.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.BusinessException;
import pbl2.sub119.backend.party.common.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.common.service.PartyHistoryService;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.event.PaymentExecutionRequestedEvent;
import pbl2.sub119.backend.payment.mapper.PartyCycleMemberPaymentMapper;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRetryService {

    private static final int RETRY_DEADLINE_DAYS = 7;

    private final PartyCycleMapper partyCycleMapper;
    private final PartyCycleMemberPaymentMapper memberPaymentMapper;
    private final PartyHistoryService partyHistoryService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void retry(Long partyCycleId, Long adminId) {
        PartyCycle cycle = partyCycleMapper.findById(partyCycleId);
        if (cycle == null) {
            throw new BusinessException(ErrorCode.PAYMENT_CYCLE_NOT_FOUND);
        }

        if (cycle.getStatus() != PartyCycleStatus.FAILED) {
            throw new BusinessException(ErrorCode.PAYMENT_CYCLE_RETRY_NOT_ALLOWED);
        }

        if (cycle.getBillingDueAt().plusDays(RETRY_DEADLINE_DAYS).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PAYMENT_CYCLE_RETRY_DEADLINE_EXCEEDED);
        }

        // cycle CAS 먼저: FAILED → PAYMENT_PENDING
        int updated = partyCycleMapper.compareAndUpdateStatus(
                partyCycleId, PartyCycleStatus.FAILED, PartyCycleStatus.PAYMENT_PENDING);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.PAYMENT_CYCLE_RETRY_NOT_ALLOWED);
        }

        // FAILED 멤버만 PAYMENT_PENDING으로 초기화 (PAID/CANCELLED 유지)
        memberPaymentMapper.resetFailedForRetry(partyCycleId);

        partyHistoryService.saveHistory(
                cycle.getPartyId(), null,
                PartyHistoryEventType.PAYMENT_RETRY_REQUESTED,
                buildPayload(partyCycleId, cycle.getCycleNo()),
                adminId
        );

        Long partyId = cycle.getPartyId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishEvent(new PaymentExecutionRequestedEvent(partyId, partyCycleId));
            }
        });

        log.info("결제 재시도 요청. partyCycleId={}, partyId={}, adminId={}", partyCycleId, partyId, adminId);
    }

    private String buildPayload(Long partyCycleId, int cycleNo) {
        return "{\"partyCycleId\":" + partyCycleId + ",\"cycleNo\":" + cycleNo + "}";
    }
}
