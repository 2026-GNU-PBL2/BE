-- test (2026.01.26 / kyh)
CREATE TABLE test (
                      id BIGINT PRIMARY KEY,
                      name VARCHAR(50),
                      amount BIGINT
);

--bankAccount (2026.01.26 / kyh)
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
                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_bank_accounts_user_fintech
    ON bank_accounts(user_id, fintech_use_num);

-- sub_product (2026.03.23 / kyh)
CREATE TABLE IF NOT EXISTS sub_product (
                                           id               VARCHAR(36)     NOT NULL,
    service_name     VARCHAR(100)    NOT NULL,
    description      TEXT            NULL,
    thumbnail_url    VARCHAR(500)    NULL,
    operation_type   VARCHAR(20)     NOT NULL,
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
