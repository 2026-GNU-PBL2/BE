-- test (2026.01.26 / kyh)
CREATE TABLE test (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50),
    amount BIGINT
);

--bankAccount (2026.04.09 / kyh)
DROP TABLE IF EXISTS bank_accounts;

CREATE TABLE IF NOT EXISTS bank_accounts (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             user_id BIGINT NOT NULL,
                                             fintech_use_num VARCHAR(24) NOT NULL,
    access_token VARCHAR(1000) NOT NULL,
    refresh_token VARCHAR(1000) NOT NULL,
    bank_tran_id VARCHAR(20),
    bank_name VARCHAR(50),
    account_alias VARCHAR(100),
    account_num_masked VARCHAR(20),
    balance_amt BIGINT DEFAULT 0,

    bank_code VARCHAR(10),
    account_number VARCHAR(50),
    account_holder_name VARCHAR(100),
    account_holder_birth_date CHAR(8),
    account_type VARCHAR(20),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    verified_at TIMESTAMP NULL,
    last_verified_at TIMESTAMP NULL,
    fail_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE UNIQUE INDEX IF NOT EXISTS uk_bank_accounts_user_fintech
    ON bank_accounts(user_id, fintech_use_num);

CREATE INDEX IF NOT EXISTS idx_bank_accounts_user_primary
    ON bank_accounts(user_id, is_primary);

CREATE UNIQUE INDEX IF NOT EXISTS uk_bank_accounts_user_fintech
    ON bank_accounts(user_id, fintech_use_num);

-- sub_product (2026.03.23 / kyh)
CREATE TABLE IF NOT EXISTS sub_product (
    id               VARCHAR(36)     NOT NULL,
    service_name     VARCHAR(100)    NOT NULL,
    description      TEXT            NULL,
    thumbnail_url    VARCHAR(500)    NULL,
    operation_type   VARCHAR(20)     NOT NULL,
    category         VARCHAR(30)     NOT NULL DEFAULT 'NETFLIX',
    max_member_count INT             NOT NULL,
    base_price       BIGINT          NOT NULL,
    price_per_member BIGINT          NOT NULL,
    status           VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
    );

CREATE UNIQUE INDEX IF NOT EXISTS uk_sub_product_service_name
    ON sub_product(service_name);

--oauth_user (2026.03.24/khj)
CREATE TABLE oauth_user (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            social_provider VARCHAR(30) NOT NULL,
                            social_id VARCHAR(100) NOT NULL,
                            email VARCHAR(255) NULL,
                            name VARCHAR(100) NULL,
                            profile_image_url VARCHAR(500) NULL,
                            user_role VARCHAR(20) NOT NULL,
                            user_id BIGINT NULL,
                            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT pk_oauth_user PRIMARY KEY (id),
                            CONSTRAINT uq_oauth_user_provider_social UNIQUE (social_provider, social_id)
);

--users (2026.03.24/khj)
CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       nickname VARCHAR(30) NULL,
                       submate_email VARCHAR(100) NULL,
                       phone_number VARCHAR(20) NULL,
                       pin_hash VARCHAR(255) NULL,
                       role VARCHAR(20) NOT NULL,
                       status VARCHAR(30) NOT NULL,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       deleted_at DATETIME NULL,
                       CONSTRAINT pk_users PRIMARY KEY (id),
                       CONSTRAINT uq_users_nickname UNIQUE (nickname),
                       CONSTRAINT uq_users_submate_email UNIQUE (submate_email),
                       CONSTRAINT uq_users_phone_number UNIQUE (phone_number)
);

