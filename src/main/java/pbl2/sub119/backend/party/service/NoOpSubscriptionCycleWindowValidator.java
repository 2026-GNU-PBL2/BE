package pbl2.sub119.backend.party.service;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 현재는 주기 서비스 연동 전 단계
 * 이후 Cycle Service HTTP 조회/콜백 연결 시 이 구현을 교체
 */
@Component
@Profile({"local", "test"})
public class NoOpSubscriptionCycleWindowValidator implements SubscriptionCycleWindowValidator {

    @Override
    public void validateLeaveReservationWindow(Long partyId) {
        // no-op
    }
}