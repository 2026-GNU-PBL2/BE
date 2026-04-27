package pbl2.sub119.backend.admin.party.dto;

import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;

public record AdminPartyMemberResponse(
        Long userId,
        String displayUserId,
        String nickname,
        PartyRole role,
        PartyMemberStatus status
) {
}