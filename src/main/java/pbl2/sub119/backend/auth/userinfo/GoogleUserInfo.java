package pbl2.sub119.backend.auth.userinfo;

import pbl2.sub119.backend.auth.dto.response.GoogleUserInfoResponse;

public class GoogleUserInfo implements OauthUserInfo {

    private final String socialId;
    private final String email;
    private final String name;

    public GoogleUserInfo(GoogleUserInfoResponse response) {
        this.socialId = String.valueOf(response.id());
        this.email = response.email();
        this.name = response.name();
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
