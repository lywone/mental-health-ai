# 心理健康AI助手

面向 C 端用户的心理健康 AI 助手平台，包含**用户端**（AI 心理咨询、情绪日志、心理知识库、个人中心）与**管理后台**（用户/内容/咨询管理），前后端分离的单体项目。

## 技术栈

| 端 | 技术 |
|----|------|
| 后端 [ai-serve](ai-serve/) | Java 21 · Spring Boot 3.2.5 · Spring Security + JWT · MyBatis-Plus · MySQL 8 · Redis · Knife4j(OpenAPI3) |
| 前端 [ai-vue](ai-vue/) | Vue 3（Composition API）· Vite · Vue Router 4 · Element Plus · Axios |

## 功能概览

**用户端**（`/`）
- AI 心理咨询：流式对话、危机识别、会话记录（`llm.enabled=false` 时使用内置模拟回复，可切换真实大模型）
- 情绪日志：情绪打卡、花园视图
- 心理知识库：文章浏览
- 账号体系：手机号 + 短信验证码**注册/登录**、个人中心（昵称/头像/性别/生日/简介）、AI 咨询入口登录校验

**管理后台**（`/back`）
- 管理员登录（账号种子数据见 `ai-serve/src/main/resources/db/init.sql`）
- 用户管理、内容管理、咨询记录查看

## 快速开始

### 环境要求

- JDK 21、Maven 3.8+
- Node.js 18+
- MySQL 8、Redis 5+（本地默认 `localhost`，无需密码）

### 1. 初始化数据库

新建数据库 `mental_health` 后执行建表脚本：

```sql
source ai-serve/src/main/resources/db/init.sql
```

> 存量库升级：若表结构早于「用户注册与个人中心」版本，补执行 `ai-serve/src/main/resources/db/upgrade-20261027-user-profile.sql`。

### 2. 配置并启动后端

数据库账号在 [application-dev.yml](ai-serve/src/main/resources/application-dev.yml) 中修改（默认 `root/root`）。

```bash
cd ai-serve
mvn spring-boot:run
```

- 服务端口：`8080`
- 接口文档：http://localhost:8080/doc.html

### 3. 启动前端

```bash
cd ai-vue
npm install
npm run dev
```

按终端输出的地址访问（默认 http://localhost:5173 ）。

## 常用配置说明（application.yml）

| 配置 | 说明 |
|------|------|
| `llm.enabled` | `false` 使用内置模拟 AI 回复（默认）；`true` 需填 `base-url / api-key / model` |
| `sms.channel` | 验证码发送通道，`log`=仅打印日志（默认，联调用）；预留 aliyun/tencent |
| `sms.echo-dev-code` | `true` 时发码接口回显验证码，**仅限开发环境** |
| `auth.dev-login-enabled` | 用户端开发登录开关，生产必须 `false` |

## 安全提醒（生产部署前）

- 更换 `jwt.secret`（HS256，至少 32 字节）
- 关闭 `auth.dev-login-enabled` 与 `sms.echo-dev-code`
- 修改数据库默认密码，接入真实短信服务

## 目录结构

```
├── ai-serve/              # Spring Boot 后端
│   └── src/main/java/com/mental/health
│       ├── common/        # Result / ErrorCode / 全局异常
│       ├── config/        # Security / JWT / Redis / OpenAPI
│       ├── infra/         # 存储 / 短信 / LLM 接入
│       └── module/        # auth · user · consult · admin 业务模块
├── ai-vue/                # Vue 3 前端
│   ├── src/api/           # 请求封装与接口定义
│   ├── src/components/    # UserLayout / BackendLayout
│   ├── src/views/         # 用户端（user/）与管理端（back/）页面
│   └── docs/              # 设计文档
└── README.md
```
