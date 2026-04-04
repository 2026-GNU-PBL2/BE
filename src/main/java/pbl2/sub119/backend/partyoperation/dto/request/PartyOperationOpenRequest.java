package pbl2.sub119.backend.partyoperation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PartyOperationOpenRequest(
        @NotNull
        @Positive
        Long partyId
) {
}