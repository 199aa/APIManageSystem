# 多平台API管理与聚合系统 - 使用指南

## 📖 系统简介

这是一个基于 **Vue2 + Spring Boot** 的企业级多平台API管理与聚合系统，支持：

- 🔌 **多平台API接入** - 统一管理来自不同平台的API接口
- 🔄 **服务编排聚合** - 可视化编排多个API，创建聚合接口
- 🛡️ **智能治理** - 限流、缓存、黑白名单等治理策略
- 📊 **实时监控** - 调用日志、告警中心、Dashboard统计
- 👥 **多租户管理** - 客户应用管理、权限控制

---

## 🚀 快速启动

### 环境要求

- **JDK**: 1.8+
- **Node.js**: 12+
- **MySQL**: 8.0+
- **Redis**: 5.0+
- **Maven**: 3.6+

### 1️⃣ 数据库初始化

```bash
# 连接MySQL
mysql -u root -p

# 执行初始化脚本
source backend/src/main/resources/sql/init.sql
source backend/src/main/resources/sql/permission.sql
source backend/src/main/resources/sql/alert.sql
source backend/src/main/resources/sql/governance.sql
source backend/src/main/resources/sql/customer_app.sql

# 可选：插入测试告警数据（让告警中心显示更完整）
# source backend/src/main/resources/sql/insert_test_alerts.sql

# 可选：清理测试数据（如果需要干净的生产环境）
# source backend/src/main/resources/sql/clean_test_data.sql
```

> **注意**：
> - `init.sql` 会创建基础表结构和必要的初始数据（用户、角色、平台、API等）
> - `alert.sql` 会创建告警表和告警规则，包含2条测试告警记录
> - 如果告警中心顶部统计显示0，运行 `insert_test_alerts.sql` 添加测试告警数据
> - 如果需要完全干净的生产环境，执行完上述脚本后，运行 `clean_test_data.sql` 清理测试调用日志
> - 不要执行 `insert_test_logs.sql`，该文件仅用于开发测试

### 2️⃣ 启动后端服务

```bash
cd backend

# 修改配置文件（首次使用必须）
# 编辑 src/main/resources/application.yml
# 修改数据库和Redis连接信息

# 启动服务
mvn spring-boot:run
```

**后端地址**: http://localhost:8081

### 3️⃣ 启动前端服务

```bash
cd frontend

# 安装依赖（首次使用）
npm install

# 启动开发服务器
npm run serve
```

**前端地址**: http://localhost:8080

### 4️⃣ 登录系统

**测试账号**：
- 用户名：`admin` 
- 密码：`123456`

或

- 用户名：`test`
- 密码：`123456`

---

## 📚 核心功能模块

### 1. 📊 Dashboard（首页）

**功能**：系统整体运营数据可视化

**主要指标**：
- 今日调用次数
- 成功率统计
- 平均响应时间
- 活跃告警数量
- API总数

**实时监控**：
- 调用量趋势图（24小时/7天/30天）
- API健康度分布
- 平台调用分布
- 响应最慢 Top 5
- 错误率最高 Top 5
- 实时错误流

**使用场景**：
- 系统健康状态一览
- 性能瓶颈快速定位
- 异常情况实时发现

---

### 2. 🏢 平台接入管理

**路径**：平台列表

**功能**：管理接入的第三方API平台

#### 2.1 添加平台

1. 点击 **"新增平台"** 按钮
2. 填写平台信息：
   - **平台名称**：如 "微信开放平台"
   - **平台编码**：唯一标识，如 "wechat"
   - **平台图标**：图标URL或上传
   - **基础URL**：API基础地址
   - **描述**：平台说明
   
3. 配置环境信息：
   - **开发环境**：开发测试用
   - **测试环境**：集成测试用
   - **生产环境**：正式使用
   - 每个环境可配置：
     - Base URL
     - 超时时间
     - 认证信息（API Key/Secret）
     - 请求头

4. 点击 **"保存"**

#### 2.2 平台管理

- **查看详情**：点击平台卡片查看详细配置
- **编辑平台**：修改平台信息
- **切换环境**：选择当前使用的环境
- **启用/禁用**：控制平台状态
- **删除平台**：删除不再使用的平台

**示例**：
```
平台名称：淘宝开放平台
平台编码：taobao
基础URL：https://eco.taobao.com/router/rest
描述：淘宝电商API接入
```

---

### 3. 📦 API仓库（原子接口管理）

**路径**：原子接口列表

**功能**：管理各平台的原子API接口

#### 3.1 导入API

支持三种方式：

