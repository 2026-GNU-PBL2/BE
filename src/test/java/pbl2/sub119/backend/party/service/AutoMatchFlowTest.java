package pbl2.sub119.backend.party.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pbl2.sub119.backend.party.dto.response.MatchWaitingRegisterResponse;
import pbl2.sub119.backend.party.common.entity.MatchWaitingQueue;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;
import pbl2.sub119.backend.party.common.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.common.enumerated.VacancyType;
import pbl2.sub119.backend.party.common.mapper.MatchWaitingQueueMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;

@SpringBootTest
@ActiveProfiles("local")
class AutoMatchFlowTest {

    @Autowired
    private MatchWaitingService matchWaitingService;

    @Autowired
    private VacancyEventService vacancyEventService;

    @Autowired
    private MatchWaitingQueueMapper matchWaitingQueueMapper;

    @Autowired
    private PartyMapper partyMapper;

    @Autowired
    private PartyMemberMapper partyMemberMapper;

    @Test
    @DisplayName("빈자리 발생 시 대기열 사용자가 자동매칭된다")
    void autoMatchSuccess() {
        // given
        String productId = "PRODUCT_AUTO_MATCH_" + System.nanoTime();
        Long hostUserId = 3001L;
        Long waitingUserId = 3002L;
        LocalDateTime now = LocalDateTime.now();

        Party party = Party.builder()
                .productId(productId)
                .hostUserId(hostUserId)
                .capacity(4)
                .currentMemberCount(1)
                .recruitStatus(RecruitStatus.RECRUITING)
                .operationStatus(OperationStatus.WAITING_START)
                .vacancyType(VacancyType.NONE)
                .pricePerMemberSnapshot(4250)
                .createdAt(now)
                .updatedAt(now)
                .terminatedAt(null)
                .build();

        partyMapper.insertParty(party);

        PartyMember hostMember = PartyMember.builder()
                .partyId(party.getId())
                .userId(hostUserId)
                .role(PartyRole.HOST)
                .status(PartyMemberStatus.ACTIVE)
                .joinedAt(now)
                .activatedAt(now)
                .serviceStartAt(null)
                .serviceEndAt(null)
                .leaveReservedAt(null)
                .leftAt(null)
                .replacedTargetMemberId(null)
                .build();

        partyMemberMapper.insertPartyMember(hostMember);

        MatchWaitingRegisterResponse waitingResponse =
                matchWaitingService.registerWaiting(productId, waitingUserId);

        // when
        vacancyEventService.handleVacancy(productId, party.getId(), hostUserId);

        // then
        MatchWaitingQueue queue =
                matchWaitingQueueMapper.findById(waitingResponse.waitingId());

        assertThat(queue).isNotNull();
        assertThat(queue.getStatus().name()).isEqualTo("MATCHED");
        assertThat(queue.getTargetPartyId()).isEqualTo(party.getId());
        assertThat(queue.getMatchedAt()).isNotNull();

        PartyMember joinedMember =
                partyMemberMapper.findByPartyIdAndUserId(party.getId(), waitingUserId);

        assertThat(joinedMember).isNotNull();
        assertThat(joinedMember.getUserId()).isEqualTo(waitingUserId);

        Party updatedParty = partyMapper.findById(party.getId());
        assertThat(updatedParty.getCurrentMemberCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("빈자리 발생 시 FIFO 기준으로 가장 먼저 대기한 사용자가 자동매칭된다")
    void autoMatchByFifoOrder() throws Exception {
        // given
        String productId = "_AUTO_MATCH_FIFO_" + System.nanoTime();
        Long hostUserId = 3003L;
        Long firstWaitingUserId = 3004L;
        Long secondWaitingUserId = 3005L;
        LocalDateTime now = LocalDateTime.now();

        Party party = Party.builder()
                .productId(productId)
                .hostUserId(hostUserId)
                .capacity(4)
                .currentMemberCount(1)
                .recruitStatus(RecruitStatus.RECRUITING)
                .operationStatus(OperationStatus.WAITING_START)
                .vacancyType(VacancyType.NONE)
                .pricePerMemberSnapshot(4250)
                .createdAt(now)
                .updatedAt(now)
                .terminatedAt(null)
                .build();

        partyMapper.insertParty(party);

        PartyMember hostMember = PartyMember.builder()
                .partyId(party.getId())
                .userId(hostUserId)
                .role(PartyRole.HOST)
                .status(PartyMemberStatus.ACTIVE)
                .joinedAt(now)
                .activatedAt(now)
                .serviceStartAt(null)
                .serviceEndAt(null)
                .leaveReservedAt(null)
                .leftAt(null)
                .replacedTargetMemberId(null)
                .build();

        partyMemberMapper.insertPartyMember(hostMember);

        MatchWaitingRegisterResponse firstWaiting =
                matchWaitingService.registerWaiting(productId, firstWaitingUserId);

        Thread.sleep(10);

        MatchWaitingRegisterResponse secondWaiting =
                matchWaitingService.registerWaiting(productId, secondWaitingUserId);

        // when
        vacancyEventService.handleVacancy(productId, party.getId(), hostUserId);

        // then
        MatchWaitingQueue firstQueue =
                matchWaitingQueueMapper.findById(firstWaiting.waitingId());
        MatchWaitingQueue secondQueue =
                matchWaitingQueueMapper.findById(secondWaiting.waitingId());

        assertThat(firstQueue).isNotNull();
        assertThat(firstQueue.getStatus().name()).isEqualTo("MATCHED");
        assertThat(firstQueue.getTargetPartyId()).isEqualTo(party.getId());
        assertThat(firstQueue.getMatchedAt()).isNotNull();

        assertThat(secondQueue).isNotNull();
        assertThat(secondQueue.getStatus().name()).isEqualTo("WAITING");
        assertThat(secondQueue.getTargetPartyId()).isNull();
        assertThat(secondQueue.getMatchedAt()).isNull();

        PartyMember firstJoinedMember =
                partyMemberMapper.findByPartyIdAndUserId(party.getId(), firstWaitingUserId);
        PartyMember secondJoinedMember =
                partyMemberMapper.findByPartyIdAndUserId(party.getId(), secondWaitingUserId);

        assertThat(firstJoinedMember).isNotNull();
        assertThat(secondJoinedMember).isNull();

        Party updatedParty = partyMapper.findById(party.getId());
        assertThat(updatedParty.getCurrentMemberCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("대기열 사용자가 없으면 빈자리 이벤트가 발생해도 아무 일도 일어나지 않는다")
    void handleVacancyWhenNoWaitingUser() {
        // given
        String productId = "PRODUCT_NO_WAITING_" + System.nanoTime();
        Long hostUserId = 3006L;
        LocalDateTime now = LocalDateTime.now();

        Party party = Party.builder()
                .productId(productId)
                .hostUserId(hostUserId)
                .capacity(4)
                .currentMemberCount(1)
                .recruitStatus(RecruitStatus.RECRUITING)
                .operationStatus(OperationStatus.WAITING_START)
                .vacancyType(VacancyType.NONE)
                .pricePerMemberSnapshot(4250)
                .createdAt(now)
                .updatedAt(now)
                .terminatedAt(null)
                .build();

        partyMapper.insertParty(party);

        PartyMember hostMember = PartyMember.builder()
                .partyId(party.getId())
                .userId(hostUserId)
                .role(PartyRole.HOST)
                .status(PartyMemberStatus.ACTIVE)
                .joinedAt(now)
                .activatedAt(now)
                .serviceStartAt(null)
                .serviceEndAt(null)
                .leaveReservedAt(null)
                .leftAt(null)
                .replacedTargetMemberId(null)
                .build();

        partyMemberMapper.insertPartyMember(hostMember);

        // when
        vacancyEventService.handleVacancy(productId, party.getId(), hostUserId);

        // then
        Party updatedParty = partyMapper.findById(party.getId());

        assertThat(updatedParty).isNotNull();
        assertThat(updatedParty.getCurrentMemberCount()).isEqualTo(1);

        PartyMember hostOnlyMember =
                partyMemberMapper.findByPartyIdAndUserId(party.getId(), hostUserId);

        assertThat(hostOnlyMember).isNotNull();
    }
}