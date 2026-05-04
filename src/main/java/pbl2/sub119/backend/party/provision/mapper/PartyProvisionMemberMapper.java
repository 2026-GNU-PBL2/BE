package pbl2.sub119.backend.party.provision.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionMemberResponse;
import pbl2.sub119.backend.party.provision.entity.PartyProvisionMember;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionMemberStatus;

@Mapper
public interface PartyProvisionMemberMapper {

    void insert(PartyProvisionMember partyOperationMember);

    void deleteByPartyOperationId(@Param("partyOperationId") Long partyOperationId);

    PartyProvisionMember findById(@Param("id") Long id);

    PartyProvisionMember findByPartyIdAndUserId(
            @Param("partyId") Long partyId,
            @Param("userId") Long userId
    );

    List<PartyProvisionMember> findByPartyOperationId(
            @Param("partyOperationId") Long partyOperationId
    );

    List<PartyProvisionMemberResponse> findResponsesByPartyOperationId(
            @Param("partyOperationId") Long partyOperationId
    );

    int countByPartyOperationId(@Param("partyOperationId") Long partyOperationId);

    int countActiveByPartyOperationId(@Param("partyOperationId") Long partyOperationId);

    int countByPartyIdAndUserId(
            @Param("partyId") Long partyId,
            @Param("userId") Long userId
    );

    void markActive(
            @Param("id") Long id,
            @Param("memberStatus") ProvisionMemberStatus memberStatus,
            @Param("confirmedAt") LocalDateTime confirmedAt,
            @Param("completedAt") LocalDateTime completedAt,
            @Param("activatedAt") LocalDateTime activatedAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    // confirm 미완료 타임아웃 대상 멤버 조회 (REQUIRED + N시간 경과, 미처리건만)
    List<PartyProvisionMember> findRequiredMembersTimedOut(@Param("thresholdHours") int thresholdHours);

    void markPenaltyApplied(
            @Param("id") Long id,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    void markAllResetRequired(
            @Param("partyOperationId") Long partyOperationId,
            @Param("memberStatus") ProvisionMemberStatus memberStatus,
            @Param("operationMessage") String operationMessage,
            @Param("lastResetAt") LocalDateTime lastResetAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    void markPenaltyApplied(
            @Param("id") Long id,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    // 24시간 초과 미확인 파티원 조회
    List<PartyProvisionMember> findRequiredMembersTimedOut();


    // 12시간 / 22시간 미확인 파티원 리마인드 조회
    List<PartyProvisionMember> findRequiredMembersReminderDue(@Param("elapsedHours") int elapsedHours);
}