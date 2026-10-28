# CLAUDE.md

本文件供 AI 助手快速理解项目，请与代码同步维护。

## 项目简介

心理健康AI助手：前后端分离单体项目，含**用户端**（AI 咨询/情绪日志/知识库/个人中心）与**管理后台**（用户/内容/咨询管理）。

- `ai-serve/` — Spring Boot 3.2.5 后端：Java 21 · MyBatis-Plus 3.5.7 · MySQL 8 · Redis · JJWT · Knife4j(OpenAPI3) · Spring Security
- `ai-vue/` — Vue 3 前端：Vite · Vue Router 4 · Element Plus · Axios。**无 Pinia/Vuex**，状态用组合式函数 + localStorage（见 src/auth.js）

- GitHub: https://github.com/lywone/mental-health-ai （main 分支）

## 常用命令

```bash
# 后端（dev profile 默认激活）
cd ai-serve && mvn spring-boot:run          # 启动，端口 8080，文档 /doc.html
cd ai-serve && mvn -q -DskipTests compile   # 编译检查

# 前端
cd ai-vue && npm run dev        # 开发
cd ai-vue && npm run lint       # ESLint（flat config）
cd ai-vue && npm run build      # 构建
```

## 本地环境（Windows，踩过坑）

- MySQL `localhost:3306`，库 `mental_health`，账号 `root/root`（仅 dev）
- **Redis `localhost:6379` 必须运行**：未运行时所有带登录态的接口 401、验证码接口 500（Spring 启动不会报错，是运行时才炸）。本机 Redis 免安装版：`C:\Users\lyyth\.local\share\redis\redis-server.exe`
- git 全局代理 `127.0.0.1:31181` 常不在线，push 失败时用 `git -c http.proxy= -c https.proxy= push`
- PowerShell 里 `curl.exe` 发 JSON 会丢引号，接口测试用 `Invoke-RestMethod`

## 后端架构

```
com.mental.health
├── common/    Result{code,message,data} · ErrorCode · BusinessException · GlobalExceptionHandler
├── config/    SecurityConfig · JwtAuthFilter · JwtUtil · CurrentUser · Jackson/MyBatisPlus/Knife4j 配置
├── infra/     ai(LLM接入) · sms(短信通道) · storage(本地上传) · job · init(管理员种子)
└── module/    auth · user · consult · mood · article · dashboard
```

关键规则：
- 统一响应 `Result{code=0 成功}`；业务错误抛 `BusinessException(ErrorCode[, 自定义消息])`
- ErrorCode 分段：1001-1004 认证 / 1101-1102 参数·不存在 / 1201+ 业务 / 1301-1302 文件 / **1401-1404 短信·手机号** / 1501 AI / 500 兜底
- **认证双轨（JWT）**：管理端接口 role=ADMIN，用户端 `/api/user/**` role=USER；`JwtAuthFilter` 解析 Bearer 并把 uid/role/nickname 写入 SecurityContext，Controller 用 `@AuthenticationPrincipal CurrentUser` 取 uid
- 匿名放行（SecurityConfig）：`/api/auth/admin/login`、`/api/auth/user/{dev-login,sms/send,register,login}`
- **验证码**：`SmsCodeService`，Redis key `sms:code:{scene}:{phone}`，60s 间隔 / 5min 有效 / 日 10 条 / 错 5 次锁 30min / 一次性消费；scene 隔离（LOGIN|REGISTER）；通道 `SmsSender` 接口（当前仅 log 实现，aliyun/tencent 预留）
- `dev-login` 受 `auth.dev-login-enabled` 开关控制（dev 环境 true，生产必须 false）
- LLM：`llm.enabled=false` 用 `FallbackReplies` 模拟回复；`CrisisWordDetector` 危机词检测
- 代码风格：请求 DTO 用 `@Data` 类 + jakarta validation；响应 VO 用 record；代码注释用中文
- MyBatis-Plus：`createdAt/updatedAt` 由 MetaObjectHandler 自动填充；`t_user.phone` 唯一键 `uk_phone`，接口返回脱敏 `138****5678`

## 前端架构

```
src/
├── api/       http.js(请求封装) · index.js(全部接口定义)
├── auth.js    双轨登录态（localStorage: admin_auth / user_auth）
├── router/    路由 + 守卫
├── components/ UserLayout(用户端) · BackendLayout(管理端)
└── views/     user/(用户端页面) · Login.vue + dashboard 等管理端页面
```

关键规则：
- `http.js`：`request(path, { scope: 'user'|'admin', anonymous })`；user 401 → 清登录态跳 `/user/login?redirect=...`；admin 401 → 跳 `/login`（**没有静默重登，不要加回来**）
- `auth.js`：`isLogin()/isUserLogin()/setUserLogin()/clearUserLogin()/updateUserProfile()`，login 对象结构 `{token, tokenType, expiresIn, profile}`
- 路由守卫：`/back/*` 用 `isLogin()`；用户端 UserLayout 子路由统一 `meta.requiresUserAuth`；已登录访问登录页自动回跳 redirect
- Element Plus 组件自动按需引入（unplugin），图标从 `@element-plus/icons-vue` 手动导入，`ElMessage/ElMessageBox` 手动导入
- ESLint：flat config `eslint.config.js`（`flat/essential`）；`multi-word-component-names` 已关闭；`v-model:arg` 是 Vue 3 合法语法
- 分页模式：`page/pageSize` ref + el-pagination `@current-change/@size-change` 手动同步后再请求

## 当前状态与二期规划

- ✅ 用户注册与个人中心 v1.0（设计文档：`ai-vue/docs/用户注册与个人中心-设计文档-v1.0.md`，含决策点记录）
- ⏳ 二期候选：真实短信通道（需服务商签名/模板）、手机号换绑、游客浏览文章、管理端补录存量用户手机号
- 存量种子用户（小林/阿哲/叶叶）无手机号，无法验证码登录，属已知问题（见设计文档 Q6）
