package pbl2.sub119.backend.settlement.dto.response;

import pbl2.sub119.backend.common.enumerated.WithdrawRequestStatus;
import pbl2.sub119.backend.settlement.entity.PointWithdrawRequest;

import java.time.LocalDateTime;

public record WithdrawRequestResponse(
        Long id,
        Long amount,
        WithdrawRequestStatus status,
        String bankNameSnapshot,
        String accountMaskedSnapshot,
        LocalDateTime requestedAt,
        LocalDateTime processedAt,
        String rejectReason,
        String externalTxId,
        String internalPayoutRef
) {
    public static WithdrawRequestResponse from(PointWithdrawRequest entity) {
        return new WithdrawRequestResponse(
                entity.getId(),
                entity.getAmount(),
                entity.getStatus(),
                entity.getBankNameSnapshot(),
                entity.getAccountMaskedSnapshot(),
                entity.getRequestedAt(),
                entity.getProcessedAt(),
                entity.getRejectReason(),
                entity.getExternalTxId(),
                entity.getInternalPayoutRef()
        );
    }
}
