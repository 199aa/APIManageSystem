-- 添加测试人员角色（如果不存在）
INSERT INTO `role` (
    `name`,
    `code`,
    `description`,
    `status`,
    `is_system`
  )
SELECT '测试人员',
  'ROLE_TEST',
  'API测试和验证',
  1,
  0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `role`
    WHERE `code` = 'ROLE_TEST'
  );
-- 为测试人员角色分配权限（可根据需要调整）
-- 这里给测试人员分配一些基本的查看和测试权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id,
  p.id
FROM `role` r
  CROSS JOIN `permission` p
WHERE r.code = 'ROLE_TEST'
  AND p.code IN (
    'dashboard:view',
    'api:view',
    'api:test',
    'monitor:view',
    'log:view'
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `role_permission` rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );