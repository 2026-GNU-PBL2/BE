package pbl2.sub119.backend.user.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserConstant {

    // Phone
    public static final String PHONE_NUMBER_REQUIRED_MESSAGE = "전화번호는 필수입니다.";
    public static final String PHONE_NUMBER_PATTERN_MESSAGE = "유효한 휴대폰 번호를 입력해 주세요. (예: 01012345678)";
    public static final String PHONE_NUMBER_REGEXP = "^01[016789][0-9]{7,8}$";

    // Submate Mail
    public static final String EMAIL_REQUIRED_MESSAGE = "이메일 아이디는 필수입니다.";

    // Nickname
    public static final String NICKNAME_REQUIRED_MESSAGE = "닉네임은 필수입니다.";
    public static final String NICKNAME_SIZE_MESSAGE = "닉네임은 2자 이상 30자 이하로 입력해야 합니다.";

    // PIN
    public static final String PIN_REQUIRED_MESSAGE = "PIN 번호는 필수입니다.";
    public static final String PIN_PATTERN_MESSAGE = "PIN 번호는 4자리 숫자여야 합니다.";

    // OTP
    public static final String OTP_REQUIRED_MESSAGE = "인증번호는 필수입니다.";
    public static final String OTP_PATTERN_MESSAGE = "인증번호는 6자리 숫자여야 합니다.";
}