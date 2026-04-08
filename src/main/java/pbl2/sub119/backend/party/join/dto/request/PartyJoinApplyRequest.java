package pbl2.sub119.backend.party.join.dto.request;

import jakarta.validation.constraints.NotBlank;

// 파티 자동 매칭 신청 요청
public record PartyJoinApplyRequest(
        @NotBlank
        String productId
) {
}