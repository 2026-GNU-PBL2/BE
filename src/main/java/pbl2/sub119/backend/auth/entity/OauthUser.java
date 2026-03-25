package pbl2.sub119.backend.auth.entity;

<<<<<<< HEAD
import pbl2.submate.backend.auth.enumerated.SocialProvider;
=======
import pbl2.sub119.backend.auth.enumerated.SocialProvider;
>>>>>>> 2ee923e (fix: 소프트 삭제 및 재가입 로직 개선, merge 충돌 해결)

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
