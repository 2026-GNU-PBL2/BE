package pbl2.sub119.backend.notification.event.event;

// 카드 등록 확인용 100원 테스트 결제 직후 발행
public record TestCardPaymentNoticeEvent(
        Long userId
) {}
