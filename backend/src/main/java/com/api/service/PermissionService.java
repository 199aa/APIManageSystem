package com.api.service;

import com.api.model.Permission;
import java.util.List;

public interface PermissionService {

  // 获取所有权限（树形结构）
  List<Permission> getAllPermissionsTree();

  // 获取所有权限（列表）
  List<Permission> getAllPermissions();

  // 根据角色ID获取权限
  List<Permission> getPermissionsByRoleId(Long roleId);

  // 根据角色ID获取权限ID列表
  List<Long> getPermissionIdsByRoleId(Long roleId);

  // 保存角色权限
  void saveRolePermissions(Long roleId, List<Long> permissionIds);

  // 创建权限
  Permission createPermission(Permission permission);

  // 更新权限
  Permission updatePermission(Permission permission);

  // 删除权限
  void deletePermission(Long id);
}
