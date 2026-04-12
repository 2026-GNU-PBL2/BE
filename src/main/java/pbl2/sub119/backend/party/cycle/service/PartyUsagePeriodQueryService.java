package pbl2.sub119.backend.party.cycle.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.exception.PartyException;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;
import pbl2.sub119.backend.party.cycle.dto.response.PartyUsagePeriodResponse;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyUsagePeriodQueryService {

    private final PartyCycleMapper partyCycleMapper;
    private final PartyMemberMapper partyMemberMapper;

    // 현재 이용 기간 조회
    public PartyUsagePeriodResponse getUsagePeriod(
            final Long partyId,
            final Long userId
    ) {
        final PartyMember partyMember =
                partyMemberMapper.findByPartyIdAndUserId(partyId, userId);

        if (partyMember == null) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_FORBIDDEN);
        }

        final PartyCycle cycle = partyCycleMapper.findLatestPendingOrRunningCycle(
                partyId,
                PartyCycleStatus.PAYMENT_PENDING,
                PartyCycleStatus.RUNNING
        );

        if (cycle == null) {
            throw new PartyException(ErrorCode.NOT_FOUND);
        }

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime currentStartDate = cycle.getStartAt();
        final LocalDateTime currentEndDate = cycle.getEndAt();
        final LocalDateTime nextBillingDate = cycle.getBillingDueAt();

        long daysRemaining = 0L;
        boolean endingSoon = false;
        if (currentEndDate != null) {
            daysRemaining = ChronoUnit.DAYS.between(now, currentEndDate);
            if (daysRemaining < 0) {
                daysRemaining = 0L;
            }
            endingSoon = currentEndDate.isAfter(now) && daysRemaining <= 3;
        }

        return new PartyUsagePeriodResponse(
                partyId,
                currentStartDate,
                currentEndDate,
                nextBillingDate,
                endingSoon,
                daysRemaining
        );
    }
}