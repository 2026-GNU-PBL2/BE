package pbl2.sub119.backend.auth.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pbl2.sub119.backend.common.enumerated.UserRole;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OauthUserEntity implements OauthUser {
    private Long id;
    private OauthInfo oauthInfo;
    private UserRole userRole;

    private OauthUserEntity(OauthInfo oauthInfo, UserRole userRole) {
        this.oauthInfo = oauthInfo;
        this.userRole = userRole;
    }

    public static OauthUserEntity createFromOAuth(OauthInfo oauthInfo, UserRole userRole) {
        return new OauthUserEntity(oauthInfo, userRole);
    }
}
