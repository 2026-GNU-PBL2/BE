package pbl2.sub119.backend.partyoperation.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.party.mapper.PartyMapper;
import pbl2.sub119.backend.partyoperation.dto.response.PartyOperationDashboardResponse;
import pbl2.sub119.backend.partyoperation.dto.response.PartyOperationMemberResponse;
import pbl2.sub119.backend.partyoperation.entity.PartyOperation;
import pbl2.sub119.backend.partyoperation.mapper.PartyOperationMapper;
import pbl2.sub119.backend.partyoperation.mapper.PartyOperationMemberMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyOperationQueryService {

    private final PartyMapper partyMapper;
    private final PartyOperationMapper partyOperationMapper;
    private final PartyOperationMemberMapper partyOperationMemberMapper;

    // 파티 운영이 어디까지 진행됐는지 대시보드 조회
    public PartyOperationDashboardResponse getOperationDashboard(
            final Long userId,
            final Long partyId
    ) {
        validatePartyMember(partyId, userId);

        final PartyOperation operation = partyOperationMapper.findByPartyId(partyId);
        if (operation == null) {
            throw new PartyException(ErrorCode.NOT_FOUND);
        }

        final List<PartyOperationMemberResponse> members =
                partyOperationMemberMapper.findResponsesByPartyOperationId(operation.getId());

        final int totalMemberCount = members.size();

        final int activeMemberCount = (int) members.stream()
                .filter(member -> member.memberStatus().name().equals("ACTIVE"))
                .count();

        return new PartyOperationDashboardResponse(
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
    public List<PartyOperationMemberResponse> getOperationMembers(
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

        final PartyOperation operation = partyOperationMapper.findByPartyId(partyId);

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
}