package pbl2.sub119.backend.auth.userinfo;

import pbl2.sub119.backend.auth.dto.response.NaverUserInfoResponse;

public class NaverUserInfo implements OauthUserInfo{

    private final NaverUserInfoResponse naverResponse;

    public NaverUserInfo(NaverUserInfoResponse naverUserInfoResponse) {
        if (naverUserInfoResponse == null) {
            throw new IllegalArgumentException("NaverUserInfoResponse cannot be null");
        }
        this.naverResponse = naverUserInfoResponse;
    }

    @Override
    public String getSocialId() {
        return naverResponse.response().id();
    }

    @Override
    public String getEmail() {
        return naverResponse.response().email();
    }

    @Override
    public String getName() {
        return naverResponse.response().name();
    }
}
