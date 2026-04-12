package pbl2.sub119.backend.party.dto.response;

import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;

public record PartyMemberResponse(
        Long memberId,
        Long userId,
        PartyRole role,
        PartyMemberStatus status
) {
}