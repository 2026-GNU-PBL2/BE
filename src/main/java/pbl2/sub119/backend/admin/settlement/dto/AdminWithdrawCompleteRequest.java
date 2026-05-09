package pbl2.sub119.backend.admin.settlement.dto;

import jakarta.validation.constraints.Size;

public record AdminWithdrawCompleteRequest(
        @Size(max = 255, message = "외부 거래 번호는 255자를 초과할 수 없습니다.")
        String externalTxId
) {}
