package pbl2.sub119.backend.auth.userinfo;

import pbl2.sub119.backend.auth.dto.response.KakaoUserInfoResponse;

public class KakaoUserInfo implements OauthUserInfo {

    private final String socialId;
    private final String email;
    private final String name;

    public KakaoUserInfo(KakaoUserInfoResponse response) {
        this.socialId = String.valueOf(response.id());
        this.email = response.kakaoAccount().email();
        this.name = response.kakaoAccount().profile().nickname();
    }

    @Override
    public String getSocialId() {
        return socialId;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getName() {
        return name;
    }
}
