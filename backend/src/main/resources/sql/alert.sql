-- 创建告警表
CREATE TABLE IF NOT EXISTS `alert` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '告警ID',
  `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `level` VARCHAR(20) NOT NULL COMMENT '告警级别：critical, warning, info',
  `content` VARCHAR(500) NOT NULL COMMENT '告警内容',
  `target` VARCHAR(200) DEFAULT NULL COMMENT '告警对象',
  `status` VARCHAR(20) DEFAULT 'firing' COMMENT '状态：firing-触发中, resolved-已恢复, acknowledged-已确认',
  `trace_id` VARCHAR(100) DEFAULT NULL COMMENT '关联的TraceID',
  `api_id` BIGINT(20) DEFAULT NULL COMMENT '关联的API ID',
  `platform_id` BIGINT(20) DEFAULT NULL COMMENT '关联的平台ID',
  `details` TEXT DEFAULT NULL COMMENT '详细信息（JSON）',
  `alert_time` DATETIME NOT NULL COMMENT '告警时间',
  `resolved_time` DATETIME DEFAULT NULL COMMENT '恢复时间',
  `acknowledged_time` DATETIME DEFAULT NULL COMMENT '确认时间',
  `acknowledged_by` VARCHAR(50) DEFAULT NULL COMMENT '确认人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_level` (`level`),
  KEY `idx_status` (`status`),
  KEY `idx_alert_time` (`alert_time`),
  KEY `idx_api_id` (`api_id`),
  KEY `idx_platform_id` (`platform_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '告警表';
-- 创建告警规则表
CREATE TABLE IF NOT EXISTS `alert_rule` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `type` VARCHAR(50) NOT NULL COMMENT '规则类型：error_rate, response_time, traffic',
  `level` VARCHAR(20) NOT NULL COMMENT '告警级别：critical, warning, info',
  `condition` TEXT NOT NULL COMMENT '触发条件（JSON）',
  `target` VARCHAR(50) NOT NULL COMMENT '监控目标：api, platform, system',
  `target_id` BIGINT(20) DEFAULT NULL COMMENT '目标ID',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `notify_channels` VARCHAR(200) DEFAULT NULL COMMENT '通知渠道（JSON）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_target` (`target`, `target_id`),
  KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '告警规则表';
-- 插入一些示例告警规则
INSERT INTO alert_rule (
    name,
    type,
    level,
    `condition`,
    target,
    target_id,
    status
  )
VALUES (
    'API错误率告警',
    'error_rate',
    'critical',
    '{"threshold": 5, "window": "5m"}',
    'api',
    NULL,
    1
  ),
  (
    'API响应时间告警',
    'response_time',
    'warning',
    '{"threshold": 1000, "window": "5m"}',
    'api',
    NULL,
    1
  ),
  (
    '平台流量异常',
    'traffic',
    'info',
    '{"threshold_type": "drop", "percent": 50, "window": "10m"}',
    'platform',
    NULL,
    1
  );
-- 插入一些测试告警记录（模拟实际触发的告警）
INSERT INTO alert (
    rule_name,
    level,
    content,
    target,
    status,
    alert_time,
    api_id,
    details
  )
VALUES (
    'API响应时间告警',
    'warning',
    'API /api/user/info 平均响应时间超过1000ms，当前值：1250ms',
    'API: /api/user/info',
    'firing',
    NOW() - INTERVAL 10 MINUTE,
    1,
    '{"current_value": 1250, "threshold": 1000, "duration": "5m"}'
  ),
  (
    '平台流量异常',
    'info',
    '平台「百度」调用量在过去10分钟下降60%',
    '平台: 百度',
    'firing',
    NOW() - INTERVAL 5 MINUTE,
    NULL,
    '{"platform_id": 1, "drop_percent": 60, "window": "10m"}'
  );