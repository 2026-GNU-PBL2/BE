package pbl2.sub119.backend.auth.userinfo;

import org.springframework.stereotype.Component;

@Component
public interface OauthUserInfo {

    String getSocialId();

    String getEmail();

    String getName();
}
