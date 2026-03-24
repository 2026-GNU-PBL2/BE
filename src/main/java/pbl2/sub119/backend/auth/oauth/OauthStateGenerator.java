package pbl2.sub119.backend.auth.oauth;

import java.security.SecureRandom;
import java.util.Base64;

public final class OauthStateGenerator {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private OauthStateGenerator() {}

    public static String generate() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
