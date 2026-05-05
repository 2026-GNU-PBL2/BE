package pbl2.sub119.backend.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.common.enumerated.UserStatus;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.BusinessException;
import pbl2.sub119.backend.user.dto.request.UserRequest;
import pbl2.sub119.backend.user.dto.response.DuplicateCheckResponse;
import pbl2.sub119.backend.user.dto.response.UserResponse;
import pbl2.sub119.backend.user.dto.response.UserSignUpResponse;
import pbl2.sub119.backend.user.dto.response.UserUpdateResponse;

import pbl2.sub119.backend.user.entity.UserEntity;
import pbl2.sub119.backend.user.mapper.UserMapper;

import pbl2.sub119.backend.user.util.PinEncoder;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final String SUBMATE_EMAIL_DOMAIN = "@submate.cloud";

    private final UserMapper userMapper;
    private final PhoneVerificationService phoneVerificationService;

    @Transactional
    public UserSignUpResponse signUp(final Accessor accessor, final UserRequest request) {

        final UserEntity user = findUserOrThrow(accessor.getUserId());
        validateWithdrawnUser(user);

        if (user.getStatus() != UserStatus.PENDING_SIGNUP) {
            throw new IllegalArgumentException("이미 회원가입이 완료된 회원입니다.");
        }

//        if (!phoneVerificationService.isVerified(accessor.getUserId(), request.phoneNumber())) {
//            throw new BusinessException(ErrorCode.PHONE_NOT_VERIFIED);
//        }

        validateNicknameDuplication(user.getId(), request.nickname());
        validatePhoneNumberDuplication(user.getId(), request.phoneNumber());

        final String fullEmail = buildSubmateEmail(request.submateEmail());
        validateEmailDuplication(user.getId(), fullEmail);

        final String pinHash = PinEncoder.encode(request.pinNumber());

        userMapper.updateSignupInfo(
                user.getId(),
                request.nickname(),
                fullEmail,
                request.phoneNumber(),
                pinHash,
                UserStatus.ACTIVE.name()
        );

        // phoneVerificationService.consumeVerification(accessor.getUserId(), request.phoneNumber());

        final UserEntity updatedUser = findActiveUserOrThrow(user.getId());
        return UserSignUpResponse.from(updatedUser);
    }

    public UserResponse findUser(final Accessor accessor) {
        final UserEntity user = findUserOrThrow(accessor.getUserId());
        validateWithdrawnUser(user);
        return UserResponse.from(user);
    }

    @Transactional
    public UserUpdateResponse update(final Accessor accessor, final UserRequest request) {
        final UserEntity user = findUserOrThrow(accessor.getUserId());
        validateWithdrawnUser(user);

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("회원가입이 완료된 회원만 수정할 수 있습니다.");
        }

        validateNicknameDuplication(user.getId(), request.nickname());
        validatePhoneNumberDuplication(user.getId(), request.phoneNumber());

        final String fullEmail = buildSubmateEmail(request.submateEmail());
        validateEmailDuplication(user.getId(), fullEmail);

        final String pinHash = PinEncoder.encode(request.pinNumber());

        userMapper.updateUserInfo(
                user.getId(),
                request.nickname(),
                fullEmail,
                request.phoneNumber(),
                pinHash
        );

        final UserEntity updatedUser = findActiveUserOrThrow(user.getId());
        return UserUpdateResponse.from(updatedUser);
    }

    @Transactional
    public void withdraw(final Accessor accessor) {
        final UserEntity user = findUserOrThrow(accessor.getUserId());
        validateWithdrawnUser(user);

        userMapper.withdraw(user.getId(), UserStatus.WITHDRAWN.name());
    }

    public DuplicateCheckResponse checkEmail(final Long currentUserId, final String emailId) {
        final String fullEmail = buildSubmateEmail(emailId);
        final UserEntity existing = userMapper.findBySubmateEmail(fullEmail);
        if (existing == null || existing.getId().equals(currentUserId)) {
            return DuplicateCheckResponse.ofAvailable();
        }
        return DuplicateCheckResponse.ofUnavailable();
    }

    public DuplicateCheckResponse checkNickname(final Long currentUserId, final String nickname) {
        final UserEntity existing = userMapper.findByNickname(nickname);
        if (existing == null || existing.getId().equals(currentUserId)) {
            return DuplicateCheckResponse.ofAvailable();
        }
        return DuplicateCheckResponse.ofUnavailable();
    }

    private UserEntity findUserOrThrow(final Long userId) {
        final UserEntity user = userMapper.findById(userId);

        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }

        return user;
    }

    private UserEntity findActiveUserOrThrow(final Long userId) {
        final UserEntity user = userMapper.findActiveById(userId);

        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }

        return user;
    }

    private void validateWithdrawnUser(final UserEntity user) {
        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new IllegalArgumentException("이미 탈퇴한 회원입니다.");
        }
    }

    private void validateNicknameDuplication(final Long currentUserId, final String nickname) {
        final UserEntity existingUser = userMapper.findByNickname(nickname);

        if (existingUser != null && !existingUser.getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.USER_NICKNAME_DUPLICATE);
        }
    }

    private void validatePhoneNumberDuplication(final Long currentUserId, final String phoneNumber) {
        final UserEntity existingUser = userMapper.findByPhoneNumber(phoneNumber);

        if (existingUser != null && !existingUser.getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.USER_PHONE_DUPLICATE);
        }
    }

    private void validateEmailDuplication(final Long currentUserId, final String submateEmail) {
        final UserEntity existingUser = userMapper.findBySubmateEmail(submateEmail);

        if (existingUser != null && !existingUser.getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.USER_EMAIL_DUPLICATE);
        }
    }

    private String buildSubmateEmail(final String emailId) {
        final String normalized = emailId == null ? "" : emailId.trim();

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        if (normalized.contains("@")) {
            throw new IllegalArgumentException("이메일 아이디만 입력해야 합니다.");
        }

        return normalized + SUBMATE_EMAIL_DOMAIN;
    }
}
