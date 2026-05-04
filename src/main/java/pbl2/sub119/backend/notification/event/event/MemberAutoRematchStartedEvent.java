package pbl2.sub119.backend.notification.event.event;

import java.util.List;

public record MemberAutoRematchStartedEvent(
        Long partyId,
        List<Long> requeuedUserIds
) {}
