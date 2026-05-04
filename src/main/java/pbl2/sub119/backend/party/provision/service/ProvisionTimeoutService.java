package pbl2.sub119.backend.party.provision.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import java.util.ArrayList;
import pbl2.sub119.backend.notification.event.event.HostProvisionDelayedNoticeEvent;
import pbl2.sub119.backend.notification.event.event.MemberAutoRematchStartedEvent;
import pbl2.sub119.backend.notification.event.event.HostProvisionReminderEvent;
import pbl2.sub119.backend.notification.event.event.MemberProvisionReminderEvent;
import pbl2.sub119.backend.notification.event.event.MemberProvisionTimeoutNoticeEvent;
import pbl2.sub119.backend.notification.event.event.PartyTerminatedEvent;
import pbl2.sub119.backend.party.common.entity.MatchWaitingQueue;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.enumerated.MatchWaitingStatus;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.common.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.common.mapper.MatchWaitingQueueMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;
import pbl2.sub119.backend.party.common.service.PartyHistoryService;
import pbl2.sub119.backend.party.provision.entity.PartyProvision;
import pbl2.sub119.backend.party.provision.entity.PartyProvisionMember;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMapper;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMemberMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProvisionTimeoutService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyProvisionMapper partyProvisionMapper;
    private final PartyProvisionMemberMapper partyProvisionMemberMapper;
    private final MatchWaitingQueueMapper matchWaitingQueueMapper;
    private final PartyHistoryService partyHistoryService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    @Lazy
    private ProvisionTimeoutService self;

    // 파티장 미등록 24h: 파티장 리마인드 + 파티원 지연 안내 동시 발행
    public void processHostProvisionAt24h() {
        final List<PartyProvision> due = partyProvisionMapper.findHostProvisionAt24hDue(24);

        for (final PartyProvision provision : due) {
            try {
                self.publishHostAt24hInIsolation(provision);
            } catch (Exception e) {
                log.error("파티장 provision 24시간 안내 실패. partyId={}", provision.getPartyId(), e);
            }
        }
    }

    // 파티장 미등록 48h 경과 시 파티 해체 + 자동 재매칭
    public void processHostTimeout() {
        final List<PartyProvision> timedOut = partyProvisionMapper.findTimedOutProvisions();

        for (final PartyProvision provision : timedOut) {
            try {
                self.dissolvePartyInIsolation(provision.getPartyId());
            } catch (Exception e) {
                log.error("파티 해체 처리 실패. partyId={}", provision.getPartyId(), e);
            }
        }
    }

    // 파티원 미확인 24h 리마인드
    public void processMemberProvisionReminders() {
        publishMemberProvisionReminders(24);
    }

    // 파티원 24h 초과: 강제 탈퇴 없음, 환불 없음, 이력 기록 + 안내
    public void processMemberTimeout() {
        final List<PartyProvisionMember> timedOut = partyProvisionMemberMapper.findRequiredMembersTimedOut();

        for (final PartyProvisionMember member : timedOut) {
            try {
                self.applyMemberTimeoutInIsolation(member);
            } catch (Exception e) {
                log.error("파티원 타임아웃 처리 실패. memberId={}", member.getId(), e);
            }
        }
    }

    private void publishMemberProvisionReminders(final int elapsedHours) {
        final List<PartyProvisionMember> members =
                partyProvisionMemberMapper.findRequiredMembersReminderDue(elapsedHours);

        for (final PartyProvisionMember member : members) {
            try {
                self.publishMemberProvisionReminderInIsolation(member, elapsedHours);
            } catch (Exception e) {
                log.error("파티원 provision 리마인드 실패. memberId={}, elapsedHours={}",
                        member.getId(), elapsedHours, e);
            }
        }
    }

    // 파티장 24h: 파티장 리마인드 + 파티원 지연 안내 동시 발행 — 각 파티별 독립 트랜잭션
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishHostAt24hInIsolation(final PartyProvision provision) {
        final Party party = partyMapper.findById(provision.getPartyId());
        if (party == null || party.getOperationStatus() == OperationStatus.TERMINATED) {
            return;
        }

        eventPublisher.publishEvent(
                new HostProvisionReminderEvent(provision.getPartyId(), party.getHostUserId(), 24)
        );

        final List<Long> memberUserIds = partyMemberMapper.findMembersByPartyId(provision.getPartyId())
                .stream()
                .filter(member -> !member.getUserId().equals(party.getHostUserId()))
                .filter(member -> member.getStatus() != PartyMemberStatus.LEFT)
                .filter(member -> member.getStatus() != PartyMemberStatus.REMOVED)
                .map(PartyMember::getUserId)
                .toList();

        if (!memberUserIds.isEmpty()) {
            eventPublisher.publishEvent(
                    new HostProvisionDelayedNoticeEvent(provision.getPartyId(), memberUserIds)
            );
        }

        log.info("파티장 provision 24시간 안내 발행. partyId={}", provision.getPartyId());
    }

    // 파티 해체 — 각 파티별 독립 트랜잭션
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dissolvePartyInIsolation(final Long partyId) {
        final Party party = partyMapper.findByIdForUpdate(partyId);
        if (party == null || party.getOperationStatus() == OperationStatus.TERMINATED) {
            return;
        }

        final List<PartyMember> members = partyMemberMapper.findMembersByPartyId(partyId);
        final List<Long> memberUserIds = members.stream()
                .map(PartyMember::getUserId)
                .toList();

        final LocalDateTime now = LocalDateTime.now();

        partyMapper.terminateParty(partyId, now);

        final List<Long> requeuedUserIds = new ArrayList<>();
        for (final PartyMember member : members) {
            if (member.getStatus() != PartyMemberStatus.LEFT
                    && member.getStatus() != PartyMemberStatus.REMOVED) {
                partyMemberMapper.updateStatusAndLeftAt(member.getId(), PartyMemberStatus.LEFT);

                // 파티원은 자동 재매칭 대기열 복귀
                if (!member.getUserId().equals(party.getHostUserId())) {
                    requeue(party.getProductId(), member.getUserId(), now);
                    requeuedUserIds.add(member.getUserId());
                }
            }
        }

        partyHistoryService.saveHistory(
                partyId,
                null,
                PartyHistoryEventType.PARTY_PROVISION_DISSOLVED,
                "{\"reason\":\"host_provision_timeout\"}",
                party.getHostUserId()
        );

        eventPublisher.publishEvent(
                new PartyTerminatedEvent(partyId, memberUserIds, "파티장이 기한 내 이용 정보를 등록하지 않음")
        );

        if (!requeuedUserIds.isEmpty()) {
            eventPublisher.publishEvent(new MemberAutoRematchStartedEvent(partyId, requeuedUserIds));
        }

        log.info("파티 해체 완료. partyId={}", partyId);
    }

    // 파티원 리마인드 이벤트 발행 — 각 멤버별 독립 트랜잭션
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishMemberProvisionReminderInIsolation(
            final PartyProvisionMember member,
            final int elapsedHours
    ) {
        eventPublisher.publishEvent(
                new MemberProvisionReminderEvent(
                        member.getPartyId(),
                        member.getPartyOperationId(),
                        member.getUserId(),
                        elapsedHours
                )
        );

        log.info("파티원 provision 리마인드 발행. userId={}, partyId={}, elapsedHours={}",
                member.getUserId(), member.getPartyId(), elapsedHours);
    }

    // 파티원 타임아웃 — 강제 탈퇴 없이 이력 기록 + 안내
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void applyMemberTimeoutInIsolation(final PartyProvisionMember member) {
        final int updated = partyProvisionMemberMapper.markPenaltyApplied(member.getId(), LocalDateTime.now());
        if (updated == 0) {
            log.info("패널티 이미 적용됨 — 건너뜀. memberId={}", member.getId());
            return;
        }

        partyHistoryService.saveHistory(
                member.getPartyId(),
                member.getPartyMemberId(),
                PartyHistoryEventType.MEMBER_PROVISION_TIMEOUT,
                "{\"userId\":" + member.getUserId() + "}",
                member.getUserId()
        );

        eventPublisher.publishEvent(
                new MemberProvisionTimeoutNoticeEvent(
                        member.getPartyId(),
                        member.getPartyOperationId(),
                        member.getUserId()
                )
        );

        log.info("파티원 provision 타임아웃 처리 완료. userId={}, partyId={}",
                member.getUserId(), member.getPartyId());
    }

    private void requeue(
            final String productId,
            final Long userId,
            final LocalDateTime now
    ) {
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