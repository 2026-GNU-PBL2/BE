package pbl2.sub119.backend.party.dto.response;

public record JoinOrQueueResponse(
        boolean joined,
        boolean queued,
        Long partyId,
        Long waitingId,
        String message
) {
}