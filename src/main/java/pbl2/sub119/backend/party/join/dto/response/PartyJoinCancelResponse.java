package pbl2.sub119.backend.party.join.dto.response;

import java.time.LocalDateTime;

// 자동 매칭 신청 취소 응답
public record PartyJoinCancelResponse(
        Long joinRequestId,
        LocalDateTime canceledAt,
        String message
) {
}