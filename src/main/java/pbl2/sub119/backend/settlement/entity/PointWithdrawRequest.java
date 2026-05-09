package pbl2.sub119.backend.settlement.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pbl2.sub119.backend.common.enumerated.WithdrawRequestStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointWithdrawRequest {
    private Long id;
    private Long userId;
    private Long amount;
    private WithdrawRequestStatus status;
    private Long bankAccountId;
    private String bankNameSnapshot;
    private String accountMaskedSnapshot;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private Long processedBy;
    private String rejectReason;
    private String externalTxId;
    private String internalPayoutRef;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