-- sub_product (2026.03.23 / kyh)
CREATE TABLE IF NOT EXISTS sub_product (
                                           id               VARCHAR(36)     NOT NULL,
                                           service_name     VARCHAR(100)    NOT NULL,
                                           description      TEXT            NULL,
                                           thumbnail_url    VARCHAR(500)    NULL,
                                           operation_type   VARCHAR(20)     NOT NULL,
                                           category         VARCHAR(30)     NOT NULL DEFAULT 'NETFLIX',
                                           max_member_count INT             NOT NULL,
                                           base_price       BIGINT          NOT NULL,
                                           price_per_member BIGINT          NOT NULL,
                                           status           VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
                                           created_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           updated_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                           PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sub_product_service_name
    ON sub_product(service_name);


-- received_mail (2026.03.28 / kyh )
CREATE TABLE IF NOT EXISTS received_mail (
                                             id BIGINT NOT NULL AUTO_INCREMENT,
                                             user_id BIGINT NOT NULL,
                                             sender VARCHAR(255),
                                             subject VARCHAR(500),
                                             body CLOB,
                                             raw_s3_key VARCHAR(512),
                                             received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_received_mail_user_id
    ON received_mail(user_id);

-- party (2026.03.28/khj)
CREATE TABLE party (
                       id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       product_id VARCHAR(36) NOT NULL,
                       host_user_id BIGINT NOT NULL,
                       capacity INT NOT NULL,
                       current_member_count INT NOT NULL,
                       recruit_status VARCHAR(30) NOT NULL,
                       operation_status VARCHAR(30) NOT NULL,
                       vacancy_type VARCHAR(30) NULL,
                       created_at DATETIME NOT NULL,
                       updated_at DATETIME NOT NULL,
                       terminated_at DATETIME NULL,
                       price_per_member_snapshot INT NOT NULL
);

-- party_member (2026.03.28/khj)
CREATE TABLE party_member (
                              id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                              party_id BIGINT NOT NULL,
                              user_id BIGINT NOT NULL,
                              role VARCHAR(20) NOT NULL,
                              status VARCHAR(30) NOT NULL,
                              joined_at DATETIME NOT NULL,
                              activated_at DATETIME NULL,
                              service_start_at DATETIME NULL,
                              service_end_at DATETIME NULL,
                              leave_reserved_at DATETIME NULL,
                              left_at DATETIME NULL,
                              replaced_target_member_id BIGINT NULL,
                              CONSTRAINT uq_party_member_party_user UNIQUE (party_id, user_id)
);

-- party_history (2026.03.28/khj)
CREATE TABLE party_history (
                               id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                               party_id BIGINT NOT NULL,
                               member_id BIGINT NULL,
                               event_type VARCHAR(50) NOT NULL,
                               event_payload TEXT NULL,
                               created_at DATETIME NOT NULL,
                               created_by BIGINT NOT NULL
);

CREATE INDEX idx_party_product ON party(product_id);
CREATE INDEX idx_party_member_party_status ON party_member(party_id, status);
CREATE INDEX idx_party_member_user ON party_member(user_id);

-- match_waiting_queue(2026.03.29/khj)
CREATE TABLE match_waiting_queue (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                     product_id VARCHAR(100) NOT NULL,
                                     user_id BIGINT NOT NULL,
                                     status VARCHAR(30) NOT NULL,
                                     requested_at DATETIME NOT NULL,
                                     matched_at DATETIME NULL,
                                     canceled_at DATETIME NULL,
                                     target_party_id BIGINT NULL,
                                     created_at DATETIME NOT NULL,
                                     updated_at DATETIME NOT NULL
);

CREATE INDEX idx_match_waiting_product_status_requested
    ON match_waiting_queue(product_id, status, requested_at, id);

CREATE INDEX idx_match_waiting_user_status
    ON match_waiting_queue(user_id, status);

-- host_transfer_request(2026.04.01/khj)
CREATE TABLE host_transfer_request
(
    id                BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    party_id          BIGINT      NOT NULL,
    requester_user_id BIGINT      NOT NULL,
    target_user_id    BIGINT      NOT NULL,
    status            VARCHAR(30) NOT NULL,
    requested_at      DATETIME    NOT NULL,
    responded_at      DATETIME    NULL,
    completed_at      DATETIME    NULL,
    created_at        DATETIME    NOT NULL,
    updated_at        DATETIME    NOT NULL
);

CREATE INDEX idx_host_transfer_request_party_id
    ON host_transfer_request(party_id);

CREATE INDEX idx_host_transfer_request_target_user_id
    ON host_transfer_request(target_user_id);

CREATE INDEX idx_host_transfer_request_party_status
    ON host_transfer_request(party_id, status);



-- billing_key (2026.03.28 / kyh)
CREATE TABLE IF NOT EXISTS billing_key (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    user_id             BIGINT          NOT NULL,
    billing_key         VARCHAR(255)    NOT NULL,
    customer_key        VARCHAR(255)    NOT NULL,
    provider            VARCHAR(50)     NOT NULL DEFAULT 'TOSS',
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    card_company        VARCHAR(50)     NULL,
    masked_card_number  VARCHAR(50)     NULL,
    issued_at           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at          DATETIME        NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_billing_key_user_id (user_id)
    );

-- party_CYLCE (2026.04.03 / kyh )
CREATE TABLE party_cycle (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    party_id BIGINT NOT NULL,
    cycle_no INT NOT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NULL,
    billing_due_at DATETIME NOT NULL,
    status VARCHAR(30) NOT NULL,
    member_count_snapshot INT NOT NULL,
    price_per_member_snapshot INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE UNIQUE INDEX uk_party_cycle_party_cycle_no
    ON party_cycle(party_id, cycle_no);

CREATE INDEX idx_party_cycle_party_status
    ON party_cycle(party_id, status);

-- party_operation (2026.04.03 / khj)
CREATE TABLE IF NOT EXISTS party_operation (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    party_id BIGINT NOT NULL,
    operation_type VARCHAR(30) NOT NULL,
    operation_status VARCHAR(30) NOT NULL,
    invite_value VARCHAR(500) NULL,
    shared_account_email VARCHAR(255) NULL,
    shared_account_password_encrypted VARCHAR(500) NULL,
    operation_guide TEXT NULL,
    operation_started_at DATETIME NULL,
    operation_completed_at DATETIME NULL,
    last_reset_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uq_party_operation_party UNIQUE (party_id)
);


-- party_operation_member (2026.04.03 / khj)
CREATE TABLE IF NOT EXISTS party_operation_member
(
    id                 BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    party_operation_id BIGINT       NOT NULL,
    party_member_id    BIGINT       NOT NULL,
    party_id           BIGINT       NOT NULL,
    user_id            BIGINT       NOT NULL,
    member_status      VARCHAR(30)  NOT NULL,
    invite_sent_at     DATETIME     NULL,
    must_complete_by   DATETIME     NULL,
    confirmed_at       DATETIME     NULL,
    completed_at       DATETIME     NULL,
    activated_at       DATETIME     NULL,
    last_reset_at      DATETIME     NULL,
    penalty_applied    TINYINT(1)   NOT NULL DEFAULT 0,
    operation_message  VARCHAR(500) NULL,
    created_at         DATETIME     NOT NULL,
    updated_at         DATETIME     NOT NULL
);

CREATE UNIQUE INDEX uk_party_operation_member_operation_member
    ON party_operation_member(party_operation_id, party_member_id);

-- settlement (2026.04.04 / kyh)
-- fee_deducted 추가 (2026.04.26 / kyh) : 파티원 수수료 전액 + 파티장 수수료 공제 합산
CREATE TABLE settlement (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    party_id BIGINT NOT NULL,
    party_cycle_id BIGINT NOT NULL,
    host_user_id BIGINT NOT NULL,
    member_count INT NOT NULL,
    unit_amount INT NOT NULL,
    total_amount BIGINT NOT NULL,
    fee_deducted BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE UNIQUE INDEX uk_settlement_party_cycle
    ON settlement(party_cycle_id);


-- point_wallet (2026.04.04 / kyh)
CREATE TABLE point_wallet (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    balance BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE INDEX idx_party_operation_member_operation_id
    ON party_operation_member(party_operation_id);

CREATE INDEX idx_party_operation_member_party_id
    ON party_operation_member(party_id);

CREATE INDEX idx_party_operation_member_user_id
    ON party_operation_member(user_id);
CREATE UNIQUE INDEX uk_point_wallet_user
    ON point_wallet(user_id);

-- notification (2026.04.28 / khj)
CREATE TABLE notification (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              party_id BIGINT NULL,
                              type VARCHAR(80) NOT NULL,
                              title VARCHAR(255) NOT NULL,
                              content TEXT NOT NULL,
                              status VARCHAR(30) NOT NULL,
                              is_read BOOLEAN NOT NULL DEFAULT FALSE,
                              scheduled_at DATETIME NULL,
                              sent_at DATETIME NULL,
                              read_at DATETIME NULL,
                              created_at DATETIME NOT NULL,
                              updated_at DATETIME NOT NULL
);

CREATE INDEX idx_notification_user_id ON notification(user_id);
CREATE INDEX idx_notification_status_scheduled_at ON notification(status, scheduled_at);

-- party_cycle_member_payment (2026.04 / kyh)
CREATE TABLE party_cycle_member_payment (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    party_cycle_id  BIGINT          NOT NULL,
    party_id        BIGINT          NOT NULL,
    party_member_id BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    amount          INT             NOT NULL,
    status          VARCHAR(30)     NOT NULL,
    failure_reason  VARCHAR(255)    NULL,
    failure_code    VARCHAR(100)    NULL,
    external_tx_id  VARCHAR(255)    NULL,
    idempotency_key VARCHAR(255)    NOT NULL,
    paid_at         DATETIME        NULL,
    failed_at       DATETIME        NULL,
    created_at      DATETIME        NOT NULL,
    updated_at      DATETIME        NOT NULL,
    UNIQUE KEY uk_pcmp_idempotency (idempotency_key)
);

CREATE INDEX idx_pcmp_party_cycle_id ON party_cycle_member_payment(party_cycle_id);
CREATE INDEX idx_pcmp_user_id ON party_cycle_member_payment(user_id);

-- sms_send_log (2026.04.28 / khj)
CREATE TABLE sms_send_log (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              notification_id BIGINT NULL,
                              user_id BIGINT NOT NULL,
                              phone_number VARCHAR(30) NOT NULL,
                              content TEXT NOT NULL,
                              status VARCHAR(30) NOT NULL,
                              fail_reason TEXT NULL,
                              created_at DATETIME NOT NULL
);