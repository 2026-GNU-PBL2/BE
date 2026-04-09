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
    private final PartyProvisionMapper partyProvisionMapper;
    private final PartyProvisionMemberMapper partyProvisionMemberMapper;
    private final CryptoUtil cryptoUtil;

    // 파티 provision 전체 현황 조회
    public PartyProvisionDashboardResponse getProvisionDashboard(
            final Long userId,
            final Long partyId
    ) {
        validateProvisionMember(partyId, userId);

        final PartyProvision provision = partyProvisionMapper.findByPartyId(partyId);
        if (provision == null) {
            throw new PartyException(ErrorCode.NOT_FOUND);
        }

        final List<PartyProvisionMemberResponse> members =
                partyProvisionMemberMapper.findResponsesByPartyOperationId(provision.getId());

        final int totalMemberCount = members.size();

        final int activeMemberCount = (int) members.stream()
                .filter(member -> member.memberStatus().name().equals("ACTIVE"))
                .count();

        return new PartyProvisionDashboardResponse(
                provision.getId(),
                provision.getPartyId(),
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

    // 파티장이 provision 멤버 상태 목록 조회
    public List<PartyProvisionMemberResponse> getProvisionMembers(
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

        final PartyProvision provision = partyProvisionMapper.findByPartyId(partyId);

        if (provision == null) {
            throw new PartyException(ErrorCode.NOT_FOUND);
        }

        return partyProvisionMemberMapper.findResponsesByPartyOperationId(provision.getId());
    }

    // 파티 소속 여부 확인
    private void validateProvisionMember(final Long partyId, final Long userId) {
        final Integer count = partyProvisionMemberMapper.countByPartyIdAndUserId(partyId, userId);

        if (count == null || count == 0) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_FORBIDDEN);
        }
    }

    // 공유계정 이메일 마스킹
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

    // 본인에게 필요한 provision 정보 조회
    public PartyProvisionMeResponse getMyProvisionInfo(
            final Long userId,
            final Long partyId
    ) {
        final PartyProvisionMember provisionMember =
                partyProvisionMemberMapper.findByPartyIdAndUserId(partyId, userId);

        // provision 대상 멤버만 조회 가능
        if (provisionMember == null) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_MEMBER_NOT_FOUND);
        }

        // 아직 안내를 볼 수 없는 상태는 차단
        validateReadableMemberStatus(provisionMember);

        final PartyProvision provision = partyProvisionMapper.findByPartyId(partyId);

        if (provision == null) {
            throw new PartyException(ErrorCode.PARTY_OPERATION_NOT_FOUND);
        }

        final String inviteValue =
                provision.getOperationType() == ProvisionType.INVITE_LINK
                        ? provision.getInviteValue()
                        : null;

        final String sharedAccountEmail =
                provision.getOperationType() == ProvisionType.ACCOUNT_SHARED
                        ? provision.getSharedAccountEmail()
                        : null;

        final String sharedAccountPassword =
                provision.getOperationType() == ProvisionType.ACCOUNT_SHARED
                        ? decryptSharedPassword(provision.getSharedAccountPasswordEncrypted())
                        : null;

        return new PartyProvisionMeResponse(
                provision.getId(),
                provision.getPartyId(),
                provision.getOperationType(),
                provision.getOperationStatus(),
                provisionMember.getMemberStatus(),
                inviteValue,
                sharedAccountEmail,
                sharedAccountPassword,
                provision.getOperationGuide(),
                provision.getOperationStartedAt(),
                provision.getOperationCompletedAt(),
                provision.getLastResetAt()
        );
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