**方式一：手动创建**
1. 点击 **"新增API"** 
2. 填写基本信息：
   - API名称
   - 请求路径
   - 请求方法（GET/POST/PUT/DELETE）
   - 所属平台
   - 描述

3. 配置请求参数：
   ```json
   {
     "userId": {
       "type": "string",
       "required": true,
       "description": "用户ID"
     },
     "page": {
       "type": "integer",
       "required": false,
       "default": 1
     }
   }
   ```

4. 添加响应示例：
   ```json
   {
     "code": 200,
     "message": "success",
     "data": {
       "userId": "123",
       "username": "张三"
     }
   }
   ```

**方式二：Swagger导入**（批量）
- 输入Swagger文档URL
- 系统自动解析并批量导入API

**方式三：API文档导入**（推荐）
- 上传OpenAPI 3.0/Postman Collection文件
- 自动识别并导入

#### 3.2 API详情

点击API进入详情页，查看：
- **基本信息**：名称、路径、方法、状态
- **请求参数**：参数列表、类型、必填项
- **响应结构**：返回数据结构
- **调用统计**：调用次数、成功率、平均响应时间
- **版本历史**：API变更记录

#### 3.3 API测试

在API详情页或专门的测试页面：
1. 选择请求方法
2. 输入请求URL：`http://localhost:8081/api/test/hello?name=测试`
3. 添加请求头（可选）
4. 输入请求体（POST/PUT）
5. 点击 **"发送请求"**
6. 查看响应结果

**提示**：测试时会记录到调用日志，会影响统计数据！

---

### 4. 🔄 服务编排（聚合接口管理）

**路径**：聚合接口管理 / 聚合接口编排

**功能**：将多个原子API组合成一个聚合接口

#### 4.1 创建聚合接口

1. 点击 **"新建聚合"**
2. 填写聚合接口信息：
   - 名称：如 "用户详情聚合接口"
   - 路径：如 `/aggregate/user/detail`
   - 方法：GET/POST
   - 描述

3. 进入可视化编排页面

#### 4.2 可视化编排

**编排流程**：

```
┌─────────┐     ┌─────────┐     ┌─────────┐
│ 开始节点 │ ──> │ API节点 │ ──> │ 结束节点 │
└─────────┘     └─────────┘     └─────────┘
```

**节点类型**：
- **开始节点**：接收请求参数
- **API调用节点**：调用原子API
- **条件判断节点**：根据条件分支
- **数据转换节点**：数据映射、聚合
- **结束节点**：返回结果

**编排步骤**：

1. **拖拽节点**：从左侧工具栏拖拽到画布
2. **连接节点**：拖拽连线连接节点
3. **配置节点**：双击节点配置
   - API节点：选择要调用的API、参数映射
   - 条件节点：设置判断条件
   - 转换节点：编写数据转换脚本

4. **参数映射**：
   ```javascript
   // 示例：将上一个节点的结果传递给下一个节点
   {
     "userId": "$input.userId",  // 来自请求
     "token": "$context.token",   // 来自上下文
     "data": "$prev.data"         // 来自上一节点
   }
   ```

5. **数据转换**：
   ```javascript
   // 合并多个API返回的数据
   {
     "user": $api1.data,
     "orders": $api2.data.list,
     "total": $api2.data.total
   }
   ```

6. **保存配置**
7. **测试运行**：
   - 输入测试参数
   - 点击 **"测试运行"**
   - 查看执行结果和每个节点的输出

8. **发布上线**：测试通过后点击 **"发布"**

#### 4.3 聚合接口示例

**场景**：订单详情页需要调用多个接口

```
接口名称：订单详情聚合
路径：/aggregate/order/detail
输入：{ orderId: "123456" }

编排流程：
1. 调用订单API → 获取订单信息
2. 调用用户API → 获取用户信息（使用订单中的userId）
3. 调用商品API → 获取商品列表（使用订单中的商品IDs）
4. 数据转换 → 合并三个API的结果
5. 返回聚合数据

输出：
{
  "order": { ... },
  "user": { ... },
  "products": [ ... ]
}
```

---

### 5. 🛡️ 治理中心

#### 5.1 限流策略

**路径**：治理中心 → 限流策略

**功能**：控制API调用频率，防止系统过载

**创建限流规则**：
1. 选择目标API
2. 设置限流类型：
   - **QPS限流**：每秒请求数
   - **并发限流**：同时请求数
   - **用户限流**：单用户限流
   
3. 配置限流参数：
   - 阈值：如 100次/秒
   - 时间窗口：如 1秒
   - 限流策略：拒绝/排队

4. 选择生效范围：
   - 全局
   - 指定应用
   - 指定用户

