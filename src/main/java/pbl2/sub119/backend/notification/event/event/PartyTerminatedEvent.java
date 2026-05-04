package pbl2.sub119.backend.notification.event.event;

import java.util.List;

// provision 48시간 초과로 파티 해체 시 → ProvisionTimeoutService.dissolvePartyInIsolation()에서 발행
public record PartyTerminatedEvent(
        Long partyId,
        List<Long> memberUserIds,
        String reason
) {
}
