package pbl2.sub119.backend.party.provision.dto.response;

import java.time.LocalDateTime;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionMemberStatus;

// 이용 대상 멤버 상태 응답
public record PartyProvisionMemberResponse(
        Long provisionMemberId,
        Long partyMemberId,
        Long userId,
        String nickname,
        ProvisionMemberStatus memberStatus,
        LocalDateTime inviteSentAt,
        LocalDateTime mustCompleteBy,
        LocalDateTime confirmedAt,
        LocalDateTime completedAt,
        LocalDateTime activatedAt,
        LocalDateTime lastResetAt,
        Boolean penaltyApplied,
        String provisionMessage
) {
}