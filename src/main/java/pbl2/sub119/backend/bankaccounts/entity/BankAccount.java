package pbl2.sub119.backend.bankaccounts.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pbl2.sub119.backend.bankaccounts.enums.AccountType;
import pbl2.sub119.backend.bankaccounts.enums.VerificationStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BankAccount {
    private Long id;
    private Long userId;
    private String fintechUseNum;
    private String accessToken;
    private String refreshToken;
    private String bankTranId;
    private String bankName;
    private String accountAlias;
    private String accountNumMasked;
    private Long balanceAmt;

    private String bankCode;
    private String accountNumber;
    private String accountHolderName;
    private String accountHolderBirthDate;
    private AccountType accountType;
    private Boolean isPrimary;
    private VerificationStatus verificationStatus;
    private LocalDateTime verifiedAt;
    private LocalDateTime lastVerifiedAt;
    private String failReason;
    private LocalDateTime updatedAt;
}