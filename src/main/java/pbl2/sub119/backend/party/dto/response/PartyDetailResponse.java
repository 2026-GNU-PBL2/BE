package pbl2.sub119.backend.party.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import pbl2.sub119.backend.party.enumerated.OperationStatus;
import pbl2.sub119.backend.party.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.enumerated.VacancyType;

public record PartyDetailResponse(
        Long partyId,
        String productId,
        Long hostUserId,
        Integer capacity,
        Integer currentMemberCount,
        RecruitStatus recruitStatus,
        OperationStatus operationStatus,
        VacancyType vacancyType,
        Integer pricePerMemberSnapshot,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime terminatedAt,
        List<PartyMemberResponse> members
) {
}