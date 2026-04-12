package pbl2.sub119.backend.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.cycle.service.PartyCycleService;
import pbl2.sub119.backend.payment.dto.RecurringPaymentTarget;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.event.PaymentExecutionRequestedEvent;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;
import pbl2.sub119.backend.payment.mapper.RecurringPaymentQueryMapper;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringPaymentService {

    private final RecurringPaymentQueryMapper recurringPaymentQueryMapper;
    private final PartyCycleMapper partyCycleMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final PartyCycleService partyCycleService;

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

    private void createNextCycleAndPublish(
            RecurringPaymentTarget target,
            LocalDateTime nextBillingDueAt
    ) {
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
        int billableMemberCount = partyCycleService.countRecurringBillableMembers(target.getPartyId());

        PartyCycle nextCycle = PartyCycle.builder()
                .partyId(target.getPartyId())
                .cycleNo(nextCycleNo)
                .startAt(nextBillingDueAt)
                .endAt(null)
                .billingDueAt(nextBillingDueAt)
                .status(PartyCycleStatus.PAYMENT_PENDING)
                .memberCountSnapshot(billableMemberCount)
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

        eventPublisher.publishEvent(
                new PaymentExecutionRequestedEvent(target.getPartyId(), nextCycle.getId())
        );
    }
}