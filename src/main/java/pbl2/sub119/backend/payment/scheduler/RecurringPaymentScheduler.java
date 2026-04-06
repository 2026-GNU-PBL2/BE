package pbl2.sub119.backend.payment.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pbl2.sub119.backend.payment.service.RecurringPaymentService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringPaymentScheduler {

    private final RecurringPaymentService recurringPaymentService;

    @Scheduled(fixedDelay = 60000)
    public void run() {
        log.info("반복 결제 스케줄러 실행");
        recurringPaymentService.processDueCycles();
    }
}