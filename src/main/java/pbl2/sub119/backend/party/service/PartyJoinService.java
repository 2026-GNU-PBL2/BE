package pbl2.sub119.backend.party.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.entity.PartyMember;
import pbl2.sub119.backend.party.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.enumerated.PartyRole;
import pbl2.sub119.backend.party.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.party.mapper.PartyMapper;
import pbl2.sub119.backend.party.mapper.PartyMemberMapper;

@Service
@RequiredArgsConstructor
public class PartyJoinService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyHistoryService partyHistoryService;

    @Transactional
    public void joinParty(Long partyId, Long userId) {
        Party party = partyMapper.findByIdForUpdate(partyId);
        if (party == null) {
            throw new PartyException("존재하지 않는 파티입니다.");
        }

        if (party.getRecruitStatus() != RecruitStatus.RECRUITING) {
            throw new PartyException("현재 모집 중인 파티가 아닙니다.");
        }

        PartyMember existingMember = partyMemberMapper.findByPartyIdAndUserId(partyId, userId);
        if (existingMember != null
                && existingMember.getStatus() != PartyMemberStatus.LEFT
                && existingMember.getStatus() != PartyMemberStatus.REMOVED) {
            throw new PartyException("이미 해당 파티에 참여 중이거나 처리 중인 사용자입니다.");
        }

        int occupiedCount = partyMemberMapper.countOccupiedMembers(partyId);
        if (occupiedCount >= party.getCapacity()) {
            partyMapper.updateRecruitStatus(partyId, RecruitStatus.FULL);

            partyHistoryService.saveHistory(
                    partyId,
                    null,
                    PartyHistoryEventType.RECRUIT_STATUS_CHANGED,
                    "{\"before\":\"RECRUITING\",\"after\":\"FULL\"}",
                    userId
            );

            throw new PartyException("정원이 가득 찼습니다.");
        }

        PartyMember newMember = PartyMember.builder()
                .partyId(partyId)
                .userId(userId)
                .role(PartyRole.MEMBER)
                .status(PartyMemberStatus.PENDING)
                .joinedAt(LocalDateTime.now())
                .activatedAt(null)
                .serviceStartAt(null)
                .serviceEndAt(null)
                .leaveReservedAt(null)
                .leftAt(null)
                .replacedTargetMemberId(null)
                .build();

        partyMemberMapper.insertPartyMember(newMember);

        int updatedCount = partyMemberMapper.countOccupiedMembers(partyId);
        partyMapper.updateCurrentMemberCount(partyId, updatedCount);

        if (updatedCount >= party.getCapacity()) {
            partyMapper.updateRecruitStatus(partyId, RecruitStatus.FULL);

            partyHistoryService.saveHistory(
                    partyId,
                    newMember.getId(),
                    PartyHistoryEventType.RECRUIT_STATUS_CHANGED,
                    "{\"before\":\"RECRUITING\",\"after\":\"FULL\"}",
                    userId
            );
        }

        partyHistoryService.saveHistory(
                partyId,
                newMember.getId(),
                PartyHistoryEventType.MEMBER_JOINED,
                "{\"userId\":" + userId + "}",
                userId
        );
    }
}