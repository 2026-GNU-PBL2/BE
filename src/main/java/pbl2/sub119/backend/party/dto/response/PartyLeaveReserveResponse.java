package pbl2.sub119.backend.party.dto.response;

import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;

import java.time.LocalDateTime;

public record PartyLeaveReserveResponse(
        Long partyId,
        Long partyMemberId,
        Long userId,
        PartyMemberStatus status,
        LocalDateTime leaveReservedAt,
        String message
) {
}