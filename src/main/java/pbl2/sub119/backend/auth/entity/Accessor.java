package pbl2.sub119.backend.auth.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.catalina.User;
import pbl2.sub119.backend.common.enumerated.UserRole;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Accessor {

    private final Long userId;  // platform 고유 ID
    private final String email; // 유니크 체크용 이메일
    private final UserRole role; // nullable (확장성)

    public static Accessor user(Long userId, String email, UserRole userRole) {
        return new Accessor(userId, email, userRole);
    }
}
