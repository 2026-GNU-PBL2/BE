package pbl2.sub119.backend.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.BillingKeyStatus;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.common.enumerated.PartyMemberRole;
import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.payment.dto.PartyPaymentReadinessInfo;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;
import pbl2.sub119.backend.payment.mapper.PartyPaymentQueryMapper;

@Service
@RequiredArgsConstructor
public class PartyPaymentReadinessService {

    private final PartyPaymentQueryMapper partyPaymentQueryMapper;
    private final PartyCycleMapper partyCycleMapper;

    @Transactional(readOnly = true)
    public boolean isReady(Long partyId) {
        if (partyCycleMapper.existsPendingOrRunningCycle(
                partyId,
                PartyCycleStatus.PAYMENT_PENDING,
                PartyCycleStatus.RUNNING
        )) {
            return false;
        }

        return partyPaymentQueryMapper.findPaymentReadinessInfo(
                        partyId,
                        PartyMemberRole.MEMBER,
                        PartyMemberStatus.PENDING,
                        BillingKeyStatus.ACTIVE
                )
                .map(PartyPaymentReadinessInfo::isReadyForInitialPayment)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public PartyPaymentReadinessInfo getReadinessInfo(Long partyId) {
        return partyPaymentQueryMapper.findPaymentReadinessInfo(
                        partyId,
                        PartyMemberRole.MEMBER,
                        PartyMemberStatus.PENDING,
                        BillingKeyStatus.ACTIVE
                )
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파티입니다. partyId=" + partyId));
    }
}