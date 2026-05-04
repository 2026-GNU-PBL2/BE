package pbl2.sub119.backend.notification.event.event;

// 자동결제 실패 시 → AutoPaymentService.execute() failCycle() 직전에서 발행
public record PaymentFailedEvent(
        Long partyId,
        Long partyCycleId,
        Long failedUserId
) {
}
