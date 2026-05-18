package pbl2.sub119.backend.party.leave.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
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
import pbl2.sub119.backend.party.cycle.service.SubscriptionCycleWindowValidator;
import pbl2.sub119.backend.party.leave.dto.response.CancelLeaveResponse;
import pbl2.sub119.backend.party.leave.dto.response.PartyLeaveReservationMemberResponse;
import pbl2.sub119.backend.party.leave.dto.response.PartyLeaveReserveResponse;
import pbl2.sub119.backend.party.leave.event.PartyRematchRequestedEvent;

@Service
@RequiredArgsConstructor
public class PartyLeaveService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyHistoryService partyHistoryService;
    private final SubscriptionCycleWindowValidator subscriptionCycleWindowValidator;
    private final ApplicationEventPublisher eventPublisher;

    // 다음 결제일 기준 탈퇴 예약
    @Transactional
    public PartyLeaveReserveResponse reserveLeave(final Long partyId, final Long userId) {
        final Party party = partyMapper.findByIdForUpdate(partyId);
        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        final PartyMember member = partyMemberMapper.findByPartyIdAndUserIdForUpdate(partyId, userId);
        if (member == null) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_FORBIDDEN);
        }

        if (member.getStatus() == PartyMemberStatus.LEAVE_RESERVED) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_ALREADY_RESERVED);
        }

        if (member.getStatus() != PartyMemberStatus.ACTIVE) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_NOT_ALLOWED);
        }

        subscriptionCycleWindowValidator.validateLeaveReservationWindow(partyId);

        final int updated = partyMemberMapper.updateLeaveReserved(partyId, userId);
        if (updated == 0) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_NOT_ALLOWED);
        }

        final VacancyType vacancyType =
                member.getRole() == PartyRole.HOST ? VacancyType.HOST : VacancyType.MEMBER;

        partyMapper.updateVacancyType(partyId, vacancyType);
        partyMapper.updateRecruitStatus(partyId, RecruitStatus.RECRUITING);

        final PartyMember updatedMember = partyMemberMapper.findByPartyIdAndUserId(partyId, userId);

        partyHistoryService.saveHistory(
                partyId,
                updatedMember.getId(),
                PartyHistoryEventType.LEAVE_RESERVED,
                "{\"userId\":" + userId + ",\"role\":\"" + member.getRole() + "\"}",
                userId
        );

        return new PartyLeaveReserveResponse(
                partyId,
                updatedMember.getId(),
                userId,
                updatedMember.getRole(),
                updatedMember.getStatus(),
                updatedMember.getLeaveReservedAt(),
                vacancyType,
                "탈퇴 예약이 완료되었습니다."
        );
    }

    // 탈퇴 예약 취소
    @Transactional
    public CancelLeaveResponse cancelLeave(final Long partyId, final Long userId) {
        final Party party = partyMapper.findByIdForUpdate(partyId);
        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        final PartyMember member = partyMemberMapper.findByPartyIdAndUserIdForUpdate(partyId, userId);
        if (member == null) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_FORBIDDEN);
        }

        if (member.getStatus() != PartyMemberStatus.LEAVE_RESERVED) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_NOT_ALLOWED);
        }

        final List<PartyMember> switchWaitingMembers = partyMemberMapper.findSwitchWaitingMembers(partyId);

        if (switchWaitingMembers.isEmpty()) {
            // Case A: 입장 대기자 없음 → 탈퇴 예약 취소
            final int updated = partyMemberMapper.clearLeaveReserved(partyId, userId);
            if (updated == 0) {
                throw new PartyException(ErrorCode.PARTY_LEAVE_NOT_ALLOWED);
            }

            final List<PartyMember> reservedMembers = partyMemberMapper.findLeaveReservedMembers(partyId);
            if (reservedMembers.isEmpty()) {
                partyMapper.updateVacancyType(partyId, VacancyType.NONE);
                final int occupiedCount = partyMemberMapper.countOccupiedMembers(partyId);
                partyMapper.updateRecruitStatus(partyId,
                        occupiedCount >= party.getCapacity() ? RecruitStatus.FULL : RecruitStatus.RECRUITING);
            } else {
                final boolean hasHostReservation = reservedMembers.stream()
                        .anyMatch(m -> m.getRole() == PartyRole.HOST);
                partyMapper.updateVacancyType(partyId, hasHostReservation ? VacancyType.HOST : VacancyType.MEMBER);
            }

            partyHistoryService.saveHistory(
                    partyId,
                    member.getId(),
                    PartyHistoryEventType.LEAVE_RESERVATION_CANCELED,
                    "{\"userId\":" + userId + "}",
                    userId
            );
            return CancelLeaveResponse.cancelled();
        } else {
            // Case B: 입장 대기자(SWITCH_WAITING) 있음 → 탈퇴 강제 확정 + AFTER_COMMIT 재매칭
            // 취소 시도자가 마음을 바꿨으나 대기자가 자리를 선점해 번복 불가 → 다른 파티로 재매칭
            partyMemberMapper.updateStatusAndLeftAt(member.getId(), PartyMemberStatus.LEFT);

            final int occupiedCount = partyMemberMapper.countOccupiedMembers(partyId);
            partyMapper.updateCurrentMemberCount(partyId, occupiedCount);
            partyMapper.updateVacancyType(partyId, VacancyType.NONE);
            partyMapper.updateRecruitStatus(partyId, RecruitStatus.FULL);

            partyHistoryService.saveHistory(
                    partyId,
                    member.getId(),
                    PartyHistoryEventType.MEMBER_LEFT,
                    "{\"userId\":" + userId + "}",
                    userId
            );

            // 외부 트랜잭션 커밋 후 재매칭 처리 (정합성 보장)
            eventPublisher.publishEvent(
                    new PartyRematchRequestedEvent(partyId, party.getProductId(), userId)
            );
            return CancelLeaveResponse.forcedLeft();
        }
    }

    // 파티장이 탈퇴 예약 멤버 목록 조회
    @Transactional(readOnly = true)
    public List<PartyLeaveReservationMemberResponse> getLeaveReservations(
            final Long partyId,
            final Long requesterUserId
    ) {
        final Party party = partyMapper.findById(partyId);
        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        if (!party.getHostUserId().equals(requesterUserId)) {
            throw new PartyException(ErrorCode.PARTY_HOST_ONLY);
        }

        return partyMemberMapper.findLeaveReservedMembers(partyId)
                .stream()
                .map(member -> new PartyLeaveReservationMemberResponse(
                        member.getId(),
                        member.getUserId(),
                        member.getRole(),
                        member.getStatus(),
                        member.getLeaveReservedAt()
                ))
                .toList();
    }
}
