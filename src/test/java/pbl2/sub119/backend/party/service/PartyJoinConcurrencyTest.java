package pbl2.sub119.backend.party.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

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
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;
import pbl2.sub119.backend.party.join.service.PartyJoinService;

@Disabled("도메인 구조 변경으로 테스트 리팩토링 필요")
@SpringBootTest
@ActiveProfiles("local")
class PartyJoinConcurrencyTest {

    @Autowired
    private PartyJoinService partyJoinService;

    @Autowired
    private PartyMapper partyMapper;

    @Autowired
    private PartyMemberMapper partyMemberMapper;

    @Test
    @DisplayName("동시에 여러 명이 참여해도 정원을 초과하지 않는다")
    void joinPartyWithPessimisticLock() throws InterruptedException {
        // given
        Party party = Party.builder()
                .productId("f759e32a-b0ef-4afb-9731-eb72e388367e")
                .hostUserId(1L)
                .capacity(4)
                .currentMemberCount(3)
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
                .userId(1L)
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

        PartyMember member2 = PartyMember.builder()
                .partyId(party.getId())
                .userId(2L)
                .role(PartyRole.MEMBER)
                .status(PartyMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .activatedAt(LocalDateTime.now())
                .serviceStartAt(null)
                .serviceEndAt(null)
                .leaveReservedAt(null)
                .leftAt(null)
                .replacedTargetMemberId(null)
                .build();

        PartyMember member3 = PartyMember.builder()
                .partyId(party.getId())
                .userId(3L)
                .role(PartyRole.MEMBER)
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
        partyMemberMapper.insertPartyMember(member2);
        partyMemberMapper.insertPartyMember(member3);

        Long partyId = party.getId();
        int threadCount = 2;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        for (long userId = 101L; userId <= 102L; userId++) {
            final long currentUserId = userId;
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    partyJoinService.joinParty(partyId, currentUserId);
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertThat(doneLatch.await(5, TimeUnit.SECONDS)).isTrue();
        executorService.shutdown();
        assertThat(errors).hasSize(1);

        // then
        Party resultParty = partyMapper.findById(partyId);
        int occupiedCount = partyMemberMapper.countOccupiedMembers(partyId);

        assertThat(occupiedCount).isEqualTo(4);
        assertThat(resultParty.getCurrentMemberCount()).isEqualTo(4);
        assertThat(resultParty.getRecruitStatus()).isEqualTo(RecruitStatus.FULL);
    }
}