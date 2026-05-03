package pbl2.sub119.backend.notification.event.event;

import java.util.List;

// 1회차 결제 전원 성공 후 → AutoPaymentService.execute() afterCommit에서 발행
public record PartyMatchedEvent(
        Long partyId,
        Long partyCycleId,
        List<Long> memberUserIds
) {
}
