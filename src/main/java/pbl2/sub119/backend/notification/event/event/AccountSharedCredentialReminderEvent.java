package pbl2.sub119.backend.notification.event.event;

// 공유계정 확인 기한 2시간 전 미완료 파티원 → ProvisionTimeoutService.processProvisionWarn()에서 발행
public record AccountSharedCredentialReminderEvent(
        Long partyId,
        Long provisionId,
        Long memberUserId
) {
}
