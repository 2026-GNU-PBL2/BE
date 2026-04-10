package pbl2.sub119.backend.party.dto.response;

import pbl2.sub119.backend.party.common.enumerated.MatchWaitingStatus;

public record MatchWaitingRegisterResponse(
        Long waitingId,
        String productId,
        Long userId,
        MatchWaitingStatus status
) {
}