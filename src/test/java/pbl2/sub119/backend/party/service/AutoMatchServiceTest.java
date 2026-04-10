package pbl2.sub119.backend.party.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pbl2.sub119.backend.party.dto.response.JoinOrQueueResponse;
import pbl2.sub119.backend.party.common.entity.MatchWaitingQueue;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;
import pbl2.sub119.backend.party.common.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.common.enumerated.VacancyType;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.party.common.mapper.MatchWaitingQueueMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;

@SpringBootTest
@ActiveProfiles("local")
class AutoMatchServiceTest {

    @Autowired
    private AutoMatchService autoMatchService;

    @Autowired
    private PartyMapper partyMapper;

    @Autowired
    private PartyMemberMapper partyMemberMapper;

    @Autowired
    private MatchWaitingQueueMapper matchWaitingQueueMapper;

    @Test
    @DisplayName("즉시 참여 가능한 파티가 있으면 바로 참여한다")
    void joinImmediately() {
        // given
        String productId = "PRODUCT_JOIN_NOW_" + System.nanoTime();
        Long hostUserId = 2001L;
        Long joinUserId = 2002L;
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
        JoinOrQueueResponse response =
                autoMatchService.requestJoinOrQueue(productId, joinUserId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.joined()).isTrue();
        assertThat(response.queued()).isFalse();
        assertThat(response.partyId()).isEqualTo(party.getId());
        assertThat(response.waitingId()).isNull();

        PartyMember joinedMember =
                partyMemberMapper.findByPartyIdAndUserId(party.getId(), joinUserId);

        assertThat(joinedMember).isNotNull();
        assertThat(joinedMember.getUserId()).isEqualTo(joinUserId);

        Party updatedParty = partyMapper.findById(party.getId());
        assertThat(updatedParty.getCurrentMemberCount()).isEqualTo(2);

        MatchWaitingQueue waiting =
                matchWaitingQueueMapper.findWaitingByProductIdAndUserId(productId, joinUserId);

        assertThat(waiting).isNull();
    }

    @Test
    @DisplayName("즉시 참여 가능한 파티가 없으면 대기열에 등록한다")
    void queueWhenNoJoinableParty() {
        // given
        String productId = "TEST_PRODUCT_QUEUE_" + System.nanoTime();
        Long userId = 2003L;

        // intentionally no joinable party created

        // when
        JoinOrQueueResponse response =
                autoMatchService.requestJoinOrQueue(productId, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.joined()).isFalse();
        assertThat(response.queued()).isTrue();
        assertThat(response.partyId()).isNull();
        assertThat(response.waitingId()).isNotNull();

        MatchWaitingQueue waiting =
                matchWaitingQueueMapper.findWaitingByProductIdAndUserId(productId, userId);

        assertThat(waiting).isNotNull();
        assertThat(waiting.getStatus().name()).isEqualTo("WAITING");
    }

    @Test
    @DisplayName("상품 ID가 null이면 즉시 참여 또는 대기열 등록 실패")
    void requestJoinOrQueueFailWhenProductIdIsNull() {
        // given
        Long userId = 2004L;

        // when & then
        assertThrows(
                PartyException.class,
                () -> autoMatchService.requestJoinOrQueue(null, userId)
        );
    }

    @Test
    @DisplayName("상품 ID가 공백이면 즉시 참여 또는 대기열 등록 실패")
    void requestJoinOrQueueFailWhenProductIdIsBlank() {
        // given
        Long userId = 2005L;

        // when & then
        assertThrows(
                PartyException.class,
                () -> autoMatchService.requestJoinOrQueue("   ", userId)
        );
    }

    @Test
    @DisplayName("즉시 참여 가능한 파티가 없을 때 같은 사용자가 다시 요청하면 중복 대기열 등록 실패")
    void duplicateQueueFailWhenRequestAgain() {
        // given
        String productId = "TEST_PRODUCT_DUP_QUEUE_" + System.nanoTime();
        Long userId = 2006L;

        autoMatchService.requestJoinOrQueue(productId, userId);

        // when & then
        assertThrows(
                PartyException.class,
                () -> autoMatchService.requestJoinOrQueue(productId, userId)
        );

        MatchWaitingQueue waiting =
                matchWaitingQueueMapper.findWaitingByProductIdAndUserId(productId, userId);

        assertThat(waiting).isNotNull();
        assertThat(waiting.getStatus().name()).isEqualTo("WAITING");
    }
}