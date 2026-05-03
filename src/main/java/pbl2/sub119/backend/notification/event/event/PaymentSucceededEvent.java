package pbl2.sub119.backend.notification.event.event;

// 파티원 개별 결제 성공 직후 → AutoPaymentService에서 멤버별로 각각 발행
public record PaymentSucceededEvent(
        Long partyId,
        Long partyCycleId,
        Long payerUserId
) {}
