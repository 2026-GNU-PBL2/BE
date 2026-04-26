package pbl2.sub119.backend.settlement.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.SettlementStatus;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;
import pbl2.sub119.backend.payment.policy.FeePolicy;
import pbl2.sub119.backend.pointWallet.entity.PointWallet;
import pbl2.sub119.backend.pointWallet.mapper.PointWalletMapper;
import pbl2.sub119.backend.settlement.entity.Settlement;
import pbl2.sub119.backend.settlement.mapper.SettlementMapper;

@Slf4j
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
            log.info("이미 처리된 정산입니다. partyCycleId={}", partyCycleId);
            return;
        }

        PartyCycle cycle = partyCycleMapper.findById(partyCycleId);
        if (cycle == null) {
            throw new IllegalStateException("party_cycle이 존재하지 않습니다. partyCycleId=" + partyCycleId);
        }

        if (!cycle.getPartyId().equals(partyId)) {
            throw new IllegalStateException(
                    "partyId와 partyCycleId 소속이 일치하지 않습니다. partyId=" + partyId + ", cycle.partyId=" + cycle.getPartyId()
            );
        }

        Party party = partyMapper.findById(partyId);
        if (party == null) {
            throw new IllegalStateException("파티가 존재하지 않습니다. partyId=" + partyId);
        }

        // 정산 인원수는 호스트를 제외한 ACTIVE MEMBER 기준 snapshot 값을 그대로 사용한다.
        int memberCount = cycle.getMemberCountSnapshot();
        if (memberCount <= 0) {
            log.info("정산 대상 멤버가 없습니다. partyId={}, partyCycleId={}", partyId, partyCycleId);
            return;
        }

        int unitAmount = cycle.getPricePerMemberSnapshot();
        long totalCharged = (long) memberCount * unitAmount;

        // 파티원 수수료 전액 회수 + 파티장 수수료 공제
        long feeDeducted = (long) memberCount * FeePolicy.MEMBER_FEE + FeePolicy.HOST_FEE;
        long totalAmount = Math.max(0L, totalCharged - feeDeducted);

        LocalDateTime now = LocalDateTime.now();

        Settlement settlement = Settlement.builder()
                .partyId(partyId)
                .partyCycleId(partyCycleId)
                .hostUserId(party.getHostUserId())
                .memberCount(memberCount)
                .unitAmount(unitAmount)
                .totalAmount(totalAmount)
                .feeDeducted(feeDeducted)
                .status(SettlementStatus.ACCRUED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        try {
            settlementMapper.insertSettlement(settlement);
        } catch (DataIntegrityViolationException e) {
            log.info("정산 중복 생성 감지. partyCycleId={}", partyCycleId);
            return;
        }

        int updated = pointWalletMapper.updateBalance(party.getHostUserId(), totalAmount);
        if (updated == 0) {
            try {
                pointWalletMapper.insertPointWallet(
                        PointWallet.builder()
                                .userId(party.getHostUserId())
                                .balance(totalAmount)
                                .createdAt(now)
                                .updatedAt(now)
                                .build()
                );
            } catch (DataIntegrityViolationException e) {
                log.info("지갑 동시 생성 감지. balance 누적 재시도. userId={}", party.getHostUserId());
                pointWalletMapper.updateBalance(party.getHostUserId(), totalAmount);
            }
        }

        log.info("정산 적립 완료. partyId={}, partyCycleId={}, hostUserId={}, amount={}",
                partyId, partyCycleId, party.getHostUserId(), totalAmount);
    }
}
