package pbl2.sub119.backend.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "중복확인 응답 DTO")
public record DuplicateCheckResponse(

        @Schema(description = "사용 가능 여부", example = "true")
        boolean available
) {
    public static DuplicateCheckResponse ofAvailable() {
        return new DuplicateCheckResponse(true);
    }

    public static DuplicateCheckResponse ofUnavailable() {
        return new DuplicateCheckResponse(false);
    }
}
