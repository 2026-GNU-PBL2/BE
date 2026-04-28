package pbl2.sub119.backend.payment.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.event.PaymentExecutionRequestedEvent;
import pbl2.sub119.backend.payment.mapper.PaymentExecutionQueryMapper;
import pbl2.sub119.backend.payment.service.InitialPaymentCycleService;
import pbl2.sub119.backend.payment.service.PartyPaymentReadinessService;
import pbl2.sub119.backend.toss.event.BillingKeyIssuedEvent;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingKeyIssuedEventListener {

    private final PartyPaymentReadinessService partyPaymentReadinessService;
    private final InitialPaymentCycleService initialPaymentCycleService;
    private final PaymentExecutionQueryMapper paymentExecutionQueryMapper;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BillingKeyIssuedEvent event) {
        Long userId = event.userId();

        List<Long> candidatePartyIds =
                paymentExecutionQueryMapper.findPendingPartyIdsByUserId(userId);

        for (Long partyId : candidatePartyIds) {
            try {
                tryTriggerInitialPayment(partyId);
            } catch (Exception e) {
                log.error("초기 결제 트리거 실패. partyId={}", partyId, e);
            }
        }
    }

    private void tryTriggerInitialPayment(Long partyId) {
        // isReady: capacity 충족 + 전원 빌링키 보유 + PAYMENT_PENDING/RUNNING cycle 없음 확인 포함
        if (!partyPaymentReadinessService.isReady(partyId)) {
            log.info("파티 결제 준비 미완료. partyId={}", partyId);
            return;
        }

        // DuplicateKeyException 시 기존 cycle 반환 (내부 멱등 처리)
        PartyCycle partyCycle = initialPaymentCycleService.createInitialCycle(partyId);

        log.info("파티 결제일 확정 완료. partyId={}, partyCycleId={}", partyId, partyCycle.getId());

        eventPublisher.publishEvent(
                new PaymentExecutionRequestedEvent(partyId, partyCycle.getId())
        );
    }
}