
# 信息管理系统

基于 Spring Boot + Vue 3 前后端分离的信息管理系统，支持管理员和用户两种角色，提供用户管理、请假审批、公告发布、数据可视化等功能。

## ✨ 功能特性

### 用户管理
- 管理员 / 普通用户 分角色登录
- 用户注册与登录（JWT 鉴权）
- 个人信息维护（头像、密码、手机号等）
- 用户数据的增删改查、批量删除
- 用户数据导入 / 导出（Excel）

### 管理员管理
- 管理员信息的增删改查
- 管理员数据导入 / 导出（Excel）

### 请假审批
- 用户提交请假申请
- 管理员审核请假申请（通过 / 拒绝）
- 请假状态统计与可视化

### 公告管理
- 管理员发布 / 编辑 / 删除公告
- 用户查看公告详情

### 数据可视化
- 请假状态分布饼图（ECharts）
- 月度请假趋势柱状图（ECharts）
- 首页统计卡片（总数、待审核、已通过、已拒绝）

## 🛠️ 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 1.8 | 开发语言 |
| Spring Boot | 2.6.13 | 核心框架 |
| MyBatis | 2.2.2 | ORM 框架 |
| MySQL | 8.x | 数据库 |
| Redis | - | 缓存 |
| PageHelper | 1.4.6 | 分页插件 |
| Hutool | 5.8.25 | 工具库（Excel 导入导出） |
| Apache POI | 5.2.3 | Excel 处理 |
| JWT (auth0) | 4.3.0 | 身份认证 |
| Lombok | 1.18.30 | 简化代码 |
| FastJSON2 | 2.0.43 | JSON 处理 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.x | 前端框架 |
| Vue Router | 5.x | 路由管理 |
| Element Plus | 2.13.x | UI 组件库 |
| Axios | 1.14.x | HTTP 请求 |
| ECharts | 6.x | 数据可视化 |
| Vite | 7.x | 构建工具 |
| Sass | - | CSS 预处理器 |

## 📁 项目结构

```

springbootandvue/
├── springboot/                  # 后端项目
│   └── src/main/java/com/example/
│       ├── common/              # 公共配置（CORS、JWT拦截器、统一响应）
│       ├── controller/          # 控制器层
│       ├── entity/              # 实体类
│       ├── exception/           # 全局异常处理
│       ├── mapper/              # MyBatis Mapper 接口
│       ├── service/             # 业务逻辑层
│       ├── utils/               # 工具类（Token 工具类）
│       └── SpringbootApplication.java
├── vue/                         # 前端项目
│   └── src/
│       ├── assets/              # 静态资源
│       ├── components/          # 公共组件
│       ├── router/              # 路由配置
│       ├── views/               # 页面视图
│       ├── App.vue              # 根组件
│       └── main.js              # 入口文件
└── files/                       # 文件上传存储目录
```
## 🚀 快速开始

### 环境要求

- JDK 1.8+
- Node.js 20.19+ 或 22.12+
- MySQL 8.x
- Redis
- Maven 3.x

### 后端启动

1. 创建 MySQL 数据库 `admin`，导入数据库脚本
2. 修改配置文件 `springboot/src/main/resources/application-dev.yml` 中的数据库和 Redis 连接信息：

```
yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/admin
    username: root
    password: 你的密码

redis:
  host: 127.0.0.1
  port: 6379
  password: 你的Redis密码
```
3. 启动后端服务：

```
bash
cd springboot
mvn spring-boot:run
```
后端服务默认运行在 `http://localhost:8083`

### 前端启动

```
bash
cd vue
npm install
npm run dev
```
前端服务默认运行在 `http://localhost:5173`

## 📋 功能模块说明

| 模块 | 路由 | 说明 |
|------|------|------|
| 登录 | `/login` | 支持管理员 / 用户角色选择 |
| 注册 | `/register` | 新用户注册 |
| 首页 | `/manager/home` | 数据统计 + 图表 + 最新公告 |
| 管理员管理 | `/manager/admin` | 管理员信息维护 |
| 用户管理 | `/manager/user` | 用户信息维护 |
| 请假管理 | `/manager/leaveRequest` | 请假申请与审批 |
| 公告管理 | `/manager/announcement` | 公告发布与管理 |
| 个人中心 | `/manager/person` | 个人信息修改 |

## 🔐 认证方式

项目采用 JWT（JSON Web Token）进行身份认证：
- 登录成功后服务端签发 Token
- 前端将 Token 存储在 `localStorage` 中
- 每次请求通过 Axios 拦截器自动携带 Token
- 后端通过 JWT 拦截器校验请求合法性

## 📄 License

MIT License

---

> 如果对你有帮助，欢迎 Star ⭐ 支持！
```
```

建议改为更有意义的项目名称，如 `info-management-system`。
