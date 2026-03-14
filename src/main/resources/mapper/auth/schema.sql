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