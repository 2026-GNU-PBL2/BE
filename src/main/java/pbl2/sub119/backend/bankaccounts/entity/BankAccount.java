package pbl2.sub119.backend.bankaccounts.entity;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BankAccount {
    private Long id;                  // PK
    private Long userId;              // 사용자 FK
    private String fintechUseNum;     // 핀테크이용번호
    private String accessToken;       // OAuth 접근 토큰
    private String refreshToken;      // OAuth 갱신 토큰
    private String bankTranId;        // 은행거래고유번호
    private String bankName;          // 은행명
    private String accountAlias;      // 계좌 별칭
    private String accountNumMasked;  // 마스킹된 계좌번호
    private Long balanceAmt;          // 계좌 잔액
}
