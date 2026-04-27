package pbl2.sub119.backend.party.history.dto;

import java.time.LocalDateTime;

public record PartyHistoryResponse(
        Long partyId,
        String displayPartyId,
        String productId,
        String productName,
        String role,
        String status,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}