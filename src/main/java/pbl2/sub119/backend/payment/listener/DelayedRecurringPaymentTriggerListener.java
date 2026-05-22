package pbl2.sub119.backend.payment.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pbl2.sub119.backend.party.event.PartyProvisionSetupCompletedEvent;
import pbl2.sub119.backend.payment.service.RecurringPaymentService;

@Slf4j
@Component
@RequiredArgsConstructor
public class DelayedRecurringPaymentTriggerListener {

    private final RecurringPaymentService recurringPaymentService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(PartyProvisionSetupCompletedEvent event) {
        log.info("지연 반복결제 트리거 수신. partyId={}", event.partyId());
        recurringPaymentService.triggerDelayedPaymentForParty(event.partyId());
    }
}
