package pbl2.sub119.backend.partyoperation.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.util.CryptoUtil;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.entity.PartyMember;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.party.mapper.PartyMapper;
import pbl2.sub119.backend.party.mapper.PartyMemberMapper;
import pbl2.sub119.backend.partyoperation.dto.request.PartyOperationResetRequest;
import pbl2.sub119.backend.partyoperation.dto.request.PartyOperationSetupRequest;
import pbl2.sub119.backend.partyoperation.dto.response.PartyOperationConfirmResponse;
import pbl2.sub119.backend.partyoperation.dto.response.PartyOperationSetupResponse;
import pbl2.sub119.backend.partyoperation.entity.PartyOperation;
import pbl2.sub119.backend.partyoperation.entity.PartyOperationMember;
import pbl2.sub119.backend.partyoperation.enumerated.OperationMemberStatus;
import pbl2.sub119.backend.partyoperation.enumerated.OperationStatus;
import pbl2.sub119.backend.partyoperation.enumerated.OperationType;
import pbl2.sub119.backend.partyoperation.mapper.PartyOperationMapper;
import pbl2.sub119.backend.partyoperation.mapper.PartyOperationMemberMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyOperationCommandService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyOperationMapper partyOperationMapper;
    private final PartyOperationMemberMapper partyOperationMemberMapper;
    private final CryptoUtil cryptoUtil;

    // 파티장이 운영 정보 최초 등록 또는 수정
    public PartyOperationSetupResponse setupOperation(
            final Long userId,
            final Long partyId,
            final PartyOperationSetupRequest request
    ) {
        final Party party = getParty(partyId);

        // 파티장만 운영 정보 등록 가능
        validateHost(userId, party);

        // 운영 방식별 필수값 검증
        validateSetupRequest(request);

        final LocalDateTime now = LocalDateTime.now();
        final PartyOperation existingOperation = partyOperationMapper.findByPartyId(partyId);

        final String encryptedPassword = encryptSharedPasswordIfNeeded(request);
        final String normalizedInviteValue =
                request.operationType() == OperationType.INVITE_LINK ? request.inviteValue() : null;
        final String normalizedSharedEmail =
                request.operationType() == OperationType.ACCOUNT_SHARED ? request.sharedAccountEmail() : null;

        // 최초 등록
        if (existingOperation == null) {
            final PartyOperation partyOperation = PartyOperation.builder()
                    .partyId(partyId)
                    .operationType(request.operationType())
                    .operationStatus(OperationStatus.IN_PROGRESS)
                    .inviteValue(normalizedInviteValue)
                    .sharedAccountEmail(normalizedSharedEmail)
                    .sharedAccountPasswordEncrypted(encryptedPassword)
                    .operationGuide(request.operationGuide())
                    .operationStartedAt(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            partyOperationMapper.insert(partyOperation);

            // 현재 운영 대상 멤버 기준으로 operation_member 초기화
            initializeMembers(partyOperation.getId(), partyId, party.getHostUserId(), now);

            return new PartyOperationSetupResponse(
                    partyOperation.getId(),
                    partyId,
                    partyOperation.getOperationType(),
                    partyOperation.getOperationStatus()
            );
        }

        // 수정
        partyOperationMapper.updateSetup(
                existingOperation.getId(),
                request.operationType(),
                normalizedInviteValue,
                normalizedSharedEmail,
                encryptedPassword,
                request.operationGuide(),
                OperationStatus.IN_PROGRESS,
                now,
                now
        );

        // 운영 정보 수정 시 멤버 상태 재초기화
        resetMemberRows(existingOperation.getId(), partyId, party.getHostUserId(), now);

        return new PartyOperationSetupResponse(
                existingOperation.getId(),
                partyId,
                request.operationType(),
                OperationStatus.IN_PROGRESS
        );
    }

    // 파티원이 운영 완료를 확인하면 멤버 상태 및 파티 전체 운영 상태 갱신
    public PartyOperationConfirmResponse confirmOperation(
            final Long userId,
            final Long partyId
    ) {
        final PartyOperation operation = getOperation(partyId);

        if (operation.getOperationStatus() == OperationStatus.ACTIVE) {
                    throw new PartyException(ErrorCode.PARTY_OPERATION_ALREADY_ACTIVE);
                }

        if (operation.getOperationStatus() == OperationStatus.RESET_REQUIRED) {
                  throw new PartyException(ErrorCode.PARTY_OPERATION_RESET_REQUIRED);
        }

        final PartyOperationMember member = getOperationMember(partyId, userId);
        final LocalDateTime now = LocalDateTime.now();

        partyOperationMemberMapper.markActive(
                member.getId(),
                OperationMemberStatus.ACTIVE,
                now,
                now,
                now,
                now
        );

        // 전원 완료 여부에 따라 파티 운영 상태 갱신
        refreshOperationStatus(operation.getId(), now);

        final PartyOperationMember updated = partyOperationMemberMapper.findById(member.getId());

        return new PartyOperationConfirmResponse(
                partyId,
                userId,
                updated.getMemberStatus(),
                updated.getConfirmedAt(),
                updated.getActivatedAt()
        );
    }

    // 파티장이 운영 정보를 재설정 상태로 변경
    public void resetOperation(
            final Long userId,
            final Long partyId,
            final PartyOperationResetRequest request
    ) {
        final Party party = getParty(partyId);

        // 파티장만 재설정 가능
        validateHost(userId, party);

        final PartyOperation operation = getOperation(partyId);
        final LocalDateTime now = LocalDateTime.now();

        partyOperationMapper.markResetRequired(
                operation.getId(),
                OperationStatus.RESET_REQUIRED,
                now,
                now
        );

        partyOperationMemberMapper.markAllResetRequired(
                operation.getId(),
                OperationMemberStatus.RESET_REQUIRED,
                request.operationMessage(),
                now,
                now
        );
    }

    // 현재 운영 대상 멤버들로 operation_member 초기 생성
    private void initializeMembers(
            final Long partyOperationId,
            final Long partyId,
            final Long hostUserId,
            final LocalDateTime now
    ) {
        final List<PartyMember> members = partyMemberMapper.findOperationTargetMembersByPartyId(partyId);

        for (PartyMember member : members) {
            final boolean isHost = member.getUserId().equals(hostUserId);

            final PartyOperationMember operationMember = PartyOperationMember.builder()
                    .partyOperationId(partyOperationId)
                    .partyMemberId(member.getId())
                    .partyId(partyId)
                    .userId(member.getUserId())
                    .memberStatus(isHost ? OperationMemberStatus.ACTIVE : OperationMemberStatus.REQUIRED)
                    .inviteSentAt(now)
                    .mustCompleteBy(now.plusHours(24))
                    .confirmedAt(isHost ? now : null)
                    .completedAt(isHost ? now : null)
                    .activatedAt(isHost ? now : null)
                    .lastResetAt(null)
                    .penaltyApplied(false)
                    .operationMessage(isHost ? "파티장" : null)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            partyOperationMemberMapper.insert(operationMember);
        }
    }

    // 운영 정보 수정 시 기존 멤버 상태 삭제 후 다시 생성
    private void resetMemberRows(
            final Long partyOperationId,
            final Long partyId,
            final Long hostUserId,
            final LocalDateTime now
    ) {
        partyOperationMemberMapper.deleteByPartyOperationId(partyOperationId);
        initializeMembers(partyOperationId, partyId, hostUserId, now);
    }

    // 전원 완료 여부에 따라 operation_status 갱신
    private void refreshOperationStatus(
            final Long partyOperationId,
            final LocalDateTime now
    ) {
        final int totalCount = partyOperationMemberMapper.countByPartyOperationId(partyOperationId);
        final int activeCount = partyOperationMemberMapper.countActiveByPartyOperationId(partyOperationId);

        if (totalCount > 0 && totalCount == activeCount) {
            partyOperationMapper.updateStatusAndCompletedAt(
                    partyOperationId,
                    OperationStatus.ACTIVE,
                    now,
                    now
            );
            return;
        }

        partyOperationMapper.updateStatus(
                partyOperationId,
                OperationStatus.IN_PROGRESS,
                now
        );
    }

    // 파티 조회
    private Party getParty(final Long partyId) {
        final Party party = partyMapper.findById(partyId);

        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        return party;
    }

    // 파티 운영 정보 조회
    private PartyOperation getOperation(final Long partyId) {
        final PartyOperation operation = partyOperationMapper.findByPartyId(partyId);

        if (operation == null) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_NOT_FOUND);
        }

        return operation;
    }

    // 현재 유저 운영 멤버 정보 조회
    private PartyOperationMember getOperationMember(final Long partyId, final Long userId) {
        final PartyOperationMember operationMember =
                partyOperationMemberMapper.findByPartyIdAndUserId(partyId, userId);

        if (operationMember == null) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_MEMBER_NOT_FOUND);
        }

        return operationMember;
    }

    // 파티장 권한 체크
    private void validateHost(final Long userId, final Party party) {
        if (!party.getHostUserId().equals(userId)) {
            throw new PartyException(ErrorCode.PARTY_HOST_ONLY);
        }
    }

    // 운영 방식별 필수값 검증
    private void validateSetupRequest(final PartyOperationSetupRequest request) {
        if (request.operationType() == null) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_TYPE_REQUIRED);
        }

        if (request.operationType() == OperationType.INVITE_LINK) {
            if (request.inviteValue() == null || request.inviteValue().isBlank()) {
                throw new PartyException(ErrorCode.PARTY_OPERATION_INVITE_VALUE_REQUIRED);
            }
        }

        if (request.operationType() == OperationType.ACCOUNT_SHARED) {
            if (request.sharedAccountEmail() == null || request.sharedAccountEmail().isBlank()) {
                throw new PartyException(ErrorCode.PARTY_OPERATION_SHARED_EMAIL_REQUIRED);
            }

            if (request.sharedAccountPassword() == null || request.sharedAccountPassword().isBlank()) {
                throw new PartyException(ErrorCode.PARTY_OPERATION_SHARED_PASSWORD_REQUIRED);
            }
        }
    }

    // 계정공유형일 때만 비밀번호 암호화
    private String encryptSharedPasswordIfNeeded(final PartyOperationSetupRequest request) {
        if (request.operationType() != OperationType.ACCOUNT_SHARED) {
            return null;
        }

        return cryptoUtil.encrypt(request.sharedAccountPassword());
    }
}