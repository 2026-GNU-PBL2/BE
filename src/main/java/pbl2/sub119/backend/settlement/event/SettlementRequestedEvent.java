package pbl2.sub119.backend.settlement.event;

public record SettlementRequestedEvent(
        Long partyId,
        Long partyCycleId
) {
}
