package pbl2.sub119.backend.party.provision.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
import pbl2.sub119.backend.party.provision.dto.request.PartyProvisionResetRequest;
import pbl2.sub119.backend.party.provision.dto.request.PartyProvisionSetupRequest;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionConfirmResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionSetupResponse;
import pbl2.sub119.backend.party.provision.entity.PartyProvision;
import pbl2.sub119.backend.party.provision.entity.PartyProvisionMember;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionMemberStatus;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionStatus;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionType;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMapper;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMemberMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyProvisionCommandService {

    private static final String CYCLE_MEMBER_CHANGED_MESSAGE =
            "결제일 기준 멤버 변경이 반영되어 운영 재설정이 필요합니다.";

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyProvisionMapper partyOperationMapper;
    private final PartyProvisionMemberMapper partyOperationMemberMapper;
    private final CryptoUtil cryptoUtil;

    // 파티장이 운영 정보 최초 등록 또는 수정
    public PartyProvisionSetupResponse setupOperation(
            final Long userId,
            final Long partyId,
            final PartyProvisionSetupRequest request
    ) {
        final Party party = getParty(partyId);

        // 파티장만 운영 정보 등록 가능
        validateHost(userId, party);

        // 운영 방식별 필수값 검증
        validateSetupRequest(request);

        final LocalDateTime now = LocalDateTime.now();
        final PartyProvision existingOperation = partyOperationMapper.findByPartyId(partyId);

        final String encryptedPassword = encryptSharedPasswordIfNeeded(request);
        final String normalizedInviteValue =
                request.operationType() == ProvisionType.INVITE_LINK ? request.inviteValue() : null;
        final String normalizedSharedEmail =
                request.operationType() == ProvisionType.ACCOUNT_SHARED ? request.sharedAccountEmail() : null;

        // 최초 등록
        if (existingOperation == null) {
            final PartyProvision partyOperation = PartyProvision.builder()
                    .partyId(partyId)
                    .operationType(request.operationType())
                    .operationStatus(ProvisionStatus.IN_PROGRESS)
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

            return new PartyProvisionSetupResponse(
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
                ProvisionStatus.IN_PROGRESS,
                now,
                now
        );

        // 운영 정보 수정 시 멤버 상태 재초기화
        resetMemberRows(existingOperation.getId(), partyId, party.getHostUserId(), now);

        return new PartyProvisionSetupResponse(
                existingOperation.getId(),
                partyId,
                request.operationType(),
                ProvisionStatus.IN_PROGRESS
        );
    }

    // cycle start 이후 운영 자동 상태 반영
    public void handleCycleStart(final Long partyId) {
        final Party party = getParty(partyId);
        final PartyProvision operation = partyOperationMapper.findByPartyIdForUpdate(partyId);

        // 아직 운영 정보가 등록되지 않은 파티는 후속 처리하지 않음
        if (operation == null) {
            return;
        }


        final List<PartyMember> currentTargetMembers =
                partyMemberMapper.findProvisionTargetMembersByPartyId(partyId);

        final List<PartyProvisionMember> existingOperationMembers =
                partyOperationMemberMapper.findByPartyOperationId(operation.getId());

        final boolean memberChanged = hasOperationTargetChanged(currentTargetMembers, existingOperationMembers);

        // 멤버 변경이 없으면 기존 운영 상태 유지
        if (!memberChanged) {
            return;
        }

        final LocalDateTime now = LocalDateTime.now();

        partyOperationMapper.updateCycleStartResetState(
                operation.getId(),
                ProvisionStatus.RESET_REQUIRED,
                operation.getOperationStartedAt(),
                now,
                now
        );

        resetMemberRows(operation.getId(), partyId, party.getHostUserId(), now);

        partyOperationMemberMapper.markAllResetRequired(
                operation.getId(),
                ProvisionMemberStatus.RESET_REQUIRED,
                CYCLE_MEMBER_CHANGED_MESSAGE,
                now,
                now
        );
    }

    // 파티원이 운영 완료를 확인하면 멤버 상태 및 파티 전체 운영 상태 갱신
    public PartyProvisionConfirmResponse confirmOperation(
            final Long userId,
            final Long partyId
    ) {
        final PartyProvision operation = getOperationForUpdate(partyId);

        if (operation.getOperationStatus() == ProvisionStatus.ACTIVE) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_ALREADY_ACTIVE);
        }

        if (operation.getOperationStatus() == ProvisionStatus.RESET_REQUIRED) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_RESET_REQUIRED);
        }

        final PartyProvisionMember member = getOperationMember(partyId, userId);

        if (member.getMemberStatus() == ProvisionMemberStatus.ACTIVE) {
            return new PartyProvisionConfirmResponse(
                    partyId,
                    userId,
                    member.getMemberStatus(),
                    member.getConfirmedAt(),
                    member.getActivatedAt()
            );
        }

        final LocalDateTime now = LocalDateTime.now();

        partyOperationMemberMapper.markActive(
                member.getId(),
                ProvisionMemberStatus.ACTIVE,
                now,
                now,
                now,
                now
        );

        // 전원 완료 여부에 따라 파티 운영 상태 갱신
        refreshOperationStatus(operation.getId(), now);

        final PartyProvisionMember updated = partyOperationMemberMapper.findById(member.getId());

        return new PartyProvisionConfirmResponse(
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
            final PartyProvisionResetRequest request
    ) {
        final Party party = getParty(partyId);

        // 파티장만 재설정 가능
        validateHost(userId, party);

        final PartyProvision operation = getOperation(partyId);
        final LocalDateTime now = LocalDateTime.now();

        partyOperationMapper.markResetRequired(
                operation.getId(),
                ProvisionStatus.RESET_REQUIRED,
                now,
                now
        );

        partyOperationMemberMapper.markAllResetRequired(
                operation.getId(),
                ProvisionMemberStatus.RESET_REQUIRED,
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
        final List<PartyMember> members = partyMemberMapper.findProvisionTargetMembersByPartyId(partyId);

        for (PartyMember member : members) {
            final boolean isHost = member.getUserId().equals(hostUserId);

            final PartyProvisionMember operationMember = PartyProvisionMember.builder()
                    .partyOperationId(partyOperationId)
                    .partyMemberId(member.getId())
                    .partyId(partyId)
                    .userId(member.getUserId())
                    .memberStatus(isHost ? ProvisionMemberStatus.ACTIVE : ProvisionMemberStatus.REQUIRED)
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
                    ProvisionStatus.ACTIVE,
                    now,
                    now
            );
            return;
        }

        partyOperationMapper.updateStatusIfNotActive(
                partyOperationId,
                ProvisionStatus.IN_PROGRESS,
                now
        );
    }

    private boolean hasOperationTargetChanged(
            final List<PartyMember> currentTargetMembers,
            final List<PartyProvisionMember> existingOperationMembers
    ) {
        if (currentTargetMembers.size() != existingOperationMembers.size()) {
            return true;
        }

        final Set<Long> currentPartyMemberIds = currentTargetMembers.stream()
                .map(PartyMember::getId)
                .collect(Collectors.toSet());

        final Set<Long> existingPartyMemberIds = existingOperationMembers.stream()
                .map(PartyProvisionMember::getPartyMemberId)
                .collect(Collectors.toSet());

        return !currentPartyMemberIds.equals(existingPartyMemberIds);
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
    private PartyProvision getOperation(final Long partyId) {
        final PartyProvision operation = partyOperationMapper.findByPartyId(partyId);

        if (operation == null) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_NOT_FOUND);
        }

        return operation;
    }

    // 운영 완료 처리 시 운영 상태 계산 중 다른 트랜잭션이 끼어들어 상태 덮어쓰는 문제 방지
    private PartyProvision getOperationForUpdate(final Long partyId) {
        final PartyProvision operation = partyOperationMapper.findByPartyIdForUpdate(partyId);

        if (operation == null) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_NOT_FOUND);
        }

        return operation;
    }

    // 현재 유저 운영 멤버 정보 조회
    private PartyProvisionMember getOperationMember(final Long partyId, final Long userId) {
        final PartyProvisionMember operationMember =
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
    private void validateSetupRequest(final PartyProvisionSetupRequest request) {
        if (request.operationType() == null) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_TYPE_REQUIRED);
        }

        if (request.operationType() == ProvisionType.INVITE_LINK) {
            if (request.inviteValue() == null || request.inviteValue().isBlank()) {
                throw new PartyException(ErrorCode.PARTY_OPERATION_INVITE_VALUE_REQUIRED);
            }
        }

        if (request.operationType() == ProvisionType.ACCOUNT_SHARED) {
            if (request.sharedAccountEmail() == null || request.sharedAccountEmail().isBlank()) {
                throw new PartyException(ErrorCode.PARTY_OPERATION_SHARED_EMAIL_REQUIRED);
            }

            if (request.sharedAccountPassword() == null || request.sharedAccountPassword().isBlank()) {
                throw new PartyException(ErrorCode.PARTY_OPERATION_SHARED_PASSWORD_REQUIRED);
            }
        }
    }

    // 계정공유형일 때만 비밀번호 암호화
    private String encryptSharedPasswordIfNeeded(final PartyProvisionSetupRequest request) {
        if (request.operationType() != ProvisionType.ACCOUNT_SHARED) {
            return null;
        }

        return cryptoUtil.encrypt(request.sharedAccountPassword());
    }
}