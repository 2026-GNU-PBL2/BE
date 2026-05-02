package pbl2.sub119.backend.party.cycle.listener;

import java.util.List;
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

        // operationStatus 복구 불필요:
        // AutoPaymentService는 결제 중 operationStatus를 변경하지 않으므로
        // 첫 회차 실패 시 파티는 이미 WAITING_START 상태 유지.
        partyHistoryService.saveHistory(
                event.partyId(),
                null,
                PartyHistoryEventType.PAYMENT_EXECUTION_FAILED,
                "{\"reason\":\"" + event.reason() + "\""
                        + ",\"failedCount\":" + event.failedMemberCount()
                        + ",\"pendingCount\":" + event.pendingMemberCount() + "}",
                null
        );
    }

    private void handleFirstCycleCompletion(Long partyId) {
        // PENDING 멤버 ACTIVE 처리 + 멤버별 성공 히스토리 기록
        List<PartyMember> pendingMembers = partyMemberMapper.findPendingMembers(partyId);
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

        // operationStatus ACTIVE 반영
        partyMapper.updateOperationStatus(partyId, OperationStatus.ACTIVE);

        // provision 시작 + current_member_count / recruit_status / vacancy_type 갱신
        partyCycleService.handleCycleStart(partyId);
    }

    private void handleRecurringCycleCompletion(Long partyId) {
        // 상태 전환 전 결제 대상(ACTIVE + LEAVE_RESERVED MEMBER)에 대해 먼저 히스토리 기록
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

        // LEAVE_RESERVED → LEFT, SWITCH_WAITING → ACTIVE
        // + current_member_count / recruit_status / vacancy_type 갱신
        // + provision 멤버 구성 변경 반영
        partyCycleService.confirmRecurringCycleStart(partyId);
    }
}
