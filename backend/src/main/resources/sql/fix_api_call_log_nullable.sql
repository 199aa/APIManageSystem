-- 修复api_call_log表，允许系统接口的api_id为null
-- 执行时间: 2026-01-03
-- 说明: 系统级接口（如/platform/list等）不属于任何API定义，api_id应该可为null

USE api_manage;

-- 修改api_id字段，允许null值
ALTER TABLE `api_call_log` 
MODIFY COLUMN `api_id` bigint(20) NULL COMMENT 'API ID（系统接口可为null）';

-- 查看修改结果
DESCRIBE api_call_log;
