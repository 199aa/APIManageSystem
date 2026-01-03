package com.api.mapper;

import com.api.model.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PermissionMapper {

  // 查询所有权限
  List<Permission> selectAll();

  // 根据角色ID查询权限
  List<Permission> selectByRoleId(@Param("roleId") Long roleId);

  // 根据ID查询权限
  Permission selectById(@Param("id") Long id);

  // 插入权限
  int insert(Permission permission);

  // 更新权限
  int update(Permission permission);

  // 删除权限
  int deleteById(@Param("id") Long id);

  // 保存角色权限关联
  int insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

  // 删除角色的所有权限
  int deleteRolePermissions(@Param("roleId") Long roleId);

  // 批量插入角色权限
  int batchInsertRolePermissions(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);
}
