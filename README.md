## 一、项目介绍

**蓝海编程平台**是一个面向编程爱好者的综合技术社区系统，集资源分享、技术文章、需求对接、AI 智能客服、实时消息通知等核心功能于一体。项目采用**前后端分离**架构，后端基于 **Spring Boot 3 + Spring Security 6 + MyBatis-Plus** 构建 RESTful 服务，前端基于 **Vue 3 + TypeScript + Naive UI** 构建 SPA 应用。涵盖**门户端（前台）** 与**管理后台**两套前端，支持完整的用户认证授权、内容审核、积分体系、实时通信等业务闭环。

项目遵循 Maven 多模块分层架构设计，各模块职责清晰、解耦充分，具备良好的可扩展性和可维护性。

### 核心功能矩阵

| 模块 | 功能 |
|------|------|
| 资源分享 | 资源 CRUD、分类标签、OSS 文件存储、积分下载、收藏评论 |
| 编程提升 | 技术文章、草稿/发布管理、点赞收藏、分类标签、热门排行 |
| 需求发布 | 需求发布/接单、预算/截止日期、接单竞标、附件管理 |
| AI 智能客服 | 多轮对话、FAQ 匹配、知识库检索、工单系统、转人工 |
| 实时消息 | WebSocket 私信、关注/粉丝、系统通知、公告推送 |
| 管理后台 | 仪表盘、用户/角色/权限管理、审核、系统配置、日志审计 |
| 用户体系 | JWT 双 Token、BCrypt 加密、角色权限 RBAC、个人中心 |

---

## 二、项目技术栈

### 后端技术栈

| 类别 | 技术选型 | 版本 |
|------|----------|------|
| 核心框架 | Spring Boot (WebMVC) | 3.4.4 |
| 安全框架 | Spring Security 6 + JWT (jjwt) | 6.4.4 / 0.12.6 |
| ORM 框架 | MyBatis-Plus (Spring Boot 3 Starter) | 3.5.9 |
| 数据库 | MySQL 8.0+ (utf8mb4) | — |
| 缓存 | Redis (Spring Cache + @Cacheable) | — |
| 消息队列 | RabbitMQ (AMQP) | — |
| 实时通信 | WebSocket (STOMP over SockJS) | — |
| AI 集成 | LangChain4j (火山引擎豆包大模型) | 1.13.1 |
| 对象存储 | 阿里云 OSS SDK | 3.17.4 |
| 接口文档 | SpringDoc OpenAPI (Swagger) | 2.7.0 |
| 工具库 | Lombok, Hutool, EasyExcel, Jackson | — |
| 构建工具 | Maven (多模块) | — |
| JDK | Java 17 | — |

### 前端技术栈

| 类别 | 技术选型 | 版本 |
|------|----------|------|
| 框架 | Vue 3 (Composition API) | — |
| 语言 | TypeScript | — |
| 构建 | Vite | 5.x |
| UI 组件库 | Naive UI | 2.x |
| 状态管理 | Pinia | 3.x |
| 路由 | Vue Router | 4.x |
| HTTP 客户端 | Axios | — |

### 项目模块架构

```
blueseacode-platform
├── blueseacode-common          # 公共模块：常量、枚举、异常、响应封装、工具类
├── blueseacode-dao             # 数据访问层：实体、Mapper、DTO、MyBatis XML
├── blueseacode-security        # 安全模块：JWT 过滤器、认证授权、登录处理器
├── blueseacode-service         # 业务逻辑层：资源/文章/需求/用户/消息/积分等服务
├── blueseacode-web             # Web 层：Portal 端 + Admin 端控制器（共 25+ 个）
│   ├── controller/portal/      # 门户端 API（14 个控制器）
│   └── controller/admin/       # 管理后台 API（11 个控制器）
├── blueseacode-ai              # AI 模块：LangChain4j 集成、FAQ、知识库、工单
└── blueseacode-websocket       # WebSocket 模块：私信、在线用户、关注
```
## 快速启动

1. 导入 `db/*.sql` 初始化数据库
2. 配置 `application.yaml` 中的数据库、Redis、OSS 等参数
3. 启动后端：运行 `BlueseacodeApplication.java`
4. 启动前端：`cd blueseacode-frontend && npm install && npm run dev`
