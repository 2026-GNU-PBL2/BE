package pbl2.sub119.backend.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfoResponse(
        @JsonProperty("id")
        Long id,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
            @JsonProperty("email")
            String email,

            @JsonProperty("profile")
            Profile profile
    ){
        public record Profile(
                @JsonProperty("nickname")
                String nickname
        ) {

        }
    }

}
