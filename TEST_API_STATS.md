# API 调用统计测试指南

## 问题说明

首页的"今日调用次数"不变化的原因：

1. **原测试页面是模拟的** - 之前的 `frontend/src/views/api/test.vue` 只是模拟请求，不会真正调用后端
2. **管理接口被排除统计** - 大部分管理接口（如用户管理、平台管理等）被排除在统计之外
3. **只统计业务 API** - 只有 `ApiController` 中的接口会被统计

## 已修复内容

### 1. 修复了测试页面 (frontend/src/views/api/test.vue)

- ✅ 改为发送真实的 HTTP 请求
- ✅ 支持 GET、POST、PUT、DELETE 方法
- ✅ 支持自定义请求头和请求体
- ✅ 显示真实的响应状态、耗时、数据

### 2. 添加了测试端点 (backend/.../ApiController.java)

- ✅ `GET /api/test/hello?name=xxx` - 简单的 Hello 测试
- ✅ `POST /api/test/echo` - Echo 回显测试

### 3. 增强了日志输出 (backend/.../ApiCallLogAspect.java)

- ✅ 详细的拦截日志
- ✅ Redis 更新日志
- ✅ 便于调试和排查问题

## 测试步骤

### 方式一：使用前端测试工具

1. **启动后端服务**

   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **启动前端服务**

   ```bash
   cd frontend
   npm run serve
   ```

3. **打开测试页面**

   - 导航到：API 管理 -> API 测试
   - 或直接访问：http://localhost:8080/#/api/test

4. **测试 GET 请求**

   - 请求方法：GET
   - 请求 URL：`http://localhost:8081/api/test/hello?name=张三`
   - 点击"发送请求"

5. **测试 POST 请求**

   - 请求方法：POST
   - 请求 URL：`http://localhost:8081/api/test/echo`
   - 请求体：
     ```json
     {
       "message": "测试消息",
       "userId": 123
     }
     ```
   - 点击"发送请求"

6. **查看首页统计**
   - 返回首页（Dashboard）
   - 查看"今日调用"卡片
   - 数字应该已经增加

### 方式二：使用 Postman/curl

```bash
# GET测试
curl http://localhost:8081/api/test/hello?name=测试用户

# POST测试
curl -X POST http://localhost:8081/api/test/echo \
  -H "Content-Type: application/json" \
  -d '{"message":"测试"}'
```

### 方式三：使用浏览器直接访问

直接在浏览器中打开：

```
http://localhost:8081/api/test/hello?name=浏览器测试
```

## 查看日志

在后端控制台中，你会看到类似这样的日志：

```
========================================
[API统计] 拦截到API调用
[API统计] 路径: /api/test/hello
[API统计] 方法: GET
[API统计] Controller: com.api.controller.ApiController
[API统计] Method: testHello
========================================
[API统计] 调用成功，状态码: 200
[API统计] 响应时间: 15ms
[Redis统计] 开始更新计数器, 日期: 2026-01-04
[Redis统计] 累计调用总数: 1
[Redis统计] 今日调用数 (Key: dashboard:today_calls:2026-01-04): 1
[Redis统计] 今日成功调用数 (Key: dashboard:today_success:2026-01-04): 1
[Redis统计] 计数器更新完成
```

## 验证 Redis 数据

可以使用 Redis 客户端查看数据：

```bash
# 连接Redis
redis-cli

# 查看累计调用数
GET dashboard:total_calls

# 查看今日调用数
GET dashboard:today_calls:2026-01-04

# 查看今日成功数
GET dashboard:today_success:2026-01-04
```

## 注意事项

1. **确保 Redis 正在运行** - 统计数据存储在 Redis 中
2. **确保后端端口正确** - 默认是 8081
3. **首次使用可能需要刷新** - 前端页面可能需要刷新才能看到最新数据
4. **只统计业务 API** - 管理接口（如/dashboard/stats、/user/login 等）不会被统计

## 被统计的接口

只有 `ApiController` 中的接口会被统计：

- ✅ `/api/list` - API 列表
- ✅ `/api/detail/{id}` - API 详情
- ✅ `/api/create` - 创建 API
- ✅ `/api/update/{id}` - 更新 API
- ✅ `/api/delete/{id}` - 删除 API
- ✅ `/api/test/hello` - 测试 Hello（新增）
- ✅ `/api/test/echo` - 测试 Echo（新增）

## 不被统计的接口

以下接口不会被统计：

- ❌ `/dashboard/*` - 仪表板
- ❌ `/user/*` - 用户管理
- ❌ `/platform/*` - 平台管理
- ❌ `/alert/*` - 告警管理
- ❌ 等等...（所有管理接口）

## 故障排查

如果统计仍然不工作：

1. **检查 Redis 连接**

   - 查看后端日志是否有 Redis 连接错误
   - 确认 `application.yml` 中的 Redis 配置正确

2. **检查切面是否生效**

   - 查看后端日志是否有"[API 统计]"字样
   - 如果没有，可能是 Spring AOP 配置问题

3. **检查调用的接口**

   - 确认调用的是 `/api/` 开头的接口
   - 不是 `/dashboard/`、`/user/` 等管理接口

4. **查看完整错误日志**
   - 在后端控制台查看详细的错误堆栈
