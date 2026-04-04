package pbl2.sub119.backend.partyoperation.dto.response;

import java.time.LocalDateTime;
import pbl2.sub119.backend.partyoperation.enumerated.OperationMemberStatus;

public record PartyOperationMemberResponse(
        Long partyOperationMemberId,
        Long partyMemberId,
        Long userId,
        OperationMemberStatus memberStatus,
        LocalDateTime inviteSentAt,
        LocalDateTime mustCompleteBy,
        LocalDateTime confirmedAt,
        LocalDateTime completedAt,
        LocalDateTime activatedAt,
        LocalDateTime lastResetAt,
        Boolean penaltyApplied,
        String operationMessage
) {
}