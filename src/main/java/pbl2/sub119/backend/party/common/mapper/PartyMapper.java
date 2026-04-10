package pbl2.sub119.backend.party.common.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.common.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.common.enumerated.VacancyType;

@Mapper
public interface PartyMapper {

    // 새로운 파티 생성
    int insertParty(Party party);

    // 파티 상세 정보 조회
    Party findById(@Param("partyId") Long partyId);

    // 참여, 탈퇴, 승계 시 상태 변경 전 파티 정보 조회
    Party findByIdForUpdate(@Param("partyId") Long partyId);

    // 상품별 파티 목록 조회
    List<Party> findByProductId(@Param("productId") String productId);

    // 참여, 탈퇴, cycle 시 현재 파티 인원 수 갱신
    int updateCurrentMemberCount(
            @Param("partyId") Long partyId,
            @Param("currentMemberCount") int currentMemberCount
    );

    // 모집 상태 변경할 때 FULL, RECRUITING 전환
    int updateRecruitStatus(
            @Param("partyId") Long partyId,
            @Param("recruitStatus") RecruitStatus recruitStatus
    );

    // 이용 상태 변경 시 WAITING, ACTIVE, 종료 상태 전환
    int updateOperationStatus(
            @Param("partyId") Long partyId,
            @Param("operationStatus") OperationStatus operationStatus
    );

    // 결원 발생, 해소 시 NONE / MEMBER 상태 변경
    int updateVacancyType(
            @Param("partyId") Long partyId,
            @Param("vacancyType") VacancyType vacancyType
    );

    // 파티장 승계 시 새로운 파티장으로 변경
    int updateHostUserId(
            @Param("partyId") Long partyId,
            @Param("hostUserId") Long hostUserId
    );

    // 결원 있고 바로 참여 가능한 파티 목록 조회
    List<Party> findJoinablePartiesByProductId(@Param("productId") String productId);
}