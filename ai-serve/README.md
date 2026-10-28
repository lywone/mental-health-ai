# 心理健康AI助手 · 后端（mental-health-server）

Spring Boot 3.2.5 + MyBatis-Plus + Redis + JWT 的《心理健康AI助手》后端服务，实现管理后台 + 用户端全部 27 个接口，与 `ai-vue` 前端（Vue3 + Element Plus）联调。

## 环境要求

- JDK 17+
- MySQL 8.0
- Redis 6+
- Maven 3.8+（或直接用 IDEA 内置 Maven）

## 快速开始

### 1. 导入数据库

```bash
mysql -uroot -p < src/main/resources/db/init.sql
```

脚本会自动创建数据库 `mental_health`、6 张表（`sys_admin` / `t_user` / `t_mood_log` / `t_consult_session` / `t_consult_message` / `t_article`）及种子数据（3 用户、12 篇文章、25 条情绪日志、8 个咨询会话及消息）。时间字段基于导入当天动态生成，保证统计口径自洽。

### 2. 修改配置（`src/main/resources/application-dev.yml`）

按本机环境修改以下默认值：

| 配置点 | 默认值 | 说明 |
| --- | --- | --- |
| `spring.datasource.url/username/password` | `localhost:3306/mental_health`，root/root | 本地 MySQL |
| `spring.data.redis.host/port/password` | `localhost:6379`，无密码 | 本地 Redis |

`application.yml` 中其余公共配置一般无需改动：

| 配置点 | 默认值 | 说明 |
| --- | --- | --- |
| `jwt.secret` | 内置 64 字节随机串 | HS256 密钥，生产环境必须替换 |
| `jwt.admin-expire-seconds` | `7200` | 管理员 token 有效期 2h |
| `jwt.user-expire-seconds` | `86400` | 用户 token 有效期 24h |
| `llm.enabled` | `false` | 是否接入真实 LLM（OpenAI 兼容接口）；`false` 时走降级语料 |
| `llm.base-url` / `llm.api-key` / `llm.model` | 示例值 | 接入 DeepSeek/OpenAI 兼容服务时填写 |
| `llm.timeout-seconds` | `5` | LLM 调用超时，超时/异常自动降级 |
| `storage.upload-dir` | `./upload` | 封面图存储目录，URL 前缀 `/upload` |

### 3. 启动

```bash
# 方式一：Maven
mvn spring-boot:run

# 方式二：IDEA 打开工程，运行 MentalHealthApplication#main
```

- 默认端口：`8080`
- 接口文档（Knife4j）：http://localhost:8080/doc.html
- 首次启动自动初始化默认管理员：**admin / 123456**（`sys_admin` 表为空时）
- 用户端无需注册：`POST /api/auth/user/dev-login` 即可获取测试 token（缺省返回最小 ID 用户，表空自动创建「小林」）

### 4. 与 ai-vue 前端联调

- CORS 已放行 `http://localhost:5173`（方法与请求头全放开），前端 `npm run dev` 后可直接访问本服务；
- 前端按《后端接口设计文档 v1.0》第 7 章替换 `src/mock` 中的 Mock 请求：统一 Base URL `/api`，响应码 `code===0` 取 `data`，`1001/1003/1004` 跳转登录，错误码映射见文档 2.4；
- 前端请求拦截器统一携带 `Authorization: Bearer <token>`，`tokenType` 与 `expiresIn` 由登录接口返回。

## 接口总览（27 个）

| 分组 | 接口 | 权限 |
| --- | --- | --- |
| 认证 | `POST /api/auth/admin/login`、`POST /api/auth/admin/logout`、`GET /api/auth/admin/me`、`POST /api/auth/user/dev-login` | 公开(2) / ADMIN(2) / 公开 |
| 数据看板 | `GET /api/admin/dashboard/stats`、`GET /api/admin/dashboard/trends` | ADMIN |
| 文章管理 | `GET/POST /api/admin/articles`、`GET/PUT/DELETE /api/admin/articles/{id}`、`PUT /api/admin/articles/{id}/status` | ADMIN |
| 上传 | `POST /api/admin/upload/image`（jpg/png/webp ≤5MB） | ADMIN |
| 咨询管理 | `GET /api/admin/consultations`、`GET /api/admin/consultations/{id}` | ADMIN |
| 情绪日志 | `GET /api/admin/moods`、`GET /api/admin/moods/{id}` | ADMIN |
| 情绪花园 | `GET /api/user/emotion-garden` | USER |
| 咨询对话 | `GET /api/user/consultations`、`POST /api/user/chat/messages`、`POST /api/user/consultations/{id}/end`、`PUT /api/user/consultations/{id}/score` | USER |
| 情绪记录 | `POST /api/user/moods`、`GET /api/user/moods` | USER |
| 文章阅读 | `GET /api/user/articles`、`GET /api/user/articles/{id}` | USER |
| 公共字典 | `GET /api/common/dicts?type=article_category\|mood_tag` | 任意登录 |

响应统一结构：`{code, message, data}`；分页：`{list, total, page, pageSize}`；错误码见设计文档 2.4（`0/1001/1002/1003/1004/1101/1102/1201/1202/1203/1204/1301/1302/1501/500`）。

## 关键设计说明

- **JWT**：HS256，claims 含 `uid/role/nickname`；无状态 + Redis 黑名单登出（TTL=剩余有效期）；
- **AI 对话**：危机词命中直接返回干预话术（`risk_flag=1`，不调用 LLM）；否则调用 OpenAI 兼容 `/chat/completions`（system 人设「宁澄AI助手/小暖」+ 最近 10 条历史），超时/异常按 `AI_REPLY_RULES` 关键词规则降级，再退到 `AI_FALLBACK_REPLIES` 顺序轮换；
- **限流**：Redis `INCR chat:rl:{uid}:{yyyyMMddHHmm}`（65s 过期），每分钟 >10 条消息返回 1101；
- **定时任务**：每小时扫描静默 >24h 的进行中会话，按同一规则自动结束（`end_time=now`，`duration_min=max(1, 四舍五入分钟)`）；
- **浏览量**：用户端文章详情 `UPDATE ... SET views=views+1` 原子自增后返回；
- **审计**：管理员查看会话消息与情绪日志详情会输出审计日志（v1.1 落库 `sys_audit_log`）。

## 技术栈

Spring Boot 3.2.5 · Java 17 · MyBatis-Plus 3.5.7（spring-boot3-starter）· MySQL 8 · Redis · Spring Security + JJWT 0.12.5 · Knife4j 4.4.0（OpenAPI3）· Lombok · Spring @Scheduled

# 1. 先起 Redis
C:\Users\lyyth\.local\share\redis\redis-server.exe

# 2. 再起后台
cd D:\code\ai-product\ai-serve
mvn spring-boot:run
