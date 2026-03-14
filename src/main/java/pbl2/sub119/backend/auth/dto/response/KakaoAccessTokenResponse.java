package pbl2.sub119.backend.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoAccessTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") int expiresIn,
        @JsonProperty("refresh_token_expires_in") int refreshTokenExpiresIn,
        String scope,
        @JsonProperty("id_token") String idToken
) {
}
