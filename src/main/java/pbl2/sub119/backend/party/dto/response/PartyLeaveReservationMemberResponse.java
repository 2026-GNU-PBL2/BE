package pbl2.sub119.backend.party.dto.response;

import java.time.LocalDateTime;
import pbl2.sub119.backend.party.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.enumerated.PartyRole;

public record PartyLeaveReservationMemberResponse(
        Long partyMemberId,
        Long userId,
        PartyRole role,
        PartyMemberStatus status,
        LocalDateTime leaveReservedAt
) {
}