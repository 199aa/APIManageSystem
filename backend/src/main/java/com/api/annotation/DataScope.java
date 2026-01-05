package com.api.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * 用于控制不同角色用户看到不同的数据范围
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 数据权限类型
     */
    DataScopeType value() default DataScopeType.ALL;

    /**
     * 用户ID字段名（用于过滤用户自己的数据）
     */
    String userIdColumn() default "user_id";

    /**
     * 部门ID字段名（用于过滤部门数据）
     */
    String deptIdColumn() default "dept_id";
}
