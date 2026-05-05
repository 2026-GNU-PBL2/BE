package pbl2.sub119.backend.payment.listener;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.notification.event.event.PartyMatchedEvent;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.event.PartyCyclePaymentCompletedEvent;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;
import pbl2.sub119.backend.settlement.event.SettlementRequestedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartyCycleStateEventListener {

    private final PartyCycleMapper partyCycleMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPaymentCompleted(PartyCyclePaymentCompletedEvent event) {
        log.info("결제 완료 → 사이클 상태 전환. partyCycleId={}, cycleNo={}",
                event.partyCycleId(), event.cycleNo());

        final int updated = partyCycleMapper.compareAndUpdateStatus(
                event.partyCycleId(), PartyCycleStatus.PROCESSING, PartyCycleStatus.RUNNING);

        if (updated == 0) {
            log.warn("사이클 상태 전환 실패 — 정산 발행 중단. partyCycleId={}", event.partyCycleId());
            return;
        }

        if (event.cycleNo() == 1) {
            final List<Long> memberUserIds = partyMemberMapper.findMembersByPartyId(event.partyId())
                    .stream()
                    .filter(m -> m.getRole() == PartyRole.MEMBER)
                    .map(m -> m.getUserId())
                    .toList();
            eventPublisher.publishEvent(new PartyMatchedEvent(event.partyId(), event.partyCycleId(), memberUserIds));
        } else {
            closePreviousCycle(event.partyId(), event.cycleNo());
        }

        eventPublisher.publishEvent(new SettlementRequestedEvent(event.partyId(), event.partyCycleId()));
    }

    private void closePreviousCycle(final Long partyId, final int currentCycleNo) {
        final PartyCycle previousCycle =
                partyCycleMapper.findByPartyIdAndCycleNo(partyId, currentCycleNo - 1);
        if (previousCycle != null) {
            partyCycleMapper.closeCycle(
                    previousCycle.getId(),
                    PartyCycleStatus.RUNNING,
                    PartyCycleStatus.CLOSED,
                    LocalDateTime.now()
            );
        }
    }
}
