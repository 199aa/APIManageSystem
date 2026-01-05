USE api_manage;
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
    DATE_SUB(NOW(), INTERVAL 10 MINUTE),
    1,
    '{"current_value": 1250, "threshold": 1000, "duration": "5m"}'
  ),
  (
    '平台流量异常',
    'info',
    '平台百度调用量在过去10分钟下降60%',
    '平台: 百度',
    'firing',
    DATE_SUB(NOW(), INTERVAL 5 MINUTE),
    NULL,
    '{"platform_id": 1, "drop_percent": 60, "window": "10m"}'
  );
SELECT COUNT(*) as alert_count
FROM alert;