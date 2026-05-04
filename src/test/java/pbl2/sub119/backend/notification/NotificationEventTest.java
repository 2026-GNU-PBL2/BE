package pbl2.sub119.backend.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import pbl2.sub119.backend.notification.entity.Notification;
import pbl2.sub119.backend.notification.enumerated.NotificationType;
import pbl2.sub119.backend.notification.event.event.MemberAutoRematchStartedEvent;
import pbl2.sub119.backend.notification.event.event.PartyMatchedEvent;
import pbl2.sub119.backend.notification.event.event.PartyTerminatedEvent;
import pbl2.sub119.backend.notification.event.event.PaymentFailedEvent;
import pbl2.sub119.backend.notification.event.event.PaymentSucceededEvent;
import pbl2.sub119.backend.notification.event.event.SettlementCompletedEvent;
import pbl2.sub119.backend.notification.mapper.NotificationMapper;

@ActiveProfiles("local")
@SpringBootTest
class NotificationEventTest {

    private static final Long PARTY_ID = 1L;
    private static final Long HOST_USER_ID = 1L;
    private static final Long MEMBER_USER_ID = 2L;
    private static final Long MEMBER_USER_ID2 = 3L;
    private static final Long MEMBER_USER_ID3 = 4L;
    private static final Long CYCLE_ID = 1L;

    private static final String SUB_PRODUCT_ID = "11111111-1111-1111-1111-111111111111";

    @Autowired private ApplicationEventPublisher eventPublisher;
    @Autowired private TransactionTemplate transactionTemplate;
    @Autowired private NotificationMapper notificationMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        cleanup();

        jdbcTemplate.update("""
            INSERT INTO users (id, phone_number, role, status, created_at, updated_at)
            VALUES
            (1, NULL, 'CUSTOMER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            (2, NULL, 'CUSTOMER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            (3, NULL, 'CUSTOMER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            (4, NULL, 'CUSTOMER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """);

        jdbcTemplate.update("""
            INSERT INTO sub_product (
                id, service_name, description, thumbnail_url,
                operation_type, category, max_member_count,
                base_price, price_per_member,
                status, created_at, updated_at
            ) VALUES (
                ?, '넷플릭스 프리미엄', '테스트 상품',
                NULL, 'ACCOUNT_SHARE', 'NETFLIX', 4,
                17000, 4500, 'ACTIVE',
                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
            )
        """, SUB_PRODUCT_ID);

        jdbcTemplate.update("""
            INSERT INTO party (
                id, product_id, host_user_id, capacity,
                current_member_count, recruit_status, operation_status,
                vacancy_type, created_at, updated_at,
                price_per_member_snapshot
            ) VALUES (
                1, ?, 1, 4,
                4, 'FULL', 'ACTIVE',
                'NONE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
                4500
            )
        """, SUB_PRODUCT_ID);

        jdbcTemplate.update("""
            INSERT INTO party_member (
                id, party_id, user_id, role, status,
                joined_at, activated_at
            ) VALUES
            (1, 1, 1, 'HOST', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            (2, 1, 2, 'MEMBER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            (3, 1, 3, 'MEMBER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            (4, 1, 4, 'MEMBER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """);

