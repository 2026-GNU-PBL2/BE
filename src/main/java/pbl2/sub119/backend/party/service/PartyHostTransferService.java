package pbl2.sub119.backend.party.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.dto.response.HostTransferResponse;
import pbl2.sub119.backend.party.entity.HostTransferRequest;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.entity.PartyMember;
import pbl2.sub119.backend.party.enumerated.*;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.party.mapper.HostTransferRequestMapper;
import pbl2.sub119.backend.party.mapper.PartyMapper;
import pbl2.sub119.backend.party.mapper.PartyMemberMapper;

@Service
@RequiredArgsConstructor
public class PartyHostTransferService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final HostTransferRequestMapper hostTransferRequestMapper;
    private final PartyHistoryService partyHistoryService;

    // 파티장이 탈퇴 예약 전 파티원에게 파티장 승계 요청 함
    @Transactional
    public HostTransferResponse requestTransfer(Long partyId, Long requesterId, Long targetUserId) {

        Party party = partyMapper.findByIdForUpdate(partyId);
        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        // 현재 파티장만 요청 가능
        if (!party.getHostUserId().equals(requesterId)) {
            throw new PartyException(ErrorCode.PARTY_HOST_ONLY);
        }

        // 파티장이 파티 승계 대상으로 자기 자신 지정 불가
        if (requesterId.equals(targetUserId)) {
            throw new PartyException(ErrorCode.PARTY_HOST_TRANSFER_SELF_NOT_ALLOWED);
        }

        // 이미 진행 중인 요청이 존재하는 경우 불가
        HostTransferRequest existing = hostTransferRequestMapper.findActiveRequestByPartyId(partyId);
        if (existing != null) {
            throw new PartyException(ErrorCode.PARTY_HOST_TRANSFER_ALREADY_EXISTS);
        }

        PartyMember targetMember = partyMemberMapper.findByPartyIdAndUserIdForUpdate(partyId, targetUserId);

        // 파티원에게만 승계 요청 가능
        if (targetMember == null || targetMember.getRole() != PartyRole.MEMBER) {
            throw new PartyException(ErrorCode.PARTY_HOST_TRANSFER_INVALID_TARGET);
        }

        // 계속 파티 활동 중인 파티원에게만 승계 요청 가능
        if (targetMember.getStatus() != PartyMemberStatus.ACTIVE) {
            throw new PartyException(ErrorCode.PARTY_HOST_TRANSFER_INVALID_TARGET);
        }

        HostTransferRequest request = HostTransferRequest.builder()
                .partyId(partyId)
                .requesterUserId(requesterId)
                .targetUserId(targetUserId)
                .status(HostTransferStatus.REQUESTED)
                .requestedAt(java.time.LocalDateTime.now())
                .respondedAt(null)
                .completedAt(null)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();

        hostTransferRequestMapper.insertHostTransferRequest(request);

        partyHistoryService.saveHistory(
                partyId,
                targetMember.getId(),
                PartyHistoryEventType.HOST_TRANSFER_REQUESTED,
                "{\"requestId\":" + request.getId() + ",\"requesterUserId\":" + requesterId + ",\"targetUserId\":" + targetUserId + "}",
                requesterId
        );

        return toResponse(request);
    }

    @Transactional
    public void acceptTransfer(Long requestId, Long userId) {
        HostTransferRequest request = hostTransferRequestMapper.findByIdForUpdate(requestId);
        if (request == null) {
            throw new PartyException(ErrorCode.PARTY_HOST_TRANSFER_NOT_FOUND);
        }

        // 요청받은 상태일 때만 수락 가능
        if (request.getStatus() != HostTransferStatus.REQUESTED) {
            throw new PartyException(ErrorCode.PARTY_HOST_TRANSFER_NOT_FOUND);
        }

        // 요청 받은 대상만 수락 가능
        if (!request.getTargetUserId().equals(userId)) {
            throw new PartyException(ErrorCode.PARTY_HOST_TRANSFER_FORBIDDEN);
        }

        Party party = partyMapper.findByIdForUpdate(request.getPartyId());
        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        PartyMember oldHostMember = partyMemberMapper.findHostMemberByPartyIdForUpdate(party.getId());
        PartyMember newHostMember = partyMemberMapper.findByPartyIdAndUserIdForUpdate(party.getId(), userId);

        if (oldHostMember == null || newHostMember == null) {
            throw new PartyException(ErrorCode.PARTY_HOST_TRANSFER_INVALID_TARGET);
        }

        // 1. 기존 HOST -> MEMBER로 변경한 후
        partyMemberMapper.updateRole(oldHostMember.getId(), PartyRole.MEMBER);

        // 2. 승계 수락한 MEMBER -> HOST로 변경
        partyMemberMapper.updateRole(newHostMember.getId(), PartyRole.HOST);

        // 3. HOST ID를 승계 수락한 MEMBER ID로 변경
        partyMapper.updateHostUserId(party.getId(), userId);

        // 4. 기존 HOST는 승계 완료와 동시에 탈퇴 예약 상태로 전환
        partyMemberMapper.updateLeaveReserved(party.getId(), request.getRequesterUserId());

        // 5. 파티장 결원은 해소 -> 기존 HOST가 다음 주기 탈퇴 예정이므로 MEMBER 결원 노출
        partyMapper.updateVacancyType(party.getId(), VacancyType.MEMBER);

        // 6. 요청 완료 처리
        hostTransferRequestMapper.updateStatusWithCompletedAt(requestId, HostTransferStatus.COMPLETED);

        partyHistoryService.saveHistory(
                party.getId(),
                newHostMember.getId(),
                PartyHistoryEventType.HOST_TRANSFER_ACCEPTED,
                "{\"requestId\":" + requestId + ",\"newHostUserId\":" + userId + "}",
                userId
        );

        partyHistoryService.saveHistory(
                party.getId(),
                oldHostMember.getId(),
                PartyHistoryEventType.HOST_TRANSFER_COMPLETED,
                "{\"requestId\":" + requestId + ",\"oldHostUserId\":" + request.getRequesterUserId() + ",\"newHostUserId\":" + userId + "}",
                userId
        );
    }

    @Transactional
    public void rejectTransfer(Long requestId, Long userId) {
        HostTransferRequest request = hostTransferRequestMapper.findByIdForUpdate(requestId);
        if (request == null) {
            throw new PartyException(ErrorCode.PARTY_HOST_TRANSFER_NOT_FOUND);
        }

        // 요청받은 상태일 때만 거절 가능
        if (request.getStatus() != HostTransferStatus.REQUESTED) {
            throw new PartyException(ErrorCode.PARTY_HOST_TRANSFER_NOT_FOUND);
        }

        // 요청 받은 대상만 거절 가능
        if (!request.getTargetUserId().equals(userId)) {
            throw new PartyException(ErrorCode.PARTY_HOST_TRANSFER_FORBIDDEN);
        }

        hostTransferRequestMapper.updateStatusWithRespondedAt(requestId, HostTransferStatus.REJECTED);

        PartyMember targetMember = partyMemberMapper.findByPartyIdAndUserId(request.getPartyId(), userId);

        partyHistoryService.saveHistory(
                request.getPartyId(),
                targetMember == null ? null : targetMember.getId(),
                PartyHistoryEventType.HOST_TRANSFER_REJECTED,
                "{\"requestId\":" + requestId + ",\"targetUserId\":" + userId + "}",
                userId
        );
    }

    // 파티장이 승계 진행 상태 조회
    @Transactional(readOnly = true)
    public List<HostTransferResponse> getTransferRequests(Long partyId, Long requesterUserId) {
        Party party = partyMapper.findById(partyId);
        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        // 파티장만 승계 요청 현황 조회 가능
        if (!party.getHostUserId().equals(requesterUserId)) {
            throw new PartyException(ErrorCode.PARTY_HOST_ONLY);
        }

        return hostTransferRequestMapper.findByPartyId(partyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private HostTransferResponse toResponse(HostTransferRequest request) {
        return new HostTransferResponse(
                request.getId(),
                request.getPartyId(),
                request.getRequesterUserId(),
                request.getTargetUserId(),
                request.getStatus(),
                request.getRequestedAt(),
                request.getRespondedAt(),
                request.getCompletedAt()
        );
    }
}