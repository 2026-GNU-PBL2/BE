package pbl2.sub119.backend.party.provision.dto.response;

import java.time.LocalDateTime;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionMemberStatus;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionStatus;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionType;

// 본인에게 필요한 이용 정보 응답
public record PartyProvisionMeResponse(
        Long provisionId,
        Long partyId,
        ProvisionType provisionType,
        ProvisionStatus provisionStatus,
        ProvisionMemberStatus memberStatus,
        String inviteValue,
        String sharedAccountEmail,
        String sharedAccountPassword,
        String provisionGuide,
        LocalDateTime provisionStartedAt,
        LocalDateTime provisionCompletedAt,
        LocalDateTime lastResetAt
) {
}