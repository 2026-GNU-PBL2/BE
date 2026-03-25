package pbl2.sub119.backend.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.auth.dto.request.OauthLoginRequest;
import pbl2.sub119.backend.auth.dto.response.AuthTokenDto;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.auth.entity.OauthInfo;
import pbl2.sub119.backend.auth.entity.OauthUserEntity;
import pbl2.sub119.backend.auth.enumerated.SocialProvider;
import pbl2.sub119.backend.auth.jwt.JwtProvider;
import pbl2.sub119.backend.auth.mapper.OauthUserMapper;
import pbl2.sub119.backend.auth.provider.OauthProvider;
import pbl2.sub119.backend.auth.provider.OauthProviders;
import pbl2.sub119.backend.auth.userinfo.OauthUserInfo;
<<<<<<< HEAD
import pbl2.sub119.backend.common.enumerated.UserRole;
=======
import pbl2.sub119.backend.common.enumerated.UserStatus;
>>>>>>> 2ee923e (fix: 소프트 삭제 및 재가입 로직 개선, merge 충돌 해결)
import pbl2.sub119.backend.common.exception.AuthException;
import pbl2.sub119.backend.user.entity.UserEntity;
import pbl2.sub119.backend.user.mapper.UserMapper;

import static pbl2.sub119.backend.common.error.ErrorCode.AUTH_USER_NOT_FOUND;

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
        final OauthProvider provider = oauthProviders.getProvider(request.socialProvider());
        final String oauthAccessToken = provider.getAccessToken(request.code());
        final OauthUserInfo userInfo = provider.getUserInfo(oauthAccessToken);

        final OauthUserEntity oauthUser = findOrCreateOauthUser(provider.getProvider(), userInfo);
        final Long userId = getOrCreateServiceUser(oauthUser);

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
            log.info("Existing oauth user found. oauthUser.id={}, oauthUser.userId={}",
                    existing.getId(), existing.getUserId());
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
        final Long linkedUserId = oauthUser.getUserId();

        if (linkedUserId != null) {
            final UserEntity linkedUser = userMapper.findById(linkedUserId);

            if (linkedUser != null) {
                if (linkedUser.getDeletedAt() == null) {
                    return linkedUserId;
                }

                userMapper.restoreForResignup(
                        linkedUserId,
                        UserStatus.PENDING_SIGNUP.name(),
                        UserStatus.WITHDRAWN.name()
                );
                return linkedUserId;
            }
        }

        final UserEntity newUser = UserEntity.createPendingUser(DEFAULT_USER_ROLE);
        userMapper.insert(newUser);
        oauthUserMapper.updateUserId(oauthUser.getId(), newUser.getId());
        oauthUser.connectUser(newUser.getId());

        return newUser.getId();
    }
}