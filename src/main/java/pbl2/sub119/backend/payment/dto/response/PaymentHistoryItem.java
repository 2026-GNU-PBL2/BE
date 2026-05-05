package pbl2.sub119.backend.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pbl2.sub119.backend.payment.enumerated.MemberPaymentStatus;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryItem {
    private Long partyId;
    private Long partyCycleId;
    private String serviceName;
    private Integer amount;
    private MemberPaymentStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime failedAt;
    private String failureReason;
    private String failureCode;
    private LocalDateTime createdAt;
}
