package pbl2.sub119.backend.payment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.payment.entity.PartyCycleMemberPayment;
import pbl2.sub119.backend.payment.enumerated.MemberPaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PartyCycleMemberPaymentMapper {

    int insertIfAbsent(PartyCycleMemberPayment payment);

    int compareAndUpdateStatus(
            @Param("partyCycleId") Long partyCycleId,
            @Param("partyMemberId") Long partyMemberId,
            @Param("expectedStatus") MemberPaymentStatus expectedStatus,
            @Param("newStatus") MemberPaymentStatus newStatus
    );

    int markPaid(
            @Param("partyCycleId") Long partyCycleId,
            @Param("partyMemberId") Long partyMemberId,
            @Param("externalTxId") String externalTxId,
            @Param("paidAt") LocalDateTime paidAt
    );

    int markFailed(
            @Param("partyCycleId") Long partyCycleId,
            @Param("partyMemberId") Long partyMemberId,
            @Param("failureReason") String failureReason,
            @Param("failureCode") String failureCode,
            @Param("failedAt") LocalDateTime failedAt
    );

    // deadline 전용: PAYMENT_PENDING / PROCESSING 모두 실패 처리
    int bulkMarkFailedForDeadline(
            @Param("partyCycleId") Long partyCycleId,
            @Param("failureReason") String failureReason,
            @Param("failureCode") String failureCode,
            @Param("failedAt") LocalDateTime failedAt
    );

    // CANCELLED 는 정상 취소로 간주 — non-paid 집계에서 제외
    int countNonPaid(@Param("partyCycleId") Long partyCycleId);

    // 실패 이벤트의 미처리 인원 집계: PAYMENT_PENDING + PROCESSING 합산
    int countPendingOrProcessing(@Param("partyCycleId") Long partyCycleId);

    int countByStatus(
            @Param("partyCycleId") Long partyCycleId,
            @Param("status") MemberPaymentStatus status
    );

    // 재시도 전용: FAILED 레코드만 PAYMENT_PENDING으로 초기화
    int resetFailedForRetry(@Param("partyCycleId") Long partyCycleId);

    List<Long> findDeadlineExceededCycleIds(@Param("deadline") LocalDateTime deadline);

    List<PartyCycleMemberPayment> findByCycleId(@Param("partyCycleId") Long partyCycleId);
}