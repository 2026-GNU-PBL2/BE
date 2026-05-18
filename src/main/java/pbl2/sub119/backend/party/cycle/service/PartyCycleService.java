package pbl2.sub119.backend.party.cycle.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.common.error.ErrorCode;
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

@Service
@RequiredArgsConstructor
public class PartyCycleService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyHistoryService partyHistoryService;
    private final PartyProvisionCommandService partyProvisionCommandService;
    private final ApplicationEventPublisher eventPublisher;

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
            terminateEmptyParty(partyId);
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

    // D-1 파티장 사전 활성화: SWITCH_WAITING HOST만 처리 (결제 전날 호출)
    // 기존 LEAVE_RESERVED HOST를 LEFT 처리하고, 신규 HOST를 ACTIVE로 사전 활성화한다.
    @Transactional
    public void activateSwitchWaitingHost(final Long partyId) {
        // for update로 배타 락 획득 후 TERMINATED 여부 재검증 (TOCTOU 방지)
        final Party party = partyMapper.findByIdForUpdate(partyId);
        if (party == null || party.getOperationStatus() == OperationStatus.TERMINATED) {
            return;
        }

        final List<PartyMember> switchWaitingMembers = partyMemberMapper.findSwitchWaitingMembers(partyId);
        final PartyMember newHostMember = switchWaitingMembers.stream()
                .filter(m -> m.getRole() == PartyRole.HOST)
                .findFirst()
                .orElse(null);

        if (newHostMember == null) {
            return;
        }

        // 기존 LEAVE_RESERVED HOST → LEFT
        final List<PartyMember> leaveReservedMembers = partyMemberMapper.findLeaveReservedMembers(partyId);
        for (final PartyMember member : leaveReservedMembers) {
            if (member.getRole() == PartyRole.HOST) {
                partyMemberMapper.updateStatusAndLeftAt(member.getId(), PartyMemberStatus.LEFT);
                partyHistoryService.saveHistory(
                        partyId,
                        member.getId(),
                        PartyHistoryEventType.MEMBER_LEFT,
                        "{\"userId\":" + member.getUserId() + ",\"role\":\"HOST\"}",
                        member.getUserId()
                );
            }
        }

        // 신규 HOST → ACTIVE
        partyMemberMapper.updateStatusAndActivatedAt(newHostMember.getId(), PartyMemberStatus.ACTIVE);
        partyMapper.updateHostUserId(partyId, newHostMember.getUserId());

        partyHistoryService.saveHistory(
                partyId,
                newHostMember.getId(),
                PartyHistoryEventType.MEMBER_JOINED,
                "{\"userId\":" + newHostMember.getUserId() + ",\"status\":\"ACTIVE\"}",
                newHostMember.getUserId()
        );

        // provision 멤버 구성 변경 반영 (provision RESET_REQUIRED)
        partyProvisionCommandService.handleCycleStart(partyId);

        // 신규 파티장에게 OTT 이용 정보 등록 안내
        final List<Long> memberUserIds = partyMemberMapper
                .findProvisionTargetMembersByPartyId(partyId)
                .stream()
                .filter(m -> !m.getUserId().equals(newHostMember.getUserId()))
                .map(pbl2.sub119.backend.party.common.entity.PartyMember::getUserId)
                .toList();

        eventPublisher.publishEvent(new HostChangedEvent(partyId, newHostMember.getUserId(), memberUserIds));
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

    private void terminateEmptyParty(final Long partyId) {
        final LocalDateTime now = LocalDateTime.now();
        partyMapper.terminateParty(partyId, now);
        partyMapper.updateRecruitStatus(partyId, RecruitStatus.CLOSED);
        partyMapper.updateVacancyType(partyId, VacancyType.NONE);
        partyMapper.updateCurrentMemberCount(partyId, 0);
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