package pbl2.sub119.backend.notification.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pbl2.sub119.backend.notification.entity.Notification;
import pbl2.sub119.backend.notification.mapper.NotificationMapper;
import pbl2.sub119.backend.notification.service.NotificationCommandService;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationMapper notificationMapper;
    private final NotificationCommandService notificationCommandService;

    @Scheduled(fixedDelay = 60000)
    public void sendScheduledNotifications() {
        final LocalDateTime now = LocalDateTime.now();
        final List<Notification> notifications = notificationMapper.findPendingScheduledNotifications(now);

        for (final Notification notification : notifications) {
            try {
                notificationCommandService.sendScheduled(notification);
            } catch (Exception e) {
                log.error("예약 알림 발송 실패. notificationId={}", notification.getId(), e);
            }
        }
    }
}