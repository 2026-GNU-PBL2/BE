package pbl2.sub119.backend.payment.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pbl2.sub119.backend.toss.event.BillingKeyIssuedEvent;

@Slf4j
@Component
public class BillingKeyIssuedEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BillingKeyIssuedEvent event) {
        log.info("빌링키 발급 완료. userId={}", event.userId());
    }
}