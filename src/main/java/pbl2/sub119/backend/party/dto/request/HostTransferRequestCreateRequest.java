package pbl2.sub119.backend.party.dto.request;

import jakarta.validation.constraints.NotNull;

public record HostTransferRequestCreateRequest(
        @NotNull
        Long targetUserId
) {
}