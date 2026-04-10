package pbl2.sub119.backend.party.dto.response;

import java.time.LocalDateTime;
import pbl2.sub119.backend.party.common.enumerated.HostTransferStatus;

public record HostTransferResponse(
        Long requestId,
        Long partyId,
        Long requesterUserId,
        Long targetUserId,
        HostTransferStatus status,
        LocalDateTime requestedAt,
        LocalDateTime respondedAt,
        LocalDateTime completedAt
) {
}