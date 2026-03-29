package pbl2.sub119.backend.mail.entity;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReceivedMail {
    private Long id;
    private Long userId;
    private String sender;
    private String subject;
    private String body;
    private String rawS3Key;
    private LocalDateTime receivedAt;
}