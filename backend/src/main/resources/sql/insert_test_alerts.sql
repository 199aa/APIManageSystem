-- ========================================
-- 添加告警测试数据
-- 用于让告警中心页面显示更完整
-- ========================================

USE api_manage;

-- 清空现有告警记录（可选）
-- DELETE FROM alert WHERE id > 0;

-- 插入测试告警记录（模拟实际触发的告警）
INSERT INTO alert (rule_name, level, content, target, status, alert_time, api_id, details) VALUES
('API错误率告警', 'critical', 'API /api/user/login 错误率超过5%，当前值：8.5%', 'API: /api/user/login', 'firing', NOW() - INTERVAL 30 MINUTE, 1, '{"current_value": 8.5, "threshold": 5, "window": "5m"}'),
('API响应时间告警', 'warning', 'API /api/user/info 平均响应时间超过1000ms，当前值：1250ms', 'API: /api/user/info', 'firing', NOW() - INTERVAL 10 MINUTE, 2, '{"current_value": 1250, "threshold": 1000, "duration": "5m"}'),
('平台流量异常', 'info', '平台「百度」调用量在过去10分钟下降60%', '平台: 百度', 'firing', NOW() - INTERVAL 5 MINUTE, NULL, '{"platform_id": 1, "drop_percent": 60, "window": "10m"}'),
('API错误率告警', 'critical', 'API /api/payment/create 错误率超过5%，当前值：12%', 'API: /api/payment/create', 'resolved', NOW() - INTERVAL 2 HOUR, 3, '{"current_value": 12, "threshold": 5, "window": "5m"}');

-- 查看告警统计
SELECT 
  level,
  status,
  COUNT(*) as count
FROM alert
GROUP BY level, status
ORDER BY level, status;

-- 查看最新的告警
SELECT * FROM alert ORDER BY alert_time DESC LIMIT 10;

