package pbl2.sub119.backend.partyoperation.dto.response;

import java.time.LocalDateTime;
import pbl2.sub119.backend.partyoperation.enumerated.OperationMemberStatus;

public record PartyOperationConfirmResponse(
        Long partyId,
        Long userId,
        OperationMemberStatus memberStatus,
        LocalDateTime confirmedAt,
        LocalDateTime activatedAt
) {
}