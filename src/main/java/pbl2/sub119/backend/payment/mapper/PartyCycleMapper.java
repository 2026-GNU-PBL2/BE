package pbl2.sub119.backend.payment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.payment.entity.PartyCycle;

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

    int findNextCycleNo(@Param("partyId") Long partyId);

    int save(PartyCycle partyCycle);
}