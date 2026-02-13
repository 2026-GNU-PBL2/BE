package pbl2.sub119.backend.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverUserInfoResponse(

        @JsonProperty("resultcode")
        String resultCode,

        @JsonProperty("message")
        String message,

        @JsonProperty("response")
        Response response
) {

    public record Response(
            @JsonProperty("id")
            String id,

            @JsonProperty("email")
            String email,

            @JsonProperty("name")
            String name
    ) {

    }
}
