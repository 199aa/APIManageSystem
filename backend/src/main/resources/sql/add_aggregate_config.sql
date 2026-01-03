-- 为聚合接口管理添加配置字段
-- 执行时间: 2026-01-03
-- 说明: 扩展api_info表，支持聚合接口的动态编排和管理

USE api_manage;

-- 添加聚合配置字段（存储JSON格式的编排配置）
ALTER TABLE `api_info` 
ADD COLUMN `aggregate_config` TEXT NULL COMMENT '聚合接口配置(JSON格式)' AFTER `response_example`;

-- 查看修改结果
DESCRIBE api_info;

-- aggregate_config 字段存储格式示例：
-- {
--   "executeMode": "serial",  // serial: 串行, parallel: 并行
--   "timeout": 30000,  // 总超时时间(ms)
--   "nodes": [  // 子API节点列表
--     {
--       "apiId": 1,
--       "apiName": "获取用户基础信息",
--       "apiPath": "/user/info",
--       "method": "GET",
--       "platformId": 1,
--       "timeout": 5000,
--       "onError": "abort",  // abort: 中断, continue: 继续, retry: 重试
--       "retryCount": 0,
--       "paramMappings": [  // 参数映射
--         {"source": "input.userId", "target": "userId", "required": true}
--       ],
--       "responsePath": "userBasic"  // 结果存放路径
--     }
--   ],
--   "inputSchema": {  // 入参定义
--     "userId": {"type": "string", "required": true, "description": "用户ID"}
--   },
--   "outputSchema": {  // 出参定义
--     "userBasic": "object",
--     "userOrders": "array"
--   }
-- }
