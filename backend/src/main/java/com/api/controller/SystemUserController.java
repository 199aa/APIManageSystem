package com.api.controller;

import com.api.annotation.OperationLog;
import com.api.common.Result;
import com.api.model.User;
import com.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/user")
@CrossOrigin
public class SystemUserController {

  @Autowired
  private UserService userService;

  /**
   * 分页查询用户
   */
  @GetMapping("/list")
  public Result<Map<String, Object>> getUsersByPage(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int pageSize,
      @RequestParam(required = false) String username,
      @RequestParam(required = false) Long roleId,
      @RequestParam(required = false) Integer status) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("page", page);
      params.put("pageSize", pageSize);
      params.put("username", username);
      params.put("roleId", roleId);
      params.put("status", status);

      Map<String, Object> result = userService.getUsersByPage(params);
      return Result.success(result);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 根据ID查询用户
   */
  @GetMapping("/{id}")
  public Result<User> getUserById(@PathVariable Long id) {
    try {
      User user = userService.getUserById(id);
      if (user == null) {
        return Result.error("用户不存在");
      }
      return Result.success(user);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 创建用户
   */
  @PostMapping
  @OperationLog(type = "CREATE", module = "user", description = "创建用户")
  public Result<User> createUser(@RequestBody User user) {
    try {
      User newUser = userService.createUser(user);
      return Result.success(newUser);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 更新用户
   */
  @PutMapping
  @OperationLog(type = "UPDATE", module = "user", description = "更新用户信息")
  public Result<User> updateUser(@RequestBody User user) {
    try {
      User updatedUser = userService.updateUser(user);
      return Result.success(updatedUser);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 删除用户
   */
  @DeleteMapping("/{id}")
  @OperationLog(type = "DELETE", module = "user", description = "删除用户")
  public Result<Void> deleteUser(@PathVariable Long id) {
    try {
      userService.deleteUser(id);
      return Result.success(null);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 批量删除用户
   */
  @DeleteMapping("/batch")
  public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
    try {
      userService.deleteBatch(ids);
      return Result.success(null);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 更新用户状态
   */
  @PutMapping("/{id}/status")
  public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> data) {
    try {
      Integer status = data.get("status");
      userService.updateStatus(id, status);
      return Result.success(null);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 重置用户密码
   */
  @PutMapping("/{id}/reset-password")
  @OperationLog(type = "UPDATE", module = "user", description = "重置用户密码")
  public Result<Void> resetPassword(@PathVariable Long id, @RequestBody(required = false) Map<String, String> data) {
    try {
      String newPassword = data != null ? data.get("password") : null;
      userService.resetPassword(id, newPassword);
      return Result.success(null);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
}
