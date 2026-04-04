package pbl2.sub119.backend.partyoperation.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.party.entity.PartyMember;
import pbl2.sub119.backend.party.mapper.PartyMemberMapper;
import pbl2.sub119.backend.partyoperation.entity.PartyOperation;
import pbl2.sub119.backend.partyoperation.entity.PartyOperationMember;
import pbl2.sub119.backend.partyoperation.enumerated.OperationMemberStatus;
import pbl2.sub119.backend.partyoperation.enumerated.OperationStatus;
import pbl2.sub119.backend.partyoperation.mapper.PartyOperationMapper;
import pbl2.sub119.backend.partyoperation.mapper.PartyOperationMemberMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyOperationInternalService {

    private final PartyOperationMapper partyOperationMapper;
    private final PartyOperationMemberMapper partyOperationMemberMapper;
    private final PartyMemberMapper partyMemberMapper;

    // 결제 완료 후 파티 운영 단계 진입할 때 1회 실행
    public void openOperation(final Long partyId) {
        final LocalDateTime now = LocalDateTime.now();
        final PartyOperation existing = partyOperationMapper.findByPartyId(partyId);

        // 이미 운영 생성되어 있으면 중복 생성 방지
        if (existing != null) {
            return;
        }

        final PartyOperation operation = PartyOperation.builder()
                .partyId(partyId)
                .operationStatus(OperationStatus.WAITING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        partyOperationMapper.insert(operation);

        // 현재 이용중인 파티원 기준으로 운영 멤버 생성
        final List<PartyMember> members = partyMemberMapper.findActiveMembersByPartyId(partyId);

        for (PartyMember member : members) {
            final PartyOperationMember operationMember = PartyOperationMember.builder()
                    .partyOperationId(operation.getId())
                    .partyMemberId(member.getId())
                    .partyId(partyId)
                    .userId(member.getUserId())
                    .memberStatus(OperationMemberStatus.WAITING)
                    .penaltyApplied(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            partyOperationMemberMapper.insert(operationMember);
        }
    }

    // 다음 결제 주기 시작 시 탈퇴/신규 멤버 반영 후 해당 메서드 호출 (운영 설정 리셋)
    public void syncOperationByCycle(final Long partyId) {
        final PartyOperation operation = partyOperationMapper.findByPartyId(partyId);

        // 운영 없으면 아무것도 안함
        if (operation == null) {
            return;
        }

        final LocalDateTime now = LocalDateTime.now();

        // 운영 상태 리셋
        partyOperationMapper.markResetRequired(
                operation.getId(),
                OperationStatus.RESET_REQUIRED,
                now,
                now
        );

        // 멤버 상태도 전부 리셋
        partyOperationMemberMapper.markAllResetRequired(
                operation.getId(),
                OperationMemberStatus.RESET_REQUIRED,
                "주기 변경으로 운영 재설정이 필요합니다.",
                now,
                now
        );
    }
}