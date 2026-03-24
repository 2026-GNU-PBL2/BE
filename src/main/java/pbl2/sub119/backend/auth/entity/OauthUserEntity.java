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
        this.userId = userId;
    }
}