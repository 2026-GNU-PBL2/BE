package pbl2.sub119.backend.party.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MatchWaitingRegisterRequest(
        @NotBlank
        String productId
) {
}