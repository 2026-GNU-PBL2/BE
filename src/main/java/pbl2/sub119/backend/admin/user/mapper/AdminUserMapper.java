package pbl2.sub119.backend.admin.user.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.admin.user.dto.AdminUserPartyResponse;
import pbl2.sub119.backend.admin.user.dto.AdminUserResponse;
import pbl2.sub119.backend.user.entity.UserEntity;

@Mapper
public interface AdminUserMapper {

    // 관리자 회원 목록 조회
    List<AdminUserResponse> findUsers();

    // 관리자 회원 상세 기본 정보 조회
    UserEntity findUserById(@Param("userId") Long userId);

    // 관리자 회원 이메일 조회
    String findUserEmailByUserId(@Param("userId") Long userId);

    // 파티장으로 이용중인 파티 목록 조회
    List<AdminUserPartyResponse> findUsingPartiesByHostUserId(@Param("userId") Long userId);
}