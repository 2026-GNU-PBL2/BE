package pbl2.sub119.backend.party.join.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.entity.MatchWaitingQueue;
import pbl2.sub119.backend.party.enumerated.MatchWaitingStatus;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.party.join.dto.response.PartyJoinMeResponse;
import pbl2.sub119.backend.party.mapper.MatchWaitingQueueMapper;
import pbl2.sub119.backend.subproduct.dto.SubProductResponse;
import pbl2.sub119.backend.subproduct.service.SubProductService;

@Service
@RequiredArgsConstructor
public class PartyJoinQueryService {

    private static final long PLATFORM_FEE = 990L;
    private static final String DEFAULT_COMMITMENT_PERIOD_TEXT =
            "서비스 정책에 따라 이용 기간이 달라질 수 있습니다.";

    private final MatchWaitingQueueMapper matchWaitingQueueMapper;
    private final SubProductService subProductService;

    // 내 자동 매칭 신청 상태 조회
    @Transactional(readOnly = true)
    public List<PartyJoinMeResponse> getMyJoinRequests(final Long userId) {
        if (userId == null) {
            throw new PartyException(ErrorCode.PARTY_INVALID_USER_ID);
        }

        return matchWaitingQueueMapper.findAllWaitingByUserId(userId)
                .stream()
                .map(this::toMyJoinResponse)
                .toList();
    }

    // 내 신청 상태 응답 변환
    private PartyJoinMeResponse toMyJoinResponse(final MatchWaitingQueue queue) {
        final SubProductResponse product = subProductService.getProduct(queue.getProductId());
        final long expectedPaymentAmount = product.getPricePerMember() + PLATFORM_FEE;

        return new PartyJoinMeResponse(
                queue.getId(),
                queue.getProductId(),
                product.getServiceName(),
                product.getThumbnailUrl(),
                queue.getStatus().name(),
                queue.getRequestedAt(),
                queue.getMatchedAt(),
                DEFAULT_COMMITMENT_PERIOD_TEXT,
                expectedPaymentAmount,
                toStatusLabel(queue.getStatus()),
                toStatusMessage(queue.getStatus())
        );
    }

    // 상태 라벨 변환
    private String toStatusLabel(final MatchWaitingStatus status) {
        return switch (status) {
            case WAITING -> "자동 매칭 대기중";
            case MATCHED -> "매칭 완료";
            case CANCELED -> "신청 취소";
        };
    }

    // 상태 문구 변환
    private String toStatusMessage(final MatchWaitingStatus status) {
        return switch (status) {
            case WAITING -> "참여 가능한 파티를 찾는 중입니다.";
            case MATCHED -> "파티 참여가 완료되었습니다.";
            case CANCELED -> "자동 매칭 신청이 취소되었습니다.";
        };
    }
}