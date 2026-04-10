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

    @Transactional(readOnly = true)
    public int countRecurringBillableMembers(Long partyId) {
        return partyMemberMapper.countRecurringBillableMembers(partyId);
    }

    @Transactional
    public void confirmRecurringCycleStart(Long partyId) {
        Party party = partyMapper.findByIdForUpdate(partyId);
        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        List<PartyMember> leaveReservedMembers = partyMemberMapper.findLeaveReservedMembers(partyId);
        for (PartyMember member : leaveReservedMembers) {
            partyMemberMapper.updateStatusAndLeftAt(member.getId(), PartyMemberStatus.LEFT);

            partyHistoryService.saveHistory(
                    partyId,
                    member.getId(),
                    PartyHistoryEventType.MEMBER_LEFT,
                    "{\"userId\":" + member.getUserId() + "}",
                    member.getUserId()
            );
        }

        List<PartyMember> switchWaitingMembers = partyMemberMapper.findSwitchWaitingMembers(partyId);
        for (PartyMember member : switchWaitingMembers) {
            partyMemberMapper.updateStatusAndActivatedAt(member.getId(), PartyMemberStatus.ACTIVE);
        }

        int occupiedCount = partyMemberMapper.countOccupiedMembers(partyId);
        partyMapper.updateCurrentMemberCount(partyId, occupiedCount);

        if (occupiedCount >= party.getCapacity()) {
            partyMapper.updateRecruitStatus(partyId, RecruitStatus.FULL);
            partyMapper.updateVacancyType(partyId, VacancyType.NONE);
        } else {
            partyMapper.updateRecruitStatus(partyId, RecruitStatus.RECRUITING);
            partyMapper.updateVacancyType(partyId, VacancyType.MEMBER);
        }

        partyOperationCommandService.handleCycleStart(partyId);
    }

    @Transactional
    public void handleCycleStart(Long partyId) {
        confirmRecurringCycleStart(partyId);
    }
}
