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


