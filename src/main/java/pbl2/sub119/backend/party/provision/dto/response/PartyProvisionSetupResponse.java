package pbl2.sub119.backend.party.provision.dto.response;

import pbl2.sub119.backend.party.provision.enumerated.ProvisionStatus;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionType;

// 이용 등록 응답
public record PartyProvisionSetupResponse(
        Long provisionId,
        Long partyId,
        ProvisionType provisionType,
        ProvisionStatus provisionStatus
) {
}