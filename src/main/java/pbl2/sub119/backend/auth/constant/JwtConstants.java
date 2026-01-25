package pbl2.sub119.backend.auth.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class JwtConstants {
    // JWT Claims
    public static final String USER_ID = "user_id";
    public static final String EMAIL = "email";
    public static final String USER_ROLE = "user_role";
    public static final String TOKEN_TYPE = "token_type";

    // Header & Prefix
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    // Request Attributes
    public static final String REQUEST_ATTR_USER_ID = "userId";
    public static final String REQUEST_ATTR_EMAIL = "email";
    public static final String REQUEST_ATTR_USER_ROLE = "userRole";
}