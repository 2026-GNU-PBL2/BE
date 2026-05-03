package pbl2.sub119.backend.notification.event.event;

import java.util.List;

// 파티장이 초대 링크를 등록했을 때 → PartyProvisionCommandService.setupProvision() afterCommit에서 발행
public record InviteLinkRequiredEvent(
        Long partyId,
        Long provisionId,
        List<Long> memberUserIds
) {
}
