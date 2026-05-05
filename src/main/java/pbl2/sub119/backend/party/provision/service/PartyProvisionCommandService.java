package pbl2.sub119.backend.party.provision.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pbl2.sub119.backend.notification.event.event.AccountSharedCredentialRequiredEvent;
import pbl2.sub119.backend.notification.event.event.InviteLinkRequiredEvent;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.event.PartyProvisionSetupCompletedEvent;
import pbl2.sub119.backend.common.util.CryptoUtil;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.exception.PartyException;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;
import pbl2.sub119.backend.party.provision.dto.request.PartyProvisionResetRequest;
import pbl2.sub119.backend.party.provision.dto.request.PartyProvisionSetupRequest;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionConfirmResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionSetupResponse;
import pbl2.sub119.backend.party.provision.entity.PartyProvision;
import pbl2.sub119.backend.party.provision.entity.PartyProvisionMember;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionMemberStatus;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionStatus;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMapper;
import pbl2.sub119.backend.subproduct.enumerated.OperationType;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMemberMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyProvisionCommandService {

    private static final String CYCLE_MEMBER_CHANGED_MESSAGE =
            "결제일 기준 멤버 변경이 반영되어 provision 재설정이 필요합니다.";

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyProvisionMapper partyProvisionMapper;
    private final PartyProvisionMemberMapper partyProvisionMemberMapper;
    private final CryptoUtil cryptoUtil;
    private final ApplicationEventPublisher eventPublisher;

    // 파티장이 provision 정보 최초 등록 또는 다시 저장
    public PartyProvisionSetupResponse setupProvision(
            final Long userId,
            final Long partyId,
            final PartyProvisionSetupRequest request
    ) {
        final Party party = getParty(partyId);

        // 파티장만 등록 가능
        validateHost(userId, party);

        // provision 방식별 필수값 검증
        validateSetupRequest(request);

        final LocalDateTime now = LocalDateTime.now();
        final PartyProvision existingProvision = partyProvisionMapper.findByPartyId(partyId);

        final String encryptedPassword = encryptSharedPasswordIfNeeded(request);
        final String normalizedInviteValue =
                request.provisionType() == OperationType.INVITE_CODE ? request.inviteValue() : null;
        final String normalizedSharedEmail =
                request.provisionType() == OperationType.ACCOUNT_SHARE ? request.sharedAccountEmail() : null;
        final String normalizedGuide = request.provisionGuide();

        // 최초 등록
        if (existingProvision == null) {
            final PartyProvision provision = PartyProvision.builder()
                    .partyId(partyId)
                    .operationType(request.provisionType())
                    .operationStatus(ProvisionStatus.IN_PROGRESS)
                    .inviteValue(normalizedInviteValue)
                    .sharedAccountEmail(normalizedSharedEmail)
                    .sharedAccountPasswordEncrypted(encryptedPassword)
                    .operationGuide(normalizedGuide)
                    .operationStartedAt(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            partyProvisionMapper.insert(provision);

            // 현재 provision 대상 멤버 초기화
            initializeMembers(provision.getId(), partyId, party.getHostUserId(), now);

            publishProvisionRequiredEvent(partyId, provision.getId(), party.getHostUserId(), request.provisionType());
            publishProvisionSetupCompletedEvent(partyId);

            return new PartyProvisionSetupResponse(
                    provision.getId(),
                    partyId,
                    provision.getOperationType(),
                    provision.getOperationStatus()
            );
        }

        // 수정 또는 비밀번호 변경도 같은 API로 처리
        partyProvisionMapper.updateSetup(
                existingProvision.getId(),
                request.provisionType(),
                normalizedInviteValue,
                normalizedSharedEmail,
                encryptedPassword,
                normalizedGuide,
                ProvisionStatus.IN_PROGRESS,
                now,
                now
        );

        // 다시 저장하면 기존 멤버는 처음부터 다시 확인
        resetMemberRows(existingProvision.getId(), partyId, party.getHostUserId(), now);
        publishProvisionSetupCompletedEvent(partyId);

        return new PartyProvisionSetupResponse(
                existingProvision.getId(),
                partyId,
                request.provisionType(),
                ProvisionStatus.IN_PROGRESS
        );
    }

    // 결제일 기준 멤버 변경 후 provision 상태 재반영
    public void handleCycleStart(final Long partyId) {
        final Party party = getParty(partyId);
        final PartyProvision provision = partyProvisionMapper.findByPartyIdForUpdate(partyId);

        // 아직 provision 등록 전이면 후속 처리 안 함
        if (provision == null) {
            return;
        }

        final List<PartyMember> currentTargetMembers =
                partyMemberMapper.findProvisionTargetMembersByPartyId(partyId);

        final List<PartyProvisionMember> existingProvisionMembers =
                partyProvisionMemberMapper.findByPartyOperationId(provision.getId());

        final boolean memberChanged = hasProvisionTargetChanged(currentTargetMembers, existingProvisionMembers);

        // 멤버 변경 없으면 그대로 유지
        if (!memberChanged) {
            return;
        }

        final LocalDateTime now = LocalDateTime.now();

        partyProvisionMapper.updateCycleStartResetState(
                provision.getId(),
                ProvisionStatus.RESET_REQUIRED,
                provision.getOperationStartedAt(),
                now,
                now
        );

        // 현재 호스트가 기존 provision 멤버였던 경우에만 ACTIVE 유지
        final boolean keepCurrentHostActive = existingProvisionMembers.stream()
                .anyMatch(member -> member.getUserId().equals(party.getHostUserId()));

        resetMemberRows(provision.getId(), partyId, party.getHostUserId(), now);

        partyProvisionMemberMapper.markAllResetRequired(
                provision.getId(),
                ProvisionMemberStatus.RESET_REQUIRED,
                CYCLE_MEMBER_CHANGED_MESSAGE,
                now,
                now
        );

        if (keepCurrentHostActive) {
            restoreHostActive(partyId, party.getHostUserId(), now);
        }
    }

    // 파티원이 provision 완료 확인
    public PartyProvisionConfirmResponse confirmProvision(
            final Long userId,
            final Long partyId
    ) {
        final PartyProvision provision = getProvisionForUpdate(partyId);

        // 파티장이 재설정 전이면 먼저 파티장이 새 정보 저장해야 함
        if (provision.getOperationStatus() == ProvisionStatus.RESET_REQUIRED) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_RESET_REQUIRED);
        }

        final PartyProvisionMember provisionMember = getProvisionMember(partyId, userId);

        // 이미 완료된 멤버는 그대로 반환
        if (provisionMember.getMemberStatus() == ProvisionMemberStatus.ACTIVE) {
            return new PartyProvisionConfirmResponse(
                    partyId,
                    userId,
                    provisionMember.getMemberStatus(),
                    provisionMember.getConfirmedAt(),
                    provisionMember.getActivatedAt()
            );
        }

        final LocalDateTime now = LocalDateTime.now();

        partyProvisionMemberMapper.markActive(
                provisionMember.getId(),
                ProvisionMemberStatus.ACTIVE,
                now,
                now,
                now,
                now
        );

        // 전원 완료 여부에 따라 provision 상태 갱신
        refreshProvisionStatus(provision.getId(), now);

        final PartyProvisionMember updated = partyProvisionMemberMapper.findById(provisionMember.getId());

        return new PartyProvisionConfirmResponse(
                partyId,
                userId,
                updated.getMemberStatus(),
                updated.getConfirmedAt(),
                updated.getActivatedAt()
        );
    }

    // 파티장이 provision 재설정
    public void resetProvision(
            final Long userId,
            final Long partyId,
            final PartyProvisionResetRequest request
    ) {
        final Party party = getParty(partyId);

        // 파티장만 재설정 가능
        validateHost(userId, party);

        final PartyProvision provision = getProvision(partyId);
        final LocalDateTime now = LocalDateTime.now();

        partyProvisionMapper.markResetRequired(
                provision.getId(),
                ProvisionStatus.RESET_REQUIRED,
                now,
                now
        );

        // 일괄 RESET_REQUIRED 후 현재 호스트만 ACTIVE 복원
        partyProvisionMemberMapper.markAllResetRequired(
                provision.getId(),
                ProvisionMemberStatus.RESET_REQUIRED,
                request.provisionMessage(),
                now,
                now
        );

        restoreHostActive(partyId, party.getHostUserId(), now);
    }

    // 현재 provision 대상 멤버 초기 생성
    private void initializeMembers(
            final Long provisionId,
            final Long partyId,
            final Long hostUserId,
            final LocalDateTime now
    ) {
        final List<PartyMember> members = partyMemberMapper.findProvisionTargetMembersByPartyId(partyId);

        for (PartyMember member : members) {
            final boolean isHost = member.getUserId().equals(hostUserId);

            final PartyProvisionMember provisionMember = PartyProvisionMember.builder()
                    .partyOperationId(provisionId)
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

            partyProvisionMemberMapper.insert(provisionMember);
        }
    }

    // provision 정보 다시 저장할 때 멤버 상태 초기화
    private void resetMemberRows(
            final Long provisionId,
            final Long partyId,
            final Long hostUserId,
            final LocalDateTime now
    ) {
        partyProvisionMemberMapper.deleteByPartyOperationId(provisionId);
        initializeMembers(provisionId, partyId, hostUserId, now);
    }

    // 현재 호스트를 ACTIVE 상태로 복원
    private void restoreHostActive(
            final Long partyId,
            final Long hostUserId,
            final LocalDateTime now
    ) {
        final PartyProvisionMember hostProvisionMember =
                partyProvisionMemberMapper.findByPartyIdAndUserId(partyId, hostUserId);

        if (hostProvisionMember == null) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_MEMBER_NOT_FOUND);
        }

        partyProvisionMemberMapper.markActive(
                hostProvisionMember.getId(),
                ProvisionMemberStatus.ACTIVE,
                now,
                now,
                now,
                now
        );
    }

    // 전원 완료 여부에 따라 provision 상태 갱신
    private void refreshProvisionStatus(
            final Long provisionId,
            final LocalDateTime now
    ) {
        final int totalCount = partyProvisionMemberMapper.countByPartyOperationId(provisionId);
        final int activeCount = partyProvisionMemberMapper.countActiveByPartyOperationId(provisionId);

        if (totalCount > 0 && totalCount == activeCount) {
            partyProvisionMapper.updateStatusAndCompletedAt(
                    provisionId,
                    ProvisionStatus.ACTIVE,
                    now,
                    now
            );
            return;
        }

        partyProvisionMapper.updateStatusIfNotActive(
                provisionId,
                ProvisionStatus.IN_PROGRESS,
                now
        );
    }

    // 현재 provision 대상 멤버 구성이 바뀌었는지 확인
    private boolean hasProvisionTargetChanged(
            final List<PartyMember> currentTargetMembers,
            final List<PartyProvisionMember> existingProvisionMembers
    ) {
        if (currentTargetMembers.size() != existingProvisionMembers.size()) {
            return true;
        }

        final Set<Long> currentPartyMemberIds = currentTargetMembers.stream()
                .map(PartyMember::getId)
                .collect(Collectors.toSet());

        final Set<Long> existingPartyMemberIds = existingProvisionMembers.stream()
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

    // provision 정보 조회
    private PartyProvision getProvision(final Long partyId) {
        final PartyProvision provision = partyProvisionMapper.findByPartyId(partyId);

        if (provision == null) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_NOT_FOUND);
        }

        return provision;
    }

    // 상태 반영 중 동시 수정 방지용 provision 조회
    private PartyProvision getProvisionForUpdate(final Long partyId) {
        final PartyProvision provision = partyProvisionMapper.findByPartyIdForUpdate(partyId);

        if (provision == null) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_NOT_FOUND);
        }

        return provision;
    }

    // 현재 유저 provision 정보 조회
    private PartyProvisionMember getProvisionMember(final Long partyId, final Long userId) {
        final PartyProvisionMember provisionMember =
                partyProvisionMemberMapper.findByPartyIdAndUserId(partyId, userId);

        if (provisionMember == null) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_MEMBER_NOT_FOUND);
        }

        return provisionMember;
    }

    // 파티장 권한 체크
    private void validateHost(final Long userId, final Party party) {
        if (!party.getHostUserId().equals(userId)) {
            throw new PartyException(ErrorCode.PARTY_HOST_ONLY);
        }
    }

    // provision 방식별 필수값 검증
    private void validateSetupRequest(final PartyProvisionSetupRequest request) {
        if (request.provisionType() == null) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_TYPE_REQUIRED);
        }

        if (request.provisionType() == OperationType.INVITE_CODE) {
            if (request.inviteValue() == null || request.inviteValue().isBlank()) {
                throw new PartyException(ErrorCode.PARTY_OPERATION_INVITE_VALUE_REQUIRED);
            }
        }

        if (request.provisionType() == OperationType.ACCOUNT_SHARE) {
            if (request.sharedAccountEmail() == null || request.sharedAccountEmail().isBlank()) {
                throw new PartyException(ErrorCode.PARTY_OPERATION_SHARED_EMAIL_REQUIRED);
            }

            if (request.sharedAccountPassword() == null || request.sharedAccountPassword().isBlank()) {
                throw new PartyException(ErrorCode.PARTY_OPERATION_SHARED_PASSWORD_REQUIRED);
            }
        }
    }

    // 공유계정형일 때만 비밀번호 암호화
    private String encryptSharedPasswordIfNeeded(final PartyProvisionSetupRequest request) {
        if (request.provisionType() != OperationType.ACCOUNT_SHARE) {
            return null;
        }

        return cryptoUtil.encrypt(request.sharedAccountPassword());
    }

    // provision 설정 완료 이벤트 발행 - 리스너가 @TransactionalEventListener(AFTER_COMMIT)이므로 트랜잭션 커밋 후 실행 보장
    private void publishProvisionSetupCompletedEvent(final Long partyId) {
        eventPublisher.publishEvent(new PartyProvisionSetupCompletedEvent(partyId));
    }

    // provision 최초 등록 후 파티원에게 알림 이벤트 발행
    private void publishProvisionRequiredEvent(
            final Long partyId,
            final Long provisionId,
            final Long hostUserId,
            final OperationType provisionType
    ) {
        final List<Long> memberUserIds = partyMemberMapper
                .findProvisionTargetMembersByPartyId(partyId)
                .stream()
                .filter(m -> !m.getUserId().equals(hostUserId))
                .map(PartyMember::getUserId)
                .toList();

        if (memberUserIds.isEmpty()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (provisionType == OperationType.ACCOUNT_SHARE) {
                    eventPublisher.publishEvent(
                            new AccountSharedCredentialRequiredEvent(partyId, provisionId, memberUserIds));
                } else if (provisionType == OperationType.INVITE_CODE) {
                    eventPublisher.publishEvent(
                            new InviteLinkRequiredEvent(partyId, provisionId, memberUserIds));
                }
            }
        });
    }
}