-- Submate RDS MySQL schema
-- Generated from the uploaded H2/MySQL-mode schema.
-- Apply to the target database/schema, e.g. mysql -h <RDS_ENDPOINT> -u <USER> -p submate < submate_rds_schema_mysql.sql

SET NAMES utf8mb4;
SET time_zone = '+09:00';

CREATE TABLE IF NOT EXISTS users (
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
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_nickname (nickname),
    UNIQUE KEY uq_users_submate_email (submate_email),
    UNIQUE KEY uq_users_phone_number (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS oauth_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    social_provider VARCHAR(30) NOT NULL,
    social_id VARCHAR(100) NOT NULL,
    email VARCHAR(255) NULL,
    name VARCHAR(100) NULL,
    profile_image_url VARCHAR(500) NULL,
    user_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_oauth_user_provider_social (social_provider, social_id),
    KEY idx_oauth_user_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bank_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    fintech_use_num VARCHAR(24) NOT NULL,
    access_token VARCHAR(1000) NOT NULL,
    refresh_token VARCHAR(1000) NOT NULL,
    bank_tran_id VARCHAR(20) NULL,
    bank_name VARCHAR(50) NULL,
    account_alias VARCHAR(100) NULL,
    account_num_masked VARCHAR(20) NULL,
    balance_amt BIGINT DEFAULT 0,
    bank_code VARCHAR(10) NULL,
    account_number VARCHAR(50) NULL,
    account_holder_name VARCHAR(100) NULL,
    account_holder_birth_date CHAR(8) NULL,
    account_type VARCHAR(20) NULL,
    is_primary TINYINT(1) NOT NULL DEFAULT 0,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    verified_at DATETIME NULL,
    last_verified_at DATETIME NULL,
    fail_reason VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bank_accounts_user_fintech (user_id, fintech_use_num),
    KEY idx_bank_accounts_user_primary (user_id, is_primary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sub_product (
    id VARCHAR(36) NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    thumbnail_url VARCHAR(500) NULL,
    operation_type VARCHAR(20) NOT NULL,
    category VARCHAR(30) NOT NULL DEFAULT 'NETFLIX',
    max_member_count INT NOT NULL,
    base_price BIGINT NOT NULL,
    price_per_member BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sub_product_service_name (service_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS received_mail (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    sender VARCHAR(255) NULL,
    subject VARCHAR(500) NULL,
    body LONGTEXT NULL,
    raw_s3_key VARCHAR(512) NULL,
    received_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_received_mail_user_id (user_id),
    KEY idx_received_mail_raw_s3_key (raw_s3_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS party (
    id BIGINT NOT NULL AUTO_INCREMENT,
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
    price_per_member_snapshot INT NOT NULL,
    dissolution_date DATE NULL,
    warning_level TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_party_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS party_member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    party_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    joined_at DATETIME NOT NULL,
    activated_at DATETIME NULL,
    leave_reserved_at DATETIME NULL,
    left_at DATETIME NULL,
    replaced_target_member_id BIGINT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_party_member_party_user (party_id, user_id),
    KEY idx_party_member_party_status (party_id, status),
    KEY idx_party_member_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS party_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    party_id BIGINT NOT NULL,
    member_id BIGINT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_payload TEXT NULL,
    created_at DATETIME NOT NULL,
    created_by BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_party_history_party_id (party_id),
    KEY idx_party_history_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS match_waiting_queue (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    requested_at DATETIME NOT NULL,
    matched_at DATETIME NULL,
    canceled_at DATETIME NULL,
    target_party_id BIGINT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_match_waiting_product_status_requested (product_id, status, requested_at, id),
    KEY idx_match_waiting_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS host_transfer_request (
    id BIGINT NOT NULL AUTO_INCREMENT,
    party_id BIGINT NOT NULL,
    requester_user_id BIGINT NOT NULL,
    target_user_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    requested_at DATETIME NOT NULL,
    responded_at DATETIME NULL,
    completed_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_host_transfer_request_party_id (party_id),
    KEY idx_host_transfer_request_target_user_id (target_user_id),
    KEY idx_host_transfer_request_party_status (party_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS billing_key (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    billing_key VARCHAR(255) NOT NULL,
    customer_key VARCHAR(255) NOT NULL,
    provider VARCHAR(50) NOT NULL DEFAULT 'TOSS',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    card_company VARCHAR(50) NULL,
    masked_card_number VARCHAR(50) NULL,
    issued_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_billing_key_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS party_cycle (
    id BIGINT NOT NULL AUTO_INCREMENT,
    party_id BIGINT NOT NULL,
    cycle_no INT NOT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NULL,
    billing_due_at DATETIME NOT NULL,
    status VARCHAR(30) NOT NULL,
    member_count_snapshot INT NOT NULL,
    price_per_member_snapshot INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_party_cycle_party_cycle_no (party_id, cycle_no),
    KEY idx_party_cycle_party_status (party_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS party_operation (
    id BIGINT NOT NULL AUTO_INCREMENT,
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
    PRIMARY KEY (id),
    UNIQUE KEY uq_party_operation_party (party_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS party_operation_member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    party_operation_id BIGINT NOT NULL,
    party_member_id BIGINT NOT NULL,
    party_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    member_status VARCHAR(30) NOT NULL,
    invite_sent_at DATETIME NULL,
    must_complete_by DATETIME NULL,
    confirmed_at DATETIME NULL,
    completed_at DATETIME NULL,
    activated_at DATETIME NULL,
    last_reset_at DATETIME NULL,
    penalty_applied TINYINT(1) NOT NULL DEFAULT 0,
    operation_message VARCHAR(500) NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_party_operation_member_operation_member (party_operation_id, party_member_id),
    KEY idx_party_operation_member_operation_id (party_operation_id),
    KEY idx_party_operation_member_party_id (party_id),
    KEY idx_party_operation_member_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS settlement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    party_id BIGINT NOT NULL,
    party_cycle_id BIGINT NOT NULL,
    host_user_id BIGINT NOT NULL,
    member_count INT NOT NULL,
    unit_amount INT NOT NULL,
    total_amount BIGINT NOT NULL,
    fee_deducted BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_settlement_party_cycle (party_cycle_id),
    KEY idx_settlement_party_id (party_id),
    KEY idx_settlement_host_user_id (host_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS point_wallet (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    balance BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_point_wallet_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notification (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    party_id BIGINT NULL,
    type VARCHAR(80) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    web_content TEXT NULL,
    status VARCHAR(30) NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    scheduled_at DATETIME NULL,
    sent_at DATETIME NULL,
    read_at DATETIME NULL,
    reference_id BIGINT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_notification_user_id (user_id),
    KEY idx_notification_status_scheduled_at (status, scheduled_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS party_cycle_member_payment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    party_cycle_id BIGINT NOT NULL,
    party_id BIGINT NOT NULL,
    party_member_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    failure_reason VARCHAR(255) NULL,
    failure_code VARCHAR(100) NULL,
    external_tx_id VARCHAR(255) NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    paid_at DATETIME NULL,
    failed_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pcmp_idempotency (idempotency_key),
    KEY idx_pcmp_party_cycle_id (party_cycle_id),
    KEY idx_pcmp_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sms_send_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    notification_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    phone_number VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    fail_reason TEXT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_sms_send_log_notification_id (notification_id),
    KEY idx_sms_send_log_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS point_withdraw_request (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    bank_account_id BIGINT NOT NULL,
    bank_name_snapshot VARCHAR(50) NOT NULL,
    account_masked_snapshot VARCHAR(30) NOT NULL,
    internal_payout_ref VARCHAR(64) NOT NULL,
    requested_at DATETIME NOT NULL,
    processed_at DATETIME NULL,
    processed_by BIGINT NULL,
    reject_reason VARCHAR(500) NULL,
    external_tx_id VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_pwr_user_created (user_id, created_at),
    KEY idx_pwr_status_created (status, created_at),
    UNIQUE KEY uk_pwr_internal_payout_ref (internal_payout_ref)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS concurrent_incident (
    id BIGINT NOT NULL AUTO_INCREMENT,
    party_id BIGINT NOT NULL,
    reported_by BIGINT NULL,
    detection_source VARCHAR(30) NOT NULL,
    report_type VARCHAR(40) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    first_warned_at DATETIME NULL,
    host_deadline DATETIME NULL,
    dissolution_date DATE NULL,
    admin_escalated_at DATETIME NULL,
    resolved_at DATETIME NULL,
    web_notified TINYINT(1) NOT NULL DEFAULT 0,
    sms_notified TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_concurrent_incident_party_id (party_id),
    KEY idx_concurrent_incident_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_violation_record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    party_id BIGINT NOT NULL,
    incident_id BIGINT NULL,
    violation_type VARCHAR(40) NOT NULL,
    weight DECIMAL(3,1) NOT NULL DEFAULT 1.0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_violation_record_user_id (user_id),
    KEY idx_user_violation_record_party_id (party_id),
    KEY idx_user_violation_record_incident_id (incident_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS device_detection_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    party_id BIGINT NOT NULL,
    detected_device VARCHAR(100) NULL,
    detected_location VARCHAR(100) NULL,
    detected_at DATETIME NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    notified_user_ids TEXT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_device_detection_event_party_id (party_id),
    KEY idx_device_detection_event_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS device_detection_response (
    id BIGINT NOT NULL AUTO_INCREMENT,
    detection_event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    mine TINYINT(1) NOT NULL DEFAULT 0,
    responded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_device_response (detection_event_id, user_id),
    KEY idx_device_detection_response_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS party_member_device (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    party_id BIGINT NOT NULL,
    device_type VARCHAR(20) NOT NULL,
    os VARCHAR(50) NOT NULL,
    browser VARCHAR(50) NOT NULL,
    ip_location VARCHAR(100) NULL,
    is_vpn TINYINT(1) NOT NULL DEFAULT 0,
    registration_method VARCHAR(20) NULL,
    registered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_party_member_device (user_id, party_id, device_type, os, browser),
    KEY idx_party_member_device_party_id (party_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
