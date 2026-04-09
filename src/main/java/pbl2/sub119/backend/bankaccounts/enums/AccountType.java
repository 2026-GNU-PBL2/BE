package pbl2.sub119.backend.bankaccounts.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "계좌 유형")
public enum AccountType {
    WITHDRAWAL,
    SETTLEMENT
}
