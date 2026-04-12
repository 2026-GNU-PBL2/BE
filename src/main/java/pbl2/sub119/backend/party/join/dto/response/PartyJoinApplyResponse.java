package pbl2.sub119.backend.party.join.dto.response;

// 파티 자동 매칭 신청 응답
public record PartyJoinApplyResponse(
        boolean joined,
        boolean waiting,
        Long partyId,
        Long joinRequestId,
        String message
) {
}