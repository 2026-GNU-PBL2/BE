package pbl2.sub119.backend.admin.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminDashboardMapper {

    // 운영 중 상품 수 조회
    int countOperatingProducts();

    // 활성 회원 수 조회
    int countActiveMembers();

    // 모집 중 파티 수 조회
    int countRecruitingParties();

    // 미입금+결제 실패 건수 조회
    int countFailedPayments();

    // 결제 실패 파티 수 조회
    int countFailedPaymentParties();

    // 자동 매칭 대기 회원 수 조회
    int countWaitingMatchUsers();
}