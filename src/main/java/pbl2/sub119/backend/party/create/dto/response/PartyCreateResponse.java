package pbl2.sub119.backend.party.create.dto.response;

import java.time.LocalDateTime;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.common.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.common.enumerated.VacancyType;

// 파티 생성 완료 응답값
// 생성 직후 프론트가 다음 화면으로 이동할 수 있도록 최소한의 결과 정보를 반환

public record PartyCreateResponse(
        Long partyId,
        String productId,
        String productName,
        Long hostUserId,
        Integer capacity,
        Integer currentMemberCount,
        RecruitStatus recruitStatus,
        OperationStatus operationStatus,
        VacancyType vacancyType,
        Integer pricePerMemberSnapshot,
        LocalDateTime createdAt,
        String nextAction
) {
}