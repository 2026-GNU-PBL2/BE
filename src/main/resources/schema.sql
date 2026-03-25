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
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,,
                       deleted_at DATETIME NULL,
                       CONSTRAINT pk_users PRIMARY KEY (id),
                       CONSTRAINT uq_users_nickname UNIQUE (nickname),
                       CONSTRAINT uq_users_submate_email UNIQUE (submate_email),
                       CONSTRAINT uq_users_phone_number UNIQUE (phone_number)
);