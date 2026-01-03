USE api_manage;

ALTER TABLE platform 
ADD COLUMN auth_type VARCHAR(20) DEFAULT 'NO_AUTH' COMMENT 'Auth type' AFTER base_url,
ADD COLUMN auth_config TEXT COMMENT 'Auth config JSON encrypted' AFTER auth_type,
ADD COLUMN health_status VARCHAR(20) DEFAULT 'UNKNOWN' COMMENT 'Health status' AFTER auth_config,
ADD COLUMN last_check_time DATETIME COMMENT 'Last check time' AFTER health_status,
ADD COLUMN swagger_url VARCHAR(500) COMMENT 'Swagger URL' AFTER last_check_time,
ADD COLUMN check_interval INT DEFAULT 300 COMMENT 'Check interval seconds' AFTER swagger_url,
ADD COLUMN environment VARCHAR(20) DEFAULT 'PROD' COMMENT 'Environment' AFTER check_interval,
ADD COLUMN timeout INT DEFAULT 5000 COMMENT 'Timeout milliseconds' AFTER environment,
ADD COLUMN retry_count INT DEFAULT 3 COMMENT 'Retry count' AFTER timeout;

ALTER TABLE platform ADD INDEX idx_health_status (health_status);
ALTER TABLE platform ADD INDEX idx_environment (environment);

UPDATE platform SET auth_type = 'NO_AUTH', health_status = 'UNKNOWN', environment = 'PROD' WHERE auth_type IS NULL;
