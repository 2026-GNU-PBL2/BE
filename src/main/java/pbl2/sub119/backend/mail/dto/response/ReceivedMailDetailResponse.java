package pbl2.sub119.backend.mail.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import pbl2.sub119.backend.mail.entity.ReceivedMail;

import java.time.LocalDateTime;
@Schema(description = "수신 메일 상세 응답 DTO")
@Builder
public record ReceivedMailDetailResponse(
        Long id,
        String sender,
        String subject,
        String body,
        LocalDateTime receivedAt
) {
    public static ReceivedMailDetailResponse from(final ReceivedMail mail) {
        return ReceivedMailDetailResponse.builder()
                .id(mail.getId())
                .sender(mail.getSender())
                .subject(mail.getSubject())
                .body(mail.getBody())
                .receivedAt(mail.getReceivedAt())
                .build();
    }
}
