package pbl2.sub119.backend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentChargeTarget {

    private Long partyId;
    private Long partyCycleId;
    private Long memberId;
    private Long userId;
    private String billingKey;
    private String customerKey;
    private Integer amount;
}