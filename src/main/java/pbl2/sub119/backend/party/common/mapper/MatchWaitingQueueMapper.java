package pbl2.sub119.backend.party.common.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.party.common.entity.MatchWaitingQueue;

@Mapper
public interface MatchWaitingQueueMapper {

    // [자동 매칭 신청] 대기열 저장
    int insertMatchWaitingQueue(MatchWaitingQueue queue);

    // [파티 해체 재대기] 동일 상품 WAITING 없을 때만 원자적 삽입
    int insertIfAbsent(MatchWaitingQueue queue);

    // [중복 신청 방지] 같은 상품에 이미 대기중인지 조회
    MatchWaitingQueue findWaitingByProductIdAndUserId(
            @Param("productId") String productId,
            @Param("userId") Long userId
    );

    // [결원 자동 매칭] 가장 먼저 기다린 사용자 1건 조회
    MatchWaitingQueue findFirstWaitingByProductId(@Param("productId") String productId);

    // [관리/확인용] 상품 기준 전체 대기 목록 조회
    List<MatchWaitingQueue> findAllWaitingByProductId(@Param("productId") String productId);

    // [내 신청 상태] 사용자 기준 전체 신청 목록 조회
    List<MatchWaitingQueue> findAllWaitingByUserId(@Param("userId") Long userId);

    // [신청 취소/매칭 처리] 대기 신청 단건 조회
    MatchWaitingQueue findById(@Param("id") Long id);

    // [자동 매칭 완료] 대기 상태 → 매칭 완료 변경
    int updateMatched(
            @Param("id") Long id,
            @Param("targetPartyId") Long targetPartyId
    );

    // [신청 취소] 대기 상태 → 취소 변경
    int updateCanceled(@Param("id") Long id);

    // [대기 인원 확인] 상품 기준 현재 대기 수 조회
    int countWaitingByProductId(@Param("productId") String productId);
}