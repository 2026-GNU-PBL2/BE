package pbl2.sub119.backend.party.vacancy.dto.response;

import java.time.LocalDateTime;

// 파티장 결원 예정/결원 파티 목록 응답
public record HostVacancyPartyResponse(
        Long partyId,
        String productId,
        String productName,
        String thumbnailUrl,
        Integer totalCapacity,
        Integer currentMemberCount,
        Integer remainingSeatCount,
        Long monthlyPaymentAmount,
        LocalDateTime nextPaymentDate,
        String joinButtonLabel
) {
}