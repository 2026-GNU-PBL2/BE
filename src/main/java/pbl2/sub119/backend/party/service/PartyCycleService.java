package pbl2.sub119.backend.party.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.entity.PartyMember;
import pbl2.sub119.backend.party.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.enumerated.VacancyType;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.party.mapper.PartyMapper;
import pbl2.sub119.backend.party.mapper.PartyMemberMapper;
import pbl2.sub119.backend.partyoperation.service.PartyOperationCommandService;

@Service
@RequiredArgsConstructor
public class PartyCycleService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyHistoryService partyHistoryService;
    private final PartyOperationCommandService partyOperationCommandService;

    // 결제 성공 후 시작되는 이용 주기 기준으로 파티 상태를 반영
    @Transactional
    public void handleCycleStart(Long partyId) {

        Party party = partyMapper.findByIdForUpdate(partyId);
        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        // 이번 결제일 기준으로 탈퇴 예정 멤버를 실제 탈퇴 처리
        List<PartyMember> leaveReservedMembers = partyMemberMapper.findLeaveReservedMembers(partyId);
        for (PartyMember member : leaveReservedMembers) {
            partyMemberMapper.updateStatusAndLeftAt(member.getId(), PartyMemberStatus.LEFT);

            // 파티 이력에 탈퇴 반영 기록 저장
            partyHistoryService.saveHistory(
                    partyId,
                    member.getId(),
                    PartyHistoryEventType.MEMBER_LEFT,
                    "{\"userId\":" + member.getUserId() + "}",
                    member.getUserId()
            );
        }

        // 결원 충원 대기 멤버를 이번 주기부터 실제 이용 가능 상태로 전환
        List<PartyMember> switchWaitingMembers = partyMemberMapper.findSwitchWaitingMembers(partyId);
        for (PartyMember member : switchWaitingMembers) {
            partyMemberMapper.updateStatusAndActivatedAt(member.getId(), PartyMemberStatus.ACTIVE);
        }

        // 결제일 반영 후 실제 점유 인원 수를 다시 계산
        int occupiedCount = partyMemberMapper.countOccupiedMembers(partyId);
        partyMapper.updateCurrentMemberCount(partyId, occupiedCount);

        // 현재 인원 기준으로 모집 상태와 결원 유형 갱신
        if (occupiedCount >= party.getCapacity()) {
            partyMapper.updateRecruitStatus(partyId, RecruitStatus.FULL);
            partyMapper.updateVacancyType(partyId, VacancyType.NONE);
        } else {
            partyMapper.updateRecruitStatus(partyId, RecruitStatus.RECRUITING);
            partyMapper.updateVacancyType(partyId, VacancyType.MEMBER);
        }

        // 주기 시작 후 멤버 변경 결과를 기준으로 운영 상태도 후속 반영
        partyOperationCommandService.handleCycleStart(partyId);
    }
}