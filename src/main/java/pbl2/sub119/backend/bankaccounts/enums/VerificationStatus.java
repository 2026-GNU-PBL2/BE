package pbl2.sub119.backend.bankaccounts.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "계좌 검증 상태")
public enum VerificationStatus {
    PENDING,
    VERIFIED,
    FAILED
}
