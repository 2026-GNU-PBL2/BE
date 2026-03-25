package pbl2.sub119.backend.auth.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OauthUserEntity implements OauthUser {
    private Long id;
    private OauthInfo oauthInfo;
    private UserRole userRole;
    private Long userId;

    private OauthUserEntity(OauthInfo oauthInfo, UserRole userRole) {
        this.oauthInfo = oauthInfo;
        this.userRole = userRole;
    }

    public static OauthUserEntity createFromOAuth(OauthInfo oauthInfo, UserRole userRole) {
        return new OauthUserEntity(oauthInfo, userRole);
    }

    public void connectUser(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
               }
        if (this.userId != null && !this.userId.equals(userId)) {
            throw new IllegalStateException("OAuth 계정은 이미 다른 사용자와 연결되어 있습니다.");
        }
        this.userId = userId;
    }

    public void reconnectUser(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId는 null일 수 없습니다.");
        }
        this.userId = userId;
    }
}