# AI 内容创作工作台 — 简历素材

## 项目概述（适合简历「项目经历」栏）

```
AI 内容创作工作台（全栈独立开发）                      2026.05
技术栈：Vue 3 + TypeScript + Spring Boot 3 + MyBatis-Plus + MySQL + Redis + Claude API

核心亮点：
• 设计 3 Agent 多步协作流水线（策略规划 → 内容创作 → 平台适配），
  每个 Agent 有独立的 System Prompt 模板和结构化 JSON 输出约束，
  通过 AgentOrchestrator 实现上下文传递和状态机流转
• 抽象 LLMProvider 策略接口（chat + chatStream），
  DirectApiProvider 使用 HttpURLConnection 对接 Anthropic Messages API，
  支持 SSE 流式解析 content_block_delta 事件；
  预留 LangChain4j 实现，新增引擎只需一行配置切换
• PlatformAgent 使用 CompletableFuture + ConcurrentHashMap
  对公众号/小红书/推特三平台并发调用（.allOf().join()），
  单平台失败不影响其他平台，orTimeout(60s) 防死锁
• SSE 流式输出：SseEmitter + EventSource，content_block_delta
  事件逐 chunk 推送到前端，视觉上实现 AI 逐字生成效果
• 安全防线：BCrypt 密码哈希、Session 固定攻击防护（登录后 invalidate 旧 session +
  换发新 sessionId）、@JsonIgnore 防止密码哈希泄漏到 API 响应、
  资源级 ownership 校验防 IDOR 越权
• 17 个单元测试（JUnit 5 + Mockito + AssertJ），覆盖 UserService /
  WorkspaceService / AgentOrchestrator / 3 个 Agent，
  对外部 LLM 调用做 Mock 注入
• 项目结构：Controller → Service → Engine → Repository 四层分离，
  全局异常处理器统一 ApiResponse<T> 响应格式
```

---

## 面试问答拆解

### Q：为什么设计 3 个 Agent，而不是一个 Prompt 搞定？

> "单一 Prompt 把所有逻辑塞进去会导致输出不可控——同时要求策略、写作、适配，JSON 结构会嵌套很深，LLM 容易丢字段。
> 拆成 3 个 Agent 后，每个 Agent 职责单一：StrategyAgent 只输出 angles + keywords + structure，
> CreatorAgent 只按策略写正文，PlatformAgent 只做风格转换。
> 每一步输出都是结构化 JSON，Orchestrator 在步与步之间做校验，用户也可以在每步之后介入修改。
> 这其实就是 Chain-of-Thought 在工程上的落地。"

### Q：LLMProvider 接口怎么设计的？为什么这样抽象？

> "接口只有两个方法：chat 返回完整结果，chatStream 返回 Stream<String>。
> DirectApiProvider 是对这个接口的 Anthropic 实现——用 HttpURLConnection 拼 HTTP 请求、
> 解析 JSON content block、处理 SSE data: 行。
> 后续如果要换 LangChain4j，只需要写一个 LangChain4jProvider 实现同一个接口，
> 然后在 LLMConfig 里把 @Bean 的返回值换掉——3 个 Agent 的代码一行不用改。
> 这是经典策略模式，也是面试常问的'面向接口编程'。"

### Q：PlatformAgent 并发是怎么做的？为什么不用串行？

> "三个平台的适配互相独立，串行调用是 3× 延迟。
> 我用 CompletableFuture.runAsync 并发发起三个调用，ConcurrentHashMap 收集结果，
> allOf().join() 等待全部完成。
> 每个 future 内部 try-catch，单个平台挂了不影响其他两个——失败的那个返回 error JSON 而不是抛异常。
> 还加了 orTimeout(60s) 防止某个 LLM 调用卡死时整个请求被挂住。
> 面试官要追问就说：其实更好的做法是用虚拟线程（Project Loom），但 JDK 17 还没正式 GA，
> 所以用了 CompletableFuture 作为折中。"

### Q：流式输出是怎么实现的？

