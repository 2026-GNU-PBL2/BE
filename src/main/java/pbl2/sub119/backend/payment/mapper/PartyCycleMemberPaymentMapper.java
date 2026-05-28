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

    /**
     * 성능 개선: 기존 N회 개별 insertIfAbsent → 1회 배치 INSERT IGNORE.
     * 멱등성: INSERT IGNORE로 중복 레코드 생성 방지 (기존 insertIfAbsent와 동일 의미).
     *
     * @param payments 초기화할 멤버 결제 레코드 목록 (비어 있으면 호출하지 않을 것)
     */
    int batchInsertIfAbsent(@Param("payments") List<PartyCycleMemberPayment> payments);

    /**
     * fail-fast PG 실패 시 PG 미호출 멤버 복구.
     *
     * <p>Phase1에서 선점(PAYMENT_PENDING→PROCESSING)했으나 fail-fast로 PG를 호출하지 못한
     * 멤버를 PAYMENT_PENDING으로 되돌린다.
     * 기존 순차 루프 코드에서는 미처리 멤버가 자동으로 PAYMENT_PENDING에 남았으므로,
     * 이 메서드는 그 동작을 명시적으로 복원한다.
     *
     * <p>WHERE status='PROCESSING' 조건으로 이미 PAID/FAILED 상태인 멤버에게는 영향 없음.
     *
     * @param partyCycleId 대상 사이클 ID
     * @param memberIds    복구할 partyMemberId 목록
     */
    int resetProcessingToPaymentPending(
            @Param("partyCycleId") Long partyCycleId,
            @Param("memberIds") List<Long> memberIds);

    List<PartyCycleMemberPayment> findByCycleId(@Param("partyCycleId") Long partyCycleId);
}