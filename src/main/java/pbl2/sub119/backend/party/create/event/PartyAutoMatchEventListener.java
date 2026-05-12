package pbl2.sub119.backend.party.create.event;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.common.entity.MatchWaitingQueue;
import pbl2.sub119.backend.party.common.exception.PartyException;
import pbl2.sub119.backend.party.common.mapper.MatchWaitingQueueMapper;
import pbl2.sub119.backend.party.join.service.PartyJoinService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartyAutoMatchEventListener {

    private final PartyJoinService partyJoinService;
    private final MatchWaitingQueueMapper matchWaitingQueueMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePartyCreated(final PartyCreatedEvent event) {
        final List<MatchWaitingQueue> waitingMembers =
                matchWaitingQueueMapper.findAllWaitingByProductId(event.productId());

        for (final MatchWaitingQueue waiting : waitingMembers) {
            try {
                partyJoinService.joinParty(event.partyId(), waiting.getUserId());
                matchWaitingQueueMapper.updateMatched(waiting.getId(), event.partyId());
            } catch (PartyException e) {
                if (e.getErrorCode() == ErrorCode.PARTY_FULL) {
                    break;
                }
                log.warn("자동 매칭 실패. partyId={}, userId={}", event.partyId(), waiting.getUserId(), e);
            }
        }
    }
}
