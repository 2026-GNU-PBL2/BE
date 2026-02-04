package pbl2.sub119.backend.auth.provider.impl;

import org.springframework.stereotype.Component;
import pbl2.sub119.backend.auth.enumerate.SocialProvider;
import pbl2.sub119.backend.common.exception.AuthException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static pbl2.sub119.backend.common.error.ErrorCode.IS_NOT_VALID_SOCIAL;

@Component
public class OauthProviders {
    private final Map<SocialProvider, OauthProvider> providerMap;

    public OauthProviders(List<OauthProvider> providers) {
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(
                        OauthProvider::getProvider, Function.identity()
                ));
    }

    public OauthProvider getProvider(SocialProvider provider) {
        OauthProvider social = providerMap.get(provider);
        if (social == null) {
            throw new AuthException(IS_NOT_VALID_SOCIAL);
        }
        return social;
    }
}
