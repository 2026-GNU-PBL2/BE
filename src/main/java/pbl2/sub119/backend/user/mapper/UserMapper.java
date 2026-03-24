package pbl2.sub119.backend.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.submate.backend.user.entity.UserEntity;

@Mapper
public interface UserMapper {

    UserEntity findById(@Param("id") Long id);

    boolean existsById(@Param("id") Long id);

    void insert(UserEntity user);

    UserEntity findByNickname(@Param("nickname") String nickname);

    UserEntity findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    UserEntity findBySubmateEmail(@Param("submateEmail") String submateEmail);

    void updateSignupInfo(
            @Param("id") Long id,
            @Param("nickname") String nickname,
            @Param("submateEmail") String submateEmail,
            @Param("phoneNumber") String phoneNumber,
            @Param("pinHash") String pinHash,
            @Param("status") String status
    );

    void updateUserInfo(
            @Param("id") Long id,
            @Param("nickname") String nickname,
            @Param("submateEmail") String submateEmail,
            @Param("phoneNumber") String phoneNumber,
            @Param("pinHash") String pinHash
    );

    void withdraw(
            @Param("id") Long id,
            @Param("status") String status
    );
}