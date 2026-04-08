package pbl2.sub119.backend.party.provision.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.util.CryptoUtil;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.party.mapper.PartyMapper;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionDashboardResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionMeResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionMemberResponse;
import pbl2.sub119.backend.party.provision.entity.PartyProvision;
import pbl2.sub119.backend.party.provision.entity.PartyProvisionMember;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionMemberStatus;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionType;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMapper;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMemberMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyProvisionQueryService {

    private final PartyMapper partyMapper;
    private final PartyProvisionMapper partyOperationMapper;
    private final PartyProvisionMemberMapper partyOperationMemberMapper;
    private final CryptoUtil cryptoUtil;

    // 파티 운영이 어디까지 진행됐는지 대시보드 조회
    public PartyProvisionDashboardResponse getOperationDashboard(
            final Long userId,
            final Long partyId
    ) {
        validatePartyMember(partyId, userId);

        final PartyProvision operation = partyOperationMapper.findByPartyId(partyId);
        if (operation == null) {
            throw new PartyException(ErrorCode.NOT_FOUND);
        }

        final List<PartyProvisionMemberResponse> members =
                partyOperationMemberMapper.findResponsesByPartyOperationId(operation.getId());

        final int totalMemberCount = members.size();

        final int activeMemberCount = (int) members.stream()
                .filter(member -> member.memberStatus().name().equals("ACTIVE"))
                .count();

        return new PartyProvisionDashboardResponse(
                operation.getId(),
                operation.getPartyId(),
                operation.getOperationType(),
                operation.getOperationStatus(),
                operation.getInviteValue(),
                maskEmail(operation.getSharedAccountEmail()),
                operation.getOperationGuide(),
                totalMemberCount,
                activeMemberCount,
                operation.getOperationStartedAt(),
                operation.getOperationCompletedAt(),
                operation.getLastResetAt(),
                members
        );
    }

    // 파티장이 전체 멤버 상태 확인
    public List<PartyProvisionMemberResponse> getOperationMembers(
            final Long userId,
            final Long partyId
    ) {
        final Party party = partyMapper.findById(partyId);

        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        // 파티장만 조회 가능
        if (!party.getHostUserId().equals(userId)) {
            throw new PartyException(ErrorCode.PARTY_HOST_ONLY);
        }

        final PartyProvision operation = partyOperationMapper.findByPartyId(partyId);

        if (operation == null) {
            throw new PartyException(ErrorCode.NOT_FOUND);
        }

        return partyOperationMemberMapper.findResponsesByPartyOperationId(operation.getId());
    }

    // 파티 소속 여부 검증
    private void validatePartyMember(final Long partyId, final Long userId) {
        final Integer count = partyOperationMemberMapper.countByPartyIdAndUserId(partyId, userId);

        if (count == null || count == 0) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_FORBIDDEN);
        }
    }

    // 계정 이메일 마스킹
    private String maskEmail(final String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        final int atIndex = email.indexOf("@");

        if (atIndex <= 2) {
            return "***" + email.substring(atIndex);
        }

        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }

    // 본인이 포함된 파티 운영 상태 조회
    public PartyProvisionMeResponse getMyOperationInfo(
            final Long userId,
            final Long partyId
    ) {
        final PartyProvisionMember operationMember =
                partyOperationMemberMapper.findByPartyIdAndUserId(partyId, userId);

        // 파티 운영 멤버만 조회 가능
        if (operationMember == null) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_MEMBER_NOT_FOUND);
        }

        // 파티 상태가 WAITING이면 운영 정보 조회 불가능
        validateReadableMemberStatus(operationMember);

        // 운영 정보 조회
        final PartyProvision operation = partyOperationMapper.findByPartyId(partyId);

        if (operation == null) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_NOT_FOUND);
        }

        // 계정 링크
        final String inviteValue =
                operation.getOperationType() == ProvisionType.INVITE_LINK
                        ? operation.getInviteValue()
                        : null;

        // 공유 계정 이메일
        final String sharedAccountEmail =
                operation.getOperationType() == ProvisionType.ACCOUNT_SHARED
                        ? operation.getSharedAccountEmail()
                        : null;

        // 공유 계정 비밀번호
        final String sharedAccountPassword =
                operation.getOperationType() == ProvisionType.ACCOUNT_SHARED
                        ? decryptSharedPassword(operation.getSharedAccountPasswordEncrypted())
                        : null;

        return new PartyProvisionMeResponse(
                operation.getId(),
                operation.getPartyId(),
                operation.getOperationType(),
                operation.getOperationStatus(),
                operationMember.getMemberStatus(),
                inviteValue,
                sharedAccountEmail,
                sharedAccountPassword,
                operation.getOperationGuide(),
                operation.getOperationStartedAt(),
                operation.getOperationCompletedAt(),
                operation.getLastResetAt()
        );
    }

    // 상태 유효성 검사
    private void validateReadableMemberStatus(final PartyProvisionMember operationMember) {
        if (operationMember.getMemberStatus() == ProvisionMemberStatus.WAITING) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_NOT_READABLE);
        }
    }

    // 복호화
    private String decryptSharedPassword(final String encryptedPassword) {
        if (encryptedPassword == null || encryptedPassword.isBlank()) {
            return null;
        }

        try {
            return cryptoUtil.decrypt(encryptedPassword);
        } catch (RuntimeException exception) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_PASSWORD_DECRYPT_FAILED);
        }
    }
}