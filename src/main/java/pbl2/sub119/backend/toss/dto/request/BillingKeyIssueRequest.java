package pbl2.sub119.backend.toss.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BillingKeyIssueRequest(
        @NotBlank(message = "authKey는 필수입니다.")
        String authKey,

        @NotBlank(message = "customerKey는 필수입니다.")
        String customerKey,

        @NotBlank(message = "partyId는 필수입니다.")
        String partyId
) {}
