-- 创建数据库
CREATE DATABASE IF NOT EXISTS api_manage DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE api_manage;
-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(200) DEFAULT NULL COMMENT '头像',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role_id` (`role_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';
-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(50) NOT NULL COMMENT '角色名称',
  `code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `is_system` tinyint(1) DEFAULT '0' COMMENT '是否系统角色：1-是，0-否',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色表';
-- 角色权限表
CREATE TABLE IF NOT EXISTS `role_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `permission_id` bigint(20) NOT NULL COMMENT '权限ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色权限表';
-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '操作用户名',
  `oper_type` varchar(20) DEFAULT NULL COMMENT '操作类型：LOGIN/CREATE/UPDATE/DELETE/EXPORT',
  `module` varchar(50) DEFAULT NULL COMMENT '操作模块',
  `description` varchar(500) DEFAULT NULL COMMENT '操作描述',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(200) DEFAULT NULL COMMENT '请求URL',
  `request_params` text COMMENT '请求参数',
  `response_result` text COMMENT '响应结果',
  `ip` varchar(50) DEFAULT NULL COMMENT '操作IP',
  `browser` varchar(500) DEFAULT NULL COMMENT '浏览器信息',
  `status` tinyint(1) DEFAULT '1' COMMENT '操作状态：1-成功，0-失败',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `cost_time` int(11) DEFAULT NULL COMMENT '耗时(ms)',
  `oper_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_oper_type` (`oper_type`),
  KEY `idx_module` (`module`),
  KEY `idx_oper_time` (`oper_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '操作日志表';
-- 平台表
CREATE TABLE IF NOT EXISTS `platform` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '平台ID',
  `name` varchar(50) NOT NULL COMMENT '平台名称',
  `code` varchar(50) NOT NULL COMMENT '平台编码',
  `icon` varchar(200) DEFAULT NULL COMMENT '平台图标',
  `base_url` varchar(200) DEFAULT NULL COMMENT '基础URL',
  `description` text COMMENT '平台描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '平台表';
-- API信息表
CREATE TABLE IF NOT EXISTS `api_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'API ID',
  `name` varchar(100) NOT NULL COMMENT 'API名称',
  `path` varchar(200) NOT NULL COMMENT '请求路径',
  `method` varchar(10) NOT NULL COMMENT '请求方法',
  `description` text COMMENT '描述',
  `platform_id` bigint(20) DEFAULT NULL COMMENT '所属平台ID',
  `is_aggregate` tinyint(1) DEFAULT '0' COMMENT '是否聚合接口：0-否，1-是',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：1-正常，0-异常，2-离线',
  `request_params` text COMMENT '请求参数',
  `response_example` text COMMENT '响应示例',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_method` (`method`),
  KEY `idx_status` (`status`),
  KEY `idx_platform_id` (`platform_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'API信息表';
-- API调用日志表
CREATE TABLE IF NOT EXISTS `api_call_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `api_id` bigint(20) NOT NULL COMMENT 'API ID',
  `api_name` varchar(100) DEFAULT NULL COMMENT 'API名称',
  `api_path` varchar(200) DEFAULT NULL COMMENT '请求路径',
  `platform_id` bigint(20) DEFAULT NULL COMMENT '平台ID',
  `platform_name` varchar(50) DEFAULT NULL COMMENT '平台名称',
  `status_code` int(11) DEFAULT NULL COMMENT '响应状态码',
  `response_time` int(11) DEFAULT NULL COMMENT '响应时间(ms)',
  `is_success` tinyint(1) DEFAULT '1' COMMENT '是否成功：1-成功，0-失败',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `request_params` text COMMENT '请求参数',
  `response_data` text COMMENT '响应数据',
  `call_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
  PRIMARY KEY (`id`),
  KEY `idx_api_id` (`api_id`),
  KEY `idx_platform_id` (`platform_id`),
  KEY `idx_call_time` (`call_time`),
  KEY `idx_is_success` (`is_success`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'API调用日志表';
-- 插入测试角色
INSERT INTO `role` (
    `name`,
    `code`,
    `description`,
    `status`,
    `is_system`
  )
VALUES ('超级管理员', 'ROLE_SUPER_ADMIN', '拥有所有权限', 1, 1),
  ('管理员', 'ROLE_ADMIN', '管理系统配置和用户', 1, 1),
  ('运维人员', 'ROLE_OPS', '管理API和监控', 1, 0),
  ('开发人员', 'ROLE_DEV', '查看API和测试', 1, 0),
  ('访客', 'ROLE_GUEST', '只读权限', 1, 0);
-- 插入测试用户（密码为：123456，已MD5加密）
INSERT INTO `user` (
    `username`,
    `password`,
    `real_name`,
    `email`,
    `phone`,
    `role_id`,
    `status`
  )
VALUES (
    'admin',
    'e10adc3949ba59abbe56e057f20f883e',
    '系统管理员',
    'admin@example.com',
    '13800138000',
    1,
    1
  ),
  (
    'test',
    'e10adc3949ba59abbe56e057f20f883e',
    '测试用户',
    'test@example.com',
    '13800138001',
    5,
    1
  );
-- 插入平台数据
INSERT INTO `platform` (
    `name`,
    `code`,
    `base_url`,
    `description`,
    `status`
  )
VALUES (
    '百度',
    'baidu',
    'https://api.baidu.com',
    '百度开放平台API',
    1
  ),
  (
    '阿里云',
    'aliyun',
    'https://api.aliyun.com',
    '阿里云开放API',
    1
  ),
  (
    '腾讯云',
    'tencent',
    'https://api.tencent.com',
    '腾讯云开放API',
    1
  ),
  (
    '高德',
    'gaode',
    'https://restapi.amap.com',
    '高德地图API',
    1
  ),
  (
    '微信',
    'wechat',
    'https://api.weixin.qq.com',
    '微信开放平台API',
    1
  ),
  (
    '支付宝',
    'alipay',
    'https://openapi.alipay.com',
    '支付宝开放平台API',
    1
  );
-- 插入测试API数据
INSERT INTO `api_info` (
    `name`,
    `path`,
    `method`,
    `description`,
    `platform_id`,
    `is_aggregate`,
    `status`
  )
VALUES (
    '天气查询',
    '/weather/v1/now',
    'GET',
    '获取当前天气信息',
    1,
    0,
    1
  ),
  (
    '地理编码',
    '/geocoding/v3',
    'GET',
    '地址转经纬度',
    1,
    0,
    1
  ),
  (
    'OSS文件上传',
    '/oss/v1/upload',
    'POST',
    '上传文件到OSS',
    2,
    0,
    1
  ),
  (
    '短信发送',
    '/sms/v1/send',
    'POST',
    '发送短信验证码',
    2,
    0,
    0
  ),
  (
    '人脸识别',
    '/ai/face/detect',
    'POST',
    'AI人脸检测',
    3,
    0,
    1
  ),
  (
    '语音转文字',
    '/ai/speech/asr',
    'POST',
    '语音识别转文字',
    3,
    0,
    2
  ),
  (
    '路径规划',
    '/direction/v2/driving',
    'GET',
    '驾车路线规划',
    4,
    0,
    1
  ),
  (
    'POI搜索',
    '/place/v2/search',
    'GET',
    '兴趣点搜索',
    4,
    0,
    1
  ),
  (
    '获取用户信息',
    '/sns/userinfo',
    'GET',
    '获取微信用户信息',
    5,
    0,
    1
  ),
  (
    '支付下单',
    '/trade/create',
    'POST',
    '创建支付订单',
    6,
    0,
    1
  ),
  (
    '用户综合信息',
    '/aggregate/userinfo',
    'GET',
    '聚合多平台用户信息',
    NULL,
    1,
    1
  ),
  (
    '天气+路况',
    '/aggregate/weather-traffic',
    'GET',
    '聚合天气和路况信息',
    NULL,
    1,
    1
  );