        jdbcTemplate.update("""
            INSERT INTO party_cycle (
                id, party_id, cycle_no,
                start_at, end_at, billing_due_at,
                status, member_count_snapshot, price_per_member_snapshot,
                created_at, updated_at
            ) VALUES (
                1, 1, 1,
                CURRENT_TIMESTAMP,
                DATEADD('MONTH', 1, CURRENT_TIMESTAMP),
                CURRENT_TIMESTAMP,
                'RUNNING', 4, 4500,
                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
            )
        """);
    }

    @AfterEach
    void cleanup() {
        /*jdbcTemplate.update("DELETE FROM sms_send_log");
        jdbcTemplate.update("DELETE FROM notification");
        jdbcTemplate.update("DELETE FROM party_cycle");
        jdbcTemplate.update("DELETE FROM party_member");
        jdbcTemplate.update("DELETE FROM party_history");
        jdbcTemplate.update("DELETE FROM party");
        jdbcTemplate.update("DELETE FROM sub_product");
        jdbcTemplate.update("DELETE FROM users");*/
    }

    private List<NotificationType> types(Long userId) {
        return notificationMapper.findByUserId(userId)
                .stream()
                .map(Notification::getType)
                .toList();
    }

    private boolean smsSuccess(Long userId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT status FROM sms_send_log WHERE user_id = ? ORDER BY created_at DESC LIMIT 1",
                userId
        );
        return !rows.isEmpty() && "SUCCESS".equals(rows.get(0).get("status"));
    }

    private void waitAsync() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @Test
    @DisplayName("파티 매칭")
    void partyMatched() {
        // given
        List<Long> members = List.of(MEMBER_USER_ID, MEMBER_USER_ID2, MEMBER_USER_ID3);

        // when
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(new PartyMatchedEvent(PARTY_ID, CYCLE_ID, members));
            return null;
        });

        waitAsync();

        // then
        assertThat(types(MEMBER_USER_ID)).contains(NotificationType.PARTY_MATCHED);
        assertThat(types(MEMBER_USER_ID2)).contains(NotificationType.PARTY_MATCHED);
        assertThat(types(MEMBER_USER_ID3)).contains(NotificationType.PARTY_MATCHED);
        assertThat(types(HOST_USER_ID)).contains(NotificationType.HOST_PARTY_MATCHED);

        assertThat(smsSuccess(HOST_USER_ID)).isTrue();
    }

    @Test
    @DisplayName("결제 완료")
    void paymentSucceeded() {
        // given
        Long memberUserId = MEMBER_USER_ID;

        // when
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(new PaymentSucceededEvent(PARTY_ID, CYCLE_ID, memberUserId));
            return null;
        });

        waitAsync();

        // then
        assertThat(types(memberUserId)).contains(NotificationType.PAYMENT_SUCCEEDED);
    }

    @Test
    @DisplayName("결제 실패")
    void paymentFailed() {
        // given
        Long memberUserId = MEMBER_USER_ID;

        // when
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(new PaymentFailedEvent(PARTY_ID, CYCLE_ID, memberUserId));
            return null;
        });

        waitAsync();

        // then
        assertThat(types(memberUserId)).contains(NotificationType.PAYMENT_FAILED);
    }

    @Test
    @DisplayName("정산 완료")
    void settlementCompleted() {
        // given
        Long hostUserId = HOST_USER_ID;

        // when
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(new SettlementCompletedEvent(PARTY_ID, CYCLE_ID, hostUserId));
            return null;
        });

        waitAsync();

        // then
        assertThat(types(hostUserId)).contains(NotificationType.SETTLEMENT_COMPLETED);
    }

    @Test
    @DisplayName("파티 해체")
    void partyTerminated() {
        // given
        List<Long> users = List.of(HOST_USER_ID, MEMBER_USER_ID, MEMBER_USER_ID2, MEMBER_USER_ID3);

        // when
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(new PartyTerminatedEvent(PARTY_ID, users, "테스트"));
            return null;
        });

        waitAsync();

        // then
        assertThat(types(HOST_USER_ID)).contains(NotificationType.HOST_PROVISION_TIMEOUT_TERMINATED);
        assertThat(types(MEMBER_USER_ID)).contains(NotificationType.PARTY_TERMINATED);
    }

    @Test
    @DisplayName("자동 재매칭")
    void memberAutoRematchStarted() {
        // given
        List<Long> members = List.of(MEMBER_USER_ID, MEMBER_USER_ID2, MEMBER_USER_ID3);

        // when
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(new MemberAutoRematchStartedEvent(PARTY_ID, members));
            return null;
        });

        waitAsync();

        // then
        assertThat(types(MEMBER_USER_ID)).contains(NotificationType.MEMBER_AUTO_REMATCH_STARTED);
    }
}