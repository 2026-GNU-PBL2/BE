package pbl2.sub119.backend.party.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (long userId = 101L; userId <= 102L; userId++) {
            final long currentUserId = userId;
            executorService.submit(() -> {
                try {
                    partyJoinService.joinParty(partyId, currentUserId);
                } catch (Exception e) {
                    // 한 명은 실패할 수 있으므로 무시
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Party resultParty = partyMapper.findById(partyId);
        int occupiedCount = partyMemberMapper.countOccupiedMembers(partyId);

        assertThat(occupiedCount).isEqualTo(4);
        assertThat(resultParty.getCurrentMemberCount()).isEqualTo(4);
        assertThat(resultParty.getRecruitStatus()).isEqualTo(RecruitStatus.FULL);
    }
}