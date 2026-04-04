package pbl2.sub119.backend.partyoperation.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.partyoperation.dto.response.PartyOperationMemberResponse;
import pbl2.sub119.backend.partyoperation.entity.PartyOperationMember;
import pbl2.sub119.backend.partyoperation.enumerated.OperationMemberStatus;

@Mapper
public interface PartyOperationMemberMapper {

    void insert(PartyOperationMember partyOperationMember);

    void deleteByPartyOperationId(@Param("partyOperationId") Long partyOperationId);

    PartyOperationMember findById(@Param("id") Long id);

    PartyOperationMember findByPartyIdAndUserId(
            @Param("partyId") Long partyId,
            @Param("userId") Long userId
    );

    List<PartyOperationMemberResponse> findResponsesByPartyOperationId(
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
            @Param("memberStatus") OperationMemberStatus memberStatus,
            @Param("confirmedAt") LocalDateTime confirmedAt,
            @Param("completedAt") LocalDateTime completedAt,
            @Param("activatedAt") LocalDateTime activatedAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    void markAllResetRequired(
            @Param("partyOperationId") Long partyOperationId,
            @Param("memberStatus") OperationMemberStatus memberStatus,
            @Param("operationMessage") String operationMessage,
            @Param("lastResetAt") LocalDateTime lastResetAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}