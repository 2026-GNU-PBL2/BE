package pbl2.sub119.backend.settlement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.SettlementStatus;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.mapper.PartyMapper;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;
import pbl2.sub119.backend.pointWallet.entity.PointWallet;
import pbl2.sub119.backend.settlement.entity.Settlement;
import pbl2.sub119.backend.pointWallet.mapper.PointWalletMapper;
import pbl2.sub119.backend.settlement.mapper.SettlementMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementMapper settlementMapper;
    private final PointWalletMapper pointWalletMapper;
    private final PartyMapper partyMapper;
    private final PartyCycleMapper partyCycleMapper;

    @Transactional
    public void process(Long partyId, Long partyCycleId) {
        Settlement existing = settlementMapper.findByPartyCycleId(partyCycleId);
        if (existing != null) {
            return;
        }

        Party party = partyMapper.findById(partyId);
        if (party == null) {
            throw new IllegalStateException("파티가 존재하지 않습니다. partyId=" + partyId);
        }

        PartyCycle cycle = partyCycleMapper.findById(partyCycleId);
        if (cycle == null) {
            throw new IllegalStateException("party_cycle이 존재하지 않습니다. partyCycleId=" + partyCycleId);
        }

        int memberCount = cycle.getMemberCountSnapshot() - 1; // host 제외
        if (memberCount <= 0) {
            return;
        }

        int unitAmount = cycle.getPricePerMemberSnapshot();
        long totalAmount = (long) memberCount * unitAmount;
        LocalDateTime now = LocalDateTime.now();

        Settlement settlement = Settlement.builder()
                .partyId(partyId)
                .partyCycleId(partyCycleId)
                .hostUserId(party.getHostUserId())
                .memberCount(memberCount)
                .unitAmount(unitAmount)
                .totalAmount(totalAmount)
                .status(SettlementStatus.ACCRUED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        settlementMapper.insertSettlement(settlement);

        PointWallet wallet = pointWalletMapper.findByUserId(party.getHostUserId());
        if (wallet == null) {
            pointWalletMapper.insertPointWallet(
                    PointWallet.builder()
                            .userId(party.getHostUserId())
                            .balance(totalAmount)
                            .createdAt(now)
                            .updatedAt(now)
                            .build()
            );
        } else {
            pointWalletMapper.updateBalance(party.getHostUserId(), totalAmount);
        }
    }
}