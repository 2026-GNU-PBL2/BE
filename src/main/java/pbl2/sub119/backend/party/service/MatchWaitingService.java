package pbl2.sub119.backend.party.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.dto.response.MatchWaitingRegisterResponse;
import pbl2.sub119.backend.party.dto.response.MatchWaitingResponse;
import pbl2.sub119.backend.party.entity.MatchWaitingQueue;
import pbl2.sub119.backend.party.enumerated.MatchWaitingStatus;
import pbl2.sub119.backend.party.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.party.mapper.MatchWaitingQueueMapper;

@Service
@RequiredArgsConstructor
public class MatchWaitingService {

    private final MatchWaitingQueueMapper matchWaitingQueueMapper;
    private final PartyHistoryService partyHistoryService;

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new PartyException(ErrorCode.PARTY_INVALID_USER_ID);
        }
    }

    @Transactional
    public MatchWaitingRegisterResponse registerWaiting(String productId, Long userId) {
        validateUserId(userId);

        if (productId == null || productId.isBlank()) {
            throw new PartyException(ErrorCode.PARTY_INVALID_PRODUCT_ID);
        }

        MatchWaitingQueue existing = matchWaitingQueueMapper.findWaitingByProductIdAndUserId(productId, userId);
        if (existing != null) {
            throw new PartyException(ErrorCode.PARTY_WAITING_ALREADY_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();

        MatchWaitingQueue queue = MatchWaitingQueue.builder()
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

        return new MatchWaitingRegisterResponse(
                queue.getId(),
                queue.getProductId(),
                queue.getUserId(),
                queue.getStatus()
        );
    }

    @Transactional
    public void cancelWaiting(Long waitingId, Long userId) {
        validateUserId(userId);

        MatchWaitingQueue queue = matchWaitingQueueMapper.findById(waitingId);
        if (queue == null || queue.getStatus() != MatchWaitingStatus.WAITING) {
            throw new PartyException(ErrorCode.PARTY_WAITING_NOT_FOUND);
        }

        if (!queue.getUserId().equals(userId)) {
            throw new PartyException(ErrorCode.PARTY_WAITING_FORBIDDEN);
        }

        int updated = matchWaitingQueueMapper.updateCanceled(waitingId);
        if (updated == 0) {
            throw new PartyException(ErrorCode.PARTY_WAITING_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public MatchWaitingQueue findFirstWaitingByProductId(String productId) {
        return matchWaitingQueueMapper.findFirstWaitingByProductId(productId);
    }

    @Transactional
    public void markMatched(Long waitingId, Long targetPartyId, Long userId) {
        validateUserId(userId);

        int updated = matchWaitingQueueMapper.updateMatched(waitingId, targetPartyId);
        if (updated == 0) {
            throw new PartyException(ErrorCode.PARTY_WAITING_NOT_FOUND);
        }

        partyHistoryService.saveHistory(
                targetPartyId,
                null,
                PartyHistoryEventType.AUTO_MATCHED,
                "{\"waitingId\":" + waitingId + ",\"userId\":" + userId + "}",
                userId
        );
    }

    @Transactional(readOnly = true)
    public List<MatchWaitingResponse> getMyWaitingList(Long userId) {
        validateUserId(userId);

        return matchWaitingQueueMapper.findAllWaitingByUserId(userId)
                .stream()
                .map(queue -> new MatchWaitingResponse(
                        queue.getId(),
                        queue.getProductId(),
                        queue.getUserId(),
                        queue.getStatus(),
                        queue.getRequestedAt(),
                        queue.getTargetPartyId()
                ))
                .toList();
    }
}