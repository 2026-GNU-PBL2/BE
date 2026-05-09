package pbl2.sub119.backend.settlement.dto.response;

import pbl2.sub119.backend.common.enumerated.SettlementStatus;
import pbl2.sub119.backend.settlement.entity.Settlement;

import java.time.LocalDateTime;

public record SettlementHistoryResponse(
        Long id,
        Long partyId,
        Integer memberCount,
        Integer unitAmount,
        Long totalAmount,
        Long feeDeducted,
        SettlementStatus status,
        LocalDateTime createdAt
) {
    public static SettlementHistoryResponse from(Settlement entity) {
        return new SettlementHistoryResponse(
                entity.getId(),
                entity.getPartyId(),
                entity.getMemberCount(),
                entity.getUnitAmount(),
                entity.getTotalAmount(),
                entity.getFeeDeducted(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
