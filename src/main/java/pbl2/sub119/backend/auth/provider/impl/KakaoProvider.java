package pbl2.sub119.backend.auth.provider.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import pbl2.sub119.backend.auth.dto.response.KakaoAccessTokenResponse;
import pbl2.sub119.backend.auth.dto.response.KakaoUserInfoResponse;
import pbl2.sub119.backend.auth.enumerated.SocialProvider;
import pbl2.sub119.backend.auth.provider.OauthProvider;
import pbl2.sub119.backend.auth.userinfo.KakaoUserInfo;
import pbl2.sub119.backend.auth.userinfo.OauthUserInfo;
import pbl2.sub119.backend.common.exception.AuthException;
import reactor.core.publisher.Mono;

import static pbl2.sub119.backend.auth.constant.KakaoOauthConstants.*;
import static pbl2.sub119.backend.common.error.ErrorCode.OAUTH_TOKEN_REQUEST_FAILED;
import static pbl2.sub119.backend.common.error.ErrorCode.OAUTH_USERINFO_RESPONSE_EMPTY;

@Component
public class KakaoProvider implements OauthProvider {

    private final WebClient webClient;
    private final String clientId;
    private final String redirectUri;
    private final String tokenUri;
    private final String userInfoUri;
    private final String clientSecret;

    public KakaoProvider(
            final WebClient webClient,
            @Value("${oauth.kakao.client-id}") final String clientId,
            @Value("${oauth.kakao.redirect-uri}") final String redirectUri,
            @Value("${oauth.kakao.token-uri}") final String tokenUri,
            @Value("${oauth.kakao.user-info-uri}") final String userInfoUri,
            @Value("${oauth.kakao.secret}") final String clientSecret) {
        this.webClient = webClient;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.tokenUri = tokenUri;
        this.userInfoUri = userInfoUri;
        this.clientSecret = clientSecret;
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.KAKAO;
    }

    @Override
    public String getAccessToken(String code) {
        final MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add(GRANT_TYPE_KEY, GRANT_TYPE);
        data.add(CLIENT_ID_KEY, clientId);
        data.add(REDIRECT_URI_KEY, redirectUri);
        data.add(CLIENT_SECRET_KEY, clientSecret);
        data.add(CODE_KEY, code);
        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(data))
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        this::handleOauthError
                )
                .bodyToMono(KakaoAccessTokenResponse.class)
                .map(KakaoAccessTokenResponse::accessToken)
                .block();
    }

    @Override
    public OauthUserInfo getUserInfo(String accessToken) {
        return new KakaoUserInfo(fetchUserInfo(accessToken));
    }

    private KakaoUserInfoResponse fetchUserInfo(String accessToken) {
        return webClient
                .post()
                .uri(userInfoUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        this::handleOauthError
                )
                .bodyToMono(KakaoUserInfoResponse.class)
                .blockOptional()
                .orElseThrow(() -> new AuthException(OAUTH_USERINFO_RESPONSE_EMPTY));
    }

    private Mono<? extends Throwable> handleOauthError(final ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(errorBody ->
                        Mono.error(new AuthException(OAUTH_TOKEN_REQUEST_FAILED))
                );
    }
}
