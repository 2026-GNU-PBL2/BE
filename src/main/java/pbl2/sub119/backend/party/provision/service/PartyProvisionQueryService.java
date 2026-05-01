package pbl2.sub119.backend.party.provision.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.util.CryptoUtil;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.exception.PartyException;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionDashboardResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionMeResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionMemberResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionPasswordRevealResponse;
import pbl2.sub119.backend.party.provision.entity.PartyProvision;
import pbl2.sub119.backend.party.provision.entity.PartyProvisionMember;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionMemberStatus;
import pbl2.sub119.backend.subproduct.enumerated.OperationType;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMapper;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMemberMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyProvisionQueryService {

    private final PartyMapper partyMapper;
    private final PartyProvisionMapper partyProvisionMapper;
    private final PartyProvisionMemberMapper partyProvisionMemberMapper;
    private final CryptoUtil cryptoUtil;

    // 파티장이 이용 전체 현황 조회
    public PartyProvisionDashboardResponse getProvisionDashboard(
            final Long userId,
            final Long partyId
    ) {
        final Party party = getHostParty(userId, partyId);
        final PartyProvision provision = getProvisionByPartyId(partyId);

        final List<PartyProvisionMemberResponse> members =
                partyProvisionMemberMapper.findResponsesByPartyOperationId(provision.getId());

        final int totalMemberCount = members.size();

        final int activeMemberCount = (int) members.stream()
                .filter(member -> member.memberStatus().name().equals("ACTIVE"))
                .count();

        return new PartyProvisionDashboardResponse(
                provision.getId(),
                party.getId(),
                provision.getOperationType(),
                provision.getOperationStatus(),
                provision.getInviteValue(),
                maskEmail(provision.getSharedAccountEmail()),
                provision.getOperationGuide(),
                totalMemberCount,
                activeMemberCount,
                provision.getOperationStartedAt(),
                provision.getOperationCompletedAt(),
                provision.getLastResetAt(),
                members
        );
    }

    // 파티장이 이용 멤버 상태 목록 조회
    public List<PartyProvisionMemberResponse> getProvisionMembers(
            final Long userId,
            final Long partyId
    ) {
        getHostParty(userId, partyId);

        final PartyProvision provision = getProvisionByPartyId(partyId);

        return partyProvisionMemberMapper.findResponsesByPartyOperationId(provision.getId());
    }

    // 본인에게 필요한 이용 정보 조회
    public PartyProvisionMeResponse getMyProvisionInfo(
            final Long userId,
            final Long partyId
    ) {
        final PartyProvision provision = getProvisionByPartyId(partyId);
        final PartyProvisionMember provisionMember = getReadableProvisionMember(userId, partyId);

        final String inviteValue =
                provision.getOperationType() == OperationType.INVITE_CODE
                        ? provision.getInviteValue()
                        : null;

        final String sharedAccountEmail =
                provision.getOperationType() == OperationType.ACCOUNT_SHARE
                        ? provision.getSharedAccountEmail()
                        : null;

        final String maskedSharedAccountPassword =
                provision.getOperationType() == OperationType.ACCOUNT_SHARE
                        ? maskPassword(decryptSharedPassword(provision.getSharedAccountPasswordEncrypted()))
                        : null;

        final boolean passwordRevealAvailable =
                provision.getOperationType() == OperationType.ACCOUNT_SHARE;

        return new PartyProvisionMeResponse(
                provision.getId(),
                provision.getPartyId(),
                provision.getOperationType(),
                provision.getOperationStatus(),
                provisionMember.getMemberStatus(),
                inviteValue,
                sharedAccountEmail,
                maskedSharedAccountPassword,
                passwordRevealAvailable,
                provision.getOperationGuide(),
                provision.getOperationStartedAt(),
                provision.getOperationCompletedAt(),
                provision.getLastResetAt()
        );
    }

    // 보기 버튼 클릭 시 평문 비밀번호 조회
    public PartyProvisionPasswordRevealResponse getMyProvisionPassword(
            final Long userId,
            final Long partyId
    ) {
        final PartyProvision provision = getProvisionByPartyId(partyId);
        getReadableProvisionMember(userId, partyId);

        if (provision.getOperationType() != OperationType.ACCOUNT_SHARE) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_NOT_READABLE);
        }

        return new PartyProvisionPasswordRevealResponse(
                decryptSharedPassword(provision.getSharedAccountPasswordEncrypted())
        );
    }

    // 파티 존재 + 파티장 권한 확인
    private Party getHostParty(
            final Long userId,
            final Long partyId
    ) {
        final Party party = partyMapper.findById(partyId);

        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        if (!party.getHostUserId().equals(userId)) {
            throw new PartyException(ErrorCode.PARTY_HOST_ONLY);
        }

        return party;
    }

    // provision 존재 확인
    private PartyProvision getProvisionByPartyId(final Long partyId) {
        final PartyProvision provision = partyProvisionMapper.findByPartyId(partyId);

        if (provision == null) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_NOT_FOUND);
        }

        return provision;
    }

    // 이용 대상 멤버 + readable 상태 검증
    private PartyProvisionMember getReadableProvisionMember(
            final Long userId,
            final Long partyId
    ) {
        final PartyProvisionMember provisionMember =
                partyProvisionMemberMapper.findByPartyIdAndUserId(partyId, userId);

        if (provisionMember == null) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_MEMBER_NOT_FOUND);
        }

        validateReadableMemberStatus(provisionMember);
        return provisionMember;
    }

    // 공유계정 이메일 마스킹
    private String maskEmail(final String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        final int atIndex = email.indexOf("@");

        if (atIndex < 0) {
            return "***";
        }

        if (atIndex <= 2) {
            return "***" + email.substring(atIndex);
        }

        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }

    // 공유계정 비밀번호 마스킹
    private String maskPassword(final String password) {
        if (password == null || password.isBlank()) {
            return null;
        }

        if (password.length() <= 2) {
            return "*".repeat(password.length());
        }

        if (password.length() <= 4) {
            return password.charAt(0) + "*".repeat(password.length() - 1);
        }

        return password.substring(0, 2)
                + "*".repeat(password.length() - 4)
                + password.substring(password.length() - 2);
    }

    // 안내를 볼 수 있는 상태인지 확인
    private void validateReadableMemberStatus(final PartyProvisionMember provisionMember) {
        if (provisionMember.getMemberStatus() == ProvisionMemberStatus.WAITING) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_NOT_READABLE);
        }
    }

    // 공유계정 비밀번호 복호화
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