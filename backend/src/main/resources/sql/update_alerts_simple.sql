-- 只插入测试告警记录
USE api_manage;

-- 先删除可能存在的旧数据
DELETE FROM alert WHERE id > 0;

-- 插入测试告警记录
INSERT INTO alert (rule_name, level, content, target, status, alert_time, api_id, details) VALUES
('API响应时间告警', 'warning', 'API /api/user/info 平均响应时间超过1000ms，当前值：1250ms', 'API: /api/user/info', 'firing', NOW() - INTERVAL 10 MINUTE, 1, '{"current_value": 1250, "threshold": 1000, "duration": "5m"}'),
('平台流量异常', 'info', '平台百度调用量在过去10分钟下降60%', '平台: 百度', 'firing', NOW() - INTERVAL 5 MINUTE, NULL, '{"platform_id": 1, "drop_percent": 60, "window": "10m"}');

-- 查看结果
SELECT '告警统计:' as info;
SELECT level, status, COUNT(*) as count FROM alert GROUP BY level, status;

SELECT '最新告警:' as info;
SELECT * FROM alert ORDER BY alert_time DESC LIMIT 5;
