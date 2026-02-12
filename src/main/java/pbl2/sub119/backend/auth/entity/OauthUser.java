package pbl2.sub119.backend.auth.entity;

import pbl2.sub119.backend.auth.enumerated.SocialProvider;
import pbl2.sub119.backend.common.enumerated.UserRole;

public interface OauthUser {
    Long getId();

    OauthInfo getOauthInfo();

    UserRole getUserRole();

    default String getEmail() {
        return getOauthInfo().getEmail();
    }

    default String getName() {
        return getOauthInfo().getName();
    }

    default String getSocialId() {
        return getOauthInfo().getSocialId();
    }

    default SocialProvider getSocialProvider() {
        return getOauthInfo().getSocialProvider();
    }
}
