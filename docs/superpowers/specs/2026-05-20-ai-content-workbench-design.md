# AI 内容创作工作台 — 需求设计文档

## 项目定位

面向全栈开发实习生的简历项目，展示前后端全栈能力 + AI Agent 开发能力 + 多语言后端实现能力。

## 业务概述

AI 驱动的内容创作工作台，覆盖**短文 + 长文 + 多平台适配**。用户输入主题，3 个 AI Agent 协作完成策略规划 → 内容创作 → 平台适配的完整链路。

目标平台：**公众号（长文干货） + 小红书（种草短文案） + 推特（Thread 线程体）**。

## 技术选型

| 层 | 技术 | 说明 |
|-----|------|------|
| 前端 | Vue 3 + Vite + TypeScript + Pinia + Vue Router | SPA 单页应用 |
| UI 库 | Naive UI | 快速出好看的界面 |
| 后端 | Java 17 + Spring Boot 3 + MyBatis-Plus | 主流企业技术栈 |
| 数据库 | MySQL 8.0 + Redis | MySQL 存业务数据，Redis 做缓存/Session |
| Agent 引擎 A | LangChain4j | Java 版 LangChain |
| Agent 引擎 B | HTTP Client 直调 Claude API / OpenAI API | 纯 API 实现，不依赖框架 |
| 大语言模型 | Claude API + DeepSeek API | 双模型，可对比效果 |

**后续扩展**：同项目分别用 Python (FastAPI) 和 Go (Gin) 实现后端，并补全 LangChain4j / API 直调两种 Agent 引擎。

## 系统架构

```
Vue 3 + TS 前端
    │ REST API (JSON)
Spring Boot 后端
├── Controller 层
│   ├── AuthController         用户认证
│   ├── WorkspaceController    工作区 CRUD
│   └── AgentController        Agent 交互接口
├── Service 层
│   ├── WorkspaceService       工作区业务逻辑
│   ├── AgentOrchestrator      编排 3 个 Agent 协作
│   └── ContentService         内容版本管理
├── Engine 层（接口抽象，两种实现可切换）
│   ├── LLMProvider (接口)
│   │   ├── LangChain4jProvider
│   │   └── DirectApiProvider
│   ├── StrategyAgent
│   ├── CreatorAgent
│   └── PlatformAgent
└── Repository 层 (MyBatis-Plus)
    └── MySQL ←── Redis 缓存
```

**核心设计：LLMProvider 接口抽象**

```java
public interface LLMProvider {
    String chat(String systemPrompt, String userMessage);
    Stream<String> chatStream(String systemPrompt, String userMessage);
}
```

通过 `application.yml` 中的 `llm.provider` 配置切换 `langchain4j` / `direct-api`，体现策略模式。

## 数据库设计

### users
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 自增 |
| username | VARCHAR(50) | 用户名 |
| email | VARCHAR(100) | 邮箱 |
| password_hash | VARCHAR(255) | 加密密码 |
| created_at | DATETIME | 注册时间 |

### workspaces
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 自增 |
| user_id | BIGINT FK | 所属用户 |
| title | VARCHAR(200) | 工作区标题 |
| topic | TEXT | 用户输入的主题/需求 |
| status | TINYINT | 0=草稿 1=策划中 2=创作中 3=已完成 |
| created_at | DATETIME | |
| updated_at | DATETIME | |

### agent_executions
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| workspace_id | BIGINT FK | |
| agent_role | VARCHAR(20) | STRATEGY / CREATOR / PLATFORM |
| input_prompt | TEXT | 输入 prompt |
| output_content | JSON | 结构化输出 |
| platform | VARCHAR(20) | 仅 PLATFORM agent 时有值 |
| tokens_used | INT | token 消耗 |
| created_at | DATETIME | |

### content_versions
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| workspace_id | BIGINT FK | |
| agent_execution_id | BIGINT FK | 由哪次 Agent 执行产生 |
| platform | VARCHAR(20) | 通用 / 公众号 / 小红书 / 推特 |
| title | VARCHAR(500) | |
| body | LONGTEXT | 内容正文 |
| version | INT | 版本号 |
| is_user_edited | TINYINT | 是否用户手动改过 |
| created_at | DATETIME | |

### 表关系
```
users 1──N workspaces 1──N agent_executions
                              │
                              └──1──N content_versions
```

## Agent 协作流程

3 个 Agent 角色：策略规划师 (Strategy) → 内容创作者 (Creator) → 平台优化师 (Platform)

**每一步都有用户确认节点**，不是全自动——体现实用性和人机协作。

### Step 1: 策略规划师 (StrategyAgent)

- **输入**：用户主题 + 目标受众 + 时间节点
- **输出**：选题角度、热点关键词、内容结构建议、发布时间规划
- **用户可修改确认**后再进入下一步

### Step 2: 内容创作者 (CreatorAgent)

- **输入**：确认的策略 JSON + 内容类型（长文/短文）
- **输出**：标题、正文（Markdown）、标签、SEO 元信息
- **流式输出**：用户看到内容逐字生成
- **用户在编辑器中可直接编辑**、创建新版本

