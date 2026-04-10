package pbl2.sub119.backend.payment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.payment.entity.PartyCycle;

import java.time.LocalDateTime;

@Mapper
public interface PartyCycleMapper {

    boolean existsPendingOrRunningCycle(
            @Param("partyId") Long partyId,
            @Param("pendingStatus") PartyCycleStatus pendingStatus,
            @Param("runningStatus") PartyCycleStatus runningStatus
    );

    PartyCycle findLatestPendingOrRunningCycle(
            @Param("partyId") Long partyId,
            @Param("pendingStatus") PartyCycleStatus pendingStatus,
            @Param("runningStatus") PartyCycleStatus runningStatus
    );

    PartyCycle findById(@Param("partyCycleId") Long partyCycleId);

    PartyCycle findByPartyIdAndCycleNo(
            @Param("partyId") Long partyId,
            @Param("cycleNo") Integer cycleNo
    );

    int findNextCycleNo(@Param("partyId") Long partyId);

    int compareAndUpdateStatus(
            @Param("partyCycleId") Long partyCycleId,
            @Param("expectedStatus") PartyCycleStatus expectedStatus,
            @Param("newStatus") PartyCycleStatus newStatus
    );

    int closeCycle(
            @Param("partyCycleId") Long partyCycleId,
            @Param("expectedStatus") PartyCycleStatus expectedStatus,
            @Param("newStatus") PartyCycleStatus newStatus,
            @Param("endAt") LocalDateTime endAt
    );

    int save(PartyCycle partyCycle);

    int updateMemberCountSnapshot(
            @Param("partyCycleId") Long partyCycleId,
            @Param("memberCountSnapshot") int memberCountSnapshot
    );
}