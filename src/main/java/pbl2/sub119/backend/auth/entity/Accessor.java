package pbl2.sub119.backend.auth.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Accessor {

    private final Long userId;  // platform 고유 ID
    private final String email; // 유니크 체크용 이메일
    private final String role; // nullable (확장성)

    public static Accessor user(Long userId, String email, String role) {
        return new Accessor(userId, email, role);
    }
}
