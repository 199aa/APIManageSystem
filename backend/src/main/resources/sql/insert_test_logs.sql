-- 插入测试操作日志数据
-- 用于验证操作日志功能
USE api_manage;
-- 插入测试日志记录
INSERT INTO operation_log (
    user_id,
    username,
    oper_type,
    module,
    description,
    request_method,
    request_url,
    ip,
    browser,
    status,
    cost_time,
    oper_time
  )
VALUES (
    1,
    'admin',
    'LOGIN',
    'user',
    '用户登录',
    'POST',
    '/user/login',
    '127.0.0.1',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
    1,
    156,
    NOW()
  ),
  (
    1,
    'admin',
    'CREATE',
    'platform',
    '创建平台',
    'POST',
    '/platform/create',
    '127.0.0.1',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
    1,
    234,
    DATE_SUB(NOW(), INTERVAL 1 HOUR)
  ),
  (
    1,
    'admin',
    'UPDATE',
    'api',
    '更新API信息',
    'PUT',
    '/api-info/update/1',
    '127.0.0.1',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
    1,
    189,
    DATE_SUB(NOW(), INTERVAL 2 HOUR)
  ),
  (
    2,
    'test',
    'LOGIN',
    'user',
    '用户登录',
    'POST',
    '/user/login',
    '192.168.1.100',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)',
    1,
    145,
    DATE_SUB(NOW(), INTERVAL 3 HOUR)
  ),
  (
    1,
    'admin',
    'DELETE',
    'user',
    '删除用户',
    'DELETE',
    '/system/user/delete/5',
    '127.0.0.1',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
    1,
    267,
    DATE_SUB(NOW(), INTERVAL 5 HOUR)
  ),
  (
    1,
    'admin',
    'CREATE',
    'app',
    '创建应用',
    'POST',
    '/customer/app/create',
    '127.0.0.1',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
    1,
    312,
    DATE_SUB(NOW(), INTERVAL 1 DAY)
  ),
  (
    2,
    'test',
    'UPDATE',
    'platform',
    '更新平台信息',
    'PUT',
    '/platform/update/1',
    '192.168.1.100',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)',
    0,
    456,
    DATE_SUB(NOW(), INTERVAL 1 DAY)
  ),
  (
    1,
    'admin',
    'EXPORT',
    'api',
    '导出API列表',
    'GET',
    '/api-info/export',
    '127.0.0.1',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
    1,
    892,
    DATE_SUB(NOW(), INTERVAL 2 DAY)
  ),
  (
    1,
    'admin',
    'CREATE',
    'role',
    '创建角色',
    'POST',
    '/system/role/create',
    '127.0.0.1',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
    1,
    198,
    DATE_SUB(NOW(), INTERVAL 3 DAY)
  ),
  (
    2,
    'test',
    'LOGIN',
    'user',
    '用户登录',
    'POST',
    '/user/login',
    '192.168.1.100',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)',
    1,
    167,
    DATE_SUB(NOW(), INTERVAL 5 DAY)
  );
-- 查询验证
SELECT COUNT(*) as total_logs
FROM operation_log;
SELECT *
FROM operation_log
ORDER BY oper_time DESC
LIMIT 10;