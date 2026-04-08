package pbl2.sub119.backend.party.provision.dto.request;

import jakarta.validation.constraints.Size;

public record PartyProvisionResetRequest(
        @Size(max = 500)
        String operationMessage
) {
}