### Step 3: 平台优化师 (PlatformAgent)

- **输入**：确认的内容 + 目标平台
- **并发调用 3 次**，每次指定不同平台风格
- 公众号输出：长文干货风格，含封面建议
- 小红书输出：种草风格，emoji，短段落，hashtags
- 推特输出：Thread 线程体，多条推文串联
- **前端三栏并排对比**展示，每栏可微调

### Agent 编排

AgentOrchestrator 负责：顺序执行控制、上下文传递、并发调度（PlatformAgent）、错误重试、token 统计。

## API 设计

### 认证
```
POST /api/auth/register
POST /api/auth/login
```

### 工作区
```
POST   /api/workspaces                     创建工作区
GET    /api/workspaces                      我的工作区列表
GET    /api/workspaces/{id}                 工作区详情（含完整 Agent 交互链）
```

### Agent 交互
```
POST /api/workspaces/{id}/strategy/execute   触发策略规划
PUT  /api/workspaces/{id}/strategy            用户修改策略
POST /api/workspaces/{id}/content/execute     触发内容创作（支持 stream=true SSE）
PUT  /api/workspaces/{id}/content/{versionId} 用户编辑内容
POST /api/workspaces/{id}/platform/execute    触发平台适配（3平台并发）
GET  /api/workspaces/{id}/outputs             查看所有平台输出
PUT  /api/workspaces/{id}/outputs/{outputId}  用户编辑某平台输出
GET  /api/workspaces/{id}/agent-logs          查看 Agent 交互历史
```

### 统一响应格式
```json
{
  "code": 200,
  "data": {
    "agentRole": "STRATEGY",
    "input": "...",
    "output": { ... },
    "tokensUsed": 1520,
    "durationMs": 3200
  }
}
```

### SSE 流式输出
```
POST /api/workspaces/{id}/content/execute?stream=true
→ Content-Type: text/event-stream
→ data: {"chunk": "2025", "agent": "CREATOR"}
→ ...
→ data: [DONE]
```

## 前端设计

### 路由
```
/                    重定向到 /dashboard
/dashboard            仪表盘（工作区列表 + 快速新建）
/workspace/:id        工作台主页面（3步走）
/history              历史记录
/login / /register    登录/注册
```

### 核心页面：工作台 (workspace/:id)

```
┌───────────────────────────────────────────────┐
│  ← 返回    标题：618数码促销方案    状态：策划中  │
├───────────────────────┬───────────────────────┤
│  【进度条】            │  【Agent 交互区域】     │
│  ① 策略 ② 内容 ③ 平台  │                       │
│                       │  ┌───────────────────┐ │
│                       │  │ 🤖 策略规划师      │ │
│                       │  │ 输出策略建议...    │ │
│                       │  │ [确认] [修改] [重试]│ │
│                       │  └───────────────────┘ │
│                       │  ┌───────────────────┐ │
│                       │  │ ✍️ 内容创作者      │ │
│                       │  │ (策略确认后出现)   │ │
│                       │  └───────────────────┘ │
│                       │  ┌───────────────────┐ │
│                       │  │ 📱 平台优化师      │ │
│                       │  │ (内容确认后出现)   │ │
│                       │  └───────────────────┘ │
├───────────────────────┴───────────────────────┤
│  底部工具栏：[导出 MD] [复制] [新建版本]          │
└───────────────────────────────────────────────┘
```

### 平台输出三栏对比视图

第三步时展示：公众号 | 小红书 | 推特 三栏并排，每栏内嵌简易编辑器可直接微调。

### 组件树
```
App
├── DashboardView       工作区卡片列表 + 新建按钮
├── WorkspaceView        工作台主容器
│   ├── ProgressSteps    进度指示器
│   ├── AgentChatPanel   右侧 Agent 交互面板
│   │   ├── StrategyCard
│   │   ├── CreatorCard
│   │   └── PlatformCard
│   ├── ContentEditor    中间内容编辑区（Markdown 编辑器）
│   └── PlatformCompare  三栏平台对比视图
├── HistoryView          历史工作区列表
└── AuthView             登录/注册
```

## 开发节奏

### 第一阶段（1-2 周）：Java 版本 MVP

1. 项目脚手架搭建（Spring Boot + Vue + MySQL）
2. 用户认证模块
3. 工作区 CRUD
4. Agent 引擎层（LLMProvider 接口 + DirectApiProvider 实现）
5. 3 个 Agent 实现 + AgentOrchestrator
6. 前端工作台核心页面（3步交互流程）
7. SSE 流式输出
8. 平台三栏对比视图

### 第二阶段：补齐能力

- LangChain4jProvider 实现（切换配置即用）
- 内容版本管理完善
- UI 打磨（加载态、空态、错误态）

### 第三阶段：多语言后端

- Python FastAPI 版本（复用前端，替换后端）
- Go Gin 版本

### 第四阶段：增强

- DeepSeek API 接入
- Agent 执行对比（LangChain4j vs API 直调效果对比页面）
- 导出功能（Markdown / PDF / 一键复制到各平台）
