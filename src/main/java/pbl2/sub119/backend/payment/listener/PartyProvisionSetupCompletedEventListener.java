package pbl2.sub119.backend.payment.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.party.event.PartyProvisionSetupCompletedEvent;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.event.PaymentExecutionRequestedEvent;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;
import pbl2.sub119.backend.payment.service.InitialPaymentCycleService;
import pbl2.sub119.backend.payment.service.PartyPaymentReadinessService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartyProvisionSetupCompletedEventListener {

    private final PartyPaymentReadinessService partyPaymentReadinessService;
    private final InitialPaymentCycleService initialPaymentCycleService;
    private final PartyCycleMapper partyCycleMapper;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(PartyProvisionSetupCompletedEvent event) {
        final Long partyId = event.partyId();

        // cycle_no=1이 어떤 상태로든 존재하면 자동 초기결제 트리거 차단
        // FAILED여도 자동 진행 금지 — InitialPaymentCycleService.createInitialCycle()이
        // findNextCycleNo() 기반이므로 FAILED 파티에서 호출 시 cycle_no=2가 잘못 생성됨
        PartyCycle existingInitialCycle = partyCycleMapper.findByPartyIdAndCycleNo(partyId, 1);
        if (existingInitialCycle != null) {
            if (existingInitialCycle.getStatus() == PartyCycleStatus.FAILED) {
                log.warn("초기 결제 자동 재트리거 차단. partyId={}, reason=FAILED_INITIAL_CYCLE_EXISTS", partyId);
            } else {
                log.info("초기결제 사이클 이미 존재. partyId={}, status={}", partyId, existingInitialCycle.getStatus());
            }
            return;
        }

        if (!partyPaymentReadinessService.isReady(partyId)) {
            log.info("파티 결제 준비 미완료. partyId={}", partyId);
            return;
        }

        final PartyCycle partyCycle = initialPaymentCycleService.createInitialCycle(partyId);
        log.info("파티 결제일 확정 완료. partyId={}, partyCycleId={}", partyId, partyCycle.getId());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishEvent(new PaymentExecutionRequestedEvent(partyId, partyCycle.getId()));
            }
        });
    }
}
