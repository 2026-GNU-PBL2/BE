package pbl2.sub119.backend.notification.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.notification.entity.Notification;
import pbl2.sub119.backend.notification.enumerated.NotificationStatus;
import pbl2.sub119.backend.notification.enumerated.NotificationType;
import pbl2.sub119.backend.notification.mapper.NotificationMapper;
import pbl2.sub119.backend.user.entity.UserEntity;
import pbl2.sub119.backend.user.mapper.UserMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCommandService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final SolapiSmsService solapiSmsService;

    @Transactional
    public void notify(
            final Long userId,
            final Long partyId,
            final NotificationType type,
            final String title,
            final String content
    ) {
        final LocalDateTime now = LocalDateTime.now();

        final Notification notification = Notification.builder()
                .userId(userId)
                .partyId(partyId)
                .type(type)
                .title(title)
                .content(content)
                .status(NotificationStatus.UNREAD)
                .read(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        notificationMapper.insert(notification);
        sendSms(notification);
    }

    @Transactional
    public void schedule(
            final Long userId,
            final Long partyId,
            final NotificationType type,
            final String title,
            final String content,
            final LocalDateTime scheduledAt
    ) {
        final LocalDateTime now = LocalDateTime.now();

        final Notification notification = Notification.builder()
                .userId(userId)
                .partyId(partyId)
                .type(type)
                .title(title)
                .content(content)
                .status(NotificationStatus.UNREAD)
                .read(false)
                .scheduledAt(scheduledAt)
                .createdAt(now)
                .updatedAt(now)
                .build();

        notificationMapper.insert(notification);
    }

    @Transactional
    public void sendScheduled(final Notification notification) {
        sendSms(notification);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Notification> claimPendingScheduledNotifications(final LocalDateTime now) {
        final List<Notification> pending = notificationMapper.findPendingScheduledNotificationsForUpdate(now);
        final LocalDateTime claimedAt = LocalDateTime.now();
        for (final Notification n : pending) {
            notificationMapper.updateSentAt(n.getId(), claimedAt);
        }
        return pending;
    }

    private void sendSms(final Notification notification) {
        final UserEntity user = userMapper.findById(notification.getUserId());

        if (user == null || user.getPhoneNumber() == null || user.getPhoneNumber().isBlank()) {
            log.warn("전화번호 미등록 — SMS 생략. userId={}, notificationId={}",
                    notification.getUserId(), notification.getId());
            if (notification.getScheduledAt() != null) {
                notificationMapper.updateSentAt(notification.getId(), LocalDateTime.now());
            }
            return;
        }

        solapiSmsService.send(
                notification.getUserId(),
                user.getPhoneNumber(),
                notification.getContent(),
                notification.getId()
        );

        notificationMapper.updateSentAt(notification.getId(), LocalDateTime.now());
    }
}