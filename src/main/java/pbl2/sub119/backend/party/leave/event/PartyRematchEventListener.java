package pbl2.sub119.backend.party.leave.event;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.notification.event.event.MemberAutoRematchStartedEvent;
import pbl2.sub119.backend.party.common.entity.MatchWaitingQueue;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.enumerated.MatchWaitingStatus;
import pbl2.sub119.backend.party.common.exception.PartyException;
import pbl2.sub119.backend.party.common.mapper.MatchWaitingQueueMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.join.service.PartyJoinService;

@Component
@RequiredArgsConstructor
public class PartyRematchEventListener {

    private final PartyMapper partyMapper;
    private final PartyJoinService partyJoinService;
    private final MatchWaitingQueueMapper matchWaitingQueueMapper;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleRematchRequested(final PartyRematchRequestedEvent event) {
        final Long rematchedPartyId = tryImmediateRematch(event.productId(), event.userId());
        if (rematchedPartyId == null) {
            requeueMember(event.productId(), event.userId());
        }
        eventPublisher.publishEvent(
                new MemberAutoRematchStartedEvent(event.fromPartyId(), List.of(event.userId()))
        );
    }

    private Long tryImmediateRematch(final String productId, final Long userId) {
        final List<Party> joinableParties = partyMapper.findJoinablePartiesByProductId(productId);
        for (final Party target : joinableParties) {
            try {
                partyJoinService.joinParty(target.getId(), userId);
                return target.getId();
            } catch (PartyException e) {
                if (e.getErrorCode() == ErrorCode.PARTY_FULL) {
                    continue;
                }
                throw e;
            }
        }
        return null;
    }

    private void requeueMember(final String productId, final Long userId) {
        final LocalDateTime now = LocalDateTime.now();
        matchWaitingQueueMapper.insertIfAbsent(MatchWaitingQueue.builder()
                .productId(productId)
                .userId(userId)
                .status(MatchWaitingStatus.WAITING)
                .requestedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }
}
