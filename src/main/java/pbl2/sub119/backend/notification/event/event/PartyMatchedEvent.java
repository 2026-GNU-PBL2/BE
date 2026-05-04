package pbl2.sub119.backend.notification.event.event;

import java.util.List;

// 파티원 전원 billing key 등록 완료(파티 매칭) 시 → 매칭 서비스에서 발행
public record PartyMatchedEvent(
        Long partyId,
        Long partyCycleId,
        List<Long> memberUserIds
) {
}
