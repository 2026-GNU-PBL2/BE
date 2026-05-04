package pbl2.sub119.backend.notification.event.event;

// 정산 완료 후 파티장에게 포인트 지급 시 → SettlementService에서 발행
public record SettlementCompletedEvent(
        Long partyId,
        Long partyCycleId,
        Long hostUserId
) {
}
