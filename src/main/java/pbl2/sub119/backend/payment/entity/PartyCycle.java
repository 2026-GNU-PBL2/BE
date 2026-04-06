package pbl2.sub119.backend.payment.entity;

import lombok.*;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PartyCycle {

    private Long id;
    private Long partyId;
    private Integer cycleNo;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime billingDueAt;
    private LocalDateTime nextBillingDueAt;
    private PartyCycleStatus status;
    private Integer memberCountSnapshot;
    private Integer pricePerMemberSnapshot;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}