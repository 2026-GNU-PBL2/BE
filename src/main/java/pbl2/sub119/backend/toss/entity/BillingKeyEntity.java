package pbl2.sub119.backend.toss.entity;

import lombok.*;
import pbl2.sub119.backend.common.enumerated.BillingKeyStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BillingKeyEntity {
    private Long id;
    private Long userId;
    private String billingKey;        // 토스 billingKey
    private String customerKey;       // 토스 customerKey (userId 기반 생성)
    private String provider;          // TOSS
    private BillingKeyStatus status;
    private String cardCompany;
    private String maskedCardNumber;
    private LocalDateTime issuedAt;
    private LocalDateTime expiredAt;
}
