-- 创建权限表
CREATE TABLE IF NOT EXISTS permission (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  name varchar(50) NOT NULL COMMENT '权限名称',
  code varchar(50) NOT NULL COMMENT '权限编码',
  type varchar(20) DEFAULT 'menu' COMMENT '类型：menu-菜单，button-按钮',
  parent_id bigint(20) DEFAULT NULL COMMENT '父权限ID',
  icon varchar(50) DEFAULT NULL COMMENT '图标',
  path varchar(200) DEFAULT NULL COMMENT '路径',
  sort_order int(11) DEFAULT 0 COMMENT '排序',
  status tinyint(1) DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_code (code),
  KEY idx_parent_id (parent_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '权限表';
-- 插入菜单权限数据
INSERT INTO permission (
    id,
    name,
    code,
    type,
    parent_id,
    icon,
    path,
    sort_order
  )
VALUES -- 平台接入
  (
    1,
    '平台接入',
    'platform',
    'menu',
    NULL,
    'el-icon-connection',
    '/platform',
    1
  ),
  (
    11,
    '平台列表',
    'platform:list',
    'menu',
    1,
    NULL,
    '/platform/list',
    1
  ),
  (
    12,
    '新增平台',
    'platform:create',
    'button',
    1,
    NULL,
    NULL,
    2
  ),
  (
    13,
    '编辑平台',
    'platform:update',
    'button',
    1,
    NULL,
    NULL,
    3
  ),
  (
    14,
    '删除平台',
    'platform:delete',
    'button',
    1,
    NULL,
    NULL,
    4
  ),
  -- API仓库
  (
    2,
    'API仓库',
    'api',
    'menu',
    NULL,
    'el-icon-folder-opened',
    '/api',
    2
  ),
  (
    21,
    'API列表',
    'api:list',
    'menu',
    2,
    NULL,
    '/api/list',
    1
  ),
  (
    22,
    'API详情',
    'api:detail',
    'menu',
    2,
    NULL,
    '/api/detail',
    2
  ),
  (
    23,
    '新增API',
    'api:create',
    'button',
    2,
    NULL,
    NULL,
    3
  ),
  (
    24,
    '编辑API',
    'api:update',
    'button',
    2,
    NULL,
    NULL,
    4
  ),
  (
    25,
    '删除API',
    'api:delete',
    'button',
    2,
    NULL,
    NULL,
    5
  ),
  (
    26,
    '测试API',
    'api:test',
    'button',
    2,
    NULL,
    NULL,
    6
  ),
  -- 服务编排
  (
    3,
    '服务编排',
    'orchestration',
    'menu',
    NULL,
    'el-icon-share',
    '/orchestration',
    3
  ),
  (
    31,
    '聚合API',
    'orchestration:aggregate',
    'menu',
    3,
    NULL,
    '/orchestration/aggregate',
    1
  ),
  (
    32,
    '编排设计',
    'orchestration:design',
    'menu',
    3,
    NULL,
    '/orchestration/design',
    2
  ),
  (
    33,
    '新增聚合',
    'orchestration:create',
    'button',
    3,
    NULL,
    NULL,
    3
  ),
  (
    34,
    '发布聚合',
    'orchestration:publish',
    'button',
    3,
    NULL,
    NULL,
    4
  ),
  -- 治理中心
  (
    4,
    '治理中心',
    'governance',
    'menu',
    NULL,
    'el-icon-setting',
    '/governance',
    4
  ),
  (
    41,
    '限流策略',
    'governance:ratelimit',
    'menu',
    4,
    NULL,
    '/governance/rate-limit',
    1
  ),
  (
    42,
    '黑白名单',
    'governance:blacklist',
    'menu',
    4,
    NULL,
    '/governance/blacklist',
    2
  ),
  (
    43,
    '缓存策略',
    'governance:cache',
    'menu',
    4,
    NULL,
    '/governance/cache',
    3
  ),
  -- 客户管理
  (
    5,
    '客户管理',
    'customer',
    'menu',
    NULL,
    'el-icon-user-solid',
    '/customer',
    5
  ),
  (
    51,
    '应用列表',
    'customer:apps',
    'menu',
    5,
    NULL,
    '/customer/apps',
    1
  ),
  (
    52,
    '应用详情',
    'customer:detail',
    'menu',
    5,
    NULL,
    '/customer/app-detail',
    2
  ),
  (
    53,
    '创建应用',
    'customer:create',
    'button',
    5,
    NULL,
    NULL,
    3
  ),
  (
    54,
    '管理凭证',
    'customer:credentials',
    'button',
    5,
    NULL,
    NULL,
    4
  ),
  -- 运维监控
  (
    6,
    '运维监控',
    'monitor',
    'menu',
    NULL,
    'el-icon-data-line',
    '/monitor',
    6
  ),
  (
    61,
    '调用日志',
    'monitor:logs',
    'menu',
    6,
    NULL,
    '/monitor/logs',
    1
  ),
  (
    62,
    '告警管理',
    'monitor:alerts',
    'menu',
    6,
    NULL,
    '/monitor/alerts',
    2
  ),
  -- 系统管理
  (
    7,
    '系统管理',
    'system',
    'menu',
    NULL,
    'el-icon-s-tools',
    '/system',
    7
  ),
  (
    71,
    '用户管理',
    'system:user',
    'menu',
    7,
    NULL,
    '/system/users',
    1
  ),
  (
    72,
    '角色管理',
    'system:role',
    'menu',
    7,
    NULL,
    '/system/roles',
    2
  ),
  (
    73,
    '操作日志',
    'system:log',
    'menu',
    7,
    NULL,
    '/system/logs',
    3
  );
-- 插入角色权限关联数据（超级管理员拥有所有权限）
INSERT INTO role_permission (role_id, permission_id)
SELECT 1,
  id
FROM permission
WHERE status = 1;
-- 管理员权限
INSERT INTO role_permission (role_id, permission_id)
VALUES (2, 1),
  (2, 11),
  (2, 12),
  (2, 13),
  (2, 14),
  (2, 2),
  (2, 21),
  (2, 22),
  (2, 23),
  (2, 24),
  (2, 25),
  (2, 26),
  (2, 5),
  (2, 51),
  (2, 52),
  (2, 53),
  (2, 54),
  (2, 7),
  (2, 71),
  (2, 72),
  (2, 73);
-- 运维人员权限
INSERT INTO role_permission (role_id, permission_id)
VALUES (3, 1),
  (3, 11),
  (3, 2),
  (3, 21),
  (3, 22),
  (3, 26),
  (3, 3),
  (3, 31),
  (3, 32),
  (3, 4),
  (3, 41),
  (3, 42),
  (3, 43),
  (3, 6),
  (3, 61),
  (3, 62);
-- 开发人员权限
INSERT INTO role_permission (role_id, permission_id)
VALUES (4, 1),
  (4, 11),
  (4, 2),
  (4, 21),
  (4, 22),
  (4, 26),
  (4, 3),
  (4, 31),
  (4, 6),
  (4, 61);
-- 访客权限
INSERT INTO role_permission (role_id, permission_id)
VALUES (5, 1),
  (5, 11),
  (5, 2),
  (5, 21),
  (5, 22);