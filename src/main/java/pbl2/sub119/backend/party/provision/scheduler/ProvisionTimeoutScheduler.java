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

    // provision 정책 체크
    // - 파티장 미등록: FULL 후 3h~21h 동안 3시간 간격 리마인드 / 24h 파티 해체
    // - 파티원 미확인: 24h 리마인드
    @Scheduled(fixedRate = 60 * 60 * 1000L)
    public void checkProvisionStatus() {
        log.info("provision 정책 스케줄러 실행");

        try {
            provisionTimeoutService.processHostProvisionAt24h();
        } catch (Exception e) {
            log.error("파티장 provision 24시간 안내 처리 중 오류 발생", e);
        }

        try {
            provisionTimeoutService.processHostTimeout();
        } catch (Exception e) {
            log.error("파티장 provision 타임아웃 해체 처리 중 오류 발생", e);
        }

        try {
            provisionTimeoutService.processMemberProvisionReminders();
        } catch (Exception e) {
            log.error("파티원 provision 리마인드 처리 중 오류 발생", e);
        }

        try {
            provisionTimeoutService.processMemberTimeout();
        } catch (Exception e) {
            log.error("파티원 provision 타임아웃 처리 중 오류 발생", e);
        }
    }

    // D-1: 결제일 24시간 이내 파티장 교체 예정 파티 사전 활성화
    @Scheduled(fixedRate = 60 * 60 * 1000L)
    public void activateSwitchWaitingHost() {
        log.info("D-1 파티장 사전 활성화 스케줄러 실행");

        try {
            provisionTimeoutService.activateSwitchWaitingHost();
        } catch (Exception e) {
            log.error("D-1 파티장 사전 활성화 처리 중 오류 발생", e);
        }
    }
}
