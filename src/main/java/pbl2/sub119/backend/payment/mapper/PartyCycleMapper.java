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

    PartyCycle findById(@Param("partyCycleId") Long partyCycleId);

    int findNextCycleNo(@Param("partyId") Long partyId);

    PartyCycle findByPartyIdAndCycleNo(
            @Param("partyId") Long partyId,
            @Param("cycleNo") int cycleNo
    );

    int compareAndUpdateStatus(
            @Param("partyCycleId") Long partyCycleId,
            @Param("expectedStatus") PartyCycleStatus expectedStatus,
            @Param("newStatus") PartyCycleStatus newStatus
    );

    int closeCycle(
            @Param("partyCycleId") Long partyCycleId,
            @Param("expectedStatus") PartyCycleStatus expectedStatus,
            @Param("newStatus") PartyCycleStatus newStatus,
            @Param("endAt") java.time.LocalDateTime endAt
    );

    int updateMemberCountSnapshot(
            @Param("partyCycleId") Long partyCycleId,
            @Param("memberCountSnapshot") int memberCountSnapshot,
            @Param("expectedStatus") PartyCycleStatus expectedStatus
    );

    int save(PartyCycle partyCycle);

    // deadline 전용: PAYMENT_PENDING / PROCESSING 상태 cycle을 FAILED로 전이
    int failIfPendingOrProcessing(@Param("partyCycleId") Long partyCycleId);

    // 초기 결제(cycle_no=1) FAILED 여부만 확인 — 반복회차 실패와 구분
    boolean existsFailedInitialCycle(@Param("partyId") Long partyId);
}