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

        // 다음 결제 사이클이 이미 생성된 경우(PAYMENT_PENDING) — 결제 진행 구간으로 보고 차단
        if (cycle.getStatus() != PartyCycleStatus.RUNNING) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_NOT_ALLOWED);
        }

        // RUNNING 사이클의 endAt은 다음 회차 결제 성공 시 채워지므로 null이 정상
        // endAt이 없으면 billingDueAt + 1개월(다음 결제 예정일)을 마감으로 사용
        final LocalDateTime billingDueAt = cycle.getBillingDueAt();
                final LocalDateTime deadline;
                if (cycle.getEndAt() != null) {
                    deadline = cycle.getEndAt();
                } else if (billingDueAt != null) {
                    deadline = billingDueAt.plusMonths(1);
                } else {
                    throw new PartyException(ErrorCode.PARTY_LEAVE_NOT_ALLOWED);
                }

        if (!LocalDateTime.now().isBefore(deadline)) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_NOT_ALLOWED);
        }
    }
}