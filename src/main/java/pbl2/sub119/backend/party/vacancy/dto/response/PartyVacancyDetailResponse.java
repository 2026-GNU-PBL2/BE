package pbl2.sub119.backend.party.vacancy.dto.response;

import java.time.LocalDateTime;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.common.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.common.enumerated.VacancyType;

public record PartyVacancyDetailResponse(
        Long partyId,
        String productId,
        String productName,
        String thumbnailUrl,
        Long hostUserId,
        Integer totalCapacity,
        Integer currentMemberCount,
        Integer remainingSeatCount,
        Long monthlyPaymentAmount,
        LocalDateTime nextPaymentDate,
        String operationType,
        RecruitStatus recruitStatus,
        OperationStatus operationStatus,
        VacancyType vacancyType,
        Boolean joinAvailable
) {
}