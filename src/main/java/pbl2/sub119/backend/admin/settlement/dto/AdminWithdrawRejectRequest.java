package pbl2.sub119.backend.admin.settlement.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminWithdrawRejectRequest(
        @NotBlank(message = "거절 사유는 필수입니다.")
        String reason
) {}
