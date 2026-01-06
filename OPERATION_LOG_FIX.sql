-- ===================================
-- 操作日志问题修复脚本
-- ===================================
USE api_manage;
-- 1. 检查表是否存在
SELECT TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'api_manage'
    AND TABLE_NAME = 'operation_log';
-- 2. 检查表结构
DESCRIBE operation_log;
-- 3. 查看现有数据
SELECT COUNT(*) as total_count
FROM operation_log;
SELECT *
FROM operation_log
ORDER BY oper_time DESC
LIMIT 5;
-- 4. 清空旧数据（可选）
-- TRUNCATE TABLE operation_log;
-- 5. 插入测试数据
DELETE FROM operation_log
WHERE username IN ('admin', 'test');
INSERT INTO operation_log (
        user_id,
        username,
        oper_type,
        module,
        description,
        request_method,
        request_url,
        request_params,
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
        '{"username":"admin"}',
        '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0',
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
        '{"name":"测试平台","code":"test"}',
        '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0',
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
        '{"id":1,"name":"测试API"}',
        '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0',
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
        '{"username":"test"}',
        '192.168.1.100',
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Safari/537.36',
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
        '{"userId":5}',
        '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0',
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
        '{"appName":"测试应用"}',
        '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0',
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
        '{"id":1,"status":0}',
        '192.168.1.100',
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Safari/537.36',
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
        NULL,
        '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0',
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
        '{"name":"测试角色","code":"test_role"}',
        '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0',
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
        '{"username":"test"}',
        '192.168.1.100',
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Safari/537.36',
        1,
        167,
        DATE_SUB(NOW(), INTERVAL 5 DAY)
    ),
    (
        1,
        'admin',
        'UPDATE',
        'user',
        '更新用户信息',
        'PUT',
        '/system/user/update/2',
        '{"id":2,"status":1}',
        '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0',
        1,
        203,
        DATE_SUB(NOW(), INTERVAL 6 DAY)
    ),
    (
        1,
        'admin',
        'DELETE',
        'api',
        '删除API',
        'DELETE',
        '/api-info/delete/10',
        '{"apiId":10}',
        '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0',
        1,
        178,
        DATE_SUB(NOW(), INTERVAL 7 DAY)
    );
-- 6. 验证数据插入
SELECT COUNT(*) as inserted_count
FROM operation_log;
SELECT username,
    oper_type,
    module,
    description,
    status,
    DATE_FORMAT(oper_time, '%Y-%m-%d %H:%i:%s') as operation_time
FROM operation_log
ORDER BY oper_time DESC
LIMIT 15;
-- 7. 按操作类型统计
SELECT oper_type as operation_type,
    COUNT(*) as count
FROM operation_log
GROUP BY oper_type;
-- 8. 按用户统计
SELECT username,
    COUNT(*) as operation_count
FROM operation_log
GROUP BY username;
SELECT '操作日志数据已成功插入！请刷新前端页面查看。' as message;