package pbl2.sub119.backend.notification.event.event;

import java.util.List;

// 파티장 결원 합류 후 HOST 활성화 시 → 신규 파티장에게 이용 정보 등록 안내
public record HostChangedEvent(Long partyId, Long newHostUserId, List<Long> memberUserIds) {}
