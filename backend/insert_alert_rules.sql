USE api_manage;
INSERT INTO alert_rule (name, type, level, `condition`, target, status)
VALUES (
    'API Error Rate Alert',
    'error_rate',
    'critical',
    '{"threshold": 5}',
    'api',
    1
  );
INSERT INTO alert_rule (name, type, level, `condition`, target, status)
VALUES (
    'API Response Time Alert',
    'response_time',
    'warning',
    '{"threshold": 1000}',
    'api',
    1
  );
INSERT INTO alert_rule (name, type, level, `condition`, target, status)
VALUES (
    'Platform Traffic Alert',
    'traffic',
    'warning',
    '{"threshold": 50}',
    'platform',
    1
  );
INSERT INTO alert (
    rule_name,
    level,
    content,
    target,
    status,
    alert_time
  )
VALUES (
    'API Error Rate Alert',
    'critical',
    'API error rate exceeded threshold',
    '/api/user/login',
    'firing',
    NOW()
  );
INSERT INTO alert (
    rule_name,
    level,
    content,
    target,
    status,
    alert_time
  )
VALUES (
    'API Response Time Alert',
    'warning',
    'API response time exceeded threshold',
    '/api/order/list',
    'acknowledged',
    DATE_SUB(NOW(), INTERVAL 2 HOUR)
  );
SELECT 'Alert rules and sample alerts inserted successfully!' as Message;