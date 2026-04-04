package pbl2.sub119.backend.partyoperation.dto.request;

import jakarta.validation.constraints.Size;

public record PartyOperationResetRequest(
        @Size(max = 500)
        String operationMessage
) {
}