package pbl2.sub119.backend.party.common.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;

@Mapper
public interface PartyMemberMapper {

    // 새 파티 멤버 저장
    int insertPartyMember(PartyMember partyMember);

    // 특정 파티에서 현재 사용자의 멤버 정보를 조회
    PartyMember findByPartyIdAndUserId(
            @Param("partyId") Long partyId,
            @Param("userId") Long userId
    );

    // 특정 파티에서 현재 사용자의 멤버 정보를 수정 목적으로 조회
    // 참여, 탈퇴 예약, 상태 변경처럼 동시에 요청이 들어올 수 있는 작업에서 사용
    PartyMember findByPartyIdAndUserIdForUpdate(
            @Param("partyId") Long partyId,
            @Param("userId") Long userId
    );

    // 파티장 멤버 정보를 수정 목적으로 조회
    // 승계, 파티 종료, 파티장 상태 변경처럼 파티장 정보가 바뀌는 작업에서 사용
    PartyMember findHostMemberByPartyIdForUpdate(@Param("partyId") Long partyId);

    // 멤버 ID로 단건 조회
    PartyMember findById(@Param("memberId") Long memberId);

    // 파티에 속한 전체 멤버 목록을 조회
    List<PartyMember> findMembersByPartyId(@Param("partyId") Long partyId);

    // 다음 결제일 기준으로 탈퇴 처리될 멤버 목록을 조회
    List<PartyMember> findLeaveReservedMembers(@Param("partyId") Long partyId);

    // 다음 주기에 ACTIVE로 전환될 대기 멤버 목록을 조회
    List<PartyMember> findSwitchWaitingMembers(@Param("partyId") Long partyId);

    // 이용 대상 멤버 목록을 조회
    // 현재 실제 이용에 포함되는 멤버를 기준으로 이용 정보 제공 대상을 잡을 때 사용
    List<PartyMember> findProvisionTargetMembersByPartyId(@Param("partyId") Long partyId);

    // 파티 인원 계산에 포함되는 멤버 수를 조회
    int countOccupiedMembers(@Param("partyId") Long partyId);

    // 특정 사용자를 탈퇴 예약 상태로 변경
    int updateLeaveReserved(
            @Param("partyId") Long partyId,
            @Param("userId") Long userId
    );

    // 탈퇴 예약 상태를 취소하고 다시 ACTIVE로
    int clearLeaveReserved(
            @Param("partyId") Long partyId,
            @Param("userId") Long userId
    );

    // 멤버 상태만 변경
    int updateStatus(
            @Param("memberId") Long memberId,
            @Param("status") PartyMemberStatus status
    );

    // 멤버 상태를 변경하고 탈퇴 시각을 함께 기록
    int updateStatusAndLeftAt(
            @Param("memberId") Long memberId,
            @Param("status") PartyMemberStatus status
    );

    // 멤버 상태를 변경하고 활성화 시각을 함께 기록
    int updateStatusAndActivatedAt(
            @Param("memberId") Long memberId,
            @Param("status") PartyMemberStatus status
    );

    // 멤버 역할을 변경
    // 파티장 승계처럼 HOST / MEMBER 역할이 바뀔 때 사용한
    int updateRole(
            @Param("memberId") Long memberId,
            @Param("role") PartyRole role
    );
}