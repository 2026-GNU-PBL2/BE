package pbl2.sub119.backend.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import pbl2.sub119.backend.notification.entity.Notification;
import pbl2.sub119.backend.notification.enumerated.NotificationType;
import pbl2.sub119.backend.notification.mapper.NotificationMapper;
import pbl2.sub119.backend.party.provision.service.ProvisionTimeoutService;

@ActiveProfiles("local")
@SpringBootTest
class ProvisionTimeoutNotificationTest {

    private static final Long PARTY_ID = 1L;
    private static final Long HOST_USER_ID = 1L;
    private static final Long MEMBER_USER_ID = 2L;
    private static final Long PROVISION_ID = 1L;
    private static final Long HOST_PARTY_MEMBER_ID = 1L;
    private static final Long MEMBER_PARTY_MEMBER_ID = 2L;
    private static final Long PROVISION_MEMBER_ID = 1L;

    private static final String SUB_PRODUCT_ID = "11111111-1111-1111-1111-111111111111";

    @Autowired
    private ProvisionTimeoutService provisionTimeoutService;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        cleanup();

        jdbcTemplate.update("""
            INSERT INTO users (id, phone_number, role, status, created_at, updated_at)
            VALUES
            (1, NULL, 'CUSTOMER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            (2, NULL, 'CUSTOMER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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
                2, 'FULL', 'ACTIVE',
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
            (2, 1, 2, 'MEMBER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """);

        jdbcTemplate.update("""
            INSERT INTO party_operation (
                id, party_id, operation_type, operation_status,
                invite_value, shared_account_email, shared_account_password_encrypted,
                operation_guide, operation_started_at, operation_completed_at,
                last_reset_at, created_at, updated_at
            ) VALUES (
                1, 1, 'ACCOUNT_SHARE', 'WAITING',
                NULL, NULL, NULL,
                NULL, NULL, NULL,
                NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
            )
        """);

