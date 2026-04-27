package pbl2.sub119.backend.admin.party.dto;

import java.time.LocalDateTime;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.common.enumerated.RecruitStatus;

public record AdminPartyDetailBaseResponse(
        Long partyId,
        String displayPartyId,
        String productName,
        String hostNickname,
        LocalDateTime createdAt,
        Integer currentMemberCount,
        Integer maxMemberCount,
        Integer pricePerMember,
        LocalDateTime nextBillingDate,
        RecruitStatus recruitStatus,
        OperationStatus operationStatus
) {
}