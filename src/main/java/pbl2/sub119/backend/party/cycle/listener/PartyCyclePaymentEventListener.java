package pbl2.sub119.backend.party.cycle.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.common.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;
import pbl2.sub119.backend.party.common.service.PartyHistoryService;
import pbl2.sub119.backend.party.cycle.service.PartyCycleService;
import pbl2.sub119.backend.payment.event.PartyCyclePaymentCompletedEvent;
import pbl2.sub119.backend.payment.event.PartyCyclePaymentFailedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartyCyclePaymentEventListener {

    private final PartyCycleService partyCycleService;
    private final PartyHistoryService partyHistoryService;
    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPaymentCompleted(PartyCyclePaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 수신. partyId={}, partyCycleId={}, cycleNo={}",
                event.partyId(), event.partyCycleId(), event.cycleNo());

        if (event.cycleNo() == 1) {
            handleFirstCycleCompletion(event.partyId());
        } else {
            handleRecurringCycleCompletion(event.partyId());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPaymentFailed(PartyCyclePaymentFailedEvent event) {
        log.info("결제 실패 이벤트 수신. partyId={}, partyCycleId={}, reason={}",
                event.partyId(), event.partyCycleId(), event.reason());

        try {
            final String detail = objectMapper.writeValueAsString(Map.of(
                    "reason", event.reason(),
                    "failedCount", event.failedMemberCount(),
                    "pendingCount", event.pendingMemberCount()
            ));
            partyHistoryService.saveHistory(
                    event.partyId(),
                    null,
                    PartyHistoryEventType.PAYMENT_EXECUTION_FAILED,
                    detail,
                    null
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("결제 실패 이력 직렬화 오류. partyId=" + event.partyId(), e);
        }
    }

    private void handleFirstCycleCompletion(final Long partyId) {
        final List<PartyMember> pendingMembers = partyMemberMapper.findPendingMembers(partyId);
        for (PartyMember member : pendingMembers) {
            partyMemberMapper.updateStatusAndActivatedAt(member.getId(), PartyMemberStatus.ACTIVE);
            partyHistoryService.saveHistory(
                    partyId,
                    member.getId(),
                    PartyHistoryEventType.PAYMENT_EXECUTION_SUCCEEDED,
                    "{\"userId\":" + member.getUserId() + "}",
                    member.getUserId()
            );
        }

        partyMapper.updateOperationStatus(partyId, OperationStatus.ACTIVE);
        partyCycleService.handleCycleStart(partyId);
    }

    private void handleRecurringCycleCompletion(final Long partyId) {
        partyMemberMapper.findMembersByPartyId(partyId).stream()
                .filter(m -> m.getRole() == PartyRole.MEMBER)
                .filter(m -> m.getStatus() == PartyMemberStatus.ACTIVE
                        || m.getStatus() == PartyMemberStatus.LEAVE_RESERVED)
                .forEach(member -> partyHistoryService.saveHistory(
                        partyId,
                        member.getId(),
                        PartyHistoryEventType.PAYMENT_EXECUTION_SUCCEEDED,
                        "{\"userId\":" + member.getUserId() + "}",
                        member.getUserId()
                ));

        partyCycleService.handleCycleStart(partyId);
    }
}
