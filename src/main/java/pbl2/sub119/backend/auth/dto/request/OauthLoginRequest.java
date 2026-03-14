package pbl2.sub119.backend.auth.dto.request;

import pbl2.sub119.backend.auth.enumerated.SocialProvider;

public record OauthLoginRequest(
        String code,
        SocialProvider socialProvider
) {
}