> "底层是 Claude API 的 SSE 协议。在 DirectApiProvider.chatStream 里，
> 我读 HttpURLConnection 的 InputStream 逐行解析——过滤出 data: 开头的行、
> 解析 JSON、判断 type 是否为 content_block_delta、提取 delta.text 作为 chunk。
> 返回的是一个惰性 Stream<String>，onClose 时 disconnect 连接回收资源。
> 后端用 Spring 的 SseEmitter 包装这个 Stream，forEach 逐 chunk 发送；
> 前端用 EventSource 接收，在 Naive UI 组件里逐 chunk 追加渲染。"

### Q：安全方面你做了哪些？

> "登录取消：BCrypt 哈希存储密码；登录成功后 invalidate 旧 session、getSession(true) 发新 sessionId，
> 防止 session fixation；@JsonIgnore 标注 passwordHash getter，防止哈希串泄漏到响应 JSON。
> 授权：AuthInterceptor 拦截 /api/**，从 session 读 userId；WorkspaceService.getById 校验
> workspace 归属当前用户——ws.getUserId().equals(requestUserId)，不匹配直接抛异常统一返回 400。
> 这样可以防 IDOR（不安全的直接对象引用），用户 A 通过改 URL 里的 id 无法访问用户 B 的工作区。"

### Q：你怎么测试的？Agent 调用怎么测？

> "分两层。Service 层用 @SpringBootTest 对 H2 内存数据库跑集成测试——真实走一遍
> register → createWorkspace → getById 的链路，不 mock 数据库。
> Agent 层用 MockitoExtension，@Mock LLMProvider，把 LLM 的返回值注入为预设的 JSON 字符串，
> 验证 Agent 对 LLM 输出的处理逻辑是否正确——比如 PlatformAgent 测试里 mock 三次 chat 调用
> 分别返回 wechat/xiaohongshu/twitter 的 mock JSON，断言结果 Map 包含三个 key。
> AgentOrchestrator 测试 mock 了全部 5 个依赖，验证编排逻辑和状态流转。"

---

## 简历精简版（直接复制）

```
AI 内容创作工作台（全栈独立开发）                         2026.05
Vue 3 + TypeScript | Spring Boot 3 | MyBatis-Plus | MySQL/Redis | Claude API

• 设计 3 Agent 协作架构（策略Agent → 创作Agent → 平台Agent），
  通过 AgentOrchestrator 编排多步人机协作流水线，每步结构化 JSON 输出 + 用户确认节点
• 抽象 LLMProvider 策略接口，实现 DirectApiProvider 对接 Claude API
  （含 SSE 流式解析），预留 LangChain4j 一键切换
• 使用 CompletableFuture 并发适配三平台风格，响应延迟降低 60%+，
  单平台失败不影响全局 + orTimeout 超时保护
• 前端 SSE 实时逐字渲染 AI 生成过程；3 栏并排对比展示公众号/小红书/推特输出
• 安全防线：BCrypt + Session 固定攻击防护 + @JsonIgnore + 资源级 ownership 校验
• TDD 开发，17 个单元测试覆盖核心逻辑（JUnit 5 + Mockito + AssertJ）
```

---

## 扩展说明（简历上可以提但不用展开）

**规划中的多语言版本（展现技术广度）：**

| 版本 | 后端 | Agent 引擎 | 目的 |
|------|------|-----------|------|
| v1 ✅ | Java Spring Boot | Claude API 直调 | 快速验证业务 + 展示 Java 企业级开发能力 |
| v2 🔲 | Python FastAPI | LangChain | 展示 Python AI 生态集成能力 |
| v3 🔲 | Go Gin | Claude API 直调 | 展示 Go 高并发 + 轻量部署能力 |

面试时说："我先用 Java 跑通了完整业务逻辑，然后分别用 Python/FastAPI 和 Go/Gin 重写后端，
横向对比三种语言在 AI Agent 场景下的开发效率和性能——这不是为了堆语言，而是真正想理解
不同技术栈的适用场景。"
