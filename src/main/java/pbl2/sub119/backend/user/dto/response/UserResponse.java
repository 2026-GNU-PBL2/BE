package pbl2.sub119.backend.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import pbl2.submate.backend.user.entity.UserEntity;

@Schema(description = "회원 정보 조회 응답 DTO")
public record UserResponse(

        @Schema(description = "회원 ID", example = "1")
        Long id,

        @Schema(description = "닉네임", example = "하진")
        String nickname,

        @Schema(description = "자사 이메일", example = "hajin@submate.com")
        String submateEmail,

        @Schema(description = "전화번호", example = "01012345678")
        String phoneNumber,

        @Schema(description = "권한", example = "CUSTOMER")
        String role,

        @Schema(description = "회원 상태", example = "ACTIVE")
        String status
) {
        public static UserResponse from(final UserEntity user) {
                return new UserResponse(
                        user.getId(),
                        user.getNickname(),
                        user.getSubmateEmail(),
                        user.getPhoneNumber(),
                        user.getRole().name(),
                        user.getStatus().name()
                );
        }
}