package com.api.mapper;

import com.api.model.PermissionAuditLog;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 权限审计日志Mapper
 */
@Mapper
public interface PermissionAuditLogMapper {

        /**
         * 插入审计日志
         */
        @Insert("INSERT INTO permission_audit_log (user_id, username, operation_type, target_type, " +
                        "target_id, target_name, permission_codes, before_value, after_value, description, " +
                        "ip_address, create_time) VALUES (#{userId}, #{username}, #{operationType}, " +
                        "#{targetType}, #{targetId}, #{targetName}, #{permissionCodes}, #{beforeValue}, " +
                        "#{afterValue}, #{description}, #{ipAddress}, #{createTime})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        void insert(PermissionAuditLog log);

        /**
         * 分页查询审计日志
         */
        @Select("<script>" +
                        "SELECT * FROM permission_audit_log " +
                        "<where>" +
                        "<if test='userId != null'>AND user_id = #{userId}</if>" +
                        "<if test='targetType != null'>AND target_type = #{targetType}</if>" +
                        "<if test='operationType != null'>AND operation_type = #{operationType}</if>" +
                        "<if test='startDate != null'>AND create_time &gt;= #{startDate}</if>" +
                        "<if test='endDate != null'>AND create_time &lt;= #{endDate}</if>" +
                        "</where>" +
                        "ORDER BY create_time DESC " +
                        "LIMIT #{offset}, #{limit}" +
                        "</script>")
        List<PermissionAuditLog> selectByPage(Map<String, Object> params);

        /**
         * 统计审计日志数量
         */
        @Select("<script>" +
                        "SELECT COUNT(*) FROM permission_audit_log " +
                        "<where>" +
                        "<if test='userId != null'>AND user_id = #{userId}</if>" +
                        "<if test='targetType != null'>AND target_type = #{targetType}</if>" +
                        "<if test='operationType != null'>AND operation_type = #{operationType}</if>" +
                        "<if test='startDate != null'>AND create_time &gt;= #{startDate}</if>" +
                        "<if test='endDate != null'>AND create_time &lt;= #{endDate}</if>" +
                        "</where>" +
                        "</script>")
        int countByCondition(Map<String, Object> params);
}
