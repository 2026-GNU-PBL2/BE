package pbl2.sub119.backend.party.leave.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;
import pbl2.sub119.backend.party.common.enumerated.VacancyType;
import pbl2.sub119.backend.party.common.service.PartyHistoryService;
import pbl2.sub119.backend.party.cycle.service.SubscriptionCycleWindowValidator;
import pbl2.sub119.backend.party.common.exception.PartyException;
import pbl2.sub119.backend.party.leave.dto.response.PartyLeaveReservationMemberResponse;
import pbl2.sub119.backend.party.leave.dto.response.PartyLeaveReserveResponse;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;

@Service
@RequiredArgsConstructor
public class PartyLeaveService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyHistoryService partyHistoryService;
    private final SubscriptionCycleWindowValidator subscriptionCycleWindowValidator;

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
    public void cancelLeave(final Long partyId, final Long userId) {
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

        final int updated = partyMemberMapper.clearLeaveReserved(partyId, userId);
        if (updated == 0) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_NOT_ALLOWED);
        }

        final List<PartyMember> reservedMembers = partyMemberMapper.findLeaveReservedMembers(partyId);
        final List<PartyMember> switchWaitingMembers = partyMemberMapper.findSwitchWaitingMembers(partyId);

        if (reservedMembers.isEmpty() && switchWaitingMembers.isEmpty()) {
            partyMapper.updateVacancyType(partyId, VacancyType.NONE);
        } else {
            final boolean hasHostReservation = reservedMembers.stream()
                    .anyMatch(reservedMember -> reservedMember.getRole() == PartyRole.HOST);

            partyMapper.updateVacancyType(partyId, hasHostReservation ? VacancyType.HOST : VacancyType.MEMBER);
        }

        partyHistoryService.saveHistory(
                partyId,
                member.getId(),
                PartyHistoryEventType.LEAVE_RESERVATION_CANCELED,
                "{\"userId\":" + userId + "}",
                userId
        );
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