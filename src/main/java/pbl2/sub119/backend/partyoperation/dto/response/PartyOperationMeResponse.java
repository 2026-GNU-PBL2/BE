package pbl2.sub119.backend.partyoperation.dto.response;

import java.time.LocalDateTime;
import pbl2.sub119.backend.partyoperation.enumerated.OperationMemberStatus;
import pbl2.sub119.backend.partyoperation.enumerated.OperationStatus;
import pbl2.sub119.backend.partyoperation.enumerated.OperationType;

public record PartyOperationMeResponse(
        Long operationId,
        Long partyId,
        OperationType operationType,
        OperationStatus operationStatus,
        OperationMemberStatus memberStatus,
        String inviteValue,
        String sharedAccountEmail,
        String sharedAccountPassword,
        String operationGuide,
        LocalDateTime operationStartedAt,
        LocalDateTime operationCompletedAt,
        LocalDateTime lastResetAt
) {
}