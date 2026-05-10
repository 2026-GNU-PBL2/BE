package pbl2.sub119.backend.party.leave.event;

public record PartyRematchRequestedEvent(
        Long fromPartyId,
        String productId,
        Long userId
) {}
