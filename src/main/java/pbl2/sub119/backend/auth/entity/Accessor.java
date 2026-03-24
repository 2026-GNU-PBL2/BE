package pbl2.sub119.backend.auth.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
