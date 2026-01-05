# 登录操作日志用户名显示修复

## 问题描述

之前的操作日志中，登录操作的用户名字段显示的是"登录"（按钮文本），而不是实际登录的用户名。

## 原因分析

`OperationLogAspect.java` 尝试从 JWT token 中提取用户名，但登录操作时还没有生成 token，因为 token 是登录成功后才生成的。

## 解决方案

在 `OperationLogAspect.java` 中添加特殊处理逻辑：

1. **检查操作类型**：如果 `@OperationLog` 注解的 type 为 "LOGIN"，则从请求参数中提取用户名
2. **从参数提取**：登录请求的第一个参数是 `Map<String, String>` 类型，包含 username 和 password
3. **直接设置**：将参数中的 username 直接设置到日志对象中

### 修改内容

**文件**：`backend/src/main/java/com/api/aspect/OperationLogAspect.java`

```java
// 添加 Map 导入
import java.util.Map;

// 在 recordLog 方法中添加特殊处理
if ("LOGIN".equals(annotation.type())) {
    // 登录操作：从请求参数中获取用户名
    Object[] args = joinPoint.getArgs();
    if (args != null && args.length > 0 && args[0] instanceof Map) {
        Map<String, String> loginData = (Map<String, String>) args[0];
        String username = loginData.get("username");
        log.setUsername(username);
        log.setUserId(null); // 登录时还没有用户ID
    }
} else {
    // 其他操作：从 JWT token 获取用户信息
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String jwt = authHeader.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(jwt);
        User user = userService.getUserById(userId);
        if (user != null) {
            log.setUsername(user.getUsername());
            log.setUserId(userId);
        }
    }
}
```

## 测试步骤

### 1. 重启后端服务

```bash
cd d:\ApiManageSystem\backend
mvn clean package -DskipTests
java -jar target\api-manage-backend-1.0.0.jar
```

### 2. 测试登录操作

使用不同用户登录系统：

- 超级管理员：admin / admin123
- 开发人员：dev / dev123
- 运维人员：ops / ops123
- 测试人员：test / test123

### 3. 查看操作日志

**SQL 查询**：

```sql
-- 查看最近的登录日志
SELECT
    id,
    username,
    oper_type,
    oper_module,
    oper_desc,
    create_time
FROM operation_log
WHERE oper_type = 'LOGIN'
ORDER BY create_time DESC
LIMIT 10;
```

**预期结果**：

- username 字段应该显示实际的用户名（如 "admin"、"dev"、"ops"）
- 不应该显示 "登录" 或其他按钮文本

### 4. 前端验证

1. 登录系统
2. 进入"系统管理" -> "操作日志"
3. 筛选操作类型为"登录"
4. 查看"操作用户"列是否显示正确的用户名

## 技术细节

### 登录流程

1. 用户提交登录表单（username + password）
2. 前端调用 `/user/login` 接口
3. `UserController.login()` 方法被调用
4. **OperationLogAspect 拦截并记录操作**
5. 验证成功后生成 JWT token
6. 返回 token 给前端

### 关键点

- 登录操作发生在 token 生成**之前**
- 登录请求参数包含明文用户名（用于身份验证）
- 其他操作都需要携带 token，可以从 token 中提取用户信息

### 代码逻辑

```
if (type == "LOGIN") {
    username = 从请求参数中获取
} else {
    username = 从 JWT token 中获取
}
```

## 验证结果

✅ 编译成功：`mvn clean compile -DskipTests`
✅ 打包成功：`mvn clean package -DskipTests`
✅ 服务启动：端口 8081 正常监听

## 后续建议

1. **增强日志记录**：

   - 记录登录 IP 地址
   - 记录登录失败尝试
   - 区分成功和失败的登录

2. **安全审计**：

   - 监控异常登录模式（频繁失败、异地登录等）
   - 设置登录告警规则

3. **数据保留**：
   - 定义操作日志的保留策略
   - 定期归档历史日志

## 修改文件

- ✅ `backend/src/main/java/com/api/aspect/OperationLogAspect.java`

## 相关文档

- [权限系统优化文档](PERMISSION_OPTIMIZATION.md)
- [菜单显示修复文档](PERMISSION_MENU_FIX.md)
- [403 错误修复文档](FIX_403_ERROR.md)
