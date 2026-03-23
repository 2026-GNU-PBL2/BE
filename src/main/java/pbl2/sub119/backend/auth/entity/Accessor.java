package pbl2.sub119.backend.auth.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.catalina.User;
import pbl2.sub119.backend.common.enumerated.UserRole;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Accessor {

    private final Long userId;
    private final String email;
    private final UserRole role;

    public static Accessor user(Long userId, String email, UserRole userRole) {
        return new Accessor(userId, email, userRole);
    }
}
