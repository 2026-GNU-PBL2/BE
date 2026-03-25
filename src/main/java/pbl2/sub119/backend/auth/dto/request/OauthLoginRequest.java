package pbl2.sub119.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pbl2.sub119.backend.auth.enumerated.SocialProvider;

public record OauthLoginRequest(
         @NotBlank String code,
         @NotNull SocialProvider socialProvider
) {
}