**示例**：
```
API：/api/user/info
类型：QPS限流
阈值：100次/秒
策略：超出拒绝
```

#### 5.2 黑白名单

**路径**：治理中心 → 黑白名单

**功能**：控制访问权限

**黑名单**：禁止访问
- IP黑名单
- 用户黑名单
- 应用黑名单

**白名单**：仅允许访问
- IP白名单
- 用户白名单
- 应用白名单

**添加规则**：
1. 选择类型（黑名单/白名单）
2. 选择维度（IP/用户/应用）
3. 输入值
4. 选择作用API（全局/指定API）
5. 设置有效期

#### 5.3 缓存策略

**路径**：治理中心 → 缓存策略

**功能**：提高响应速度，减少后端压力

**创建缓存规则**：
1. 选择目标API
2. 设置缓存Key规则：
   ```
   cache:api:{apiId}:{userId}:{params}
   ```
3. 设置过期时间：如 300秒
4. 缓存更新策略：
   - 时间过期
   - 手动刷新
   - 条件刷新

**示例**：
```
API：/api/product/list
缓存Key：product:list:{category}:{page}
过期时间：600秒
```

---

### 6. 👥 客户管理

**路径**：客户管理 → 应用列表

**功能**：管理接入系统的客户应用

#### 6.1 创建应用

1. 点击 **"新建应用"**
2. 填写应用信息：
   - 应用名称
   - 应用类型：Web/Mobile/Server
   - 所属客户
   - 描述

3. 系统自动生成：
   - **App Key**：应用唯一标识
   - **App Secret**：应用密钥

4. 配置API权限：
   - 勾选允许调用的API
   - 设置调用配额

#### 6.2 应用管理

- **查看密钥**：查看App Key和Secret
- **重置密钥**：安全问题时重置Secret
- **API授权**：管理API访问权限
- **调用统计**：查看应用调用数据
- **启用/禁用**：控制应用状态

#### 6.3 接入示例

```javascript
// 客户端调用示例
const axios = require('axios');
const crypto = require('crypto');

// 签名算法
function generateSign(params, appSecret) {
  const sortedParams = Object.keys(params)
    .sort()
    .map(key => `${key}=${params[key]}`)
    .join('&');
  return crypto.createHmac('sha256', appSecret)
    .update(sortedParams)
    .digest('hex');
}

// API调用
const params = {
  appKey: 'your_app_key',
  timestamp: Date.now(),
  userId: '123'
};
params.sign = generateSign(params, 'your_app_secret');

axios.post('http://localhost:8081/api/user/info', params)
  .then(res => console.log(res.data));
```

---

### 7. 📊 运维监控

#### 7.1 调用日志

**路径**：运维监控 → 调用日志

**功能**：查看所有API调用记录

**查询条件**：
- 时间范围
- API路径
- 请求方法
- 状态码
- 响应时间范围
- 调用来源（IP/应用）

**日志详情**：
- 请求时间
- API路径
- 请求方法
- 状态码
- 响应时间
- 错误信息（如有）
- 请求参数
- 响应内容

**导出功能**：
- 导出CSV
- 导出Excel

#### 7.2 告警中心

**路径**：运维监控 → 告警中心

**功能**：接收和处理系统告警

**页面说明**：
- **告警统计卡片**：显示当前实际触发的告警数量（按级别统计）
  - 严重告警、警告、通知：显示当前 `status='firing'` 的告警数
  - 已解决：显示 `status='resolved'` 的告警数
- **活跃告警列表**：显示当前触发中的告警记录
- **告警规则列表**：显示已配置的告警规则（无论是否触发）

> **注意**：如果顶部统计显示0，说明虽然配置了告警规则，但目前没有实际触发的告警。可以执行 `insert_test_alerts.sql` 插入测试告警数据来验证功能。

**告警类型**：
- **性能告警**：响应时间超阈值
- **错误告警**：错误率超阈值
- **流量告警**：调用量异常
- **可用性告警**：API下线

**告警规则配置**：
1. 选择监控指标
2. 设置阈值
3. 设置持续时间
4. 配置通知方式：
   - 站内通知
   - 邮件
   - 短信
   - Webhook

**告警处理**：
- 查看告警详情
- 确认告警（标记为已读）
- 解决告警（标记为已解决）
- 批量确认

---

### 8. ⚙️ 系统管理

#### 8.1 用户管理

**路径**：系统管理 → 用户管理

**功能**：管理系统用户

- 添加用户
- 编辑用户信息
- 分配角色
- 重置密码
- 启用/禁用用户
- 查看用户操作日志

