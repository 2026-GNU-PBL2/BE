package pbl2.sub119.backend.party.vacancy.dto.response;

import java.time.LocalDateTime;

// 결원 파티 직접 참여 응답
public record PartyVacancyJoinResponse(
        Long partyId,
        String productId,
        String productName,
        LocalDateTime joinedAt,
        String message
) {
}