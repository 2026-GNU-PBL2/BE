package pbl2.sub119.backend.payment.entity;

import lombok.*;
import pbl2.sub119.backend.payment.enumerated.MemberPaymentStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PartyCycleMemberPayment {

    private Long id;
    private Long partyCycleId;
    private Long partyId;
    private Long partyMemberId;
    private Long userId;
    private Integer amount;
    private MemberPaymentStatus status;
    private String failureReason;
    private String failureCode;
    private String externalTxId;
    private String idempotencyKey;
    private LocalDateTime paidAt;
    private LocalDateTime failedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}