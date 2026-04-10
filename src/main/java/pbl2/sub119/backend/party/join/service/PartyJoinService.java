package pbl2.sub119.backend.party.join.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;
import pbl2.sub119.backend.party.common.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.common.service.PartyHistoryService;
import pbl2.sub119.backend.party.common.exception.PartyException;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;

@Service
@RequiredArgsConstructor
public class PartyJoinService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyHistoryService partyHistoryService;

    // 특정 파티에 실제 참여 처리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void joinParty(final Long partyId, final Long userId) {
        final Party party = partyMapper.findByIdForUpdate(partyId);
        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        if (party.getRecruitStatus() != RecruitStatus.RECRUITING) {
            throw new PartyException(ErrorCode.PARTY_NOT_RECRUITING);
        }

        final PartyMember existingMember = partyMemberMapper.findByPartyIdAndUserId(partyId, userId);
        if (existingMember != null
                && existingMember.getStatus() != PartyMemberStatus.LEFT
                && existingMember.getStatus() != PartyMemberStatus.REMOVED) {
            throw new PartyException(ErrorCode.PARTY_ALREADY_JOINED);
        }

        final int occupiedCount = partyMemberMapper.countOccupiedMembers(partyId);
        if (occupiedCount >= party.getCapacity()) {
            throw new PartyException(ErrorCode.PARTY_FULL);
        }

        final PartyMember newMember = PartyMember.builder()
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

        final int updatedCount = partyMemberMapper.countOccupiedMembers(partyId);
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