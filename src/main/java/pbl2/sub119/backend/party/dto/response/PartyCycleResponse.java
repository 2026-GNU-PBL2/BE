package pbl2.sub119.backend.party.dto.response;

import java.time.LocalDateTime;

public record PartyCycleResponse(
        Long partyId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        LocalDateTime nextPaymentDate,
        boolean isEndingSoon,
        long daysRemaining
) {
}