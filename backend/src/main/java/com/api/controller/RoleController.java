package com.api.controller;

import com.api.annotation.OperationLog;
import com.api.common.Result;
import com.api.model.Role;
import com.api.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/role")
@CrossOrigin
public class RoleController {

  @Autowired
  private RoleService roleService;

  /**
   * 查询所有角色
   */
  @GetMapping("/all")
  public Result<List<Role>> getAllRoles() {
    try {
      List<Role> roles = roleService.getAllRoles();
      return Result.success(roles);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 分页查询角色
   */
  @GetMapping("/list")
  public Result<Map<String, Object>> getRolesByPage(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int pageSize) {
    try {
      Map<String, Object> result = roleService.getRolesByPage(page, pageSize);
      return Result.success(result);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 根据ID查询角色
   */
  @GetMapping("/{id}")
  public Result<Role> getRoleById(@PathVariable Long id) {
    try {
      Role role = roleService.getRoleById(id);
      if (role == null) {
        return Result.error("角色不存在");
      }
      return Result.success(role);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 创建角色
   */
  @PostMapping
  @OperationLog(type = "CREATE", module = "role", description = "创建角色")
  public Result<Role> createRole(@RequestBody Role role) {
    try {
      Role newRole = roleService.createRole(role);
      return Result.success(newRole);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 更新角色
   */
  @PutMapping
  @OperationLog(type = "UPDATE", module = "role", description = "更新角色")
  public Result<Role> updateRole(@RequestBody Role role) {
    try {
      Role updatedRole = roleService.updateRole(role);
      return Result.success(updatedRole);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 删除角色
   */
  @DeleteMapping("/{id}")
  @OperationLog(type = "DELETE", module = "role", description = "删除角色")
  public Result<Void> deleteRole(@PathVariable Long id) {
    try {
      roleService.deleteRole(id);
      return Result.success(null);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 批量删除角色
   */
  @DeleteMapping("/batch")
  public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
    try {
      roleService.deleteBatch(ids);
      return Result.success(null);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
}
