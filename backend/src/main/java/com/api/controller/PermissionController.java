package com.api.controller;

import com.api.annotation.OperationLog;
import com.api.common.Result;
import com.api.model.Permission;
import com.api.service.PermissionService;
import com.api.service.PermissionCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/permission")
@CrossOrigin
public class PermissionController {

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private PermissionCacheService permissionCacheService;

  /**
   * 获取所有权限（树形结构）
   */
  @GetMapping("/tree")
  public Result<List<Permission>> getPermissionsTree() {
    try {
      List<Permission> tree = permissionService.getAllPermissionsTree();
      return Result.success(tree);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 获取所有权限（列表）
   */
  @GetMapping("/list")
  public Result<List<Permission>> getAllPermissions() {
    try {
      List<Permission> list = permissionService.getAllPermissions();
      return Result.success(list);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 根据角色ID获取权限
   */
  @GetMapping("/role/{roleId}")
  public Result<Map<String, Object>> getPermissionsByRoleId(@PathVariable Long roleId) {
    try {
      List<Permission> permissions = permissionService.getPermissionsByRoleId(roleId);
      List<Long> permissionIds = permissionService.getPermissionIdsByRoleId(roleId);

      Map<String, Object> result = new HashMap<>();
      result.put("permissions", permissions);
      result.put("permissionIds", permissionIds);

      return Result.success(result);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 保存角色权限
   */
  @PostMapping("/role/{roleId}")
  @OperationLog(type = "UPDATE", module = "角色权限", description = "修改角色权限")
  public Result<Void> saveRolePermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
    try {
      permissionService.saveRolePermissions(roleId, permissionIds);
      // 清除所有用户权限缓存（因为角色权限已改变）
      permissionCacheService.clearAllPermissionCache();
      return Result.success(null);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 创建权限
   */
  @PostMapping
  @OperationLog(type = "CREATE", module = "权限管理", description = "创建权限")
  public Result<Permission> createPermission(@RequestBody Permission permission) {
    try {
      Permission created = permissionService.createPermission(permission);
      return Result.success(created);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 更新权限
   */
  @PutMapping
  @OperationLog(type = "UPDATE", module = "权限管理", description = "更新权限")
  public Result<Permission> updatePermission(@RequestBody Permission permission) {
    try {
      Permission updated = permissionService.updatePermission(permission);
      return Result.success(updated);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 删除权限
   */
  @DeleteMapping("/{id}")
  @OperationLog(type = "DELETE", module = "权限管理", description = "删除权限")
  public Result<Void> deletePermission(@PathVariable Long id) {
    try {
      permissionService.deletePermission(id);
      return Result.success(null);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
}
