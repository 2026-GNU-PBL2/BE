package pbl2.sub119.backend.party.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;
import pbl2.sub119.backend.party.common.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.common.enumerated.VacancyType;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;

@Disabled("도메인 구조 변경으로 테스트 리팩토링 필요")
@SpringBootTest
@ActiveProfiles("local")
class PartyJoinServiceTest {

    @Autowired
    private PartyJoinService partyJoinService;

    @Autowired
    private PartyMapper partyMapper;

    @Autowired
    private PartyMemberMapper partyMemberMapper;

    @Test
    @DisplayName("파티 참여 성공")
    void joinPartySuccess() {
        // given
        Long hostUserId = 1L;
        Long joinUserId = 10L;

        Party party = Party.builder()
                .productId("f759e32a-b0ef-4afb-9731-eb72e388367e")
                .hostUserId(hostUserId)
                .capacity(4)
                .currentMemberCount(1)
                .recruitStatus(RecruitStatus.RECRUITING)
                .operationStatus(OperationStatus.WAITING_START)
                .vacancyType(VacancyType.NONE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .terminatedAt(null)
                .pricePerMemberSnapshot(4250)
                .build();

        partyMapper.insertParty(party);

        PartyMember hostMember = PartyMember.builder()
                .partyId(party.getId())
                .userId(hostUserId)
                .role(PartyRole.HOST)
                .status(PartyMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .activatedAt(LocalDateTime.now())
                .serviceStartAt(null)
                .serviceEndAt(null)
                .leaveReservedAt(null)
                .leftAt(null)
                .replacedTargetMemberId(null)
                .build();

        partyMemberMapper.insertPartyMember(hostMember);

        Long partyId = party.getId();

        // when
        partyJoinService.joinParty(partyId, joinUserId);

        // then
        PartyMember joinedMember = partyMemberMapper.findByPartyIdAndUserId(partyId, joinUserId);
        Party updatedParty = partyMapper.findById(partyId);

        assertThat(joinedMember).isNotNull();
        assertThat(joinedMember.getUserId()).isEqualTo(joinUserId);
        assertThat(joinedMember.getRole()).isEqualTo(PartyRole.MEMBER);
        assertThat(joinedMember.getStatus()).isEqualTo(PartyMemberStatus.PENDING);

        assertThat(updatedParty).isNotNull();
        assertThat(updatedParty.getCurrentMemberCount()).isEqualTo(2);
        assertThat(updatedParty.getRecruitStatus()).isEqualTo(RecruitStatus.RECRUITING);
    }

    @Test
    @DisplayName("같은 유저가 같은 파티에 중복 참여하면 실패한다")
    void joinPartyDuplicateFail() {
        // given
        Long hostUserId = 1L;
        Long joinUserId = 10L;

        Party party = Party.builder()
                .productId("f759e32a-b0ef-4afb-9731-eb72e388367e")
                .hostUserId(hostUserId)
                .capacity(4)
                .currentMemberCount(1)
                .recruitStatus(RecruitStatus.RECRUITING)
                .operationStatus(OperationStatus.WAITING_START)
                .vacancyType(VacancyType.NONE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .terminatedAt(null)
                .pricePerMemberSnapshot(4250)
                .build();

        partyMapper.insertParty(party);

        PartyMember hostMember = PartyMember.builder()
                .partyId(party.getId())
                .userId(hostUserId)
                .role(PartyRole.HOST)
                .status(PartyMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .activatedAt(LocalDateTime.now())
                .serviceStartAt(null)
                .serviceEndAt(null)
                .leaveReservedAt(null)
                .leftAt(null)
                .replacedTargetMemberId(null)
                .build();

        partyMemberMapper.insertPartyMember(hostMember);

        Long partyId = party.getId();

        // 첫 번째 참여
        partyJoinService.joinParty(partyId, joinUserId);

        // when
        PartyException exception = assertThrows(
                PartyException.class,
                () -> partyJoinService.joinParty(partyId, joinUserId)
        );

        // then
        assertThat(exception.getMessage()).isEqualTo("이미 해당 파티에 참여 중이거나 처리 중인 사용자입니다.");

        Party updatedParty = partyMapper.findById(partyId);
        int occupiedCount = partyMemberMapper.countOccupiedMembers(partyId);

        assertThat(updatedParty.getCurrentMemberCount()).isEqualTo(2);
        assertThat(occupiedCount).isEqualTo(2);
    }

    @Test
    @DisplayName("마지막 한 명이 참여하면 파티 상태가 FULL로 전이된다")
    void joinPartyMakesPartyFull() {
        // given
        Long hostUserId = 1L;
        Long joinUserId = 20L;

        Party party = Party.builder()
                .productId("f759e32a-b0ef-4afb-9731-eb72e388367e")
                .hostUserId(hostUserId)
                .capacity(2)
                .currentMemberCount(1)
                .recruitStatus(RecruitStatus.RECRUITING)
                .operationStatus(OperationStatus.WAITING_START)
                .vacancyType(VacancyType.NONE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .terminatedAt(null)
                .pricePerMemberSnapshot(4250)
                .build();

        partyMapper.insertParty(party);

        PartyMember hostMember = PartyMember.builder()
                .partyId(party.getId())
                .userId(hostUserId)
                .role(PartyRole.HOST)
                .status(PartyMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .activatedAt(LocalDateTime.now())
                .serviceStartAt(null)
                .serviceEndAt(null)
                .leaveReservedAt(null)
                .leftAt(null)
                .replacedTargetMemberId(null)
                .build();

        partyMemberMapper.insertPartyMember(hostMember);

        Long partyId = party.getId();

        // when
        partyJoinService.joinParty(partyId, joinUserId);

        // then
        Party updatedParty = partyMapper.findById(partyId);
        PartyMember joinedMember = partyMemberMapper.findByPartyIdAndUserId(partyId, joinUserId);

        assertThat(joinedMember).isNotNull();
        assertThat(updatedParty).isNotNull();
        assertThat(updatedParty.getCurrentMemberCount()).isEqualTo(2);
        assertThat(updatedParty.getRecruitStatus()).isEqualTo(RecruitStatus.FULL);
    }
}