package pbl2.sub119.backend.payment.event;

public record PaymentExecutionRequestedEvent(
        Long partyId,
        Long partyCycleId
) {
}
