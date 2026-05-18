package pbl2.sub119.backend.notification.event.event;

import java.util.List;

// 파티장 provision 미등록 리마인드 시 → 파티원에게 이용 정보 등록 지연 안내
public record HostProvisionDelayedNoticeEvent(Long partyId, List<Long> memberUserIds) {}
