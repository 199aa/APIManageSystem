-- ========================================
-- 修复 operation_log 表 browser 字段长度问题
-- 问题：现代浏览器的User-Agent字符串通常超过100字符，导致数据插入失败
-- 解决：扩大browser字段长度从 varchar(100) 到 varchar(500)
-- 执行时间：2026-01-04
-- ========================================

USE api_manage;

-- 修改 browser 字段长度
ALTER TABLE operation_log 
MODIFY COLUMN browser VARCHAR(500) COMMENT '浏览器信息';

-- 验证修改结果
DESCRIBE operation_log;

-- ========================================
-- 说明：
-- 1. 现代浏览器User-Agent示例长度通常在200-400字符之间
-- 2. 代码中已添加截断逻辑（最大500字符）确保数据安全
-- 3. 如果遇到类似错误：Data too long for column 'browser'，执行此脚本
-- ========================================
