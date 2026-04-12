package pbl2.sub119.backend.party.create.dto.response;

// 파티 생성 전 요약 정보 응답값
public record PartyCreateSummaryResponse(
        String productId,
        String productName,
        String thumbnailUrl,
        String operationType,
        Integer maxMemberCount, // 지정할 수 있는 최대 파티원 수

        Integer totalCapacity, // 총 인원 수
        Integer recruitMemberCount, // 파티원 수

        Long ottBasePrice, // OTT 기본 가격
        Long memberPayAmount, // 파티원 결제 금액
        Long memberTotalAmount, // 총 파티원 부담 금액
        Long hostPayAmount, // 파티장이 실제 부담할 금액

        Long platformFee, // 수수료
        Long hostDiscountAmount, // 파티장 할인 금액
        Long expectedSettlementAmount, // 파티장 정산 금액

        String settlementDateGuide, // 정산일 (지금은 정산일 가이드, 추후에 정산일로 변경)
        String warningMessage
) {
}