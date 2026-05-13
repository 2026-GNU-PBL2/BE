package pbl2.sub119.backend.party.create.event;

public record PartyCreatedEvent(
        Long partyId,
        String productId
) {}
