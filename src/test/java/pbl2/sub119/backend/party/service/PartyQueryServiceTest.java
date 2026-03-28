package pbl2.sub119.backend.party.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pbl2.sub119.backend.party.dto.response.PartyDetailResponse;
import pbl2.sub119.backend.party.dto.response.PartyListResponse;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.entity.PartyMember;
import pbl2.sub119.backend.party.enumerated.OperationStatus;
import pbl2.sub119.backend.party.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.enumerated.PartyRole;
import pbl2.sub119.backend.party.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.enumerated.VacancyType;
import pbl2.sub119.backend.party.mapper.PartyMapper;
import pbl2.sub119.backend.party.mapper.PartyMemberMapper;

@SpringBootTest
@ActiveProfiles("local")
class PartyQueryServiceTest {

    @Autowired
    private PartyQueryService partyQueryService;

    @Autowired
    private PartyMapper partyMapper;

    @Autowired
    private PartyMemberMapper partyMemberMapper;

    @Test
    @DisplayName("파티 상세 조회 성공")
    void getPartyDetailSuccess() {
        // given
        Long hostUserId = 1L;

        Party party = Party.builder()
                .productId("f759e32a-b0ef-4afb-9731-eb72e388367e")
                .hostUserId(hostUserId)
                .capacity(4)
                .currentMemberCount(2)
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

        PartyMember member = PartyMember.builder()
                .partyId(party.getId())
                .userId(2L)
                .role(PartyRole.MEMBER)
                .status(PartyMemberStatus.PENDING)
                .joinedAt(LocalDateTime.now())
                .activatedAt(null)
                .serviceStartAt(null)
                .serviceEndAt(null)
                .leaveReservedAt(null)
                .leftAt(null)
                .replacedTargetMemberId(null)
                .build();

        partyMemberMapper.insertPartyMember(hostMember);
        partyMemberMapper.insertPartyMember(member);

        // when
        PartyDetailResponse response = partyQueryService.getPartyDetail(party.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.partyId()).isEqualTo(party.getId());
        assertThat(response.productId()).isEqualTo("f759e32a-b0ef-4afb-9731-eb72e388367e");
        assertThat(response.hostUserId()).isEqualTo(hostUserId);
        assertThat(response.capacity()).isEqualTo(4);
        assertThat(response.currentMemberCount()).isEqualTo(2);
        assertThat(response.recruitStatus()).isEqualTo(RecruitStatus.RECRUITING);
        assertThat(response.operationStatus()).isEqualTo(OperationStatus.WAITING_START);
        assertThat(response.vacancyType()).isEqualTo(VacancyType.NONE);
        assertThat(response.pricePerMemberSnapshot()).isEqualTo(4250);

        assertThat(response.members()).hasSize(2);
        assertThat(response.members())
                .extracting("userId")
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("상품별 파티 목록 조회 성공")
    void getPartiesByProductSuccess() {
        // given
        String productId = "f759e32a-b0ef-4afb-9731-eb72e388367e";

        Party party1 = Party.builder()
                .productId(productId)
                .hostUserId(1L)
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

        Party party2 = Party.builder()
                .productId(productId)
                .hostUserId(2L)
                .capacity(4)
                .currentMemberCount(4)
                .recruitStatus(RecruitStatus.FULL)
                .operationStatus(OperationStatus.ACTIVE)
                .vacancyType(VacancyType.NONE)
                .createdAt(LocalDateTime.now().plusSeconds(1))
                .updatedAt(LocalDateTime.now().plusSeconds(1))
                .terminatedAt(null)
                .pricePerMemberSnapshot(4250)
                .build();

        partyMapper.insertParty(party1);
        partyMapper.insertParty(party2);

        // when
        List<PartyListResponse> responses = partyQueryService.getPartiesByProduct(productId);

        // then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSizeGreaterThanOrEqualTo(2);

        assertThat(responses)
                .extracting(PartyListResponse::partyId)
                .contains(party1.getId(), party2.getId());

        assertThat(responses)
                .extracting(PartyListResponse::productId)
                .containsOnly(productId);
    }
}