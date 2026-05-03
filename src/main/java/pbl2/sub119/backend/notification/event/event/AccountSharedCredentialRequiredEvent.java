package pbl2.sub119.backend.notification.event.event;

import java.util.List;

// 파티장이 공유계정 정보를 등록했을 때 → PartyProvisionCommandService.setupProvision() afterCommit에서 발행
public record AccountSharedCredentialRequiredEvent(
        Long partyId,
        Long provisionId,
        List<Long> memberUserIds
) {
}
