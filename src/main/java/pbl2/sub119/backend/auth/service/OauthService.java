package pbl2.sub119.backend.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.auth.dto.request.OauthLoginRequest;
import pbl2.sub119.backend.auth.dto.response.AuthTokenDto;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.auth.entity.OauthInfo;
import pbl2.sub119.backend.auth.entity.OauthUser;
import pbl2.sub119.backend.auth.entity.OauthUserEntity;
import pbl2.sub119.backend.auth.enumerated.SocialProvider;
import pbl2.sub119.backend.auth.jwt.JwtProvider;
import pbl2.sub119.backend.auth.mapper.OauthUserMapper;
import pbl2.sub119.backend.auth.provider.OauthProvider;
import pbl2.sub119.backend.auth.provider.OauthProviders;
import pbl2.sub119.backend.auth.userinfo.OauthUserInfo;
import pbl2.sub119.backend.common.enumerated.UserRole;
import pbl2.sub119.backend.common.exception.AuthException;

import static pbl2.sub119.backend.common.error.ErrorCode.AUTH_USER_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class OauthService {

    private static final UserRole DEFAULT_USER_ROLE = UserRole.CUSTOMER;

    private final OauthProviders oauthProviders;
    private final OauthUserMapper oauthUserMapper;
    private final JwtProvider jwtProvider;

    @Transactional
    public AuthTokenDto login(final OauthLoginRequest request) {
        /*log.info("=== OAUTH LOGIN START ===");
        log.info("socialProvider: {}", request.socialProvider());
        log.info("code: '{}'", request.code());*/

        final OauthProvider provider = oauthProviders.getProvider(request.socialProvider());
        final String accessToken = provider.getAccessToken(request.code());
        final OauthUserInfo userInfo = provider.getUserInfo(accessToken);
        log.info("kakaoUserInfo raw = {}", userInfo);
        log.info("kakao id = {}", userInfo.getSocialId());
        log.info("kakao email = {}", userInfo.getEmail());
        log.info("kakao name = {}", userInfo.getName());


        final OauthUser user = findOrCreateUser(provider.getProvider(), userInfo);

        return AuthTokenDto.of(
                jwtProvider.createAccessToken(user.getId(), user.getSocialId(), user.getUserRole())
        );
    }

    public Accessor getCurrentAccessor(final Long userId, final String email, final UserRole userRole) {
        log.debug("Getting accessor for socialId: {}, userId: {}, userRole: {}", userId, email, userRole);

        if (!oauthUserMapper.existsById(userId)) {
            log.warn("User not found for userId: {}", userId);
            throw new AuthException(AUTH_USER_NOT_FOUND);
        }

        return Accessor.user(userId, email, userRole);
    }

    private OauthUser findOrCreateUser(final SocialProvider socialProvider, final OauthUserInfo userInfo) {
        final OauthUserEntity existing = oauthUserMapper.findByProviderAndSocialId(
                socialProvider,
                userInfo.getSocialId()
        );

        if (existing != null) {
            return existing;
        }

        final OauthInfo oauthInfo = OauthInfo.of(
                userInfo.getEmail(),
                userInfo.getName(),
                userInfo.getSocialId(),
                socialProvider
        );

        final OauthUserEntity newUser = OauthUserEntity.createFromOAuth(oauthInfo, DEFAULT_USER_ROLE);
        oauthUserMapper.insert(newUser);

        return newUser;
    }
}
