ALTER TABLE users
ADD COLUMN otp_code varchar(6) DEFAULT NULL,
ADD COLUMN otp_expiry datetime DEFAULT NULL;