package pbl2.sub119.backend.admin.user.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pbl2.sub119.backend.admin.user.dto.AdminUserDetailResponse;
import pbl2.sub119.backend.admin.user.dto.AdminUserPartyResponse;
import pbl2.sub119.backend.admin.user.dto.AdminUserResponse;
import pbl2.sub119.backend.admin.user.mapper.AdminUserMapper;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.BusinessException;
import pbl2.sub119.backend.user.entity.UserEntity;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserMapper adminUserMapper;

    // 관리자 회원 목록 조회
    public List<AdminUserResponse> getUsers() {
        return adminUserMapper.findUsers();
    }

    // 관리자 회원 상세 조회
    public AdminUserDetailResponse getUser(final Long userId) {
        final UserEntity user = adminUserMapper.findUserById(userId);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        final String email = adminUserMapper.findUserEmailByUserId(userId);
        final List<AdminUserPartyResponse> usingParties =
                adminUserMapper.findUsingPartiesByHostUserId(userId);

        return new AdminUserDetailResponse(
                user.getId(),
                formatUserId(user.getId()),
                user.getNickname(),
                email,
                user.getPhoneNumber(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                usingParties.size(),
                usingParties
        );
    }

    // 관리자 화면용 회원 ID 포맷
    private String formatUserId(final Long userId) {
        return String.format("USR-%03d", userId);
    }
}