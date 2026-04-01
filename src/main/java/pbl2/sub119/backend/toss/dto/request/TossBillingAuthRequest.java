package pbl2.sub119.backend.toss.dto.request;

public record TossBillingAuthRequest(
        String authKey,
        String customerKey
) {}
