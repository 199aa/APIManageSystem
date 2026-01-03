# API 管理系统

一个基于 Vue2 + Spring Boot 的 API 可视化管理系统。

## 项目介绍

本项目包含前端和后端两部分：

- **前端**：Vue2 + Element UI，提供 API 可视化管理界面
- **后端**：Spring Boot + MyBatis + Redis + MySQL，提供 RESTful API 服务

## 功能特性

- 用户登录/注册
- API 列表管理
- API 详情查看
- API 在线测试
- 响应式布局，支持多终端访问

## 快速开始

### 前端启动

```bash
cd frontend
npm install
npm run serve
```

访问地址：http://localhost:8080

### 后端启动

1. 确保 MySQL 和 Redis 服务已启动
2. 执行 `backend/src/main/resources/sql/init.sql` 初始化数据库
3. 修改 `backend/src/main/resources/application.yml` 配置
4. 启动后端服务

```bash
cd backend
mvn spring-boot:run
```

访问地址：http://localhost:8081

## 技术栈

### 前端

- Vue 2.6
- Vue Router 3.x
- Vuex 3.x
- Element UI 2.x
- Axios

### 后端

- Spring Boot 2.7
- MyBatis
- MySQL 8.0
- Redis
- JWT

## 测试账号

- 用户名：admin / test
- 密码：123456

## 项目结构

```
ApiManageSystem/
├── frontend/          # 前端项目
│   ├── public/       # 静态资源
│   ├── src/          # 源代码
│   └── package.json  # 依赖配置
│
└── backend/          # 后端项目
    ├── src/          # 源代码
    └── pom.xml       # Maven配置
```

## 开发说明

- 前端默认代理后端接口到 http://localhost:8081
- 后端默认开启跨域支持
- 使用 JWT 进行用户认证

## License

MIT
