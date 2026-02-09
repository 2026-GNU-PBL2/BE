package pbl2.sub119.backend.auth.dto.response;

public record AuthTokenDto(
        String accessToken
) {

    public static AuthTokenDto of(String accessToken) {
        return new AuthTokenDto(accessToken);
    }
}
