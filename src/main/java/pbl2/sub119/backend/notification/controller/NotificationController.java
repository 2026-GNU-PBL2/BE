package pbl2.sub119.backend.notification.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.notification.dto.response.NotificationResponse;
import pbl2.sub119.backend.notification.dto.response.UnreadNotificationCountResponse;
import pbl2.sub119.backend.notification.controller.docs.NotificationDocs;
import pbl2.sub119.backend.notification.service.NotificationQueryService;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationDocs {

    private final NotificationQueryService notificationQueryService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(@Auth final Accessor accessor) {
        return ResponseEntity.ok(notificationQueryService.getNotifications(accessor.getUserId()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadNotificationCountResponse> getUnreadCount(@Auth final Accessor accessor) {
        return ResponseEntity.ok(notificationQueryService.getUnreadCount(accessor.getUserId()));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markRead(
            @Auth final Accessor accessor,
            @PathVariable final Long notificationId
    ) {
        notificationQueryService.markRead(notificationId, accessor.getUserId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@Auth final Accessor accessor) {
        notificationQueryService.markAllRead(accessor.getUserId());
        return ResponseEntity.ok().build();
    }
}
