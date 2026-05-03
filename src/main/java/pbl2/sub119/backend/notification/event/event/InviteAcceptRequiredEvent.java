package pbl2.sub119.backend.notification.event.event;

// 초대 수락 기한 2시간 전 미완료 파티원 → ProvisionTimeoutService.processProvisionWarn()에서 발행
public record InviteAcceptRequiredEvent(
        Long partyId,
        Long provisionId,
        Long memberUserId
) {
}
