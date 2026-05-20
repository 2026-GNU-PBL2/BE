package pbl2.sub119.backend.party.cycle.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.notification.event.event.PartyTerminatedEvent;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.enumerated.*;
import pbl2.sub119.backend.party.common.exception.PartyException;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;
import pbl2.sub119.backend.party.common.service.PartyHistoryService;
import pbl2.sub119.backend.party.provision.service.PartyProvisionCommandService;
import pbl2.sub119.backend.notification.event.event.HostChangedEvent;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;

@Service
@RequiredArgsConstructor
public class PartyCycleService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyHistoryService partyHistoryService;
    private final PartyProvisionCommandService partyProvisionCommandService;
    private final PartyCycleMapper partyCycleMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    @Lazy
    private PartyCycleService self;

    // 회차 결제 성공 후 이용 주기 시작 시 서비스 상태 반영 (첫 회차 / 반복 회차 공통)
    @Transactional
    public void handleCycleStart(final Long partyId) {
        final Party party = getPartyForUpdate(partyId);

        final List<PartyMember> leaveReservedMembers = partyMemberMapper.findLeaveReservedMembers(partyId);
        processLeaveReservedMembers(partyId, leaveReservedMembers);

        final List<PartyMember> switchWaitingMembers = partyMemberMapper.findSwitchWaitingMembers(partyId);
        processSwitchWaitingMembers(partyId, switchWaitingMembers);

        final int occupiedCount = partyMemberMapper.countOccupiedMembers(partyId);

        // 탈퇴 예약자 전원 퇴장 후 잔류 멤버 없으면 파티 해체
        if (occupiedCount == 0) {
            terminateEmptyParty(partyId, leaveReservedMembers);
            return;
        }

        refreshPartyState(partyId, party.getCapacity(), leaveReservedMembers, occupiedCount);

        partyProvisionCommandService.handleCycleStart(partyId);
    }

    // 반복 회차 과금 대상 수 조회
    // 반복 회차는 상태 반영 이후 ACTIVE MEMBER만 과금 대상으로 본다.
    @Transactional(readOnly = true)
    public int countRecurringBillableMembers(final Long partyId) {
        return partyMemberMapper.countRecurringBillableMembers(partyId);
    }

    // 전원 탈퇴 예약 파티 해체 — 결제 없이 직접 해체 처리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dissolveAllLeaveReservedParty(final Long partyId) {
        handleCycleStart(partyId);
    }

    private Party getPartyForUpdate(final Long partyId) {
        final Party party = partyMapper.findByIdForUpdate(partyId);
        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }
        return party;
    }

    private void processLeaveReservedMembers(
            final Long partyId,
            final List<PartyMember> leaveReservedMembers
    ) {
        for (PartyMember member : leaveReservedMembers) {
            partyMemberMapper.updateStatusAndLeftAt(member.getId(), PartyMemberStatus.LEFT);

            partyHistoryService.saveHistory(
                    partyId,
                    member.getId(),
                    PartyHistoryEventType.MEMBER_LEFT,
                    "{\"userId\":" + member.getUserId() + ",\"role\":\"" + member.getRole() + "\"}",
                    member.getUserId()
            );
        }
    }

    private void processSwitchWaitingMembers(
            final Long partyId,
            final List<PartyMember> switchWaitingMembers
    ) {
        for (PartyMember member : switchWaitingMembers) {
            partyMemberMapper.updateStatusAndActivatedAt(member.getId(), PartyMemberStatus.ACTIVE);

            partyHistoryService.saveHistory(
                    partyId,
                    member.getId(),
                    PartyHistoryEventType.MEMBER_JOINED,
                    "{\"userId\":" + member.getUserId() + ",\"status\":\"ACTIVE\"}",
                    member.getUserId()
            );

            if (member.getRole() == PartyRole.HOST) {
                partyMapper.updateHostUserId(partyId, member.getUserId());

                final List<Long> memberUserIds = partyMemberMapper
                        .findProvisionTargetMembersByPartyId(partyId).stream()
                        .filter(m -> !m.getUserId().equals(member.getUserId()))
                        .map(pbl2.sub119.backend.party.common.entity.PartyMember::getUserId)
                        .toList();

                eventPublisher.publishEvent(
                        new HostChangedEvent(partyId, member.getUserId(), memberUserIds));
            }
        }
    }

    private void terminateEmptyParty(final Long partyId, final List<PartyMember> formerMembers) {
        final LocalDateTime now = LocalDateTime.now();
        partyMapper.terminateParty(partyId, now);
        partyMapper.updateRecruitStatus(partyId, RecruitStatus.CLOSED);
        partyMapper.updateVacancyType(partyId, VacancyType.NONE);
        partyMapper.updateCurrentMemberCount(partyId, 0);

        final PartyCycle runningCycle = partyCycleMapper.findLatestPendingOrRunningCycle(
                partyId, PartyCycleStatus.PAYMENT_PENDING, PartyCycleStatus.RUNNING);
        if (runningCycle != null && runningCycle.getStatus() == PartyCycleStatus.RUNNING) {
            partyCycleMapper.closeCycle(runningCycle.getId(), PartyCycleStatus.RUNNING, PartyCycleStatus.CLOSED, now);
        }

        partyHistoryService.saveHistory(
                partyId, null, PartyHistoryEventType.PARTY_TERMINATED,
                "{\"reason\":\"all_leave_reserved\"}", null
        );

        final List<Long> memberUserIds = formerMembers.stream()
                .map(PartyMember::getUserId)
                .toList();
        eventPublisher.publishEvent(new PartyTerminatedEvent(partyId, memberUserIds, "전원 탈퇴 예약으로 파티 해체"));
    }

    private void refreshPartyState(
            final Long partyId,
            final int totalCapacity,
            final List<PartyMember> leaveReservedMembers,
            final int occupiedCount
    ) {
        partyMapper.updateCurrentMemberCount(partyId, occupiedCount);

        final VacancyType vacancyType = calculateVacancyType(
                occupiedCount,
                totalCapacity,
                leaveReservedMembers
        );
        partyMapper.updateVacancyType(partyId, vacancyType);

        if (occupiedCount >= totalCapacity) {
            partyMapper.updateRecruitStatus(partyId, RecruitStatus.FULL);
        } else {
            partyMapper.updateRecruitStatus(partyId, RecruitStatus.RECRUITING);
        }
    }

    private VacancyType calculateVacancyType(
            final int occupiedCount,
            final int totalCapacity,
            final List<PartyMember> leaveReservedMembers
    ) {
        if (occupiedCount >= totalCapacity) {
            return VacancyType.NONE;
        }

        final boolean hasHostReservation = leaveReservedMembers.stream()
                .anyMatch(member -> member.getRole() == PartyRole.HOST);

        return hasHostReservation ? VacancyType.HOST : VacancyType.MEMBER;
    }
}