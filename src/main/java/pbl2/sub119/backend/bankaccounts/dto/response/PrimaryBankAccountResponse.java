package pbl2.sub119.backend.bankaccounts.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pbl2.sub119.backend.bankaccounts.entity.BankAccount;
import pbl2.sub119.backend.bankaccounts.enums.AccountType;
import pbl2.sub119.backend.bankaccounts.enums.VerificationStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대표 정산계좌 응답")
public class PrimaryBankAccountResponse {

    @Schema(description = "계좌 ID", example = "1")
    private Long id;

    @Schema(description = "금융결제원 계좌 고유번호(fintech_use_num)")
    private String fintechUseNum;

    @Schema(description = "은행명", example = "신한은행")
    private String bankName;

    @Schema(description = "계좌 별칭", example = "주계좌")
    private String accountAlias;

    @Schema(description = "마스킹된 계좌번호", example = "110-***-****")
    private String accountNumMasked;

    @Schema(description = "계좌 유형", allowableValues = {"WITHDRAWAL", "SETTLEMENT"})
    private AccountType accountType;

    @Schema(description = "대표 계좌 여부", example = "true")
    private Boolean isPrimary;

    @Schema(description = "검증 상태", allowableValues = {"PENDING", "VERIFIED", "FAILED"})
    private VerificationStatus verificationStatus;

    public static PrimaryBankAccountResponse from(BankAccount bankAccount) {
        return new PrimaryBankAccountResponse(
                bankAccount.getId(),
                bankAccount.getFintechUseNum(),
                bankAccount.getBankName(),
                bankAccount.getAccountAlias(),
                bankAccount.getAccountNumMasked(),
                bankAccount.getAccountType(),
                bankAccount.getIsPrimary(),
                bankAccount.getVerificationStatus()
        );
    }
}