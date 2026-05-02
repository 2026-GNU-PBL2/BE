package pbl2.sub119.backend.party.provision.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pbl2.sub119.backend.party.provision.service.ProvisionTimeoutService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProvisionTimeoutScheduler {

    private final ProvisionTimeoutService provisionTimeoutService;

    @Scheduled(fixedDelay = 3_600_000) // 1시간마다 실행
    public void run() {
        log.info("provision 타임아웃 스케줄러 실행");

        try {
            provisionTimeoutService.processHostTimeout();
        } catch (Exception e) {
            log.error("파티장 타임아웃 처리 중 오류 발생", e);
        }

        try {
            provisionTimeoutService.processMemberTimeout();
        } catch (Exception e) {
            log.error("파티원 타임아웃 처리 중 오류 발생", e);
        }
    }
}
