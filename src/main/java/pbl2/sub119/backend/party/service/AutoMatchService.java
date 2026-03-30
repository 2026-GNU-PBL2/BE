package pbl2.sub119.backend.party.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.dto.response.JoinOrQueueResponse;
import pbl2.sub119.backend.party.entity.MatchWaitingQueue;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.party.mapper.PartyMapper;

@Service
@RequiredArgsConstructor
public class AutoMatchService {

    private final PartyMapper partyMapper;
    private final PartyJoinService partyJoinService;
    private final MatchWaitingService matchWaitingService;

    @Transactional
    public JoinOrQueueResponse requestJoinOrQueue(String productId, Long userId) {
        if (productId == null || productId.isBlank()) {
            throw new PartyException(ErrorCode.PARTY_INVALID_PRODUCT_ID);
        }

        List<Party> joinableParties = partyMapper.findJoinablePartiesByProductId(productId);

        for (Party targetParty : joinableParties) {
            try {
                partyJoinService.joinParty(targetParty.getId(), userId);
                return new JoinOrQueueResponse(
                        true,
                        false,
                        targetParty.getId(),
                        null,
                        "즉시 참여가 완료되었습니다."
                );
            } catch (PartyException e) {
                if (e.getErrorCode() == ErrorCode.PARTY_FULL) {
                    continue; // 다음 파티 시도
                }
                throw e;
            }
        }

        var waitingResponse = matchWaitingService.registerWaiting(productId, userId);

        return new JoinOrQueueResponse(
                false,
                true,
                null,
                waitingResponse.waitingId(),
                "즉시 참여 가능한 파티가 없어 대기열에 등록되었습니다."
        );
    }

    @Transactional
    public void matchWaitingUserToVacancy(String productId, Long partyId) {
        MatchWaitingQueue waitingQueue = matchWaitingService.findFirstWaitingByProductId(productId);
        if (waitingQueue == null) {
            return;
        }

        partyJoinService.joinParty(partyId, waitingQueue.getUserId());
        matchWaitingService.markMatched(waitingQueue.getId(), partyId, waitingQueue.getUserId());
    }
}