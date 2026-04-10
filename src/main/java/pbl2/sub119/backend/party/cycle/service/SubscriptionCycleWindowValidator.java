package pbl2.sub119.backend.party.cycle.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.common.exception.PartyException;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionCycleWindowValidator {

    private final PartyCycleMapper partyCycleMapper;

    // 탈퇴 예약 가능한 시점인지 확인
    public void validateLeaveReservationWindow(final Long partyId) {
        final PartyCycle cycle = partyCycleMapper.findLatestPendingOrRunningCycle(
                partyId,
                PartyCycleStatus.PAYMENT_PENDING,
                PartyCycleStatus.RUNNING
        );

        // 아직 시작된 이용 주기가 없으면 탈퇴 예약 불가
        if (cycle == null) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_NOT_ALLOWED);
        }

        // 종료일이 없으면 주기 정보가 완성되지 않은 상태로 보고 차단
        if (cycle.getEndAt() == null) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_NOT_ALLOWED);
        }

        final LocalDateTime now = LocalDateTime.now();

        // 이미 종료된 주기면 탈퇴 예약 불가
        if (now.isAfter(cycle.getEndAt())) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_NOT_ALLOWED);
        }
    }
}