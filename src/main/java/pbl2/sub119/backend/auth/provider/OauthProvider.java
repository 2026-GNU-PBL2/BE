package pbl2.sub119.backend.auth.provider;

import pbl2.sub119.backend.auth.enumerated.SocialProvider;
import pbl2.sub119.backend.auth.userinfo.OauthUserInfo;

public interface OauthProvider {

    SocialProvider getProvider();

    String getAccessToken(String code);

    OauthUserInfo getUserInfo(String accessToken);

}
