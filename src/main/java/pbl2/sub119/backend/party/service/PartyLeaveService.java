package pbl2.sub119.backend.party.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.dto.response.PartyLeaveReservationMemberResponse;
import pbl2.sub119.backend.party.dto.response.PartyLeaveReserveResponse;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.entity.PartyMember;
import pbl2.sub119.backend.party.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.enumerated.PartyRole;
import pbl2.sub119.backend.party.enumerated.VacancyType;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.party.mapper.PartyMapper;
import pbl2.sub119.backend.party.mapper.PartyMemberMapper;

@Service
@RequiredArgsConstructor
public class PartyLeaveService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyHistoryService partyHistoryService;
    private final SubscriptionCycleWindowValidator subscriptionCycleWindowValidator;

    // 파티원 탈퇴 예약은 즉시 탈퇴가 아닌 다음 결제일 기준으로 탈퇴 예정 상태를 변경
    @Transactional
    public PartyLeaveReserveResponse reserveLeave(Long partyId, Long userId) {
        // 파티 존재 여부 및 동시성 제어
        Party party = partyMapper.findByIdForUpdate(partyId);
        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        // 해당 유저가 파티 멤버인지 확인
        PartyMember member = partyMemberMapper.findByPartyIdAndUserIdForUpdate(partyId, userId);
        if (member == null) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_FORBIDDEN);
        }

        // 파티장은 일반 탈퇴 예약 불가 -> 반드시 승계 로직 타야 함
        if (member.getRole() == PartyRole.HOST) {
            throw new PartyException(ErrorCode.PARTY_HOST_TRANSFER_REQUIRED);
        }

        // 이미 탈퇴 예약된 경우 중복 요청 방지
        if (member.getStatus() == PartyMemberStatus.LEAVE_RESERVED) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_ALREADY_RESERVED);
        }

        // ACTIVE 상태만 탈퇴 예약 가능
        if (member.getStatus() != PartyMemberStatus.ACTIVE) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_NOT_ALLOWED);
        }

        // 탈퇴 예약이 다음 결제일 기준 7일 전인지 검증
        subscriptionCycleWindowValidator.validateLeaveReservationWindow(partyId);

        // ACTIVE -> LEAVE_RESERVED로 변경
        int updated = partyMemberMapper.updateLeaveReserved(partyId, userId);
        if (updated == 0) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_NOT_ALLOWED);
        }

        // 결원 상태 반영 (자동 매칭 트리거용)
        partyMapper.updateVacancyType(partyId, VacancyType.MEMBER);

        // 변경된 멤버 재조회
        PartyMember updatedMember = partyMemberMapper.findByPartyIdAndUserId(partyId, userId);

        partyHistoryService.saveHistory(
                partyId,
                updatedMember.getId(),
                PartyHistoryEventType.LEAVE_RESERVED,
                "{\"userId\":" + userId + "}",
                userId
        );

        return new PartyLeaveReserveResponse(
                partyId,
                updatedMember.getId(),
                userId,
                updatedMember.getStatus(),
                updatedMember.getLeaveReservedAt(),
                "탈퇴 예약이 완료되었습니다."
        );
    }

    @Transactional
    public void cancelLeave(Long partyId, Long userId) {
        Party party = partyMapper.findByIdForUpdate(partyId);
        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        PartyMember member = partyMemberMapper.findByPartyIdAndUserIdForUpdate(partyId, userId);
        if (member == null) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_FORBIDDEN);
        }

        if (member.getStatus() != PartyMemberStatus.LEAVE_RESERVED) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_NOT_ALLOWED);
        }

        int updated = partyMemberMapper.clearLeaveReserved(partyId, userId);
        if (updated == 0) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_NOT_ALLOWED);
        }

        List<PartyMember> reservedMembers = partyMemberMapper.findLeaveReservedMembers(partyId);
        if (reservedMembers.isEmpty()) {
            partyMapper.updateVacancyType(partyId, VacancyType.NONE);
        }

        partyHistoryService.saveHistory(
                partyId,
                member.getId(),
                PartyHistoryEventType.LEAVE_RESERVATION_CANCELED,
                "{\"userId\":" + userId + "}",
                userId
        );
    }

    @Transactional(readOnly = true)
    public List<PartyLeaveReservationMemberResponse> getLeaveReservations(Long partyId, Long requesterUserId) {
        Party party = partyMapper.findById(partyId);
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