CREATE TABLE users (
    id binary(16) PRIMARY KEY NOT NULL,
    email varchar(256) NOT NULL UNIQUE,
    magic_token varchar(512) DEFAULT NULL,
    magic_token_expiry datetime DEFAULT NULL,
    created_at datetime NOT NULL
) ENGINE=InnoDB;