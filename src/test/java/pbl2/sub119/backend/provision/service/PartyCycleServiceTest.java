package pbl2.sub119.backend.provision.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.entity.PartyMember;
import pbl2.sub119.backend.party.enumerated.OperationStatus;
import pbl2.sub119.backend.party.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.enumerated.PartyRole;
import pbl2.sub119.backend.party.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.enumerated.VacancyType;
import pbl2.sub119.backend.party.mapper.PartyMapper;
import pbl2.sub119.backend.party.mapper.PartyMemberMapper;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionStatus;
import pbl2.sub119.backend.party.service.PartyCycleService;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionMemberResponse;
import pbl2.sub119.backend.party.provision.entity.PartyProvision;
import pbl2.sub119.backend.party.provision.entity.PartyProvisionMember;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionMemberStatus;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionType;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMapper;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMemberMapper;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class PartyCycleServiceTest {

    @Autowired
    private PartyCycleService partyCycleService;

    @Autowired
    private PartyMapper partyMapper;

    @Autowired
    private PartyMemberMapper partyMemberMapper;

    @Autowired
    private PartyProvisionMapper partyOperationMapper;

    @Autowired
    private PartyProvisionMemberMapper partyOperationMemberMapper;

    @Test
    @DisplayName("cycle start 시 멤버 변경이 없으면 운영 상태를 그대로 유지한다")
    void handleCycleStartWithoutMemberChangeKeepsOperationState() {
        // given
        Long partyId = createParty(4, 4);

        createMember(partyId, 1L, PartyRole.HOST, PartyMemberStatus.ACTIVE);
        createMember(partyId, 2L, PartyRole.MEMBER, PartyMemberStatus.ACTIVE);
        createMember(partyId, 3L, PartyRole.MEMBER, PartyMemberStatus.ACTIVE);
        createMember(partyId, 4L, PartyRole.MEMBER, PartyMemberStatus.ACTIVE);

        Long operationId = createOperation(
                partyId,
                ProvisionStatus.ACTIVE
        );

        // 현재 운영 멤버와 실제 파티 멤버 구성이 동일한 상태
        createOperationMembersByUserIds(operationId, partyId, List.of(1L, 2L, 3L, 4L));

        // when
        partyCycleService.handleCycleStart(partyId);

        // then
        Party updatedParty = partyMapper.findById(partyId);

        // 멤버 변화가 없으므로 인원/모집/결원 상태 유지
        assertThat(updatedParty.getCurrentMemberCount()).isEqualTo(4);
        assertThat(updatedParty.getRecruitStatus()).isEqualTo(RecruitStatus.FULL);
        assertThat(updatedParty.getVacancyType()).isEqualTo(VacancyType.NONE);

        PartyProvision updatedOperation = partyOperationMapper.findByPartyId(partyId);

        // 운영 상태도 그대로 유지되어야 함
        assertThat(updatedOperation.getOperationStatus())
                .isEqualTo(ProvisionStatus.ACTIVE);
        assertThat(updatedOperation.getLastResetAt()).isNull();
        assertThat(updatedOperation.getOperationCompletedAt()).isNotNull();

        List<PartyProvisionMemberResponse> operationMembers =
                partyOperationMemberMapper.findResponsesByPartyOperationId(operationId);

        // 운영 멤버도 그대로 4명 유지
        assertThat(operationMembers).hasSize(4);
        assertThat(operationMembers.stream()
                .map(PartyProvisionMemberResponse::userId)
                .toList())
                .containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    @DisplayName("cycle start 시 탈퇴 예정 멤버를 실제 탈퇴 처리하고 운영을 RESET_REQUIRED로 전환한다")
    void handleCycleStartWithLeaveReservedMemberResetsOperation() {
        // given
        Long partyId = createParty(4, 4);

        createMember(partyId, 1L, PartyRole.HOST, PartyMemberStatus.ACTIVE);
        createMember(partyId, 2L, PartyRole.MEMBER, PartyMemberStatus.ACTIVE);
        createMember(partyId, 3L, PartyRole.MEMBER, PartyMemberStatus.ACTIVE);
        createMember(partyId, 4L, PartyRole.MEMBER, PartyMemberStatus.LEAVE_RESERVED);

        Long operationId = createOperation(
                partyId,
                ProvisionStatus.ACTIVE
        );

        // 이전 주기 기준 운영 멤버 구성
        createOperationMembersByUserIds(operationId, partyId, List.of(1L, 2L, 3L, 4L));

        // when
        partyCycleService.handleCycleStart(partyId);

        // then
        Map<Long, PartyMember> membersByUserId = partyMemberMapper.findMembersByPartyId(partyId)
                .stream()
                .collect(Collectors.toMap(PartyMember::getUserId, Function.identity()));

        // 탈퇴 예약 멤버는 실제 탈퇴 처리
        PartyMember leftMember = membersByUserId.get(4L);
        assertThat(leftMember.getStatus()).isEqualTo(PartyMemberStatus.LEFT);
        assertThat(leftMember.getLeftAt()).isNotNull();

        Party updatedParty = partyMapper.findById(partyId);

        // 인원 감소 -> 모집중 / 파티원 결원
        assertThat(updatedParty.getCurrentMemberCount()).isEqualTo(3);
        assertThat(updatedParty.getRecruitStatus()).isEqualTo(RecruitStatus.RECRUITING);
        assertThat(updatedParty.getVacancyType()).isEqualTo(VacancyType.MEMBER);

        PartyProvision updatedOperation = partyOperationMapper.findByPartyId(partyId);

        // 운영 멤버 구성이 바뀌었으므로 reset 필요
        assertThat(updatedOperation.getOperationStatus())
                .isEqualTo(ProvisionStatus.RESET_REQUIRED);
        assertThat(updatedOperation.getLastResetAt()).isNotNull();
        assertThat(updatedOperation.getOperationCompletedAt()).isNull();

        List<PartyProvisionMemberResponse> operationMembers =
                partyOperationMemberMapper.findResponsesByPartyOperationId(operationId);

        // 남아 있는 3명 기준으로 재생성되어야 함
        assertThat(operationMembers).hasSize(3);
        assertThat(operationMembers.stream()
                .map(PartyProvisionMemberResponse::userId)
                .toList())
                .containsExactlyInAnyOrder(1L, 2L, 3L);

        // reset 처리 후 운영 멤버 상태는 전원 RESET_REQUIRED
        assertThat(operationMembers)
                .extracting(PartyProvisionMemberResponse::memberStatus)
                .containsOnly(ProvisionMemberStatus.RESET_REQUIRED);
    }

    @Test
    @DisplayName("cycle start 시 교체 대기 멤버를 ACTIVE로 전환하고 운영 멤버를 새 기준으로 재구성한다")
    void handleCycleStartWithSwitchWaitingMemberRebuildsOperationMembers() {
        // given
        Long partyId = createParty(4, 4);

        createMember(partyId, 1L, PartyRole.HOST, PartyMemberStatus.ACTIVE);
        createMember(partyId, 2L, PartyRole.MEMBER, PartyMemberStatus.ACTIVE);
        createMember(partyId, 3L, PartyRole.MEMBER, PartyMemberStatus.ACTIVE);
        createMember(partyId, 4L, PartyRole.MEMBER, PartyMemberStatus.LEAVE_RESERVED);
        createMember(partyId, 5L, PartyRole.MEMBER, PartyMemberStatus.SWITCH_WAITING);

        Long operationId = createOperation(
                partyId,
                ProvisionStatus.ACTIVE
        );

        // 이전 주기 기준 운영 멤버는 1,2,3,4
        createOperationMembersByUserIds(operationId, partyId, List.of(1L, 2L, 3L, 4L));

        // when
        partyCycleService.handleCycleStart(partyId);

        // then
        Map<Long, PartyMember> membersByUserId = partyMemberMapper.findMembersByPartyId(partyId)
                .stream()
                .collect(Collectors.toMap(PartyMember::getUserId, Function.identity()));

        // 기존 탈퇴 예정자는 LEFT
        assertThat(membersByUserId.get(4L).getStatus()).isEqualTo(PartyMemberStatus.LEFT);
        assertThat(membersByUserId.get(4L).getLeftAt()).isNotNull();

        // 교체 대기 멤버는 이번 주기부터 ACTIVE
        assertThat(membersByUserId.get(5L).getStatus()).isEqualTo(PartyMemberStatus.ACTIVE);
        assertThat(membersByUserId.get(5L).getActivatedAt()).isNotNull();

        Party updatedParty = partyMapper.findById(partyId);

        // 빠진 인원과 새 인원이 교체되므로 총 인원은 다시 4명
        assertThat(updatedParty.getCurrentMemberCount()).isEqualTo(4);
        assertThat(updatedParty.getRecruitStatus()).isEqualTo(RecruitStatus.FULL);
        assertThat(updatedParty.getVacancyType()).isEqualTo(VacancyType.NONE);

        PartyProvision updatedOperation = partyOperationMapper.findByPartyId(partyId);

        // 운영 멤버가 바뀌었으므로 reset 필요
        assertThat(updatedOperation.getOperationStatus())
                .isEqualTo(ProvisionStatus.RESET_REQUIRED);
        assertThat(updatedOperation.getLastResetAt()).isNotNull();
        assertThat(updatedOperation.getOperationCompletedAt()).isNull();

        List<PartyProvisionMemberResponse> operationMembers =
                partyOperationMemberMapper.findResponsesByPartyOperationId(operationId);

        // 새 운영 멤버는 1,2,3,5 기준으로 재구성
        assertThat(operationMembers).hasSize(4);
        assertThat(operationMembers.stream()
                .map(PartyProvisionMemberResponse::userId)
                .toList())
                .containsExactlyInAnyOrder(1L, 2L, 3L, 5L);

        // reset 처리 후 운영 멤버 상태는 전원 RESET_REQUIRED
        assertThat(operationMembers)
                .extracting(PartyProvisionMemberResponse::memberStatus)
                .containsOnly(ProvisionMemberStatus.RESET_REQUIRED);
    }

    @Test
    @DisplayName("cycle start 시 운영 정보가 없어도 파티 상태만 반영하고 예외 없이 완료한다")
    void handleCycleStartWithoutOperationPassesSilently() {
        // given
        Long partyId = createParty(4, 4);

        createMember(partyId, 1L, PartyRole.HOST, PartyMemberStatus.ACTIVE);
        createMember(partyId, 2L, PartyRole.MEMBER, PartyMemberStatus.ACTIVE);
        createMember(partyId, 3L, PartyRole.MEMBER, PartyMemberStatus.ACTIVE);
        createMember(partyId, 4L, PartyRole.MEMBER, PartyMemberStatus.LEAVE_RESERVED);

        // when
        partyCycleService.handleCycleStart(partyId);

        // then
        Map<Long, PartyMember> membersByUserId = partyMemberMapper.findMembersByPartyId(partyId)
                .stream()
                .collect(Collectors.toMap(PartyMember::getUserId, Function.identity()));

        // 파티 상태는 정상 반영
        assertThat(membersByUserId.get(4L).getStatus()).isEqualTo(PartyMemberStatus.LEFT);

        Party updatedParty = partyMapper.findById(partyId);
        assertThat(updatedParty.getCurrentMemberCount()).isEqualTo(3);
        assertThat(updatedParty.getRecruitStatus()).isEqualTo(RecruitStatus.RECRUITING);
        assertThat(updatedParty.getVacancyType()).isEqualTo(VacancyType.MEMBER);

        // 운영 정보가 없어도 예외 없이 지나가야 함
        PartyProvision operation = partyOperationMapper.findByPartyId(partyId);
        assertThat(operation).isNull();
    }

    // 파티 기본 데이터 생성
    private Long createParty(int capacity, int currentMemberCount) {
        Party party = Party.builder()
                .productId("TEST_PRODUCT_" + System.nanoTime())
                .hostUserId(1L)
                .capacity(capacity)
                .currentMemberCount(currentMemberCount)
                .recruitStatus(currentMemberCount >= capacity ? RecruitStatus.FULL : RecruitStatus.RECRUITING)
                .operationStatus(OperationStatus.WAITING_START)
                .vacancyType(currentMemberCount >= capacity ? VacancyType.NONE : VacancyType.MEMBER)
                .pricePerMemberSnapshot(5000)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .terminatedAt(null)
                .build();

        partyMapper.insertParty(party);
        return party.getId();
    }

    // 파티 멤버 생성
    private Long createMember(
            Long partyId,
            Long userId,
            PartyRole role,
            PartyMemberStatus status
    ) {
        LocalDateTime now = LocalDateTime.now();

        PartyMember member = PartyMember.builder()
                .partyId(partyId)
                .userId(userId)
                .role(role)
                .status(status)
                .joinedAt(now)
                .activatedAt(status == PartyMemberStatus.ACTIVE ? now : null)
                .serviceStartAt(null)
                .serviceEndAt(null)
                .leaveReservedAt(status == PartyMemberStatus.LEAVE_RESERVED ? now : null)
                .leftAt(null)
                .replacedTargetMemberId(null)
                .build();

        partyMemberMapper.insertPartyMember(member);
        return member.getId();
    }

    // 파티 운영 정보 생성
    private Long createOperation(
            Long partyId,
            ProvisionStatus status
    ) {
        LocalDateTime now = LocalDateTime.now();

        PartyProvision operation = PartyProvision.builder()
                .partyId(partyId)
                .operationType(ProvisionType.ACCOUNT_SHARED)
                .operationStatus(status)
                .inviteValue(null)
                .sharedAccountEmail("shared@test.com")
                .sharedAccountPasswordEncrypted("encrypted-password")
                .operationGuide("테스트 운영 가이드")
                .operationStartedAt(now.minusHours(1))
                .operationCompletedAt(
                        status == ProvisionStatus.ACTIVE
                                ? now
                                : null
                )
                .lastResetAt(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        partyOperationMapper.insert(operation);
        return operation.getId();
    }

    // 이전 주기 기준 운영 멤버를 특정 userId 목록으로만 생성
    private void createOperationMembersByUserIds(
            Long operationId,
            Long partyId,
            List<Long> userIds
    ) {
        Map<Long, PartyMember> membersByUserId = partyMemberMapper.findMembersByPartyId(partyId)
                .stream()
                .collect(Collectors.toMap(PartyMember::getUserId, Function.identity()));

        LocalDateTime now = LocalDateTime.now();

        for (Long userId : userIds) {
            PartyMember member = membersByUserId.get(userId);

            PartyProvisionMember operationMember = PartyProvisionMember.builder()
                    .partyOperationId(operationId)
                    .partyMemberId(member.getId())
                    .partyId(partyId)
                    .userId(member.getUserId())
                    .memberStatus(ProvisionMemberStatus.ACTIVE)
                    .inviteSentAt(now)
                    .mustCompleteBy(now.plusHours(24))
                    .confirmedAt(now)
                    .completedAt(now)
                    .activatedAt(now)
                    .lastResetAt(null)
                    .penaltyApplied(false)
                    .operationMessage("기존 운영 멤버")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            partyOperationMemberMapper.insert(operationMember);
        }
    }
}