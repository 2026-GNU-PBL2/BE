package pbl2.sub119.backend.notification.entity;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pbl2.sub119.backend.notification.enumerated.NotificationStatus;
import pbl2.sub119.backend.notification.enumerated.NotificationType;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Notification {

    private Long id;
    private Long userId;
    private Long partyId;
    private NotificationType type;
    private String title;
    private String content;
    private NotificationStatus status;
    private boolean read;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
