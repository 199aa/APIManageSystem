package com.api.controller;

import com.api.annotation.OperationLog;
import com.api.common.Result;
import com.api.model.Permission;
import com.api.model.Role;
import com.api.model.User;
import com.api.service.PermissionService;
import com.api.service.RoleService;
import com.api.service.UserService;
import com.api.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

  @Autowired
  private UserService userService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private RoleService roleService;

  @Autowired
  private JwtUtil jwtUtil;

  @PostMapping("/login")
  @OperationLog(type = "LOGIN", module = "用户登录", description = "用户登录")
  public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
    try {
      String username = loginData.get("username");
      String password = loginData.get("password");

      User user = userService.login(username, password);
      String token = jwtUtil.generateToken(user.getId().toString());

      // 获取用户权限
      List<Permission> permissions = permissionService.getPermissionsByRoleId(user.getRoleId());
      List<String> permissionCodes = permissions.stream()
          .map(Permission::getCode)
          .collect(Collectors.toList());

      Map<String, Object> data = new HashMap<>();
      data.put("token", token);
      data.put("userInfo", user);
      data.put("permissions", permissionCodes);

      return Result.success(data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 获取可注册的角色列表（排除超级管理员和管理员）
   */
  @GetMapping("/register/roles")
  public Result<List<Role>> getRegisterRoles() {
    try {
      // 排除超级管理员和管理员角色
      List<String> excludedCodes = Arrays.asList("ROLE_SUPER_ADMIN", "ROLE_ADMIN");
      List<Role> allRoles = roleService.getAllRoles();
      List<Role> registerRoles = allRoles.stream()
          .filter(role -> !excludedCodes.contains(role.getCode()) && role.getStatus() == 1)
          .collect(Collectors.toList());
      return Result.success(registerRoles);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  @PostMapping("/register")
  @OperationLog(type = "CREATE", module = "user", description = "用户注册")
  public Result<User> register(@RequestBody User user) {
    try {
      // 验证角色ID是否是允许注册的角色
      if (user.getRoleId() != null) {
        Role role = roleService.getRoleById(user.getRoleId());
        if (role == null) {
          return Result.error("选择的角色不存在");
        }
        List<String> excludedCodes = Arrays.asList("ROLE_SUPER_ADMIN", "ROLE_ADMIN");
        if (excludedCodes.contains(role.getCode())) {
          return Result.error("不允许注册该角色");
        }
      }
      User newUser = userService.register(user);
      return Result.success(newUser);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  @GetMapping("/info")
  public Result<User> getUserInfo(@RequestHeader("Authorization") String token) {
    try {
      String jwt = token.replace("Bearer ", "");
      String userId = jwtUtil.getUserIdFromToken(jwt);
      User user = userService.getUserById(Long.parseLong(userId));
      return Result.success(user);
    } catch (Exception e) {
      return Result.error(401, "未授权");
    }
  }
}
