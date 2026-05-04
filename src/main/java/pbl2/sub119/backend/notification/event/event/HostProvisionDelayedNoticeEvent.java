package pbl2.sub119.backend.notification.event.event;

import java.util.List;

// 파티장 provision 24시간 미등록 안내 → 파티원 대상
public record HostProvisionDelayedNoticeEvent(
        Long partyId,
        List<Long> memberUserIds
) {
}