package pbl2.sub119.backend.party.join.dto.response;

// 파티 참여 전 결제 안내 응답
public record PartyJoinPreviewResponse(
        String productId,
        String productName,
        String thumbnailUrl,
        Long productPricePerMember,
        Long platformFee,
        Long depositAmount,
        Long firstPaymentAmount,
        Long recurringPaymentAmount,
        String paymentNotice
) {
}