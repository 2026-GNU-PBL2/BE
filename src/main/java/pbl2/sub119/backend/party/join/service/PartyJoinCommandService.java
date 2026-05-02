package pbl2.sub119.backend.party.join.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.common.entity.MatchWaitingQueue;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.enumerated.MatchWaitingStatus;
import pbl2.sub119.backend.party.common.exception.PartyException;
import pbl2.sub119.backend.party.join.dto.response.PartyJoinApplyResponse;
import pbl2.sub119.backend.party.join.dto.response.PartyJoinCancelResponse;
import pbl2.sub119.backend.party.common.mapper.MatchWaitingQueueMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;

@Service
@RequiredArgsConstructor
public class PartyJoinCommandService {

    private final PartyMapper partyMapper;
    private final MatchWaitingQueueMapper matchWaitingQueueMapper;
    private final PartyJoinService partyJoinService;

    // 자동 매칭 신청
    @Transactional
    public PartyJoinApplyResponse applyJoin(final String productId, final Long userId) {
        validateApplyRequest(productId, userId);

        final List<Party> joinableParties = partyMapper.findJoinablePartiesByProductId(productId);

        for (Party targetParty : joinableParties) {
            try {
                partyJoinService.joinParty(targetParty.getId(), userId);

                final LocalDateTime now = LocalDateTime.now();
                final MatchWaitingQueue immediateQueue = MatchWaitingQueue.builder()
                        .productId(productId)
                        .userId(userId)
                        .status(MatchWaitingStatus.MATCHED)
                        .requestedAt(now)
                        .matchedAt(now)
                        .canceledAt(null)
                        .targetPartyId(targetParty.getId())
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                matchWaitingQueueMapper.insertMatchWaitingQueue(immediateQueue);

                return new PartyJoinApplyResponse(
                        true,
                        false,
                        targetParty.getId(),
                        immediateQueue.getId(),
                        "즉시 참여가 완료되었습니다."
                );
            } catch (PartyException e) {
                if (e.getErrorCode() == ErrorCode.PARTY_FULL) {
                    continue;
                }
                throw e;
            }
        }

        final MatchWaitingQueue existingWaiting =
                matchWaitingQueueMapper.findWaitingByProductIdAndUserId(productId, userId);

        if (existingWaiting != null) {
            throw new PartyException(ErrorCode.PARTY_WAITING_ALREADY_EXISTS);
        }

        final LocalDateTime now = LocalDateTime.now();

        final MatchWaitingQueue queue = MatchWaitingQueue.builder()
                .productId(productId)
                .userId(userId)
                .status(MatchWaitingStatus.WAITING)
                .requestedAt(now)
                .matchedAt(null)
                .canceledAt(null)
                .targetPartyId(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        matchWaitingQueueMapper.insertMatchWaitingQueue(queue);

        return new PartyJoinApplyResponse(
                false,
                true,
                null,
                queue.getId(),
                "즉시 참여 가능한 파티가 없어 자동 매칭 대기 상태로 등록되었습니다."
        );
    }

    // 자동 매칭 신청 취소
    @Transactional
    public PartyJoinCancelResponse cancelJoin(final Long joinRequestId, final Long userId) {
        if (userId == null) {
            throw new PartyException(ErrorCode.PARTY_INVALID_USER_ID);
        }

        final MatchWaitingQueue queue = matchWaitingQueueMapper.findById(joinRequestId);
        if (queue == null || queue.getStatus() != MatchWaitingStatus.WAITING) {
            throw new PartyException(ErrorCode.PARTY_WAITING_NOT_FOUND);
        }

        if (!queue.getUserId().equals(userId)) {
            throw new PartyException(ErrorCode.PARTY_WAITING_FORBIDDEN);
        }

        final int updated = matchWaitingQueueMapper.updateCanceled(joinRequestId);
        if (updated == 0) {
            throw new PartyException(ErrorCode.PARTY_WAITING_NOT_FOUND);
        }

        return new PartyJoinCancelResponse(
                joinRequestId,
                LocalDateTime.now(),
                "자동 매칭 신청이 취소되었습니다."
        );
    }

    // 신청 입력값 검증
    private void validateApplyRequest(final String productId, final Long userId) {
        if (userId == null) {
            throw new PartyException(ErrorCode.PARTY_INVALID_USER_ID);
        }

        if (productId == null || productId.isBlank()) {
            throw new PartyException(ErrorCode.PARTY_INVALID_PRODUCT_ID);
        }
    }
}