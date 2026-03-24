package pbl2.sub119.backend.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pbl2.sub119.backend.common.enumerated.UserStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    private Long id;
    private String nickname;
    private String submateEmail;
    private String phoneNumber;
    private String pinHash;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static UserEntity createPendingUser(final UserRole role) {
        return UserEntity.builder()
                .role(role)
                .status(UserStatus.PENDING_SIGNUP)
                .build();
    }
}