package pbl2.sub119.backend.party.provision.dto.response;

import java.time.LocalDateTime;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionMemberStatus;

public record PartyProvisionMemberResponse(
        Long partyOperationMemberId,
        Long partyMemberId,
        Long userId,
        ProvisionMemberStatus memberStatus,
        LocalDateTime inviteSentAt,
        LocalDateTime mustCompleteBy,
        LocalDateTime confirmedAt,
        LocalDateTime completedAt,
        LocalDateTime activatedAt,
        LocalDateTime lastResetAt,
        Boolean penaltyApplied,
        String operationMessage
) {
}