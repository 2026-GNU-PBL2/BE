package pbl2.sub119.backend.auth.provider.impl;

import pbl2.sub119.backend.auth.enumerate.SocialProvider;

public interface OauthProvider {

    SocialProvider getProvider();

    String getAccessToken(String code);

}
