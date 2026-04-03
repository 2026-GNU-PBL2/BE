package pbl2.sub119.backend.payment.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.event.PaymentExecutionRequestedEvent;
import pbl2.sub119.backend.payment.service.InitialPaymentCycleService;
import pbl2.sub119.backend.payment.service.PartyPaymentReadinessService;
import pbl2.sub119.backend.toss.event.BillingKeyIssuedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingKeyIssuedEventListener {

    private final PartyPaymentReadinessService partyPaymentReadinessService;
    private final InitialPaymentCycleService initialPaymentCycleService;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BillingKeyIssuedEvent event) {
        Long partyId = event.partyId();

        if (!partyPaymentReadinessService.isReady(partyId)) {
            log.info("파티 결제 준비 미완료. partyId={}", partyId);
            return;
        }

        PartyCycle partyCycle = initialPaymentCycleService.createInitialCycle(partyId);

        log.info("파티 결제일 확정 완료. partyId={}, partyCycleId={}",
                partyId, partyCycle.getId());

        eventPublisher.publishEvent(
                new PaymentExecutionRequestedEvent(partyId, partyCycle.getId())
        );
    }
}