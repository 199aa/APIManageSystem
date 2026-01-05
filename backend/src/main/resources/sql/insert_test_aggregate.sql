-- 插入测试用的聚合接口数据
USE api_manage;
-- 插入测试聚合接口
INSERT INTO `api_info` (
    `name`,
    `path`,
    `method`,
    `description`,
    `platform_id`,
    `is_aggregate`,
    `status`,
    `aggregate_config`
  )
VALUES (
    '用户完整信息聚合',
    '/aggregate/user-full-info',
    'POST',
    '聚合用户基础信息、订单列表、积分信息等',
    0,
    1,
    1,
    '{
    "executeMode": "serial",
    "timeout": 30000,
    "nodes": [
      {
        "apiId": 1,
        "apiName": "获取用户基础信息",
        "apiPath": "/user/info",
        "method": "GET",
        "platformId": 1,
        "timeout": 5000,
        "onError": "abort",
        "retryCount": 0,
        "paramMappings": [
          {"source": "input.userId", "target": "userId", "required": true}
        ],
        "responsePath": "userBasic"
      },
      {
        "apiId": 2,
        "apiName": "获取用户订单列表",
        "apiPath": "/order/list",
        "method": "GET",
        "platformId": 1,
        "timeout": 5000,
        "onError": "continue",
        "retryCount": 0,
        "paramMappings": [
          {"source": "input.userId", "target": "userId", "required": true}
        ],
        "responsePath": "userOrders"
      },
      {
        "apiId": 3,
        "apiName": "获取用户积分",
        "apiPath": "/points/info",
        "method": "GET",
        "platformId": 1,
        "timeout": 5000,
        "onError": "continue",
        "retryCount": 0,
        "paramMappings": [
          {"source": "input.userId", "target": "userId", "required": true}
        ],
        "responsePath": "userPoints"
      }
    ],
    "inputSchema": {
      "userId": {"type": "string", "required": true, "description": "用户ID"}
    },
    "outputSchema": {
      "userBasic": "object",
      "userOrders": "array",
      "userPoints": "object"
    }
  }'
  );
-- 查看插入结果
SELECT id,
  name,
  path,
  is_aggregate,
  status
FROM api_info
WHERE is_aggregate = 1;