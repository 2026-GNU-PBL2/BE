package pbl2.sub119.backend.mail.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import pbl2.sub119.backend.mail.entity.ReceivedMail;

import java.time.LocalDateTime;

@Schema(description = "수신 메일 목록 응답 DTO")
@Builder
public record ReceivedMailResponse(
        @Schema(description = "메일 ID", example = "1")
        Long id,

        @Schema(description = "발신자", example = "Netflix <no-reply@netflix.com>")
        String sender,

        @Schema(description = "제목", example = "인증 코드 안내")
        String subject,

        @Schema(description = "수신 시각")
        LocalDateTime receivedAt
) {
    public static ReceivedMailResponse from(final ReceivedMail mail) {
        return ReceivedMailResponse.builder()
                .id(mail.getId())
                .sender(mail.getSender())
                .subject(mail.getSubject())
                .receivedAt(mail.getReceivedAt())
                .build();
    }
}
