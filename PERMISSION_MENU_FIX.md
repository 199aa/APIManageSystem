# 权限菜单显示问题修复报告

## 🐛 问题描述

**现象**：开发人员、运维人员、访客等角色登录后，菜单显示不完整

- **开发人员** 应该看到：首页、平台接入、API 仓库、**服务编排**、**监控中心**
- **实际显示**：只有首页、平台接入、API 仓库（缺少服务编排和监控中心）

## 🔍 问题根因

前端菜单配置文件中的权限代码与数据库中的权限代码**不匹配**。

### 数据库中的权限代码

```sql
-- 开发人员(roleId=4)的实际权限
api, api:detail, api:list, api:test
monitor, monitor:logs
orchestration, orchestration:aggregate
platform, platform:list
```

### 前端配置中的权限代码（修复前）

```javascript
// ❌ 错误的配置
{
  permission: 'orchestration:list'
} // 数据库中是 'orchestration'
{
  permission: 'monitor:log'
} // 数据库中是 'monitor:logs'
{
  permission: 'governance:list'
} // 数据库中是 'governance:ratelimit'
```

## ✅ 修复内容

### 1. 修复 `frontend/src/config/menu.js`

| 菜单项   | 修复前               | 修复后          |
| -------- | -------------------- | --------------- |
| 平台接入 | `platform:list`      | `platform`      |
| API 仓库 | `api:list`           | `api`           |
| 服务编排 | `orchestration:list` | `orchestration` |
| 治理中心 | `governance:list`    | `governance`    |
| 监控中心 | `monitor:log`        | `monitor`       |
| 客户管理 | `customer:list`      | `customer`      |

### 2. 修复 `frontend/src/router/index.js`

| 路由     | 修复前                 | 修复后                    |
| -------- | ---------------------- | ------------------------- |
| API 详情 | `api:list`             | `api:detail`              |
| 聚合接口 | `orchestration:list`   | `orchestration:aggregate` |
| 接口设计 | `orchestration:create` | `orchestration:design`    |
| 限流策略 | `governance:list`      | `governance:ratelimit`    |
| 黑白名单 | `governance:list`      | `governance:blacklist`    |
| 缓存策略 | `governance:list`      | `governance:cache`        |
| 调用日志 | `monitor:log`          | `monitor:logs`            |
| 告警中心 | `monitor:alert`        | `monitor:alerts`          |

### 3. 更新权限说明文档

在 `PERMISSION_CONTROL.md` 中添加了完整的权限编码对照表，包括：

- 模块级权限（如 `api`, `platform`, `monitor`）
- 功能级权限（如 `api:list`, `monitor:logs`）

## 📊 各角色权限验证

### 开发人员 (roleId=4)

**数据库权限**：

```
api, api:detail, api:list, api:test
monitor, monitor:logs
orchestration, orchestration:aggregate
platform, platform:list
```

**应该看到的菜单**：

- ✅ 首页
- ✅ 平台接入 → 平台列表
- ✅ API 仓库 → API 列表
- ✅ 服务编排 → 聚合接口
- ✅ 监控中心 → 调用日志

### 运维人员 (roleId=3)

**数据库权限**：

```
api, api:detail, api:list, api:test
governance, governance:blacklist, governance:cache, governance:ratelimit
monitor, monitor:alerts, monitor:logs
orchestration, orchestration:aggregate, orchestration:design
platform, platform:list
```

**应该看到的菜单**：

- ✅ 首页
- ✅ 平台接入 → 平台列表
- ✅ API 仓库 → API 列表
- ✅ 服务编排 → 聚合接口
- ✅ 治理中心 → 限流策略、黑白名单、缓存策略
- ✅ 监控中心 → 调用日志、告警中心

### 访客 (roleId=5)

**数据库权限**：

```
api, api:detail, api:list
```

**应该看到的菜单**：

- ✅ 首页
- ✅ API 仓库 → API 列表
- ❌ 没有平台接入（数据库中没有 `platform` 权限）

