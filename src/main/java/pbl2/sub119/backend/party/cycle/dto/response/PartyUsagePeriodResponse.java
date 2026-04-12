package pbl2.sub119.backend.party.cycle.dto.response;

import java.time.LocalDateTime;

// 현재 이용 기간 조회 응답
public record PartyUsagePeriodResponse(
        Long partyId,
        LocalDateTime currentStartDate,
        LocalDateTime currentEndDate,
        LocalDateTime nextBillingDate,
        boolean endingSoon,
        long daysRemaining
) {
}