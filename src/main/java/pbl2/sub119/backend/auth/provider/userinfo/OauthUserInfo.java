package pbl2.sub119.backend.auth.provider.userinfo;

import org.springframework.stereotype.Component;

@Component
public interface OauthUserInfo {

    String getSocialId();

    String getEmail();

    String getName();
}
