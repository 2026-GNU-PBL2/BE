package pbl2.sub119.backend.party.leave.dto.response;

import java.time.LocalDateTime;
import pbl2.sub119.backend.party.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.enumerated.PartyRole;
import pbl2.sub119.backend.party.enumerated.VacancyType;

public record PartyLeaveReserveResponse(
        Long partyId,
        Long partyMemberId,
        Long userId,
        PartyRole role,
        PartyMemberStatus status,
        LocalDateTime leaveReservedAt,
        VacancyType vacancyType,
        String message
) {
}