        jdbcTemplate.update("""
            INSERT INTO party_operation_member (
                id, party_operation_id, party_member_id, party_id, user_id,
                member_status, invite_sent_at, must_complete_by,
                confirmed_at, completed_at, activated_at, last_reset_at,
                penalty_applied, operation_message,
                created_at, updated_at
            ) VALUES (
                1, 1, 2, 1, 2,
                'REQUIRED',
                CURRENT_TIMESTAMP,
                DATEADD('HOUR', 24, CURRENT_TIMESTAMP),
                NULL, NULL, NULL, NULL,
                FALSE, NULL,
                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
            )
        """);
    }

    @AfterEach
    void cleanup() {
        jdbcTemplate.update("DELETE FROM sms_send_log");
        jdbcTemplate.update("DELETE FROM notification");
        jdbcTemplate.update("DELETE FROM match_waiting_queue");
        jdbcTemplate.update("DELETE FROM party_operation_member");
        jdbcTemplate.update("DELETE FROM party_operation");
        jdbcTemplate.update("DELETE FROM party_cycle");
        jdbcTemplate.update("DELETE FROM party_member");
        jdbcTemplate.update("DELETE FROM party_history");
        jdbcTemplate.update("DELETE FROM party");
        jdbcTemplate.update("DELETE FROM sub_product");
        jdbcTemplate.update("DELETE FROM users");
    }

    private List<Notification> notificationsOf(Long userId) {
        return notificationMapper.findByUserId(userId);
    }

    private List<NotificationType> typesOf(Long userId) {
        return notificationsOf(userId)
                .stream()
                .map(Notification::getType)
                .toList();
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
    @DisplayName("파티장 provision 12h 리마인드 — HOST_PROVISION_REMINDER")
    void hostProvisionReminder12h() {
        // given
        jdbcTemplate.update("""
            UPDATE party_operation
            SET created_at = DATEADD('HOUR', -12, CURRENT_TIMESTAMP)
            WHERE id = ?
        """, PROVISION_ID);

        // when
        provisionTimeoutService.processHostProvisionReminders();

        waitAsync();

        // then
        assertThat(typesOf(HOST_USER_ID))
                .contains(NotificationType.HOST_PROVISION_REMINDER);
    }

    @Test
    @DisplayName("파티장 provision 22h 리마인드 — HOST_PROVISION_REMINDER")
    void hostProvisionReminder22h() {
        // given
        jdbcTemplate.update("""
            UPDATE party_operation
            SET created_at = DATEADD('HOUR', -22, CURRENT_TIMESTAMP)
            WHERE id = ?
        """, PROVISION_ID);

        // when
        provisionTimeoutService.processHostProvisionReminders();

        waitAsync();

        // then
        assertThat(typesOf(HOST_USER_ID))
                .contains(NotificationType.HOST_PROVISION_REMINDER);
    }

    @Test
    @DisplayName("파티장 24h 미등록 → 파티원 HOST_PROVISION_DELAYED_NOTICE")
    void hostDelayedNotice() {
        // given
        jdbcTemplate.update("""
            UPDATE party_operation
            SET created_at = DATEADD('HOUR', -25, CURRENT_TIMESTAMP)
            WHERE id = ?
        """, PROVISION_ID);

        // when
        provisionTimeoutService.processHostDelayedNotice();

        waitAsync();

        // then
        assertThat(typesOf(MEMBER_USER_ID))
                .contains(NotificationType.HOST_PROVISION_DELAYED_NOTICE);
    }

    @Test
    @DisplayName("파티장 48h 미등록 → 파티 해체 + 자동 재매칭 알림")
    void hostTimeout() {
        // given
        jdbcTemplate.update("""
            UPDATE party_operation
            SET created_at = DATEADD('HOUR', -49, CURRENT_TIMESTAMP)
            WHERE id = ?
        """, PROVISION_ID);

        // when
        provisionTimeoutService.processHostTimeout();

        waitAsync();

        // then
        assertThat(typesOf(HOST_USER_ID))
                .contains(NotificationType.HOST_PROVISION_TIMEOUT_TERMINATED);

        assertThat(typesOf(MEMBER_USER_ID))
                .contains(NotificationType.PARTY_TERMINATED);

        assertThat(typesOf(MEMBER_USER_ID))
                .contains(NotificationType.MEMBER_AUTO_REMATCH_STARTED);

        String partyStatus = jdbcTemplate.queryForObject(
                "SELECT operation_status FROM party WHERE id = ?",
                String.class,
                PARTY_ID
        );

        assertThat(partyStatus).isEqualTo("TERMINATED");

        Integer queueCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM match_waiting_queue WHERE user_id = ?",
                Integer.class,
                MEMBER_USER_ID
        );

        assertThat(queueCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("파티원 provision 12h 리마인드")
    void memberProvisionReminder12h() {
        // given
        jdbcTemplate.update("""
            UPDATE party_operation_member
            SET invite_sent_at = DATEADD('HOUR', -12, CURRENT_TIMESTAMP)
            WHERE id = ?
        """, PROVISION_MEMBER_ID);

        // when
        provisionTimeoutService.processMemberProvisionReminders();

        waitAsync();

        // then
        assertThat(typesOf(MEMBER_USER_ID))
                .containsAnyOf(
                        NotificationType.PROVISION_ACCOUNT_SHARED_REMINDER,
                        NotificationType.PROVISION_INVITE_ACCEPT_REQUIRED
                );
    }

    @Test
    @DisplayName("파티원 provision 22h 리마인드")
    void memberProvisionReminder22h() {
        // given
        jdbcTemplate.update("""
            UPDATE party_operation_member
            SET invite_sent_at = DATEADD('HOUR', -22, CURRENT_TIMESTAMP)
            WHERE id = ?
        """, PROVISION_MEMBER_ID);

        // when
        provisionTimeoutService.processMemberProvisionReminders();

        waitAsync();

        // then
        assertThat(typesOf(MEMBER_USER_ID))
                .containsAnyOf(
                        NotificationType.PROVISION_ACCOUNT_SHARED_REMINDER,
                        NotificationType.PROVISION_INVITE_ACCEPT_REQUIRED
                );
    }

    @Test
    @DisplayName("파티원 must_complete_by 초과 → MEMBER_PROVISION_TIMEOUT_NOTICE")
    void memberTimeout() {
        // given
        jdbcTemplate.update("""
            UPDATE party_operation_member
            SET must_complete_by = DATEADD('HOUR', -1, CURRENT_TIMESTAMP)
            WHERE id = ?
        """, PROVISION_MEMBER_ID);

        // when
        provisionTimeoutService.processMemberTimeout();

        waitAsync();

        // then
        assertThat(typesOf(MEMBER_USER_ID))
                .contains(NotificationType.MEMBER_PROVISION_TIMEOUT_NOTICE);

        Boolean penaltyApplied = jdbcTemplate.queryForObject(
                "SELECT penalty_applied FROM party_operation_member WHERE id = ?",
                Boolean.class,
                PROVISION_MEMBER_ID
        );

        assertThat(penaltyApplied).isTrue();
    }
}