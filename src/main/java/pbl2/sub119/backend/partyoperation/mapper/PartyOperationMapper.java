package pbl2.sub119.backend.partyoperation.mapper;

import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.partyoperation.entity.PartyOperation;
import pbl2.sub119.backend.partyoperation.enumerated.OperationStatus;
import pbl2.sub119.backend.partyoperation.enumerated.OperationType;

@Mapper
public interface PartyOperationMapper {

    PartyOperation findByPartyId(@Param("partyId") Long partyId);

    PartyOperation findByPartyIdForUpdate(@Param("partyId") Long partyId);

    void insert(PartyOperation partyOperation);

    void updateSetup(
            @Param("id") Long id,
            @Param("operationType") OperationType operationType,
            @Param("inviteValue") String inviteValue,
            @Param("sharedAccountEmail") String sharedAccountEmail,
            @Param("sharedAccountPasswordEncrypted") String sharedAccountPasswordEncrypted,
            @Param("operationGuide") String operationGuide,
            @Param("operationStatus") OperationStatus operationStatus,
            @Param("operationStartedAt") LocalDateTime operationStartedAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    void updateStatus(
            @Param("id") Long id,
            @Param("operationStatus") OperationStatus operationStatus,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    void updateStatusIfNotActive(
            @Param("id") Long id,
            @Param("operationStatus") OperationStatus operationStatus,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    void updateStatusAndCompletedAt(
            @Param("id") Long id,
            @Param("operationStatus") OperationStatus operationStatus,
            @Param("operationCompletedAt") LocalDateTime operationCompletedAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    void markResetRequired(
            @Param("id") Long id,
            @Param("operationStatus") OperationStatus operationStatus,
            @Param("lastResetAt") LocalDateTime lastResetAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}