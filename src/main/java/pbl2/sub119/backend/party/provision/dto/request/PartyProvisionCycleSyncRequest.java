package pbl2.sub119.backend.party.provision.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PartyProvisionCycleSyncRequest(
        @NotNull
        @Positive
        Long partyId
) {
}