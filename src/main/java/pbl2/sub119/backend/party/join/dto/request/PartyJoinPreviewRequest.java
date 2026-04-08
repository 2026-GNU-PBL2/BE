package pbl2.sub119.backend.party.join.dto.request;

import jakarta.validation.constraints.NotBlank;

// 파티 참여 전 결제 안내 조회 요청
public record PartyJoinPreviewRequest(
        @NotBlank
        String productId
) {
}