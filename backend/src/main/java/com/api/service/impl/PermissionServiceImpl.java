package com.api.service.impl;

import com.api.mapper.PermissionMapper;
import com.api.model.Permission;
import com.api.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

  @Autowired
  private PermissionMapper permissionMapper;

  @Override
  public List<Permission> getAllPermissionsTree() {
    List<Permission> allPermissions = permissionMapper.selectAll();
    return buildTree(allPermissions, null);
  }

  @Override
  public List<Permission> getAllPermissions() {
    return permissionMapper.selectAll();
  }

  @Override
  public List<Permission> getPermissionsByRoleId(Long roleId) {
    return permissionMapper.selectByRoleId(roleId);
  }

  @Override
  public List<Long> getPermissionIdsByRoleId(Long roleId) {
    List<Permission> permissions = permissionMapper.selectByRoleId(roleId);
    return permissions.stream()
        .map(Permission::getId)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void saveRolePermissions(Long roleId, List<Long> permissionIds) {
    // 删除旧的权限关联
    permissionMapper.deleteRolePermissions(roleId);

    // 插入新的权限关联
    if (permissionIds != null && !permissionIds.isEmpty()) {
      permissionMapper.batchInsertRolePermissions(roleId, permissionIds);
    }
  }

  @Override
  @Transactional
  public Permission createPermission(Permission permission) {
    permissionMapper.insert(permission);
    return permission;
  }

  @Override
  @Transactional
  public Permission updatePermission(Permission permission) {
    permissionMapper.update(permission);
    return permissionMapper.selectById(permission.getId());
  }

  @Override
  @Transactional
  public void deletePermission(Long id) {
    permissionMapper.deleteById(id);
  }

  /**
   * 构建树形结构
   */
  private List<Permission> buildTree(List<Permission> allPermissions, Long parentId) {
    List<Permission> result = new ArrayList<>();
    for (Permission permission : allPermissions) {
      if ((parentId == null && permission.getParentId() == null) ||
          (parentId != null && parentId.equals(permission.getParentId()))) {
        List<Permission> children = buildTree(allPermissions, permission.getId());
        if (!children.isEmpty()) {
          permission.setChildren(children);
        }
        result.add(permission);
      }
    }
    return result;
  }
}
