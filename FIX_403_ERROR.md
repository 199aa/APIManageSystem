# 403 错误修复说明

## 🐛 问题现象

开发人员、运维人员等角色点击左侧菜单时，浏览器控制台显示：

```
GET http://localhost:8080/api/aggregate/list?page=1&pageSize=10 403 (Forbidden)
Request failed with status code 403
```

## 🔍 问题原因

后端`PermissionInterceptor.java`中的 API 路径权限映射使用的是**旧的权限代码**，与数据库中的实际权限代码不匹配。

### 对比说明

| 场景     | 后端拦截器（修复前） | 数据库中的权限  | 结果            |
| -------- | -------------------- | --------------- | --------------- |
| 聚合接口 | `orchestration:list` | `orchestration` | ❌ 不匹配 → 403 |
| 调用日志 | `monitor:log`        | `monitor`       | ❌ 不匹配 → 403 |
| 限流策略 | `governance:list`    | `governance`    | ❌ 不匹配 → 403 |

### 权限检查流程

```
用户点击菜单
  → 前端发送API请求
  → 后端PermissionInterceptor拦截
  → 检查用户权限列表：['api', 'monitor', 'orchestration', 'platform']
  → 对比要求的权限：'orchestration:list'
  → 权限列表中没有 'orchestration:list'
  → 返回 403 Forbidden ❌
```

## ✅ 修复方案

### 统一权限检查策略

**模块级权限控制**：API 访问只检查**模块级权限**（如 `api`, `platform`, `monitor`），不检查具体功能权限。

- ✅ 拥有 `api` 权限 → 可以访问所有 `/api-info/*` 接口
- ✅ 拥有 `monitor` 权限 → 可以访问所有 `/monitor/*` 接口
- ✅ 拥有 `orchestration` 权限 → 可以访问所有 `/aggregate/*` 接口

**具体功能权限**（如 `api:create`, `api:delete`）由前端按钮控制，后端暂不严格限制。

### 修复内容

更新 `backend/src/main/java/com/api/interceptor/PermissionInterceptor.java`：

```java
// 修复前
pathPermissionMap.put("GET:/aggregate/list", "orchestration:list");  // ❌
pathPermissionMap.put("GET:/monitor/api-log/list", "monitor:log");   // ❌
pathPermissionMap.put("GET:/governance/rate-limit/list", "governance:list"); // ❌

// 修复后
pathPermissionMap.put("GET:/aggregate/list", "orchestration");  // ✅
pathPermissionMap.put("GET:/monitor/api-log/list", "monitor");  // ✅
pathPermissionMap.put("GET:/governance/rate-limit/list", "governance"); // ✅
```

## 🔧 部署步骤

1. ✅ **已完成** - 修改 `PermissionInterceptor.java`
2. ✅ **已完成** - 后端编译成功
3. ⏸️ **待执行** - 重启后端服务

### 重启命令

```bash
# 停止当前后端进程（如果正在运行）
# 然后启动
cd d:\ApiManageSystem\backend
mvn spring-boot:run
```

## 🎯 验证方法

### 1. 清除浏览器缓存

- 按 `Ctrl + Shift + Delete`
- 清除缓存和 Cookie
- 或使用无痕模式

### 2. 重新登录测试

**开发人员账号** (dev / dev123)：

- ✅ 点击"服务编排" → 应该正常显示聚合接口列表
- ✅ 点击"监控中心" → 应该正常显示调用日志

**运维人员账号** (ops / ops123)：

- ✅ 点击"治理中心" → 应该正常显示限流、黑白名单、缓存配置
- ✅ 点击"监控中心" → 应该正常显示日志和告警

### 3. 浏览器控制台检查

- 打开开发者工具 (F12)
- 切换到 Network 标签
- 点击菜单
- 确认 API 请求返回 200 而不是 403

## 📊 权限架构说明

### 三层权限体系

```
1. 后端API拦截（粗粒度）
   ├─ 检查模块级权限（如 api, platform, monitor）
   └─ 控制API接口访问

2. 前端路由守卫（中粒度）
   ├─ 检查模块级权限
   └─ 控制页面路由访问

3. 前端按钮权限（细粒度）
   ├─ 检查功能级权限（如 api:create, api:delete）
   └─ 控制操作按钮显示
```

### 权限代码层级

```
模块级（父权限）: platform, api, monitor, orchestration, governance
  ├─ 用途：控制菜单显示、API访问
  └─ 示例：拥有 'api' 可以访问所有API相关接口

功能级（子权限）: api:list, api:create, api:delete, monitor:logs
  ├─ 用途：控制按钮显示、细粒度操作
  └─ 示例：拥有 'api:create' 才显示"新增"按钮
```

## ⚠️ 注意事项

### 1. 后端服务必须重启

修改 Java 代码后，必须重启 Spring Boot 应用才能生效。

### 2. 用户需要重新登录

- 用户权限列表存储在 localStorage 中
- 重启后端不会影响已登录用户
- 但建议清除浏览器缓存后重新登录

### 3. 模块级权限的设计考虑

**为什么使用模块级权限？**

- 简化后端权限验证逻辑
- 减少配置维护成本
- 前端已有细粒度按钮控制

**是否需要更细粒度的后端验证？**
如果需要后端严格验证每个操作（如 create、delete），需要：

1. 修改`PermissionInterceptor`映射关系
2. 确保数据库中有对应的功能级权限
3. 为每个角色配置详细的功能权限

## 🔄 后续增强建议

1. **注解式权限控制**

   ```java
   @PreAuthorize("hasPermission('api:create')")
   public Result createApi(@RequestBody ApiInfo api) { ... }
   ```

2. **权限配置中心化**

   - 将权限映射关系移到配置文件或数据库
   - 支持动态修改权限规则

3. **细粒度后端验证**
   - 区分查询、创建、修改、删除权限
   - 增强数据安全性

---

**修复时间**：2026-01-04  
**影响范围**：所有非超级管理员角色的 API 访问  
**修复状态**：代码已修复，待重启服务
