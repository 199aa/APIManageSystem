# API 管理系统 - 后端

基于 Spring Boot + MyBatis + Redis + MySQL 构建的 API 管理系统后端服务。

## 技术栈

- Spring Boot 2.7.18
- MyBatis 2.3.1
- MySQL 8.0
- Redis
- Druid 连接池
- JWT 认证

## 项目结构

```
src/main/java/com/api/
├── controller/      # 控制器层
├── service/        # 服务层
├── mapper/         # 数据访问层
├── model/          # 实体类
├── config/         # 配置类
├── util/           # 工具类
└── common/         # 通用类

src/main/resources/
├── mapper/         # MyBatis XML映射文件
├── sql/            # SQL脚本
└── application.yml # 配置文件
```

## 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis

## 快速开始

### 1. 数据库配置

执行 `src/main/resources/sql/init.sql` 脚本创建数据库和表。

### 2. 修改配置

修改 `application.yml` 中的数据库和 Redis 配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/api_manage
    username: root
    password: your_password

  redis:
    host: localhost
    port: 6379
    password:
```

### 3. 启动项目

```bash
# 使用Maven启动
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/api-manage-backend-1.0.0.jar
```

服务将在 http://localhost:8081 启动。

## API 接口文档

### 用户接口

- POST `/user/login` - 用户登录
- POST `/user/register` - 用户注册
- GET `/user/info` - 获取用户信息

### API 管理接口

- GET `/api/list` - 获取 API 列表
- GET `/api/detail/{id}` - 获取 API 详情
- POST `/api/create` - 创建 API
- PUT `/api/update/{id}` - 更新 API
- DELETE `/api/delete/{id}` - 删除 API

## 测试账号

- 用户名：admin / test
- 密码：123456

## 注意事项

- 确保 MySQL 和 Redis 服务已启动
- 默认端口为 8081，可在配置文件中修改
- JWT 密钥建议在生产环境中修改