#### 8.2 角色管理

**路径**：系统管理 → 角色管理

**功能**：管理用户角色和权限

**内置角色**：
- **超级管理员**：所有权限
- **运维人员**：监控、日志权限
- **开发人员**：API管理权限
- **访客**：只读权限

**自定义角色**：
1. 创建角色
2. 设置权限：
   - 平台管理
   - API管理
   - 聚合编排
   - 治理配置
   - 监控查看
   - 系统管理

#### 8.3 操作日志

**路径**：系统管理 → 操作日志

**功能**：审计用户操作

记录内容：
- 操作人
- 操作时间
- 操作类型（创建/更新/删除/登录）
- 操作模块
- 操作详情
- 操作IP
- 操作结果

---

## 🎯 典型使用场景

### 场景1：接入新平台API

```
1. 平台列表 → 新增平台（如：京东开放平台）
2. 配置平台信息（Base URL、认证信息）
3. API仓库 → 导入API（Swagger URL或手动创建）
4. 测试API是否正常
5. 发布给客户应用使用
```

### 场景2：创建聚合接口

```
1. 确保原子API已导入
2. 聚合接口管理 → 新建聚合
3. 可视化编排（拖拽节点、配置参数）
4. 测试运行验证
5. 发布上线
6. 客户应用调用聚合接口
```

### 场景3：设置API限流

```
1. 治理中心 → 限流策略
2. 新建规则
3. 选择API：/api/order/create
4. 设置QPS：100次/秒
5. 保存生效
6. 实时监控调用情况
```

### 场景4：应用接入

```
1. 客户管理 → 新建应用
2. 获取App Key和Secret
3. API授权（勾选允许调用的API）
4. 客户端集成SDK
5. 测试调用
6. 监控调用日志
```

---

## 🔧 配置说明

### 后端配置 (application.yml)

```yaml
server:
  port: 8081  # 后端端口

spring:
  # 数据库配置
  datasource:
    url: jdbc:mysql://localhost:3306/api_manage?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  # Redis配置
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    timeout: 3000
    lettuce:
      pool:
        max-active: 8
        max-wait: -1
        max-idle: 8
        min-idle: 0

# MyBatis配置
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.api.model
  configuration:
    map-underscore-to-camel-case: true

# JWT配置
jwt:
  secret: your_jwt_secret_key_here
  expiration: 86400000  # 24小时
```

### 前端配置 (vue.config.js)

```javascript
module.exports = {
  devServer: {
    port: 8080,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
}
```

---

## 📝 常见问题

### Q1: 首页统计数据不更新？

**A**: 参考 [TEST_API_STATS.md](./TEST_API_STATS.md)
- 确保Redis正在运行
- 确认调用的是 `/api/` 开头的业务API
- 查看后端日志是否有 `[API统计]` 输出

### Q2: 无法登录系统？

**A**: 
- 检查数据库连接
- 确认用户表有数据（运行init.sql）
- 清除浏览器缓存和localStorage
- 检查后端是否正常启动

### Q3: API调用失败？

**A**:
- 检查平台配置（Base URL、认证信息）
- 查看网络连接
- 检查防火墙设置
- 查看调用日志中的错误信息

### Q4: 聚合接口编排无法保存？

**A**:
- 确保所有节点都已连接
- 检查参数映射是否正确
- 验证数据转换脚本语法
- 查看浏览器控制台错误

### Q5: 限流策略不生效？

**A**:
- 确认规则已启用
- 检查API路径是否匹配
- 验证Redis连接
- 查看规则生效时间

---

## 🚀 生产环境部署

### 后端部署

```bash
# 打包
cd backend
mvn clean package -DskipTests

# 运行
java -jar target/api-manage-backend-1.0.0.jar \
  --spring.profiles.active=prod \
  --server.port=8081
```

### 前端部署

```bash
# 构建
cd frontend
npm run build

# 部署到Nginx
cp -r dist/* /usr/share/nginx/html/
```

### Nginx配置

```nginx
server {
    listen 80;
    server_name yourdomain.com;

    # 前端
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    # 后端代理
    location /api/ {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## 📞 技术支持

如有问题或建议，请：
- 查看项目文档
- 查看系统日志
- 提交Issue
- 联系技术支持团队

---

## 📄 相关文档

- [快速开始指南](./README.md)
- [后端开发文档](./backend/README.md)
- [前端开发文档](./frontend/README.md)
- [API统计测试指南](./TEST_API_STATS.md)
- [API接口文档](http://localhost:8081/swagger-ui.html)

---

**最后更新**: 2026年1月4日
