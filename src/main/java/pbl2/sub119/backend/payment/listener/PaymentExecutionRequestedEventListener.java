package pbl2.sub119.backend.payment.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pbl2.sub119.backend.payment.event.PaymentExecutionRequestedEvent;
import pbl2.sub119.backend.toss.service.AutoPaymentService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExecutionRequestedEventListener {

    private final AutoPaymentService autoPaymentService;

    @EventListener
    public void handle(PaymentExecutionRequestedEvent event) {
        log.info("자동결제 실행 시작. partyId={}, partyCycleId={}",
                event.partyId(), event.partyCycleId());

        autoPaymentService.execute(event.partyId(), event.partyCycleId());
    }
}