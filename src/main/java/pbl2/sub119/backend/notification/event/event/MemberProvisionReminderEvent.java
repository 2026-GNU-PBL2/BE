package pbl2.sub119.backend.notification.event.event;

// 파티원 provision 확인/수락 리마인드 → 미완료 파티원 대상
public record MemberProvisionReminderEvent(
        Long partyId,
        Long provisionId,
        Long memberUserId,
        int elapsedHours
) {
}