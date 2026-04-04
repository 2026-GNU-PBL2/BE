package pbl2.sub119.backend.partyoperation.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import pbl2.sub119.backend.partyoperation.enumerated.OperationStatus;
import pbl2.sub119.backend.partyoperation.enumerated.OperationType;

public record PartyOperationDashboardResponse(
        Long partyOperationId,
        Long partyId,
        OperationType operationType,
        OperationStatus operationStatus,
        String inviteValue,
        String sharedAccountEmail,
        String operationGuide,
        int totalMemberCount,
        int activeMemberCount,
        LocalDateTime operationStartedAt,
        LocalDateTime operationCompletedAt,
        LocalDateTime lastResetAt,
        List<PartyOperationMemberResponse> members
) {
}