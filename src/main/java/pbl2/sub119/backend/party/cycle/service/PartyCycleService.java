package pbl2.sub119.backend.party.cycle.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.entity.PartyMember;
import pbl2.sub119.backend.party.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.enumerated.PartyRole;
import pbl2.sub119.backend.party.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.enumerated.VacancyType;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.party.mapper.PartyMapper;
import pbl2.sub119.backend.party.mapper.PartyMemberMapper;
import pbl2.sub119.backend.party.provision.service.PartyProvisionCommandService;
import pbl2.sub119.backend.party.service.PartyHistoryService;

@Service
@RequiredArgsConstructor
public class PartyCycleService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyHistoryService partyHistoryService;
    private final PartyProvisionCommandService partyProvisionCommandService;

    // 결제 성공 후 이용 주기 시작 시 서비스 상태 반영
    @Transactional
    public void handleCycleStart(final Long partyId) {
        final Party party = partyMapper.findByIdForUpdate(partyId);
        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        // 탈퇴 예약 멤버를 실제 탈퇴 처리
        final List<PartyMember> leaveReservedMembers = partyMemberMapper.findLeaveReservedMembers(partyId);
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

        // 다음 회차 활성화 대상 멤버를 이용 가능 상태로 전환
        final List<PartyMember> switchWaitingMembers = partyMemberMapper.findSwitchWaitingMembers(partyId);
        for (PartyMember member : switchWaitingMembers) {
            partyMemberMapper.updateStatusAndActivatedAt(member.getId(), PartyMemberStatus.ACTIVE);

            partyHistoryService.saveHistory(
                    partyId,
                    member.getId(),
                    PartyHistoryEventType.MEMBER_JOINED,
                    "{\"userId\":" + member.getUserId() + ",\"status\":\"ACTIVE\"}",
                    member.getUserId()
            );
        }

        // 현재 점유 인원 수 재계산
        final int occupiedCount = partyMemberMapper.countOccupiedMembers(partyId);
        partyMapper.updateCurrentMemberCount(partyId, occupiedCount);

        // 결원 상태 갱신
        final VacancyType vacancyType = calculateVacancyType(partyId, occupiedCount, party.getCapacity());
        partyMapper.updateVacancyType(partyId, vacancyType);

        // 모집 상태 갱신
        if (occupiedCount >= party.getCapacity()) {
            partyMapper.updateRecruitStatus(partyId, RecruitStatus.FULL);
        } else {
            partyMapper.updateRecruitStatus(partyId, RecruitStatus.RECRUITING);
        }

        // 제공 정보 후속 반영
        partyProvisionCommandService.handleCycleStart(partyId);
    }

    // 현재 상태 기준 결원 유형 계산
    private VacancyType calculateVacancyType(
            final Long partyId,
            final int occupiedCount,
            final int totalCapacity
    ) {
        if (occupiedCount >= totalCapacity) {
            return VacancyType.NONE;
        }

        final List<PartyMember> reservedMembers = partyMemberMapper.findLeaveReservedMembers(partyId);
        final boolean hasHostReservation = reservedMembers.stream()
                .anyMatch(member -> member.getRole() == PartyRole.HOST);

        return hasHostReservation ? VacancyType.HOST : VacancyType.MEMBER;
    }
}