package pbl2.sub119.backend.user.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserConstant {

    // Phone
    public static final String PHONE_NUMBER_REQUIRED_MESSAGE = "전화번호는 필수입니다.";
    public static final String PHONE_NUMBER_PATTERN_MESSAGE = "전화번호는 01012345678 형식이어야 합니다.";

    // Submate Mail
    public static final String EMAIL_REQUIRED_MESSAGE = "이메일 아이디는 필수입니다.";

    // Nickname
    public static final String NICKNAME_REQUIRED_MESSAGE = "닉네임은 필수입니다.";
    public static final String NICKNAME_SIZE_MESSAGE = "닉네임은 2자 이상 30자 이하로 입력해야 합니다.";

    // PIN
    public static final String PIN_REQUIRED_MESSAGE = "PIN 번호는 필수입니다.";
    public static final String PIN_PATTERN_MESSAGE = "PIN 번호는 4자리 숫자여야 합니다.";
}