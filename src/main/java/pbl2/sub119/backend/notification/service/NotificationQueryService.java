package pbl2.sub119.backend.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.BusinessException;
import pbl2.sub119.backend.notification.dto.response.NotificationResponse;
import pbl2.sub119.backend.notification.dto.response.UnreadNotificationCountResponse;
import pbl2.sub119.backend.notification.entity.Notification;
import pbl2.sub119.backend.notification.enumerated.NotificationStatus;
import pbl2.sub119.backend.notification.mapper.NotificationMapper;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(final Long userId) {
        return notificationMapper.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse getUnreadCount(final Long userId) {
        return new UnreadNotificationCountResponse(notificationMapper.countUnreadByUserId(userId));
    }

    @Transactional
    public void markRead(final Long notificationId, final Long userId) {
        final int updated = notificationMapper.updateStatus(
                notificationId, userId, NotificationStatus.READ, LocalDateTime.now());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }

    @Transactional
    public void markAllRead(final Long userId) {
        notificationMapper.updateAllToRead(userId, NotificationStatus.READ, LocalDateTime.now());
    }

    private NotificationResponse toResponse(final Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getPartyId(),
                n.getType(),
                n.getTitle(),
                n.getContent(),
                n.getStatus(),
                n.getCreatedAt(),
                n.getReadAt()
        );
    }
}
