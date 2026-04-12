package pbl2.sub119.backend.party.vacancy.dto.response;

import java.time.LocalDateTime;

// 결원 파티 상세 응답
public record PartyVacancyDetailResponse(
        Long partyId,
        String productId,
        String productName,
        String thumbnailUrl,
        Long hostUserId,
        Integer totalCapacity,
        Integer currentMemberCount,
        Integer remainingSeatCount,
        Long monthlyPaymentAmount,
        LocalDateTime nextPaymentDate,
        String operationType,
        String partyStatusLabel,
        Boolean joinAvailable,
        String joinButtonLabel,
        String description
) {
}