package pbl2.sub119.backend.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import pbl2.submate.backend.user.entity.UserEntity;

@Schema(description = "회원가입 완료 응답 DTO")
public record UserSignUpResponse(

        @Schema(description = "회원 ID", example = "1")
        Long id,

        @Schema(description = "닉네임", example = "하진")
        String nickname,

        @Schema(description = "자사 이메일", example = "hajin@submate.com")
        String submateEmail,

        @Schema(description = "전화번호", example = "01012345678")
        String phoneNumber,

        @Schema(description = "회원 상태", example = "ACTIVE")
        String status
) {
        public static UserSignUpResponse from(final UserEntity user) {
                return new UserSignUpResponse(
                        user.getId(),
                        user.getNickname(),
                        user.getSubmateEmail(),
                        user.getPhoneNumber(),
                        user.getStatus().name()
                );
        }
}