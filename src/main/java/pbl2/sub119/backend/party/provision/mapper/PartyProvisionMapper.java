package pbl2.sub119.backend.party.provision.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.party.provision.entity.PartyProvision;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionStatus;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionType;

@Mapper
public interface PartyProvisionMapper {

    PartyProvision findByPartyId(@Param("partyId") Long partyId);

    PartyProvision findByPartyIdForUpdate(@Param("partyId") Long partyId);

    void insert(PartyProvision partyOperation);

    void updateSetup(
            @Param("id") Long id,
            @Param("operationType") ProvisionType operationType,
            @Param("inviteValue") String inviteValue,
            @Param("sharedAccountEmail") String sharedAccountEmail,
            @Param("sharedAccountPasswordEncrypted") String sharedAccountPasswordEncrypted,
            @Param("operationGuide") String operationGuide,
            @Param("operationStatus") ProvisionStatus operationStatus,
            @Param("operationStartedAt") LocalDateTime operationStartedAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    void updateStatus(
            @Param("id") Long id,
            @Param("operationStatus") ProvisionStatus operationStatus,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    void updateStatusIfNotActive(
            @Param("id") Long id,
            @Param("operationStatus") ProvisionStatus operationStatus,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    void updateStatusAndCompletedAt(
            @Param("id") Long id,
            @Param("operationStatus") ProvisionStatus operationStatus,
            @Param("operationCompletedAt") LocalDateTime operationCompletedAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    void markResetRequired(
            @Param("id") Long id,
            @Param("operationStatus") ProvisionStatus operationStatus,
            @Param("lastResetAt") LocalDateTime lastResetAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    void updateCycleStartResetState(
            @Param("id") Long id,
            @Param("operationStatus") ProvisionStatus operationStatus,
            @Param("operationStartedAt") LocalDateTime operationStartedAt,
            @Param("lastResetAt") LocalDateTime lastResetAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    // 48시간 초과 → 파티 해체 대상
    List<PartyProvision> findTimedOutProvisions();

    // 12시간 / 22시간 → 파티장 리마인드
    List<PartyProvision> findHostProvisionReminderDue(@Param("elapsedHours") int elapsedHours);

    // 24시간 → 파티원에게 지연 안내
    List<PartyProvision> findHostProvisionDelayedNoticeDue(@Param("elapsedHours") int elapsedHours);
}