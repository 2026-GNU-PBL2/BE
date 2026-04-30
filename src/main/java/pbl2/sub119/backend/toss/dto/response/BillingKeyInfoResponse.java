package pbl2.sub119.backend.toss.dto.response;

import pbl2.sub119.backend.toss.entity.BillingKeyEntity;

import java.time.LocalDateTime;

public record BillingKeyInfoResponse(
        boolean hasBillingKey,
        String customerKey,
        String cardCompany,
        String maskedCardNumber,
        String status,
        LocalDateTime issuedAt
) {
    public static BillingKeyInfoResponse empty() {
        return new BillingKeyInfoResponse(false, null, null, null, null, null);
    }

    public static BillingKeyInfoResponse of(BillingKeyEntity entity) {
        return new BillingKeyInfoResponse(
                true,
                entity.getCustomerKey(),
                entity.getCardCompany(),
                entity.getMaskedCardNumber(),
                entity.getStatus().name(),
                entity.getIssuedAt()
        );
    }
}