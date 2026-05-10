package pbl2.sub119.backend.party.join.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;
import pbl2.sub119.backend.party.common.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.common.enumerated.VacancyType;
import pbl2.sub119.backend.party.common.exception.PartyException;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;
import pbl2.sub119.backend.party.common.service.PartyHistoryService;

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

        final int increased = partyMapper.increaseCurrentMemberCountIfNotFull(partyId);
        if (increased == 0) {
            throw new PartyException(ErrorCode.PARTY_FULL);
        }

        final LocalDateTime now = LocalDateTime.now();

        final PartyMember newMember = PartyMember.builder()
                .partyId(partyId)
                .userId(userId)
                .role(PartyRole.MEMBER)
                .status(PartyMemberStatus.PENDING)
                .joinedAt(now)
                .activatedAt(null)
                .serviceStartAt(null)
                .serviceEndAt(null)
                .leaveReservedAt(null)
                .leftAt(null)
                .replacedTargetMemberId(null)
                .build();

        partyMemberMapper.insertPartyMember(newMember);

        final Party updatedParty = partyMapper.findByIdForUpdate(partyId);
        if (updatedParty == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        if (updatedParty.getCurrentMemberCount() >= updatedParty.getCapacity()) {
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

    // 결원 파티 참여 — current_member_count 증가 없이 SWITCH_WAITING으로 자리 확정 예약
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void joinVacancyParty(final Long partyId, final Long userId) {
        final Party party = partyMapper.findByIdForUpdate(partyId);
        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        if (party.getRecruitStatus() != RecruitStatus.RECRUITING) {
            throw new PartyException(ErrorCode.PARTY_NOT_RECRUITING);
        }

        // 결원이 없는 일반 모집 파티는 결원 참여 불가
        if (party.getVacancyType() == VacancyType.NONE) {
            throw new PartyException(ErrorCode.PARTY_NOT_RECRUITING);
        }

        // 이미 SWITCH_WAITING 멤버가 있으면 자리 없음 (동시 접근 방지)
        final int existingSwitchWaitingCount = partyMemberMapper.findSwitchWaitingMembers(partyId).size();
        final int leaveReservedCount = partyMemberMapper.findLeaveReservedMembers(partyId).size();
        if (existingSwitchWaitingCount >= leaveReservedCount) {
            throw new PartyException(ErrorCode.PARTY_FULL);
        }

        final PartyMember existing = partyMemberMapper.findByPartyIdAndUserId(partyId, userId);
        if (existing != null
                && existing.getStatus() != PartyMemberStatus.LEFT
                && existing.getStatus() != PartyMemberStatus.REMOVED) {
            throw new PartyException(ErrorCode.PARTY_ALREADY_JOINED);
        }

        final LocalDateTime now = LocalDateTime.now();
        final PartyMember newMember = PartyMember.builder()
                .partyId(partyId)
                .userId(userId)
                .role(PartyRole.MEMBER)
                .status(PartyMemberStatus.SWITCH_WAITING)
                .joinedAt(now)
                .build();

        partyMemberMapper.insertPartyMember(newMember);

        // 모든 결원 자리가 채워진 경우에만 FULL
        final boolean allVacanciesFilled = (existingSwitchWaitingCount + 1) >= leaveReservedCount;
        if (allVacanciesFilled) {
            partyMapper.updateRecruitStatus(partyId, RecruitStatus.FULL);
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