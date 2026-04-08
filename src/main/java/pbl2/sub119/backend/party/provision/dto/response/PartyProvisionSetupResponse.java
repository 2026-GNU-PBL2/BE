package pbl2.sub119.backend.party.provision.dto.response;

import pbl2.sub119.backend.party.provision.enumerated.ProvisionStatus;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionType;

public record PartyProvisionSetupResponse(
        Long partyOperationId,
        Long partyId,
        ProvisionType operationType,
        ProvisionStatus operationStatus
) {
}