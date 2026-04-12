package pbl2.sub119.backend.party.dto.response;

import java.time.LocalDateTime;
import pbl2.sub119.backend.party.common.enumerated.MatchWaitingStatus;

public record MatchWaitingResponse(
        Long waitingId,
        String productId,
        Long userId,
        MatchWaitingStatus status,
        LocalDateTime requestedAt,
        Long targetPartyId
) {
}