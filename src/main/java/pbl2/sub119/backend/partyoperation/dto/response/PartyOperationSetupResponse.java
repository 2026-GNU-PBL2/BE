package pbl2.sub119.backend.partyoperation.dto.response;

import pbl2.sub119.backend.partyoperation.enumerated.OperationStatus;
import pbl2.sub119.backend.partyoperation.enumerated.OperationType;

public record PartyOperationSetupResponse(
        Long partyOperationId,
        Long partyId,
        OperationType operationType,
        OperationStatus operationStatus
) {
}