package pbl2.sub119.backend.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pbl2.submate.backend.user.constant.UserConstant;

@Schema(description = "회원 정보 등록/수정 요청 DTO")
public record UserRequest(

        @Schema(description = "전화번호", example = "01012345678")
        @NotBlank(message = UserConstant.PHONE_NUMBER_REQUIRED_MESSAGE)
        @Pattern(
                regexp = "^01[0-9]{8,9}$",
                message = UserConstant.PHONE_NUMBER_PATTERN_MESSAGE
        )
        String phoneNumber,

        @Schema(description = "자사 이메일 아이디", example = "hajin")
        @NotBlank(message = UserConstant.EMAIL_REQUIRED_MESSAGE)
        String submateEmail,

        @Schema(description = "닉네임", example = "하진")
        @NotBlank(message = UserConstant.NICKNAME_REQUIRED_MESSAGE)
        @Size(min = 2, max = 30, message = UserConstant.NICKNAME_SIZE_MESSAGE)
        String nickname,

        @Schema(description = "PIN 번호", example = "1234")
        @NotBlank(message = UserConstant.PIN_REQUIRED_MESSAGE)
        @Pattern(
                regexp = "^[0-9]{4}$",
                message = UserConstant.PIN_PATTERN_MESSAGE
        )
        String pinNumber
) {
}