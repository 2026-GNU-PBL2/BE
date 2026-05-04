package pbl2.sub119.backend.payment.event;

public record PartyCyclePaymentFailedEvent(
        Long partyId,
        Long partyCycleId,
        int failedMemberCount,
        int pendingMemberCount,
        String reason
) {}