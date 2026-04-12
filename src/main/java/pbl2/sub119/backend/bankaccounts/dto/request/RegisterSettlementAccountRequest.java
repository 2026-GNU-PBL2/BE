package pbl2.sub119.backend.bankaccounts.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pbl2.sub119.backend.bankaccounts.enums.AccountType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "정산/환불 계좌 등록 요청")
public class RegisterSettlementAccountRequest {

    @NotBlank
    @Schema(description = "금융결제원 계좌 고유번호(fintech_use_num)", example = "199003839057724125249157")
    private String fintechUseNum;

    @NotBlank
    @Pattern(regexp = "^[0-9]{2,20}$")
    @Schema(description = "은행 표준코드", example = "088")
    private String bankCode;

    @NotBlank
    @Pattern(regexp = "^[0-9]+$")
    @Schema(description = "계좌번호(숫자만)", example = "110123456789")
    private String accountNumber;

    @NotBlank
    @Schema(description = "예금주명", example = "홍길동")
    private String accountHolderName;

    @NotBlank
    @Pattern(regexp = "^[0-9]{8}$")
    @Schema(description = "예금주 생년월일(yyyyMMdd)", example = "19900101")
    private String accountHolderBirthDate;

    @NotNull
    @Schema(description = "계좌 유형", allowableValues = {"WITHDRAWAL", "SETTLEMENT"}, example = "SETTLEMENT")
    private AccountType accountType;

    @NotNull
    @Schema(description = "대표 계좌 여부", example = "true")
    private Boolean isPrimary;
}