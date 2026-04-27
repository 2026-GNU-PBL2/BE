package pbl2.sub119.backend.admin.user.dto;

import java.time.LocalDateTime;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.common.enumerated.RecruitStatus;

public record AdminUserPartyResponse(
        Long partyId,
        String productName,
        int currentMemberCount,
        int maxMemberCount,
        RecruitStatus recruitStatus,
        OperationStatus operationStatus,
        LocalDateTime nextBillingDate
) {
}