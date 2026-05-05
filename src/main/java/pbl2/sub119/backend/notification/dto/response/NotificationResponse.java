package pbl2.sub119.backend.notification.dto.response;

import java.time.LocalDateTime;
import pbl2.sub119.backend.notification.enumerated.NotificationStatus;
import pbl2.sub119.backend.notification.enumerated.NotificationType;

public record NotificationResponse(
        Long id,
        Long partyId,
        NotificationType type,
        String title,
        String content,
        String webContent,
        NotificationStatus status,
        boolean isRead,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
}
