package pbl2.sub119.backend.party.provision.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.common.entity.MatchWaitingQueue;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.enumerated.MatchWaitingStatus;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.common.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;
import pbl2.sub119.backend.party.common.mapper.MatchWaitingQueueMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;
import pbl2.sub119.backend.party.common.service.PartyHistoryService;
import pbl2.sub119.backend.party.provision.entity.PartyProvisionMember;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMemberMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProvisionTimeoutService {

    private static final int WARN_HOURS = 24;
    private static final int DISSOLVE_HOURS = 48;

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyProvisionMemberMapper partyProvisionMemberMapper;
    private final MatchWaitingQueueMapper matchWaitingQueueMapper;
    private final PartyHistoryService partyHistoryService;

    // Spring AOP 자기 호출 우회: 파티별 독립 트랜잭션 실행을 위해 프록시를 통해 자신을 호출
    @Autowired
    @Lazy
    private ProvisionTimeoutService self;

    // 파티장 provision 미등록 타임아웃 처리
    public void processHostTimeout() {
        final List<Party> dissolveTargets =
                partyMapper.findPartiesReadyForProvisionTimeout(DISSOLVE_HOURS);

        for (Party party : dissolveTargets) {
            try {
                self.dissolvePartyInIsolation(party);
            } catch (Exception e) {
                log.error("파티 해체 처리 실패. partyId={}", party.getId(), e);
            }
        }

        final List<Party> warnTargets =
                partyMapper.findPartiesReadyForProvisionTimeout(WARN_HOURS);

        for (Party party : warnTargets) {
            if (dissolveTargets.stream().anyMatch(d -> d.getId().equals(party.getId()))) {
                continue;
            }
            log.info("파티장 provision 24시간 미등록 → 알림 발송 예정. partyId={}", party.getId());
            // TODO: 파티원/파티장 알림 발송 (알림 시스템 구현 후 연동)
        }
    }

    // 파티원 provision confirm 미완료 타임아웃 처리 (소프트 데드라인)
    public void processMemberTimeout() {
        final List<PartyProvisionMember> timedOutMembers =
                partyProvisionMemberMapper.findRequiredMembersTimedOut(WARN_HOURS);

        for (PartyProvisionMember member : timedOutMembers) {
            try {
                self.applyMemberTimeoutInIsolation(member);
            } catch (Exception e) {
                log.error("멤버 타임아웃 처리 실패. partyId={}, userId={}",
                        member.getPartyId(), member.getUserId(), e);
            }
        }
    }

    @Transactional
    public void dissolvePartyInIsolation(final Party party) {
        dissolveParty(party);
    }

    @Transactional
    public void applyMemberTimeoutInIsolation(final PartyProvisionMember member) {
        log.info("파티원 provision 24시간 미완료. partyId={}, userId={}",
                member.getPartyId(), member.getUserId());

        partyHistoryService.saveHistory(
                member.getPartyId(),
                member.getPartyMemberId(),
                PartyHistoryEventType.MEMBER_PROVISION_TIMEOUT,
                "{\"userId\":" + member.getUserId() + "}",
                member.getUserId()
        );

        partyProvisionMemberMapper.markPenaltyApplied(member.getId(), LocalDateTime.now());
        // TODO: 파티원 알림 재발송 (알림 시스템 구현 후 연동)
    }

    private void dissolveParty(final Party party) {
        final Long partyId = party.getId();
        final LocalDateTime now = LocalDateTime.now();

        partyMapper.updateOperationStatus(partyId, OperationStatus.TERMINATED);

        final List<PartyMember> members = partyMemberMapper.findMembersByPartyId(partyId);
        for (PartyMember member : members) {
            partyMemberMapper.updateStatusAndLeftAt(member.getId(), PartyMemberStatus.LEFT);

            if (member.getRole() == PartyRole.MEMBER) {
                requeue(party.getProductId(), member.getUserId(), now);
            }
        }

        partyHistoryService.saveHistory(
                partyId,
                null,
                PartyHistoryEventType.HOST_PROVISION_TIMEOUT,
                "{\"hostUserId\":" + party.getHostUserId() + "}",
                party.getHostUserId()
        );

        partyHistoryService.saveHistory(
                partyId,
                null,
                PartyHistoryEventType.PARTY_TERMINATED,
                "{\"reason\":\"HOST_PROVISION_TIMEOUT\"}",
                null
        );

        // TODO: 파티원/파티장 알림 발송 (알림 시스템 구현 후 연동)
    }

    private void requeue(final String productId, final Long userId, final LocalDateTime now) {
        final MatchWaitingQueue queue = MatchWaitingQueue.builder()
                .productId(productId)
                .userId(userId)
                .status(MatchWaitingStatus.WAITING)
                .requestedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        matchWaitingQueueMapper.insertIfAbsent(queue);
    }
}
