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

-- oauth
CREATE TABLE oauth_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_role VARCHAR(30) NOT NULL,

    email VARCHAR(255) NULL,
    name VARCHAR(100) NULL,
    social_id VARCHAR(255) NOT NULL,
    social_provider VARCHAR(30) NOT NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_oauth_user_social_id (social_provider, social_id)
);