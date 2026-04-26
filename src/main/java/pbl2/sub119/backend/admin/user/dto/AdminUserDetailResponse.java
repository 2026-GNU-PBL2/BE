package pbl2.sub119.backend.admin.user.dto;

import java.time.LocalDateTime;
import java.util.List;
import pbl2.sub119.backend.common.enumerated.UserRole;
import pbl2.sub119.backend.common.enumerated.UserStatus;

public record AdminUserDetailResponse(
        Long userId,
        String displayUserId,
        String nickname,
        String email,
        String phoneNumber,
        UserRole role,
        UserStatus status,
        LocalDateTime createdAt,
        int usingPartyCount,
        List<AdminUserPartyResponse> usingParties
) {
}