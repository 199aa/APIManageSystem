-- 平台管理模块升级脚本
-- 为platform表添加认证配置、健康状态、Swagger地址等字段

USE api_manage;

-- 添加新字段
ALTER TABLE `platform` 
ADD COLUMN `auth_type` VARCHAR(20) DEFAULT 'NO_AUTH' COMMENT '认证类型: NO_AUTH, API_KEY, BEARER_TOKEN, BASIC_AUTH' AFTER `base_url`,
ADD COLUMN `auth_config` TEXT COMMENT '认证配置(JSON格式,加密存储)' AFTER `auth_type`,
ADD COLUMN `health_status` VARCHAR(20) DEFAULT 'UNKNOWN' COMMENT '健康状态: ONLINE, OFFLINE, UNKNOWN' AFTER `auth_config`,
ADD COLUMN `last_check_time` DATETIME COMMENT '最后检测时间' AFTER `health_status`,
ADD COLUMN `swagger_url` VARCHAR(500) COMMENT 'Swagger文档地址' AFTER `last_check_time`,
ADD COLUMN `check_interval` INT DEFAULT 300 COMMENT '检测频率(秒)' AFTER `swagger_url`,
ADD COLUMN `environment` VARCHAR(20) DEFAULT 'PROD' COMMENT '环境: DEV, TEST, PROD' AFTER `check_interval`,
ADD COLUMN `timeout` INT DEFAULT 5000 COMMENT '超时时间(毫秒)' AFTER `environment`,
ADD COLUMN `retry_count` INT DEFAULT 3 COMMENT '重试次数' AFTER `timeout`;

-- 添加索引
ALTER TABLE `platform` ADD INDEX `idx_health_status` (`health_status`);
ALTER TABLE `platform` ADD INDEX `idx_environment` (`environment`);

-- 更新现有数据的默认值
UPDATE `platform` SET `auth_type` = 'NO_AUTH', `health_status` = 'UNKNOWN', `environment` = 'PROD' WHERE `auth_type` IS NULL;

-- 查看表结构
DESC `platform`;
