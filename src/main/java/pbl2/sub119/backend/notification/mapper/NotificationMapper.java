package pbl2.sub119.backend.notification.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.notification.entity.Notification;
import pbl2.sub119.backend.notification.enumerated.NotificationStatus;

@Mapper
public interface NotificationMapper {

    void insert(Notification notification);

    List<Notification> findByUserId(@Param("userId") Long userId);

    int countUnreadByUserId(@Param("userId") Long userId);

    int updateStatus(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("status") NotificationStatus status,
            @Param("readAt") LocalDateTime readAt
    );

    int updateAllToRead(
            @Param("userId") Long userId,
            @Param("status") NotificationStatus status,
            @Param("readAt") LocalDateTime readAt
    );

    int updateSentAt(
            @Param("id") Long id,
            @Param("sentAt") LocalDateTime sentAt
    );

    List<Notification> findPendingScheduledNotifications(@Param("now") LocalDateTime now);
}
