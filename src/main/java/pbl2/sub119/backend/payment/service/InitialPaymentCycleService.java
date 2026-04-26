package pbl2.sub119.backend.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.payment.dto.PartyPaymentReadinessInfo;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;
import pbl2.sub119.backend.payment.policy.FeePolicy;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InitialPaymentCycleService {

    private final PartyCycleMapper partyCycleMapper;
    private final PartyPaymentReadinessService partyPaymentReadinessService;

    @Transactional
    public PartyCycle createInitialCycle(Long partyId) {
        PartyCycle existingCycle = partyCycleMapper.findLatestPendingOrRunningCycle(
                partyId,
                PartyCycleStatus.PAYMENT_PENDING,
                PartyCycleStatus.RUNNING
        );
        if (existingCycle != null) {
            return existingCycle;
        }

        PartyPaymentReadinessInfo info = partyPaymentReadinessService.getReadinessInfo(partyId);
        LocalDateTime now = LocalDateTime.now();

        PartyCycle partyCycle = PartyCycle.builder()
                .partyId(partyId)
                .cycleNo(partyCycleMapper.findNextCycleNo(partyId))
                .startAt(now)
                .endAt(null)
                .billingDueAt(now)
                .status(PartyCycleStatus.PAYMENT_PENDING)
                .memberCountSnapshot(info.getPendingMemberCount())
                .pricePerMemberSnapshot(Math.toIntExact(info.getPricePerMemberSnapshot() + FeePolicy.MEMBER_FEE))
                .createdAt(now)
                .updatedAt(now)
                .build();

        try {
            partyCycleMapper.save(partyCycle);
            return partyCycle;
        } catch (DuplicateKeyException e) {
            PartyCycle createdByAnotherTx = partyCycleMapper.findLatestPendingOrRunningCycle(
                    partyId,
                    PartyCycleStatus.PAYMENT_PENDING,
                    PartyCycleStatus.RUNNING
            );
            if (createdByAnotherTx != null) {
                return createdByAnotherTx;
            }
            throw e;
        }
    }
}
