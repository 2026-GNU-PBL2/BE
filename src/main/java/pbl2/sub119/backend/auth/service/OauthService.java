package pbl2.sub119.backend.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.auth.dto.request.OauthLoginRequest;
import pbl2.sub119.backend.auth.dto.response.AuthTokenDto;
import pbl2.submate.backend.auth.entity.Accessor;
import pbl2.submate.backend.auth.entity.OauthInfo;
import pbl2.submate.backend.auth.entity.OauthUserEntity;
import pbl2.submate.backend.auth.enumerated.SocialProvider;
import pbl2.submate.backend.auth.jwt.JwtProvider;
import pbl2.submate.backend.auth.mapper.OauthUserMapper;
import pbl2.submate.backend.auth.provider.OauthProvider;
import pbl2.submate.backend.auth.provider.OauthProviders;
import pbl2.submate.backend.auth.userinfo.OauthUserInfo;
import pbl2.submate.backend.common.exception.AuthException;
import pbl2.submate.backend.user.entity.UserEntity;
import pbl2.submate.backend.user.mapper.UserMapper;

import static pbl2.submate.backend.common.error.ErrorCode.AUTH_USER_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class OauthService {

    private static final UserRole DEFAULT_USER_ROLE = UserRole.CUSTOMER;

    private final OauthProviders oauthProviders;
    private final OauthUserMapper oauthUserMapper;
    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;

    @Transactional
    public AuthTokenDto login(final OauthLoginRequest request) {
        log.info("=== OAUTH LOGIN START ===");
        log.info("socialProvider: {}", request.socialProvider());
        log.info("code: '{}'", request.code());

        final OauthProvider provider = oauthProviders.getProvider(request.socialProvider());
        final String oauthAccessToken = provider.getAccessToken(request.code());
        final OauthUserInfo userInfo = provider.getUserInfo(oauthAccessToken);

        log.info("Info raw = {}", userInfo);
        log.info("id = {}", userInfo.getSocialId());
        log.info("email = {}", userInfo.getEmail());
        log.info("name = {}", userInfo.getName());

        final OauthUserEntity oauthUser = findOrCreateOauthUser(provider.getProvider(), userInfo);
        final Long userId = getOrCreateServiceUser(oauthUser);

        log.info("oauthUser.id = {}", oauthUser.getId());
        log.info("oauthUser.userId = {}", oauthUser.getUserId());
        log.info("issued jwt userId = {}", userId);

        return AuthTokenDto.of(
                jwtProvider.createAccessToken(userId, oauthUser.getSocialId(), oauthUser.getUserRole())
        );
    }

    public Accessor getCurrentAccessor(final Long userId, final String socialId, final UserRole userRole) {
        log.debug("Getting accessor for userId: {}, socialId: {}, userRole: {}", userId, socialId, userRole);

        if (!userMapper.existsById(userId)) {
            log.warn("User not found for userId: {}", userId);
            throw new AuthException(AUTH_USER_NOT_FOUND);
        }

        return Accessor.user(userId, socialId, userRole);
    }

    private OauthUserEntity findOrCreateOauthUser(final SocialProvider socialProvider, final OauthUserInfo userInfo) {
        final OauthUserEntity existing = oauthUserMapper.findByProviderAndSocialId(
                socialProvider,
                userInfo.getSocialId()
        );

        if (existing != null) {
            log.info("Existing oauth user found. oauthUser.id={}, oauthUser.userId={}", existing.getId(), existing.getUserId());
            return existing;
        }

        final OauthInfo oauthInfo = OauthInfo.of(
                userInfo.getEmail(),
                userInfo.getName(),
                userInfo.getSocialId(),
                socialProvider
        );

        final OauthUserEntity newOauthUser = OauthUserEntity.createFromOAuth(oauthInfo, DEFAULT_USER_ROLE);
        oauthUserMapper.insert(newOauthUser);

        log.info("New oauth user created. oauthUser.id={}", newOauthUser.getId());

        return newOauthUser;
    }

    private Long getOrCreateServiceUser(final OauthUserEntity oauthUser) {
        if (oauthUser.getUserId() != null) {
            log.info("Connected service user already exists. userId={}", oauthUser.getUserId());
            return oauthUser.getUserId();
        }

        final UserEntity newUser = UserEntity.createPendingUser(DEFAULT_USER_ROLE);

        log.info("Before users insert. newUser.id={}", newUser.getId());
        userMapper.insert(newUser);
        log.info("After users insert. newUser.id={}", newUser.getId());

        oauthUserMapper.updateUserId(oauthUser.getId(), newUser.getId());
        oauthUser.connectUser(newUser.getId());

        log.info("oauth_user updated with user_id. oauthUser.id={}, userId={}", oauthUser.getId(), newUser.getId());

        return newUser.getId();
    }
}