package pbl2.sub119.backend.auth.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pbl2.sub119.backend.common.enumerated.UserRole;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Accessor {

    private final Long userId;
    private final String socialId;
    private final UserRole role;

    public static Accessor user(final Long userId, final String socialId, final UserRole userRole) {
        return new Accessor(userId, socialId, userRole);
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}