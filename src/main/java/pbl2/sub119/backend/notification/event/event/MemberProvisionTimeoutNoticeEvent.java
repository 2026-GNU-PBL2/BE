package pbl2.sub119.backend.notification.event.event;

// 파티원 provision 24시간 초과 안내 → 환불 불가 이력 안내
public record MemberProvisionTimeoutNoticeEvent(
        Long partyId,
        Long provisionId,
        Long memberUserId
) {
}