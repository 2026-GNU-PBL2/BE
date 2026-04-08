package pbl2.sub119.backend.party.provision.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionType;

public record PartyProvisionSetupRequest(
        @NotNull
        ProvisionType operationType,

        @Size(max = 500)
        String inviteValue,

        @Size(max = 150)
        String sharedAccountEmail,

        @Size(max = 255)
        String sharedAccountPassword,

        @Size(max = 1000)
        String operationGuide
) {
}