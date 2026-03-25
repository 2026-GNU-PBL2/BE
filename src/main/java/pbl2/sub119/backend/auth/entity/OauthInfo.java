package pbl2.sub119.backend.auth.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pbl2.sub119.backend.auth.enumerated.SocialProvider;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OauthInfo {

    private String email;
    private String name;
    private String socialId;
    private SocialProvider socialProvider;

    public static OauthInfo of(
            final String email,
            final String name,
            final String socialId,
            final SocialProvider socialProvider
    ) {
        return new OauthInfo(email, name, socialId, socialProvider);
    }
}
