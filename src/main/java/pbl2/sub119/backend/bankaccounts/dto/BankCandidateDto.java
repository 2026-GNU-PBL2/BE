package pbl2.sub119.backend.bankaccounts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankCandidateDto {
    private String fintechUseNum;
    private String bankTranId;
    private String bankName;
    private String accountAlias;
    private String accountNumMasked;
    private String accessToken;
    private String refreshToken;
}
