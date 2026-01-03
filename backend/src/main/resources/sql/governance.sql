-- 治理中心相关表

-- 限流策略表
CREATE TABLE IF NOT EXISTS `rate_limit` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `target_type` VARCHAR(20) NOT NULL COMMENT '目标类型：api-特定API, app-特定应用, global-全局',
  `target_id` BIGINT(20) DEFAULT NULL COMMENT '目标ID（API ID或应用ID）',
  `target_name` VARCHAR(200) DEFAULT NULL COMMENT '目标名称',
  `limit_value` INT(11) NOT NULL COMMENT '限流阈值（QPS）',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='限流策略表';

-- 黑白名单表
CREATE TABLE IF NOT EXISTS `blacklist_whitelist` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ip_address` VARCHAR(50) NOT NULL COMMENT 'IP地址',
  `list_type` VARCHAR(20) NOT NULL COMMENT '列表类型：blacklist-黑名单, whitelist-白名单',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_list_type` (`list_type`),
  INDEX `idx_ip` (`ip_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='黑白名单表';

-- 缓存规则表
CREATE TABLE IF NOT EXISTS `cache_rule` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `api_id` BIGINT(20) NOT NULL COMMENT '聚合API ID',
  `cache_key` VARCHAR(500) NOT NULL COMMENT '缓存Key规则',
  `ttl` INT(11) NOT NULL DEFAULT 300 COMMENT '缓存时长（秒）',
  `hit_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '命中率（百分比）',
  `cache_size` VARCHAR(20) DEFAULT '0KB' COMMENT '缓存大小',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_api_id` (`api_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缓存规则表';

-- 插入测试数据
INSERT INTO `rate_limit` (`name`, `target_type`, `target_id`, `target_name`, `limit_value`, `status`) VALUES
('高德API限流', 'api', 1, '地理编码接口', 100, 1),
('全局限流', 'global', NULL, '全局', 1000, 1);

INSERT INTO `blacklist_whitelist` (`ip_address`, `list_type`, `description`) VALUES
('192.168.1.100', 'blacklist', '恶意IP'),
('127.0.0.1', 'whitelist', '本地测试');

INSERT INTO `cache_rule` (`api_id`, `cache_key`, `ttl`, `hit_rate`, `cache_size`) VALUES
(1, '${apiPath}:${params.userId}', 300, 78.50, '12.5MB'),
(2, '${apiPath}:${params.orderId}', 60, 45.30, '8.2MB');
