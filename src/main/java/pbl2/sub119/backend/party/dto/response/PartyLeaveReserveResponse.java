package pbl2.sub119.backend.party.dto.response;

import java.time.LocalDateTime;
import pbl2.sub119.backend.party.enumerated.PartyMemberStatus;

public record PartyLeaveReserveResponse(
        Long partyId,
        Long partyMemberId,
        Long userId,
        PartyMemberStatus status,
        LocalDateTime leaveReservedAt,
        String message
) {
}