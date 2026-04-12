package pbl2.sub119.backend.party.dto.response;

import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.common.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.common.enumerated.VacancyType;

public record PartyListResponse(
        Long partyId,
        String productId,
        Long hostUserId,
        Integer capacity,
        Integer currentMemberCount,
        RecruitStatus recruitStatus,
        OperationStatus operationStatus,
        VacancyType vacancyType
) {
}