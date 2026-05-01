package pbl2.sub119.backend.party.provision.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionStatus;
import pbl2.sub119.backend.subproduct.enumerated.OperationType;

// 파티 이용 전체 현황 응답
public record PartyProvisionDashboardResponse(
        Long provisionId,
        Long partyId,
        OperationType provisionType,
        ProvisionStatus provisionStatus,
        String inviteValue,
        String sharedAccountEmail,
        String provisionGuide,
        int totalMemberCount,
        int activeMemberCount,
        LocalDateTime provisionStartedAt,
        LocalDateTime provisionCompletedAt,
        LocalDateTime lastResetAt,
        List<PartyProvisionMemberResponse> members
) {
}