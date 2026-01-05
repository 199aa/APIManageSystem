package com.api.controller;

import com.api.annotation.OperationLog;
import com.api.common.Result;
import com.api.model.Permission;
import com.api.model.User;
import com.api.service.PermissionService;
import com.api.service.UserService;
import com.api.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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

  @PostMapping("/register")
  @OperationLog(type = "CREATE", module = "user", description = "用户注册")
  public Result<User> register(@RequestBody User user) {
    try {
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
