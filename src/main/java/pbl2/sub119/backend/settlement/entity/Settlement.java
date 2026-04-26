package pbl2.sub119.backend.settlement.entity;

import lombok.*;
import pbl2.sub119.backend.common.enumerated.SettlementStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Settlement {

    private Long id;
    private Long partyId;
    private Long partyCycleId;
    private Long hostUserId;
    private Integer memberCount;
    private Integer unitAmount;
    private Long totalAmount;
    private Long feeDeducted;
    private SettlementStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}