# 工贸一体 MES 制造执行系统

> 覆盖 **销售 → 生产 → 质检 → 库存 → 发货** 全流程的制造执行系统，内置 **AI 助手**：既能通过知识库 RAG 回答业务问题，又能通过 Function Calling 直接查询系统真实数据。

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F) ![Vue](https://img.shields.io/badge/Vue-3.3-42b883) ![LangChain4j](https://img.shields.io/badge/LangChain4j-0.36-4B8BBE) ![DeepSeek](https://img.shields.io/badge/LLM-DeepSeek-4D6BFE) ![MySQL](https://img.shields.io/badge/MySQL-8-4479A1)

---

## ✨ 项目亮点

**🤖 AI 原生，不是"贴个聊天窗口"**
- **RAG 知识问答**：SOP、质检标准、规格书入库分块 + 向量检索，AI 基于企业知识作答，支持溯源。
- **Function Calling 操作真实数据**：内置 **22 个数据库工具**，模型自主选择工具查订单、查库存、查工单、算不良率、做系统健康巡检……回答的都是系统里的**真实数据**，不是模型编的。
- **客户门户 AI 客服**：面向客户的无状态问答入口，降低客服沟通成本。

**🔗 业务全闭环，不是孤立的增删改查**
- 销售订单 → 审核收款 → 自动生成生产工单 → 工序报工 → 质检 → 成品入库 → 回写订单 → 发货，全链路状态机式流转，环节之间自动联动、自动回写。

**🔐 企业级权限体系**
- 注解式 RBAC 鉴权（`@RequirePermission`），菜单 / 角色 / 权限三级模型，验证码登录、注册、忘记密码闭环。

**📦 完整工程化**
- Redis 验证码缓存、RabbitMQ 异步消息通知、Knife4j 在线接口文档、Docker Compose 一键起中间件、API 冒烟测试脚本。

---

## 🧩 功能模块

| 模块 | 说明 |
| --- | --- |
| **系统管理 system** | 登录 / 验证码 / 注册 / 改密 / 菜单 / 角色 / 用户，注解式 RBAC |
| **基础资料 base** | 客户、供应商、产品（含 BOM）、工序、设备、仓库 |
| **销售管理 sale** | 销售订单、审核收款、发货单，状态全程可追踪 |
| **生产管理 production** | 生产工单、工序报工、质检记录、质检标准、工序 SOP |
| **库存管理 inventory** | 实时库存、库存调拨、生产入库、库存预警 |
| **生产看板 dashboard** | 核心 KPI + ECharts 图表，经营数据一屏总览 |
| **AI 助手 ai** | RAG 知识问答 + Function Calling 数据库工具 |
| **知识库 knowledge** | 文档上传、分块切片、向量化，支撑 RAG 检索 |
| **客户门户 portal** | 客户自助查询 + 无状态 AI 客服 |

---

## 🔄 核心业务闭环

```mermaid
flowchart LR
    A["销售订单<br/>待审核"] -->|审核收款| B["已审核"]
    B -->|一键转生产| C["生产工单<br/>待生产"]
    C -->|开工| D["生产中<br/>工序报工"]
    D -->|完工| E["已完成"]
    E -->|成品入库| F["已入库"]
    F -->|回写订单| G["待发货 → 部分发货 → 已完成"]
```

状态迁移由**状态机约束**：非法流转直接拒绝（如"待生产"不能直接入库），每个状态变迁挂载副作用——完工自动关联质检、入库自动生成库存流水并回写订单状态。

---

## 🤖 AI 能力详解

### 1. RAG 知识问答

```
用户提问 → 知识库向量检索 (all-minilm-l6-v2) → Top-K 相关片段 → 组装上下文 → DeepSeek 生成答案
```

### 2. Function Calling — 22 个数据库工具（节选）

| 工具 | 说明 |
| --- | --- |
| `queryProduct` / `queryInventory` | 按关键字查产品、查库存 |
| `getSaleOrder` / `searchOrders` / `listSaleOrders` | 订单精确查询 / 模糊搜索 / 按状态列表 |
| `getWorkOrder` / `listWorkOrders` | 工单详情 / 按状态列表 |
| `getDashboardSummary` / `systemHealthCheck` | 经营 KPI / **一键系统健康巡检诊断** |
| `getDefectRate` / `getTodayProduction` | 不良率统计 / 当日生产概况 |
| `getBomByProduct` / `searchSOP` | BOM 用料清单 / 知识库 SOP 检索 |
| `getWorkerRanking` | 报工排行榜 |

AI 通过 `AiToolExecutor` 完成 **"选工具 → 生成参数 → 执行查询 → 结果回填 → 二次生成"** 的 agent 循环，实现"用自然语言查系统"。

### 3. 客户门户 AI 客服

无状态对话，面向客户自助答疑，独立于内部 AI 助手。

---

## 🛠 技术栈

| 分层 | 技术 |
| --- | --- |
| **后端** | Java 17 · Spring Boot 3 · MyBatis-Plus · MySQL 8 · Redis · RabbitMQ · Druid · Knife4j |
| **AI** | LangChain4j 0.36 · DeepSeek · all-minilm-l6-v2 Embedding |
| **前端** | Vue 3 · Element Plus · Pinia · Vue Router · ECharts · TailwindCSS · Axios |
| **工具链** | Hutool · Apache POI · PDFBox · Docker Compose |

---

## 📁 项目结构

```
├── src/main/java/com/itheima/mes1
│   └── module
│       ├── system        # 认证 / RBAC / 菜单角色用户
│       ├── base          # 客户 / 供应商 / 产品 / 工序 / 设备 / 仓库
│       ├── sale          # 销售订单 / 发货
│       ├── production    # 工单 / 报工 / 质检 / SOP
│       ├── inventory     # 库存 / 调拨 / 入库
│       ├── dashboard     # 看板 KPI
│       ├── ai            # AI 助手：RAG + Function Calling
│       ├── knowledge     # 知识库
│       └── portal        # 客户门户 + AI 客服
├── src/views             # Vue3 前端页面
├── sql/                  # 建表 + 全量演示数据 + 迁移脚本
└── deploy/               # 部署脚本与说明
```

---

## 🚀 快速开始

### 环境要求

JDK 17+ · Maven 3.8+ · Node 16+ · MySQL 8 ·（可选）Redis / RabbitMQ

### 1. 初始化数据库

```bash
# 依次执行（sql/ 目录）
source sql/init.sql           # 基础表结构
source sql/rbac-menu-data.sql # 菜单 / 角色 / 权限初始化数据
source sql/full-data.sql      # 全量演示数据
```

> 各版本 migration 脚本（`sql/migration_v*.sql`）按需执行。

### 2. 启动后端

```bash
# 配置环境变量：复制 .env.example 为 .env，填入数据库 / Redis / RabbitMQ / DeepSeek Key
# 后端默认端口 8081
mvn spring-boot:run
```

### 3. 启动前端

```bash
npm install
npm run serve   # 开发模式热更新
```

### 4. 访问

| 入口 | 地址 |
| --- | --- |
| 前端页面 | `http://localhost:8080` |
| 接口文档 (Knife4j) | `http://localhost:8081/doc.html` |
| RabbitMQ 管理台 | `http://localhost:15672` |

> **默认账号：`admin` / `admin123`**

### 5. 部署

```bash
docker compose up -d            # 拉起 RabbitMQ 等中间件
# 完整部署说明见 deploy/部署说明.md
```

---

## 🧪 测试

- `test-all-api.sh` / `test-full-suite.sh` — 后端全接口冒烟测试脚本
- 已内置 `AuthControllerTest` 等单元测试

---

## 📝 说明

- `.env` 包含密钥等敏感配置，已被 `.gitignore` 忽略，请勿提交。
- 未配置 `DEEPSEEK_API_KEY` 时系统正常运行，仅 AI 功能不可用（懒加载，不影响启动）。
