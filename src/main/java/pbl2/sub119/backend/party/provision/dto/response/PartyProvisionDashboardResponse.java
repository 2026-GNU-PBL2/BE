package pbl2.sub119.backend.party.provision.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionStatus;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionType;

public record PartyProvisionDashboardResponse(
        Long partyOperationId,
        Long partyId,
        ProvisionType operationType,
        ProvisionStatus operationStatus,
        String inviteValue,
        String sharedAccountEmail,
        String operationGuide,
        int totalMemberCount,
        int activeMemberCount,
        LocalDateTime operationStartedAt,
        LocalDateTime operationCompletedAt,
        LocalDateTime lastResetAt,
        List<PartyProvisionMemberResponse> members
) {
}