package pbl2.sub119.backend.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import pbl2.sub119.backend.user.constant.UserConstant;

@Schema(description = "휴대폰 인증 요청 DTO")
public record PhoneVerificationRequest(

        @Schema(description = "휴대폰 번호", example = "01012345678")
        @NotBlank(message = UserConstant.PHONE_NUMBER_REQUIRED_MESSAGE)
        @Pattern(
                regexp = "^01[0-9]{8,9}$",
                message = UserConstant.PHONE_NUMBER_PATTERN_MESSAGE
        )
        String phoneNumber
) {
}
