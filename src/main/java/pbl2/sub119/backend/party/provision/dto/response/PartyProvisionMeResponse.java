package pbl2.sub119.backend.party.provision.dto.response;

import java.time.LocalDateTime;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionMemberStatus;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionStatus;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionType;

public record PartyProvisionMeResponse(
        Long operationId,
        Long partyId,
        ProvisionType operationType,
        ProvisionStatus operationStatus,
        ProvisionMemberStatus memberStatus,
        String inviteValue,
        String sharedAccountEmail,
        String sharedAccountPassword,
        String operationGuide,
        LocalDateTime operationStartedAt,
        LocalDateTime operationCompletedAt,
        LocalDateTime lastResetAt
) {
}