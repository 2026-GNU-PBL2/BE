package pbl2.sub119.backend.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionStatus;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMapper;
import pbl2.sub119.backend.payment.dto.RecurringPaymentTarget;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.event.PaymentExecutionRequestedEvent;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;
import pbl2.sub119.backend.payment.mapper.PaymentExecutionQueryMapper;
import pbl2.sub119.backend.payment.mapper.RecurringPaymentQueryMapper;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringPaymentService {

    private final RecurringPaymentQueryMapper recurringPaymentQueryMapper;
    private final PartyCycleMapper partyCycleMapper;
    private final PaymentExecutionQueryMapper paymentExecutionQueryMapper;
    private final PartyProvisionMapper partyProvisionMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processDueCycles() {
        LocalDateTime now = LocalDateTime.now();

        List<RecurringPaymentTarget> runningCycles = recurringPaymentQueryMapper.findRunningCycles(
                PartyCycleStatus.RUNNING,
                OperationStatus.ACTIVE
        );

        for (RecurringPaymentTarget target : runningCycles) {
            LocalDateTime nextBillingDueAt = target.getBillingDueAt().plusMonths(1);

            if (nextBillingDueAt.isAfter(now)) {
                continue;
            }

            createNextCycleAndPublish(target, nextBillingDueAt);
        }
    }

    // provision 완료 후 결제일이 지난 사이클의 지연 결제 트리거 (특정 파티 대상)
    @Transactional
    public void triggerDelayedPaymentIfDue(final Long partyId) {
        final LocalDateTime now = LocalDateTime.now();

        recurringPaymentQueryMapper.findRunningCycles(PartyCycleStatus.RUNNING, OperationStatus.ACTIVE)
                .stream()
                .filter(t -> t.getPartyId().equals(partyId))
                .filter(t -> !t.getBillingDueAt().plusMonths(1).isAfter(now))
                .findFirst()
                .ifPresent(target -> {
                    log.info("provision 완료 후 지연 결제 트리거. partyId={}", partyId);
                    createNextCycleAndPublish(target, target.getBillingDueAt().plusMonths(1));
                });
    }

    private void createNextCycleAndPublish(
            RecurringPaymentTarget target,
            LocalDateTime nextBillingDueAt
    ) {
        // 현재 파티장의 provision이 RESET_REQUIRED이면 사이클 생성 보류
        final var provision = partyProvisionMapper.findByPartyId(target.getPartyId());
        if (provision != null && provision.getOperationStatus() == ProvisionStatus.RESET_REQUIRED) {
            log.info("파티장 provision RESET_REQUIRED — 사이클 생성 보류. partyId={}", target.getPartyId());
            return;
        }

        int nextCycleNo = target.getCurrentCycleNo() + 1;

        PartyCycle existing = partyCycleMapper.findByPartyIdAndCycleNo(
                target.getPartyId(),
                nextCycleNo
        );
        if (existing != null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        // 다음 회차 snapshot은 반복회차 실제 과금 대상과 동일하게 ACTIVE MEMBER 수로 저장한다.
        int billableMemberCount = paymentExecutionQueryMapper.countActiveMembersByPartyId(target.getPartyId());

        PartyCycle nextCycle = PartyCycle.builder()
                .partyId(target.getPartyId())
                .cycleNo(nextCycleNo)
                .startAt(nextBillingDueAt)
                .endAt(null)
                .billingDueAt(nextBillingDueAt)
                .status(PartyCycleStatus.PAYMENT_PENDING)
                .memberCountSnapshot(billableMemberCount)
                // pricePerMemberSnapshot은 첫 회차에 수수료(FeePolicy.MEMBER_FEE)가 포함된 값으로 저장되며 이후 회차는 그대로 이어받는다.
                .pricePerMemberSnapshot(target.getPricePerMemberSnapshot())
                .createdAt(now)
                .updatedAt(now)
                .build();

        try {
            partyCycleMapper.save(nextCycle);
        } catch (DuplicateKeyException e) {
            log.info("반복 결제 cycle 중복 생성 감지. partyId={}, cycleNo={}",
                    target.getPartyId(), nextCycleNo);
            return;
        }

        log.info("다음 회차 cycle 생성 완료. partyId={}, partyCycleId={}, cycleNo={}",
                target.getPartyId(), nextCycle.getId(), nextCycleNo);

        Long partyId = target.getPartyId();
        Long partyCycleId = nextCycle.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishEvent(new PaymentExecutionRequestedEvent(partyId, partyCycleId));
            }
        });
    }
}