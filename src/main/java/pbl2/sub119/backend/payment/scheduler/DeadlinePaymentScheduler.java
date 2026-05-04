package pbl2.sub119.backend.payment.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pbl2.sub119.backend.payment.service.DeadlinePaymentService;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlinePaymentScheduler {

    private final DeadlinePaymentService deadlinePaymentService;

    @Scheduled(fixedDelay = 300_000)
    public void run() {
        log.info("결제 마감 처리 스케줄러 실행");
        deadlinePaymentService.processExpired();
    }
}
