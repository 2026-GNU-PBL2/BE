package pbl2.sub119.backend.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.payment.dto.PartyPaymentReadinessInfo;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InitialPaymentCycleService {

    private final PartyCycleMapper partyCycleMapper;
    private final PartyPaymentReadinessService partyPaymentReadinessService;

    @Transactional
    public PartyCycle createInitialCycle(Long partyId) {
        if (partyCycleMapper.existsPendingOrRunningCycle(
                partyId,
                PartyCycleStatus.PAYMENT_PENDING,
                PartyCycleStatus.RUNNING
        )) {
            throw new IllegalStateException("이미 진행 중인 결제 사이클이 존재합니다. partyId=" + partyId);
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
                .memberCountSnapshot(info.getCurrentMemberCount())
                .pricePerMemberSnapshot(info.getPricePerMemberSnapshot())
                .createdAt(now)
                .updatedAt(now)
                .build();

        partyCycleMapper.save(partyCycle);
        return partyCycle;
    }
}