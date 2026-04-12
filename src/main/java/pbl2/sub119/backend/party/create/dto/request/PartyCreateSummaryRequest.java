package pbl2.sub119.backend.party.create.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 파티 생선 전 요약 정보 조회 요청 값으로
// 사용자가 파티원 수를 바꿀 때 예상 금액과 비용 구성을 미리 확인하기 위해 사용
public record PartyCreateSummaryRequest(
        @NotBlank
        String productId,

        @NotNull
        @Min(2)
        Integer capacity
) {
}