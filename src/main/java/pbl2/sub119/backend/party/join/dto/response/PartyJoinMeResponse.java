package pbl2.sub119.backend.party.join.dto.response;

import java.time.LocalDateTime;

// 내 자동 매칭 신청 상태 응답
public record PartyJoinMeResponse(
        Long joinRequestId,
        Long partyId,
        String productId,
        String productName,
        String thumbnailUrl,
        String joinStatus,
        LocalDateTime requestedAt,
        LocalDateTime partyStartAt,
        String commitmentPeriodText,
        Long expectedPaymentAmount,
        String statusLabel,
        String statusMessage
) {
}