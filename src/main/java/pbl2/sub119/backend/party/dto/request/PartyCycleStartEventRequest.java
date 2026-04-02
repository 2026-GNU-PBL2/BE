package pbl2.sub119.backend.party.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PartyCycleStartEventRequest(
        @Positive
        @NotNull
        Long partyId
) {
}