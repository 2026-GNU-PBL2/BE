package pbl2.sub119.backend.toss.event;

public record BillingKeyIssuedEvent(
        Long userId,
        String billingKey
) {}
