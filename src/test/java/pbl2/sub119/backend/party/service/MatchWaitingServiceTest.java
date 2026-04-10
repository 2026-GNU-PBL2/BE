package pbl2.sub119.backend.party.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pbl2.sub119.backend.party.dto.response.MatchWaitingRegisterResponse;
import pbl2.sub119.backend.party.common.entity.MatchWaitingQueue;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.party.common.mapper.MatchWaitingQueueMapper;

@SpringBootTest
@ActiveProfiles("local")
class MatchWaitingServiceTest {

    @Autowired
    private MatchWaitingService matchWaitingService;

    @Autowired
    private MatchWaitingQueueMapper matchWaitingQueueMapper;

    @Test
    @DisplayName("대기열 등록 성공")
    void registerWaitingSuccess() {
        // given
        String productId = "TEST_PRODUCT_WAITING_" + System.nanoTime();
        Long userId = 1001L;

        // when
        MatchWaitingRegisterResponse response =
                matchWaitingService.registerWaiting(productId, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.waitingId()).isNotNull();
        assertThat(response.productId()).isEqualTo(productId);
        assertThat(response.userId()).isEqualTo(userId);

        MatchWaitingQueue saved =
                matchWaitingQueueMapper.findWaitingByProductIdAndUserId(productId, userId);

        assertThat(saved).isNotNull();
        assertThat(saved.getProductId()).isEqualTo(productId);
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getStatus().name()).isEqualTo("WAITING");
        assertThat(saved.getRequestedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("중복 대기열 등록 실패")
    void duplicateWaitingFail() {
        // given
        String productId = "TEST_PRODUCT_DUPLICATE_" + System.nanoTime();
        Long userId = 1002L;

        matchWaitingService.registerWaiting(productId, userId);

        // when & then
        assertThrows(
                PartyException.class,
                () -> matchWaitingService.registerWaiting(productId, userId)
        );
    }

    @Test
    @DisplayName("대기열 취소 성공")
    void cancelWaitingSuccess() {
        // given
        String productId = "TEST_PRODUCT_CANCEL_" + System.nanoTime();
        Long userId = 1003L;

        MatchWaitingRegisterResponse response =
                matchWaitingService.registerWaiting(productId, userId);

        // when
        matchWaitingService.cancelWaiting(response.waitingId(), userId);

        // then
        MatchWaitingQueue canceled = matchWaitingQueueMapper.findById(response.waitingId());

        assertThat(canceled).isNotNull();
        assertThat(canceled.getStatus().name()).isEqualTo("CANCELED");
        assertThat(canceled.getCanceledAt()).isNotNull();
        assertThat(canceled.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("상품 ID가 null이면 대기열 등록 실패")
    void registerWaitingFailWhenProductIdIsNull() {
        // given
        Long userId = 1004L;

        // when & then
        assertThrows(
                PartyException.class,
                () -> matchWaitingService.registerWaiting(null, userId)
        );
    }

    @Test
    @DisplayName("상품 ID가 공백이면 대기열 등록 실패")
    void registerWaitingFailWhenProductIdIsBlank() {
        // given
        Long userId = 1005L;

        // when & then
        assertThrows(
                PartyException.class,
                () -> matchWaitingService.registerWaiting("   ", userId)
        );
    }

    @Test
    @DisplayName("존재하지 않는 대기열 취소 실패")
    void cancelWaitingFailWhenWaitingNotFound() {
        // given
        Long invalidWaitingId = Long.MAX_VALUE;
        Long userId = 1006L;

        // when & then
        assertThrows(
                PartyException.class,
                () -> matchWaitingService.cancelWaiting(invalidWaitingId, userId)
        );
    }

    @Test
    @DisplayName("FIFO 기준으로 가장 먼저 대기한 사용자를 조회한다")
    void findFirstWaitingByProductIdFifo() throws Exception {
        // given
        String productId = "TEST_PRODUCT_FIFO_" + System.nanoTime();
        Long firstUserId = 1007L;
        Long secondUserId = 1008L;

        MatchWaitingRegisterResponse firstResponse =
                matchWaitingService.registerWaiting(productId, firstUserId);

        Thread.sleep(10);

        MatchWaitingRegisterResponse secondResponse =
                matchWaitingService.registerWaiting(productId, secondUserId);

        // when
        MatchWaitingQueue firstWaiting =
                matchWaitingService.findFirstWaitingByProductId(productId);

        // then
        assertThat(firstWaiting).isNotNull();
        assertThat(firstWaiting.getId()).isEqualTo(firstResponse.waitingId());
        assertThat(firstWaiting.getUserId()).isEqualTo(firstUserId);
        assertThat(firstWaiting.getStatus().name()).isEqualTo("WAITING");

        MatchWaitingQueue secondWaiting =
                matchWaitingQueueMapper.findById(secondResponse.waitingId());

        assertThat(secondWaiting).isNotNull();
        assertThat(secondWaiting.getUserId()).isEqualTo(secondUserId);
        assertThat(secondWaiting.getStatus().name()).isEqualTo("WAITING");
    }

    @Test
    @DisplayName("매칭 완료 처리 시 상태와 대상 파티 정보가 갱신된다")
    void markMatchedSuccess() {
        // given
        String productId = "TEST_PRODUCT_MATCHED_" + System.nanoTime();
        Long userId = 1009L;
        Long targetPartyId = 9999L;

        MatchWaitingRegisterResponse response =
                matchWaitingService.registerWaiting(productId, userId);

        // when
        matchWaitingService.markMatched(response.waitingId(), targetPartyId, userId);

        // then
        MatchWaitingQueue matched =
                matchWaitingQueueMapper.findById(response.waitingId());

        assertThat(matched).isNotNull();
        assertThat(matched.getStatus().name()).isEqualTo("MATCHED");
        assertThat(matched.getTargetPartyId()).isEqualTo(targetPartyId);
        assertThat(matched.getMatchedAt()).isNotNull();
        assertThat(matched.getUpdatedAt()).isNotNull();
    }
}