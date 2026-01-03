-- 创建客户应用表
CREATE TABLE IF NOT EXISTS `customer_app` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '应用ID',
  `name` VARCHAR(100) NOT NULL COMMENT '应用名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '应用描述',
  `icon` VARCHAR(50) DEFAULT 'el-icon-s-platform' COMMENT '图标样式',
  `color` VARCHAR(20) DEFAULT '#667eea' COMMENT '主题色',
  `app_key` VARCHAR(50) NOT NULL UNIQUE COMMENT '应用Key',
  `app_secret` VARCHAR(100) NOT NULL COMMENT '应用密钥',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `user_id` BIGINT(20) NOT NULL COMMENT '所属用户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_app_key` (`app_key`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户应用表';

-- 创建API授权表（用于记录应用与API的授权关系）
CREATE TABLE IF NOT EXISTS `api_authorization` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '授权ID',
  `app_id` BIGINT(20) NOT NULL COMMENT '应用ID',
  `api_id` BIGINT(20) NOT NULL COMMENT '接口ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_api` (`app_id`, `api_id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_api_id` (`api_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API授权表';
