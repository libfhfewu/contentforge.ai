# AI 内容创作工作台

> 基于多 Agent 协作的智能内容创作平台，支持从竞品分析到多平台发布的全流程自动化。

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green)
![Vue](https://img.shields.io/badge/Vue-3.4+-blue)
![TypeScript](https://img.shields.io/badge/TypeScript-5.3+-blue)
![License](https://img.shields.io/badge/license-MIT-green)

---

## 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术架构](#技术架构)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [核心模块](#核心模块)
- [API 文档](#api-文档)
- [部署指南](#部署指南)
- [开发计划](#开发计划)

---

## 项目简介

AI 内容创作工作台是一个基于多 Agent 协作的智能内容创作平台。平台集成了 RAG 知识增强、品牌风格学习、实时流式生成、人机协作审核等 AI 原生能力，帮助用户高效创作适配多平台的内容。

### 核心能力

- 🤖 **多 Agent 协作** — 5 种专业 Agent 分工协作，支持 ReAct 推理、任务规划、自我反思
- 📚 **RAG 知识增强** — 向量检索 + 知识回流闭环，持续优化内容质量
- 🎨 **品牌风格学习** — 自动提取品牌特征，生成内容匹配品牌调性
- ⚡ **实时流式生成** — SSE 流式输出 + WebSocket 进度推送
- 🔒 **安全可靠** — 完整认证授权、限流、CSRF 防护体系

---

## 功能特性

### 内容创作流程

```
准备阶段          创作阶段           发布阶段
─────────        ─────────         ─────────
🔍 竞品分析       ① 策略规划        ③ 平台适配
🎨 品牌风格       ② 内容创作        📊 内容效果
📚 知识库         🎨 多模态扩展      🔄 知识回流
```

### 功能模块

| 模块 | 功能 | 状态 |
|------|------|------|
| 仪表盘 | 数据统计、快速操作、工作区管理 | ✅ |
| 工作区 | 策略→创作→适配→发布全流程 | ✅ |
| 竞品分析 | 多平台竞品内容分析、差异化建议 | ✅ |
| 品牌风格 | 品牌资料学习、风格特征提取 | ✅ |
| 知识库 | 文档管理、RAG 检索、效果回流 | ✅ |
| 多模态 | 图片描述、视频脚本、播客脚本 | ✅ |
| 工作流 | 自定义 Agent 编排流程 | ✅ |
| 历史记录 | 内容版本管理、版本对比 | ✅ |
| 模板管理 | 内容模板、变量替换 | ✅ |
| 模型管理 | LLM 模型配置、切换 | ✅ |

### 支持平台

| 平台 | 内容类型 | 特点 |
|------|----------|------|
| 📱 微信公众号 | 长文深度内容 | 专业、深度、结构化 |
| 📕 小红书 | 图文笔记 | 口语化、emoji、种草 |
| 🎵 抖音 | 短视频脚本 | 前3秒hook、热门标签 |

---

## 技术架构

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                         前端层                               │
│  Vue 3 + TypeScript + Pinia + Naive UI + Vite              │
└─────────────────────────────────────────────────────────────┘
                              │ HTTP / SSE / WebSocket
┌─────────────────────────────────────────────────────────────┐
│                        API 网关层                            │
│  Spring Security + CSRF + Rate Limit + CORS                 │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                       服务层                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │ Agent 编排器 │  │ 内容服务    │  │ 用户服务    │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │ RAG 服务    │  │ 发布服务    │  │ 品牌服务    │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                       AI Agent 层                            │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ 策略Agent │ │ 创作Agent │ │ 适配Agent │ │ 研究Agent │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
│  ┌──────────┐ ┌──────────┐                                  │
│  │ 反思Agent │ │ 多模态   │                                  │
│  └──────────┘ └──────────┘                                  │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                       数据层                                 │
│  MySQL 8 + Redis 7 + RabbitMQ + 内存向量存储                 │
└─────────────────────────────────────────────────────────────┘
```

### 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 语言 | Java / TypeScript | 17 / 5.3+ |
| 后端框架 | Spring Boot | 3.3.5 |
| AI 框架 | Spring AI | 1.0.0-M6 |
| 前端框架 | Vue | 3.4+ |
| 构建工具 | Vite | 5.1+ |
| 状态管理 | Pinia | 2.1+ |
| UI 库 | Naive UI | 2.38+ |
| ORM | MyBatis-Plus | 3.5.7 |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis + Caffeine | 7 / 3.1.8 |
| 消息队列 | RabbitMQ | 3.x |
| 容器化 | Docker Compose | 3.8 |

---

## 快速开始

### 环境要求

- JDK 17+
- Node.js 20+
- MySQL 8.0+
- Redis 7+
- RabbitMQ 3.x

### 1. 克隆项目

```bash
git clone https://github.com/your-username/content-workbench.git
cd content-workbench
```

### 2. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env 文件，填入数据库密码和 API Key
```

### 3. Docker 一键启动

```bash
docker-compose up -d
```

### 4. 手动启动

**后端：**
```bash
cd backend
mvn spring-boot:run
```

**前端：**
```bash
cd frontend
npm install
npm run dev
```

### 5. 访问

- 前端：http://localhost:3000
- 后端 API：http://localhost:8080
- API 文档：http://localhost:8080/actuator

---

## 项目结构

```
content-workbench/
├── backend/                    # 后端服务
│   ├── src/main/java/
│   │   └── com/contentworkbench/
│   │       ├── agent/          # AI Agent 核心
│   │       │   ├── react/      # ReAct 推理 Agent
│   │       │   ├── planning/   # 任务规划 Agent
│   │       │   ├── reflect/    # 自我反思 Agent
│   │       │   ├── rag/        # RAG 检索增强
│   │       │   ├── memory/     # Agent 记忆
│   │       │   ├── tool/       # 工具系统
│   │       │   └── hitl/       # 人机协作
│   │       ├── engine/         # LLM 引擎 & Agent 实现
│   │       ├── controller/     # REST API
│   │       ├── service/        # 业务服务
│   │       ├── model/          # 数据模型
│   │       ├── repository/     # 数据访问
│   │       ├── security/       # 安全认证
│   │       ├── config/         # 配置
│   │       └── mq/             # 消息队列
│   └── src/test/               # 单元测试
├── frontend/                   # 前端应用
│   └── src/
│       ├── views/              # 页面视图
│       ├── components/         # 组件
│       │   ├── agent/          # Agent 相关组件
│       │   ├── workspace/      # 工作区组件
│       │   ├── multimodal/     # 多模态组件
│       │   ├── performance/    # 效果追踪组件
│       │   ├── brand/          # 品牌管理组件
│       │   └── workflow/       # 工作流组件
│       ├── stores/             # Pinia 状态管理
│       ├── api/                # API 调用
│       ├── router/             # 路由配置
│       ├── types/              # TypeScript 类型
│       ├── utils/              # 工具函数
│       ├── constants/          # 常量定义
│       └── styles/             # 样式文件
├── nginx/                      # Nginx 配置
├── monitoring/                 # 监控配置
├── docker-compose.yml          # 开发环境
├── docker-compose.prod.yml     # 生产环境
└── docs/                       # 文档
```

---

## 核心模块

### 1. 多 Agent 协作系统

```
AgentOrchestrator（编排器）
  ├── StrategyAgent    → 生成内容策略（角度、关键词、结构）
  ├── CreatorAgent     → 撰写长文/短文案（支持 SSE 流式）
  ├── PlatformAgent    → 并发适配 3 平台（3 线程池）
  ├── ResearchAgent    → 竞品分析（搜索 + LLM 混合）
  └── MultimodalAgent  → 图片/视频/播客脚本生成
```

**推理模式：**
- **ReAct**：思考→行动→观察循环，可调用工具
- **Planning**：任务分解为子步骤，支持依赖和并行
- **Reflection**：质量评分 + 自动重试

### 2. RAG 检索增强

```
用户上传文档 → 分块（1000字/块，200字重叠）
             → Bigram TF 嵌入（256维）
             → 存储到向量库
             
创作时：查询文本 → 嵌入 → 余弦相似度检索 Top-3 → 注入 Agent 上下文
```

### 3. 品牌风格学习

```
品牌资料 → LLM 分析 → 提取 5 维特征
         → 语调、用词、句式、排版、情感
         → 生成风格 Prompt → 注入 Agent 系统提示词
```

### 4. 实时流式生成

```
后端：Flux<String> → SseEmitter → 前端 EventSource
支持：中途中断、异常恢复、部分内容保存
```

### 5. 安全体系

```
认证：Session + BCrypt 密码哈希
授权：RBAC 权限模型 + @RequirePermission
防护：CSRF Token + IP 限流 + CORS
缓存：Caffeine L1 + Redis L2（双层缓存）
```

---

## API 文档

### 认证相关

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/logout | 用户登出 |
| GET | /api/auth/check | 检查登录状态 |

### 工作区相关

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/workspaces | 工作区列表 |
| POST | /api/workspaces | 创建工作区 |
| GET | /api/workspaces/{id} | 工作区详情 |
| DELETE | /api/workspaces/{id} | 删除工作区 |

### Agent 相关

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/workspaces/{id}/strategy/execute | 执行策略 Agent |
| POST | /api/workspaces/{id}/strategy/parallel-research | 并行竞品+策略 |
| POST | /api/workspaces/{id}/content/execute | 执行创作 Agent |
| GET | /api/workspaces/{id}/content/stream | SSE 流式创作 |
| POST | /api/workspaces/{id}/platform/execute | 执行平台适配 |

### RAG 相关

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/agent/knowledge | 添加知识 |
| POST | /api/agent/knowledge/batch | 批量添加 |
| GET | /api/agent/knowledge | 列出知识 |
| DELETE | /api/agent/knowledge/{id} | 删除知识 |

---

## 部署指南

### Docker Compose（推荐）

```bash
# 开发环境
docker-compose up -d

# 生产环境
docker-compose -f docker-compose.prod.yml up -d
```

### 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 3000 | Nginx 静态服务 |
| 后端 | 8080 | Spring Boot API |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| RabbitMQ | 5672 | 消息队列 |
| RabbitMQ Mgmt | 15672 | 管理界面 |
| Prometheus | 9090 | 监控 |
| Grafana | 3001 | 可视化 |

### Nginx 配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://127.0.0.1:3000;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
    }

    location /ws/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

---

## 开发计划

### 已完成 ✅

- [x] 多 Agent 协作架构
- [x] RAG 知识增强系统
- [x] 品牌风格学习
- [x] 实时流式生成
- [x] 人机协作审核
- [x] 多平台内容适配
- [x] 知识库管理
- [x] 工作流编排
- [x] 安全认证体系
- [x] Docker 容器化部署
- [x] 监控告警系统

### 进行中 🚧

- [ ] 接入真实平台 API（微信公众号/小红书/抖音）
- [ ] A/B 测试引擎
- [ ] 多模态图片生成（DALL-E 集成）
- [ ] 前端单元测试
- [ ] 性能压测优化

### 计划 📋

- [ ] 多租户支持
- [ ] 国际化（i18n）
- [ ] 移动端适配
- [ ] 插件市场
- [ ] 团队协作功能

---

## 代码统计

| 语言 | 文件数 | 代码行数 |
|------|--------|----------|
| Java | 123 | ~11,000 |
| Vue | 34 | ~5,000 |
| TypeScript | 20 | ~2,000 |
| CSS | 5 | ~1,000 |
| **合计** | **~182** | **~19,000** |

---

## 许可证

[MIT](LICENSE)

---

## 联系方式

- 作者：Your Name
- 邮箱：your.email@example.com
- GitHub：[@your-username](https://github.com/your-username)

---

> 如果这个项目对你有帮助，请给一个 ⭐ Star！
