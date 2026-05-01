package pbl2.sub119.backend.party.provision.dto.response;

import pbl2.sub119.backend.party.provision.enumerated.ProvisionStatus;
import pbl2.sub119.backend.subproduct.enumerated.OperationType;

// 이용 등록 응답
public record PartyProvisionSetupResponse(
        Long provisionId,
        Long partyId,
        OperationType provisionType,
        ProvisionStatus provisionStatus
) {
}