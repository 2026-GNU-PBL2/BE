package pbl2.sub119.backend.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.submate.backend.auth.entity.Accessor;
import pbl2.sub119.backend.common.enumerated.UserStatus;
import pbl2.sub119.backend.user.dto.request.UserRequest;
import pbl2.sub119.backend.user.dto.response.UserResponse;
import pbl2.sub119.backend.user.dto.response.UserSignUpResponse;
import pbl2.sub119.backend.user.dto.response.UserUpdateResponse;
import pbl2.submate.backend.user.entity.UserEntity;
import pbl2.submate.backend.user.mapper.UserMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final String SUBMATE_EMAIL_DOMAIN = "@submate.cloud";

    private final UserMapper userMapper;

    @Transactional
    public UserSignUpResponse signUp(final Accessor accessor, final UserRequest request) {
        final UserEntity user = findUserOrThrow(accessor.getUserId());
        validateWithdrawnUser(user);

        validateNicknameDuplication(user.getId(), request.nickname());
        validatePhoneNumberDuplication(user.getId(), request.phoneNumber());

        final String fullEmail = buildSubmateEmail(request.submateEmail());
        validateEmailDuplication(user.getId(), fullEmail);

        final String pinHash = request.pinNumber();

        userMapper.updateSignupInfo(
                user.getId(),
                request.nickname(),
                fullEmail,
                request.phoneNumber(),
                pinHash,
                UserStatus.ACTIVE.name()
        );

        final UserEntity updatedUser = findUserOrThrow(user.getId());
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

        validateNicknameDuplication(user.getId(), request.nickname());
        validatePhoneNumberDuplication(user.getId(), request.phoneNumber());

        final String fullEmail = buildSubmateEmail(request.submateEmail());
        validateEmailDuplication(user.getId(), fullEmail);

        final String pinHash = request.pinNumber();

        userMapper.updateUserInfo(
                user.getId(),
                request.nickname(),
                fullEmail,
                request.phoneNumber(),
                pinHash
        );

        final UserEntity updatedUser = findUserOrThrow(user.getId());
        return UserUpdateResponse.from(updatedUser);
    }

    @Transactional
    public void withdraw(final Accessor accessor) {
        final UserEntity user = findUserOrThrow(accessor.getUserId());
        validateWithdrawnUser(user);

        userMapper.withdraw(user.getId(), UserStatus.WITHDRAWN.name());
    }

    private UserEntity findUserOrThrow(final Long userId) {
        final UserEntity user = userMapper.findById(userId);

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
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
    }

    private void validatePhoneNumberDuplication(final Long currentUserId, final String phoneNumber) {
        final UserEntity existingUser = userMapper.findByPhoneNumber(phoneNumber);

        if (existingUser != null && !existingUser.getId().equals(currentUserId)) {
            throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
        }
    }

    private void validateEmailDuplication(final Long currentUserId, final String submateEmail) {
        final UserEntity existingUser = userMapper.findBySubmateEmail(submateEmail);

        if (existingUser != null && !existingUser.getId().equals(currentUserId)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
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