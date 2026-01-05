-- ========================================
-- 清理测试数据脚本
-- 用于删除初始化时插入的测试调用日志数据
-- 适用于已安装系统需要清理测试数据的场景
-- ========================================
USE api_manage;
-- 1. 清理 api_call_log 表中的测试调用记录
-- 删除所有调用日志（如果需要保留真实调用数据，请谨慎使用）
DELETE FROM api_call_log
WHERE call_time < DATE_SUB(NOW(), INTERVAL 1 HOUR);
-- 如果要清空整个调用日志表（谨慎！会删除所有调用日志）
-- TRUNCATE TABLE api_call_log;
-- 2. 清理 operation_log 表中的测试操作记录
-- 删除测试用户的操作日志（如果使用了 insert_test_logs.sql）
DELETE FROM operation_log
WHERE username IN ('admin', 'test')
  AND oper_time < DATE_SUB(NOW(), INTERVAL 1 DAY)
  AND description LIKE '%测试%';
-- 如果要清空整个操作日志表（谨慎！会删除所有操作日志）
-- TRUNCATE TABLE operation_log;
-- 3. 验证清理结果
SELECT '=== 清理结果统计 ===' as info;
SELECT CONCAT('api_call_log 剩余记录数: ', COUNT(*)) as result
FROM api_call_log;
SELECT CONCAT('operation_log 剩余记录数: ', COUNT(*)) as result
FROM operation_log;
-- 4. 查看最新的记录（确认清理效果）
SELECT '=== api_call_log 最新10条记录 ===' as info;
SELECT *
FROM api_call_log
ORDER BY call_time DESC
LIMIT 10;
SELECT '=== operation_log 最新10条记录 ===' as info;
SELECT *
FROM operation_log
ORDER BY oper_time DESC
LIMIT 10;
-- ========================================
-- 使用说明：
-- 1. 如果只想清理一小时前的调用日志，使用默认的 DELETE 语句
-- 2. 如果想完全清空表（包括真实数据），取消注释 TRUNCATE 语句
-- 3. 建议在执行前先备份数据库
-- 4. 执行命令：source backend/src/main/resources/sql/clean_test_data.sql
-- ========================================