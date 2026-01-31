-- 测试告警规则 - 更容易触发的阈值
-- 运行此SQL后，重新启动后端，等待约1分钟后即可在告警中心看到告警

-- 首先删除旧的测试规则（可选）
-- DELETE FROM alert WHERE rule_name LIKE '%测试%';
-- DELETE FROM alert_rule WHERE name LIKE '%测试%';

-- 插入测试告警规则
INSERT INTO alert_rule (name, type, level, `condition`, target, target_id, status)
VALUES 
-- 调用量超过1次就告警（测试用，容易触发）
('测试-调用量告警', 'call_count', 'info', '{"threshold": 1, "window": "1m"}', 'system', NULL, 1),

-- 错误率超过1%就告警（测试用）
('测试-错误率告警', 'error_rate', 'warning', '{"threshold": 1, "window": "5m"}', 'system', NULL, 1),

-- 响应时间超过10ms就告警（测试用，容易触发）
('测试-响应时间告警', 'response_time', 'info', '{"threshold": 10, "window": "5m"}', 'system', NULL, 1);

-- 查看现有规则
SELECT * FROM alert_rule WHERE status = 1;

-- 查看现有告警
SELECT * FROM alert ORDER BY alert_time DESC LIMIT 10;
