package pbl2.sub119.backend.party.dto.request;

public record PartyCreateRequest(
        String productId,
        Integer capacity,
        Integer pricePerMemberSnapshot
) {
}