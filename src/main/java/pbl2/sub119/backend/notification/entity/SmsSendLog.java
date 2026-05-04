package pbl2.sub119.backend.notification.entity;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pbl2.sub119.backend.notification.enumerated.SmsSendStatus;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SmsSendLog {

    private Long id;
    private Long notificationId;
    private Long userId;
    private String phoneNumber;
    private String content;
    private SmsSendStatus status;
    private String failReason;
    private LocalDateTime createdAt;
}
