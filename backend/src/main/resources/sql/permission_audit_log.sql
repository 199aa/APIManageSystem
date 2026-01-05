-- 权限审计日志表
CREATE TABLE IF NOT EXISTS permission_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '操作用户ID',
    username VARCHAR(50) NOT NULL COMMENT '操作用户名',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型：GRANT-授权, REVOKE-撤销, MODIFY-修改',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型：ROLE-角色, USER-用户',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    target_name VARCHAR(100) COMMENT '目标名称',
    permission_codes TEXT COMMENT '权限编码列表（JSON数组）',
    before_value TEXT COMMENT '修改前的值（JSON）',
    after_value TEXT COMMENT '修改后的值（JSON）',
    description VARCHAR(500) COMMENT '操作描述',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_target (target_type, target_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限审计日志表';
