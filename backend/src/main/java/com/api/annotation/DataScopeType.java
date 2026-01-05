package com.api.annotation;

/**
 * 数据权限类型枚举
 */
public enum DataScopeType {
    /**
     * 全部数据权限（超级管理员）
     */
    ALL,

    /**
     * 自定义数据权限（按角色权限配置）
     */
    CUSTOM,

    /**
     * 仅本人数据权限
     */
    SELF
}
