package pbl2.sub119.backend.notification.event.event;

// 파티장 provision 미등록 리마인드 → ProvisionTimeoutService에서 발행
public record HostProvisionReminderEvent(
        Long partyId,
        Long hostUserId,
        int elapsedHours
) {
}