package pbl2.sub119.backend.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverAccessTokenResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        int expiresIn
) {

}
