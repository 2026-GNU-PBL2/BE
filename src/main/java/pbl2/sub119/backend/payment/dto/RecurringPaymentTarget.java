package pbl2.sub119.backend.payment.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecurringPaymentTarget {
    private Long partyId;
    private Long currentCycleId;
    private Integer currentCycleNo;
    private LocalDateTime billingDueAt;
    private Integer memberCountSnapshot;
    private Integer pricePerMemberSnapshot;
}
