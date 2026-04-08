package pbl2.sub119.backend.party.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.dto.response.PartyCycleResponse;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyCycleQueryService {

    private final PartyCycleMapper partyCycleMapper;

    public PartyCycleResponse getPartyCycle(Long userId, Long partyId) {

        // 현재 진행 중 또는 결제 대기 중 cycle 조회
        PartyCycle cycle = partyCycleMapper.findLatestPendingOrRunningCycle(
                partyId,
                PartyCycleStatus.PAYMENT_PENDING,
                PartyCycleStatus.RUNNING
        );

        if (cycle == null) {
            throw new PartyException(ErrorCode.NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();

        // 현재 주기 시작일
        LocalDateTime startDate = cycle.getStartAt();

        // 현재 주기 종료일
        LocalDateTime endDate = cycle.getEndAt();

        // 다음 결제일
        LocalDateTime nextPaymentDate = cycle.getBillingDueAt();

        // 남은 일수 계산
        long daysRemaining = ChronoUnit.DAYS.between(now, endDate);

        // 종료 예정 여부 (3일 이하)
        boolean isEndingSoon = daysRemaining <= 3;

        return new PartyCycleResponse(
                partyId,
                startDate,
                endDate,
                nextPaymentDate,
                isEndingSoon,
                daysRemaining
        );
    }
}