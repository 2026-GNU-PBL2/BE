package pbl2.sub119.backend.notification.event.event;

// 결제 실패로 인한 정산 미진행 시 → PartyCycleStateEventListener에서 파티장에게 발행
public record SettlementSkippedPaymentFailedEvent(
        Long partyId,
        Long partyCycleId,
        Long hostUserId
) {}
