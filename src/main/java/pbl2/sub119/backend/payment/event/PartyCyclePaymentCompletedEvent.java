package pbl2.sub119.backend.payment.event;

public record PartyCyclePaymentCompletedEvent(
        Long partyId,
        Long partyCycleId,
        int cycleNo,
        int paidMemberCount,
        int totalMemberCount
) {}