package pbl2.sub119.backend.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleUserInfoResponse(
        @JsonProperty("sub") String id,
        @JsonProperty("email") String email,
        @JsonProperty("name") String name,
        @JsonProperty("picture") String picture
) {
}