**⚠️ 注意**：访客在数据库中缺少 `platform` 权限，需要添加：

```sql
-- 为访客添加平台查看权限
INSERT INTO role_permission (role_id, permission_id)
SELECT 5, id FROM permission WHERE code = 'platform';
INSERT INTO role_permission (role_id, permission_id)
SELECT 5, id FROM permission WHERE code = 'platform:list';
```

### 测试人员 (roleId=6)

**数据库权限**：

```
api, api:detail, api:list
orchestration, orchestration:aggregate
platform, platform:list
```

**应该看到的菜单**：

- ✅ 首页
- ✅ 平台接入 → 平台列表
- ✅ API 仓库 → API 列表
- ✅ 服务编排 → 聚合接口
- ❌ 缺少监控中心（文档要求有，但数据库未配置）

**⚠️ 注意**：测试人员需要添加监控日志权限：

```sql
-- 为测试人员添加监控日志权限
INSERT INTO role_permission (role_id, permission_id)
SELECT 6, id FROM permission WHERE code = 'monitor';
INSERT INTO role_permission (role_id, permission_id)
SELECT 6, id FROM permission WHERE code = 'monitor:logs';
```

## 🔧 数据库修复 SQL

执行以下 SQL 补充缺失的权限：

```sql
-- 为访客(roleId=5)添加平台查看权限
INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT 5, id FROM permission WHERE code IN ('platform', 'platform:list');

-- 为测试人员(roleId=6)添加监控日志权限
INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT 6, id FROM permission WHERE code IN ('monitor', 'monitor:logs');
```

## 📝 权限设计规则

### 两级权限体系

1. **模块级权限**（父权限）

   - 用于控制**菜单显示**
   - 格式：`module` (如 `api`, `platform`, `monitor`)
   - 示例：拥有 `api` 权限才能看到"API 仓库"菜单

2. **功能级权限**（子权限）
   - 用于控制**具体功能访问**
   - 格式：`module:action` (如 `api:list`, `api:create`)
   - 示例：拥有 `api:create` 才能点击"新增"按钮

### 权限判断流程

```javascript
// 菜单显示：检查模块级权限
if (permissions.includes('api')) {
  showApiMenu = true
}

// 功能访问：检查功能级权限
if (permissions.includes('api:create')) {
  showCreateButton = true
}
```

## 🎯 测试验证

### 测试步骤

1. **清除浏览器缓存**

   - 按 `Ctrl + Shift + Delete`
   - 清除缓存和 Cookie

2. **重启前端服务**

   ```bash
   cd frontend
   npm run serve
   ```

3. **使用不同角色登录测试**

   - 开发人员：dev / dev123
   - 运维人员：ops / ops123
   - 访客：guest / guest123
   - 测试人员：test / test123

4. **验证菜单显示**

   - 检查是否显示了应有的菜单项
   - 确认没有显示无权限的菜单

5. **验证路由访问**
   - 尝试直接访问 URL
   - 无权限应该跳转到首页

## ⚠️ 注意事项

1. **权限代码必须完全匹配**

   - 前端配置的权限代码必须与数据库中的权限代码一致
   - 区分大小写
   - 注意单复数（如 `log` vs `logs`）

2. **用户需要重新登录**

   - 修改权限配置后，用户需要重新登录才能生效
   - localStorage 中的权限列表需要刷新

3. **超级管理员特殊处理**
   - roleId=1 的超级管理员可以看到所有菜单
   - 不需要检查具体权限代码

## 📚 相关文件

- `frontend/src/config/menu.js` - 菜单配置（已修复）
- `frontend/src/router/index.js` - 路由配置（已修复）
- `PERMISSION_CONTROL.md` - 权限说明文档（已更新）
- 本文档 - 问题修复报告

---

**修复时间**：2026-01-04  
**修复人员**：GitHub Copilot  
**影响范围**：所有非超级管理员角色的菜单显示
