package pbl2.sub119.backend.settlement.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WithdrawCreateRequest(
        @NotNull(message = "환급 금액은 필수입니다.")
        @Min(value = 10_000, message = "환급 요청 금액은 최소 10,000원 이상이어야 합니다.")
        @Max(value = 100_000, message = "환급 요청 금액은 최대 100,000원 이하이어야 합니다.")
        Long amount
) {}
