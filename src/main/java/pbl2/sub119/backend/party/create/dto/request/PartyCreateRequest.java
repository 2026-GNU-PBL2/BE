package pbl2.sub119.backend.party.create.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 실제 파티 생성 요청값
public record PartyCreateRequest(
        @NotBlank
        String productId, // OTT 상품 ID

        @NotNull
        @Min(2)
        Integer capacity // 파티 전체 정원(파티장 포함)
) {
}