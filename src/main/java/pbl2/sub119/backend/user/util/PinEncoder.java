package pbl2.sub119.backend.user.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PinEncoder {

    private PinEncoder() {
    }

    public static String encode(final String rawPin) {
        return BCrypt.hashpw(rawPin, BCrypt.gensalt());
    }

    public static boolean matches(final String rawPin, final String encodedPin) {
        if (rawPin == null || encodedPin == null || encodedPin.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPin, encodedPin);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}