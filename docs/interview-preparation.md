# 造易 MES 系统 — 面试准备手册（完整版 v2）

> **本文档按照真实面试权重编排：业务领域（50%+）、前端（~25%）、后端（~25%）**
> 每个专题都从"面试官会怎么问 → 你怎么答"的角度出发。

---

## 目录

### 第一部分：业务领域（核心，占 50%+）
1. [MES 领域认知](#一mes-领域认知)
2. [工贸一体模式](#二工贸一体模式)
3. [全链路业务流程](#三全链路业务流程)
4. [销售管理业务](#四销售管理业务)
5. [生产管理业务](#五生产管理业务)
6. [库存管理业务](#六库存管理业务)
7. [BOM 与产品管理](#七bom-与产品管理)
8. [质检管理业务](#八质检管理业务)
9. [BI 经营分析业务](#九bi-经营分析业务)
10. [AI 业务助手](#十ai-业务助手)
11. [考勤管理业务](#十一考勤管理业务)
12. [知识库业务](#十二知识库业务)
13. [业务综合分析题](#十三业务综合分析题)

### 第二部分：前端（~25%）
14. [Vue 3 架构设计](#十四vue-3-架构设计)
15. [路由与导航守卫](#十五路由与导航守卫)
16. [状态管理 Pinia](#十六状态管理-pinia)
17. [权限控制三层防护](#十七权限控制三层防护)
18. [HTTP 层与拦截器](#十八http-层与拦截器)
19. [组件设计模式](#十九组件设计模式)
20. [ECharts 数据可视化](#二十echarts-数据可视化)
21. [SSE 实时推送前端](#二十一sse-实时推送前端)
22. [主题与暗黑模式](#二十二主题与暗黑模式)
23. [响应式设计](#二十三响应式设计)
24. [前端性能优化](#二十四前端性能优化)
25. [前端综合分析题](#二十五前端综合分析题)

### 第三部分：后端（~25%）
26. [权限体系全解析](#二十六权限体系全解析)
27. [数据库设计](#二十七数据库设计)
28. [MyBatis-Plus 实践](#二十八mybatis-plus-实践)
29. [事务与并发](#二十九事务与并发)
30. [Redis 缓存](#三十redis-缓存)
31. [AI 集成](#三十一ai-集成)
32. [SSE 与定时任务](#三十二sse-与定时任务)
33. [异常处理与统一返回](#三十三异常处理与统一返回)
34. [安全与部署](#三十四安全与部署)
35. [后端综合分析题](#三十五后端综合分析题)

### 附录
36. [项目 Gap 与改进方向](#三十六项目-gap-与改进方向)
37. [面试应答模板](#三十七面试应答模板)
38. [快速问答卡片](#三十八快速问答卡片)

---

# 第一部分：业务领域（>50% 篇幅）

---

## 一、MES 领域认知

### 1.1 什么是 MES？

> MES（Manufacturing Execution System，制造执行系统）是连接企业上层 ERP 和底层车间设备的中间层。它解决的核心问题是：**"工厂里正在发生什么？"**

**MES 的四大核心功能（ISA-95 标准）：**
1. **生产调度** — 工单排产、工序分派、资源分配
2. **生产追踪** — 每批产品在哪个工序、谁做的、用了多少物料
3. **质量管理** — 质检记录、不良品追踪、SPC 统计过程控制
4. **数据采集** — 报工、物料消耗、设备状态实时采集

### 1.2 这个系统解决什么业务痛点？

| 痛点 | 本系统的解决方案 |
|------|----------------|
| 订单进度不透明 | 销售订单 → 工单 → 工序流转，全链路状态追踪 |
| 库存账实不符 | 流水表记录每次变动的 before/after 数量，可审计 |
| 质量问题难追溯 | 质检记录关联产品 + 工单 + 工序，精准定位 |
| 工厂经营状况看不清 | Dashboard 6 大 KPI + BI 多维交叉分析 + AI 自然语言问答 |
| 知识随人走 | 知识库文档管理 + AI RAG 检索 |
| 信息孤岛 | 工贸一体，销售-生产-库存-质检数据打通 |

### 1.3 面试追问

**Q: MES 和 ERP 有什么区别？**

| 维度 | ERP | MES |
|------|-----|-----|
| 时间尺度 | 天/周/月（计划层） | 分钟/小时/天（执行层） |
| 关注点 | 财务、进销存、资源规划 | 生产过程、质量、设备、人员 |
| 数据粒度 | 订单级别 | 工单/工序/批次级别 |
| 用户 | 管理层、财务 | 车间主任、操作工、质检员 |

两者互补：ERP 告诉你要生产什么，MES 跟踪你实际生产得怎么样。

**Q: 这个系统适合什么类型的制造业？**

> 离散制造（Discrete Manufacturing）—— 产品可以独立计数的制造业，如机械加工、电子装配、家具制造。流程制造（Process Manufacturing，如化工、食品、制药）需要批次配方管理和连续生产过程监控，本系统不完全适配。

**Q: "工贸一体"是什么意思？**

> 企业既做生产加工（工厂/车间），也有自己的销售渠道（直接接单、发货给客户）。不是纯代工，也不是纯贸易公司。系统需要同时管理"销售接单"和"生产交付"两条线。

---

## 二、工贸一体模式

### 2.1 业务模型

```
                    ┌──────────────────────────┐
                    │       造易 MES 系统        │
                    └──────────────────────────┘
                              │
            ┌─────────────────┼─────────────────┐
            ▼                 ▼                  ▼
    ┌───────────┐     ┌───────────┐      ┌───────────┐
    │   贸易端    │     │   生产端    │      │   管理端    │
    │ (商流)     │ ──▶ │  (工流)    │ ──▶  │  (数据流)   │
    └───────────┘     └───────────┘      └───────────┘
         │                 │                  │
    客户 → 销售订单    工单 → 工序流转      Dashboard
    发货 → 交付签收   报工 → 质检          BI 分析
                      入库 → 库存           AI 助手
```

### 2.2 业务闭环

```
报价/接单 ──▶ 销售订单 ──▶ 生产工单 ──▶ 物料领用 ──▶ 工序加工
                                                │
    ┌───────────────────────────────────────────┘
    ▼              ▼              ▼
  报工记录      质检记录       完工入库
    │              │              │
    └──────────────┴──────────────┘
                    │
                    ▼
              发货交付 ──▶ 应收账款
                    │
                    ▼
              BI 经营分析 ──▶ 管理决策
```

### 2.3 面试追问

**Q: 销售订单和工单是什么关系？**

> 销售订单是"要交付什么给客户"，工单是"要在车间生产什么"。一笔销售订单可能拆成多个工单（分批生产），也可能多个销售订单合并成一个工单（合并生产）。当前系统是一对一关联（`sourceType` + `sourceNo` 字段），后续可扩展为多对多。

**Q: 发货和销售订单是什么关系？**

> 一笔销售订单可以分批发货（`deliveredQty` 跟踪每行已发货量），对应多张发货单。每张发货单有独立的物流状态：待发货 → 已发货 → 已签收。

**Q: 为什么要做"工贸一体"而不是分开两个系统？**

> 数据打通。分开两个系统，销售不知道生产进度（承诺交期靠拍脑袋），生产不知道销售优先级（紧急订单插单混乱）。一体化的好处：销售可以看到工单进度给客户准确的交期回复，生产排产可以参考订单优先级。

---

## 三、全链路业务流程

### 3.1 核心业务主流程

```
1. 客户询价 ──▶ 2. 创建销售订单（待审核）
                    │
                    ▼
              3. 订单审核（审核通过）
                    │
                    ▼
              4. 生成生产工单（待生产）
                    │
                    ▼
              5. 工单开工（生产中）──▶ 6. 工序流转 ──▶ 7. 报工记录
                    │
                    ▼
              8. 质检（来料检/过程检/完工检）
                    │
                    ▼
              9. 完工入库（库存增加）
                    │
                    ▼
              10. 发货出库（库存减少）──▶ 11. 客户签收
                    │
                    ▼
              12. 订单完成 ──▶ 13. BI 分析 / 财务结算
```

### 3.2 数据如何跨模块流动

| 阶段 | 输入 | 输出 | 影响的模块 |
|------|------|------|-----------|
| 接单 | 客户 + 产品 + 数量 + 交期 | SaleOrder (status=1) | 销售 |
| 审核 | 审核人确认 | SaleOrder (status=2) | 销售 |
| 排产 | 订单信息 | WorkOrder (status=1) | 生产 |
| 开工 | 工单 + 物料齐套 | WorkOrder (status=2), 物料库存减少 | 生产、库存 |
| 报工 | 工序完工数量 | WorkOrderProcess (status=completed), WorkReport | 生产 |
| 质检 | 报工产品 | QcRecord (pass/fail) | 生产、质量 |
| 入库 | 完工产品 | Inventory 增加, InventoryTransaction (in) | 库存 |
| 发货 | 销售订单 + 库存 | Delivery, InventoryTransaction (out) | 销售、库存 |
| 签收 | 发货单 | Delivery (status=3), SaleOrder (status=5) | 销售 |
| 分析 | 全量数据 | Dashboard KPIs, BI 报表, AI 问答 | BI、AI |

### 3.3 面试追问

**Q: 如果客户催单，你怎么查这个订单现在在哪个环节？**

> 通过订单号查：1）SaleOrder 的状态（是否审核/发货）；2）关联的 WorkOrder 的状态和进度（哪个工序、报工了多少）；3）关联的 Delivery 状态（是否发货/签收）。三个表的状态串联起来就是完整的履约进度。

**Q: 订单取消后，工单和库存怎么处理？**

> 当前系统：Order 状态设为 6（已取消），但工单不会自动取消，库存也不会自动回退。需要人工判断工单是否继续做（可转为备货库存）还是作废。这是一个已知的产品缺口。

---

## 四、销售管理业务

### 4.1 销售订单 6 种状态

```
         ┌──────────────────────────────────────────────┐
         │                  已取消(6)                     │
         │         （从任何非终态可取消）                   │
         └──────────────────────────────────────────────┘
                            ▲
待审核(1) ──审核通过──▶ 已审核(2) ──下达生产──▶ 生产中(3)
                                                    │
                                             部分发货(4)
                                                    │
                                              已完成(5)
```

**每个状态代表什么业务含义：**

| 状态 | 含义 | 谁操作 | 下一步 |
|------|------|--------|--------|
| 1 待审核 | 销售员创建，等待主管审批 | 销售主管审核 | → 2 |
| 2 已审核 | 审核通过，可下达生产 | 生产计划员 | → 3 |
| 3 生产中 | 已生成工单，车间在制造 | 车间主任 | → 4 |
| 4 部分发货 | 已发了一部分，还有欠交 | 仓库管理员 | → 4 或 5 |
| 5 已完成 | 全部交付，订单关闭 | 系统自动/手动 | 终态 |
| 6 已取消 | 订单作废 | 销售主管 | 终态 |

### 4.2 订单数据模型

```java
// SaleOrder (头)
orderNo: "SO202607120001"         // 自动生成
customerId → Customer             // 哪个客户
orderDate, deliveryDate           // 下单日、承诺交期
totalAmount: 125000.00            // 自动计算（行项目 sum）
status: 1~6

// SaleOrderItem (行)
productId → Product               // 什么产品
quantity: 100, unit: "个"         // 数量单位
price: 1250.00, amount: 125000    // 单价、行金额
deliveredQty: 30                  // 已发货量
```

### 4.3 业务规则

1. **只有待审核状态可以编辑** — 已审核后订单锁定，防止篡改
2. **总金额自动计算** — 添加/修改行项目时自动 `sum(quantity × price)`
3. **订单号自动生成** — `SO` + `yyyyMMdd` + 4 位随机数
4. **发货量不能超过订购量** — `deliveredQty ≤ quantity`
5. **取消是软操作** — `status = 6`，不物理删除

### 4.4 发货管理

```
发货单状态：1 待发货 → 2 已发货 → 3 已签收
```

**发货流程：**
1. 仓库创建发货单，选择销售订单 → 自动填充订单行
2. 填写承运人、物流单号
3. 确认发货 → 扣减库存 → `deliveredQty` 累加
4. 客户签收 → 状态变 3

### 4.5 面试追问

**Q: 为什么要分"待审核"和"已审核"？不能创建后直接生产吗？**

> 制造业的订单涉及金额较大、交期承诺有风险，需要审核环节来把关：客户信用是否OK？产品是否还在产？交期是否合理？价格是否有利润？审核不通过可以退回修改，避免错误订单进入生产环节造成浪费。

**Q: 部分发货的业务场景是什么？**

> 客户订了 1000 件，但工厂产能只能先交 300 件。先发 300 件、剩下的分批交付。`deliveredQty` 跟踪已发货量，`status=4` 表示"还有欠交"。

**Q: 发货单和销售订单的状态如何联动？**

> 当前系统弱联动——发货单创建和状态变更需要手动触发销售订单的状态更新。严谨的做法应该是：发货单确认时自动更新 `deliveredQty`，全部行都 `deliveredQty ≥ quantity` 时自动将订单从 4 → 5。

---

## 五、生产管理业务

### 5.1 工单 4 阶段生命周期

```
待生产(1) ──开工──▶ 生产中(2) ──完工──▶ 已完成(3) ──入库──▶ 已入库(4)
   │                  │                  │                  │
   └─ 未开始         └─ 车间在干活     └─ 做完了等入库    └─ 成品进仓库
```

### 5.2 工序流转

```java
// WorkOrderProcess 表示"这个工单经过哪些工序"
{
  processId: 1 (切割),  sort: 1,  status: 3 (已完成),
  planQty: 100, finishedQty: 100, qualifiedQty: 98, scrapQty: 2,
  worker: "张师傅", startTime: ..., endTime: ...
},
{
  processId: 2 (焊接),  sort: 2,  status: 2 (进行中),
  planQty: 98, finishedQty: 50, qualifiedQty: 48, scrapQty: 1,
  worker: "李师傅", startTime: ..., endTime: null
},
{
  processId: 3 (喷涂),  sort: 3,  status: 1 (待开始),
  planQty: 97, finishedQty: 0, qualifiedQty: 0, scrapQty: 0
}
```

**关键业务规则：**
- 每个工序有独立的合格量 / 不良量
- 上一工序的合格量 = 下一工序的计划量（不良品被剔除）
- 最后一个工序的合格量 = 完工入库量

### 5.3 报工（Work Report）

> 报工 = 工人汇报"我做了多少活"

| 字段 | 说明 |
|------|------|
| `workOrderNo` | 关联工单 |
| `processId` | 哪个工序 |
| `reportQty` | 本次报工数量 |
| `qualifiedQty` | 合格数 |
| `scrapQty` | 报废数 |
| `worker` | 操作工人 |
| `reportTime` | 报工时间 |

**业务场景：** 工人完成一批产品后，在系统上报"我做了 50 个，其中 48 个合格、2 个报废"。系统更新工序进度和工单总体进度。

### 5.4 面试追问

**Q: 为什么要分工序管理？不能一个工单做完就完了吗？**

> 实际工厂的生产不是"一个工单一个人从头做到尾"。一个产品要经过多个工序（切割→焊接→喷涂→包装），每个工序可能在不同车间、由不同人操作。分工序管理能回答：瓶颈在哪道工序？哪个工序不良率最高？哪个工人效率最高？

**Q: 如果上一道工序产生了不良品，下一道工序怎么办？**

> 上一工序的 `qualifiedQty` 流转到下一工序的 `planQty`。报废的 `scrapQty` 不出现在下一工序中。这体现了质量管理中的"不合格不流转"原则。

**Q: 报工和质检的关系是什么？**

> 报工是工人自己说做了多少，质检是质检员验证做得怎么样。合理的流程是：报工 → 质检判定 → 合格的进入下一工序/入库，不合格的退回或报废。

**Q: 工单状态流转有什么业务校验？**

| 操作 | 前置条件 | 校验失败提示 |
|------|---------|-------------|
| 开工 | `status == 1` | "只有待生产的工单可以开工" |
| 完工 | `status == 2` | "只有生产中的工单可以完工" |
| 入库 | `status == 3` | "只有已完成的工单可以入库" |

这种 Guard-based 状态机简单直接，适合状态少、流转规则简单的场景。

---

## 六、库存管理业务

### 6.1 库存四维定位

> 库存不是笼统的"仓库里有 100 个产品"，而是精确到四维坐标：

```
产品(Product) × 仓库(Warehouse) × 库位(Location) × 批次(Batch)
```

```sql
-- 库存唯一键
UNIQUE KEY uk_stock (product_id, warehouse_id, location_id, batch_no)
```

**为什么需要四维定位？**
- **仓库/库位** — 物料放在哪里？拣货时去哪找？
- **批次** — 不同批次的物料可能有不同的保质期、成本、供应商，出库时按 FIFO 取最早批次

### 6.2 库存流水表（审计核心）

> 核心原则：**从不直接修改库存数量，只记录流水，库存余额是流水的累加结果**

```java
// InventoryTransaction 流水记录
{
  productId: 5, warehouseId: 1, batchNo: "B20260701",
  type: "in",          // in入库 / out出库 / transfer调拨 / adjust调整
  quantity: +100,       // 正数 = 增加，负数 = 减少
  beforeQty: 50,       // 变动前 50 个
  afterQty: 150,       // 变动后 150 个
  orderNo: "SO202607120001",  // 关联业务单号（可追溯）
  createTime: "2026-07-12 14:30:00"
}
```

### 6.3 FIFO 出库算法

```
库存批次：
  批次 A (2026-01-01) → 30 个
  批次 B (2026-03-15) → 50 个
  批次 C (2026-06-20) → 40 个

出库 70 个：
  1. 先扣批次 A 的 30 个（A 清零）
  2. 再扣批次 B 的 40 个（B 剩余 10 个）
  → 批次 C 不动
```

**为什么用 FIFO？** 符合实物管理直觉（先入库的先出库），且能追踪每批次的成本。对于有保质期的物料（如化工品），FIFO 还能避免过期。

### 6.4 库存操作类型

| 类型 | 说明 | 触发场景 |
|------|------|---------|
| `in` | 入库 | 采购入库、工单完工入库、退货入库 |
| `out` | 出库 | 销售发货、生产领料、报废出库 |
| `transfer` | 调拨 | 库位 A → 库位 B（仓库内移动） |
| `adjust` | 调整 | 盘点差异调整（盘盈/盘亏） |

### 6.5 面试追问

**Q: 为什么不直接 UPDATE inventory SET quantity = quantity + 100？**

> 直接改数量，你永远不知道"这 100 个是什么时候、谁、因为什么原因加进去的"。流水表提供了完整审计追踪（Audit Trail），做到每笔库存变动可追溯。

**Q: FIFO 出库在并发场景下有什么问题？**

> 两个请求同时出库，都读了同一批次的"当前库存 30"，都认为能扣 20，结果多扣了。解决方案：1）乐观锁（version 字段）；2）悲观锁（SELECT FOR UPDATE）；3）Redis 分布式锁。

**Q: 库存盘点的差异怎么处理？**

> 盘点后发现系统库存和实物库存不一致：1）实物多于系统 → 盘盈，做一条 `adjust` 类型入库流水；2）实物少于系统 → 盘亏，做一条 `adjust` 类型出库流水。

**Q: `lockedQty` 字段是做什么的？**

> 预留库存/锁定库存。比如销售订单已经审批，但还没发货，这部分库存在系统里先锁定，避免被其他订单抢走。当前系统有这个字段但逻辑未实现。

---

## 七、BOM 与产品管理

### 7.1 什么是 BOM？

> **BOM（Bill of Materials，物料清单）** = 做一个产品需要哪些物料、各用多少、在哪个工序用。

```
产品：不锈钢工作台

BOM：
  物料              用量      工序
  304不锈钢板        2.5㎡    1 切割
  方管 40×40        12m      2 焊接
  焊条 Φ3.2         50根     2 焊接
  环氧树脂粉末       0.8kg    3 喷涂
  橡胶脚垫           4个      4 组装
```

### 7.2 当前系统：单层 BOM

```java
// Bom 表：
productId → 成品
materialId → 物料（也是产品表中的一条）
quantity → 数量
processId → 在哪个工序消耗
```

**单层的含义：** 只记录"成品 → 直接物料"的关系，不记录"半成品 → 物料"的递归关系。

### 7.3 面试追问

**Q: 单层 BOM 和多层 BOM 有什么区别？**

| 单层 BOM | 多层 BOM（树状） |
|---------|----------------|
| 成品 → 物料（一层） | 成品 → 半成品 → 物料 → ...（递归） |
| 适合简单产品 | 适合复杂产品（如汽车、电子产品） |
| 查询简单 | 需要递归 CTE 展开 |

**Q: 如果需要支持多层 BOM，怎么改？**

> 1. BOM 表改为 `parent_item_id → child_item_id + quantity`（parent 可以是成品也可以是半成品）
> 2. 查询时用 MySQL 8.0 的 `WITH RECURSIVE` 递归展开
> 3. 前端用 `el-tree-table` 展示多层树形结构

**Q: BOM 在企业里谁维护？**

> 研发/工艺部门。产品设计出来后，工艺工程师根据图纸制定 BOM 和工艺路线。BOM 是 ERP/MES 最核心的基础数据之一，BOM 错了后面全错。

---

## 八、质检管理业务

### 8.1 质检类型

| 类型 | 说明 | 业务场景 |
|------|------|---------|
| 来料检 (IQC) | 供应商送货时检验 | 入库前把关 |
| 过程检 (IPQC) | 生产过程中的抽检 | 工序间把关 |
| 完工检 (FQC) | 产品完工后的终检 | 入库/发货前把关 |

### 8.2 质检记录

```java
QcRecord {
  type: "incoming" / "in_process" / "final"   // 检验类型
  workOrderNo → WorkOrder                      // 关联工单
  productId → Product                          // 检验产品
  checkQty: 100,                               // 检验数量
  qualifiedQty: 95, ngQty: 5                   // 合格/不良
  ngRate: 5.0                                  // 不良率 = ngQty / checkQty × 100
  inspector: "王质检"                           // 检验员
  result: "pass" / "fail"                      // 判定结果
}
```

### 8.3 面试追问

**Q: 质检不合格怎么处理？**

> 1. 不良品隔离（不进入下一工序）2. 判定处理方式：返工/让步接收/报废 3. 分析不良原因（人机料法环）4. BI 告警系统检测到不良率 > 5% 时自动告警。

**Q: 为什么要把质检数据数字化？**

> 1. 可追溯：客户投诉时能查到这批货的质检记录。2. 质量趋势：用 SPC 控制图看不良率是否在恶化。3. 供应商评价：来料检数据可用于供应商评级。4. 认证需要：ISO 9001 等质量体系要求保留质检记录。

---

## 九、BI 经营分析业务

### 9.1 Dashboard 6 大 KPI 的业务含义

| KPI | 计算公式 | 告诉管理者什么 |
|-----|---------|--------------|
| 待处理订单 | `COUNT(sale_order WHERE status=1)` | 审核积压？需要加人审单？ |
| 生产中工单 | `COUNT(work_order WHERE status=2)` | 车间产能是否饱和？ |
| 今日入库 | `COUNT(transaction WHERE type='in' AND DATE=today)` | 今天的入库工作量 |
| 库存 SKU 数 | `COUNT(DISTINCT product_id WHERE qty>0)` | 库存物料种类 |
| 库存周转天数 | `360 / ((出库量 / 平均库存) × (365 / 周期天数))` | 资金使用效率，越低越好 |
| 交付率 | `已完成订单 / 总订单` | 履约能力，越高越好 |

### 9.2 BI 告警 3 大场景

| 告警 | 触发条件 | 业务影响 |
|------|---------|---------|
| 库存不足 | `quantity < 10` | 可能缺料停产 |
| 订单超期 | `delivery_date < NOW() AND status NOT IN (5,6)` | 客户满意度下降、可能面临罚款 |
| 不良率超标 | `ng_rate > 5%` | 过程失控、可能有批量质量问题 |

### 9.3 面试追问

**Q: 库存周转天数从 30 天变成 60 天意味着什么？**

> 变差了。资金压在库存上的时间翻了一倍。可能是：销售预测不准（做太多卖不掉）、采购过量、生产计划不合理。需要分析具体是哪个仓库/哪个产品类别的周转在变差。

**Q: 交付率下降怎么排查？**

> 1）看 Dashboard 的工单生产进度——是否有大量工单卡在生产中？2）看库存结构——原材料是否缺料导致停产？3）看质检数据——不良率是否上升导致返工耽误交期？4）看具体超期订单——是哪个客户/哪个产品的订单超期最多？

---

## 十、AI 业务助手

### 10.1 AI 能回答什么业务问题？

| 用户问 | AI 调用的工具 | 返回 |
|--------|------------|------|
| "查一下不锈钢板的库存" | `queryInventory("不锈钢板")` | "主仓库有 500 张，辅仓库有 200 张" |
| "SO202607120001 这个订单怎么样了" | `getSaleOrder("SO202607120001")` | "该订单状态为生产中，关联工单 WO..." |
| "最近有哪些超期的订单" | `systemHealthCheck()` | "发现 3 个超期订单：..." |
| "工厂现在整体情况怎么样" | `systemHealthCheck()` | "综合评分 85 分（良好），订单交付正常，2 个物料库存偏低..." |
| "有哪些客户" | `listCustomers("")` | 客户列表 |

### 10.2 systemHealthCheck 打分逻辑

```
基础分 100 分
- 每有一个超期未交付订单：-5 分
- 每有一个低于安全线的 SKU：-3 分
- 每有一个生产中工单超计划时间：-5 分
- 不良率 > 5%：-10 分

评分等级：≥80 优秀 / ≥60 良好 / <60 需关注
```

### 10.3 面试追问

**Q: AI 怎么回答"最近生意怎么样"这种模糊问题？**

> LLM 会调用 `getDashboardSummary()` 获取 KPI 概览，然后用自己的语言组织回答。这是 LLM 的核心价值——把结构化数据转化为自然语言。

**Q: AI 查的数据是实时的吗？**

> 是的。每个 `@Tool` 方法直接查数据库（通过 MyBatis-Plus Mapper），返回的是当前最新数据。不是离线分析，不是缓存数据。

---

## 十一、考勤管理业务

### 11.1 打卡规则

```
上班打卡：09:00 前 → 正常
         09:00 后 → 迟到（lateIn = 1）

下班打卡：18:00 后 → 正常
         18:00 前 → 早退（earlyOut = 1）
```

### 11.2 月度视图

每月展示完整日历，标注每天是工作日还是休息日，迟到/早退用红色标记。用于 HR 核算工资、统计出勤率。

---

## 十二、知识库业务

### 12.1 知识库解决什么问题？

> 制造业有大量隐性知识：SOP 标准作业程序、设备操作手册、质量检验标准、安全规范。传统方式是 Word/PDF 存在文件夹里，找起来困难。知识库把这些文档数字化、切片化，让 AI 能检索回答。

### 12.2 文档处理流程

```
上传 PDF/Word/Excel → 文本提取（PDFBox/POI）→ 500 字符切片 → 存储到 kb_chunk
                                                          │
                                           RAG 检索时关键词匹配 Top 5 → 拼接上下文
```

---

## 十三、业务综合分析题

以下是大厂面试中可能出现的业务综合分析题：

### 13.1 "如果订单交付率连续 3 个月下降到 70%，你怎么排查？"

**答题框架（MECE 原则）：**

1. **数据确认** — 拉 Dashboard 交付率趋势，确认是整体下降还是某个产品线/客户群
2. **拆解维度** —
   - 订单端：是不是接了大量短交期订单？
   - 生产端：工单完工率是否下降？瓶颈在哪道工序？
   - 物料端：原材料是否缺料？库存周转是否异常？
   - 质量端：不良率是否上升导致返工？
   - 人员端：关键岗位是否人员不足？
3. **定位根因** — BI 多维交叉分析（按产品 × 月份 / 按工序 × 产量）
4. **给出建议** — 瓶颈工序加人/加设备，或调整交期承诺

### 13.2 "老板说库存太高了要降，你怎么用系统支持这个决策？"

1. **库存结构分析** — Dashboard 按仓库看哪些仓库库存最多
2. **周转分析** — 哪些产品周转天数最长（滞销品/呆滞料）
3. **BI 多维分析** — 按产品类别 × 时间看库存趋势
4. **建议** — 对周转 > 90 天的产品：暂停采购、促销去库存、或报废处理

### 13.3 "如果要给这个系统加一个'成本核算'模块，你会怎么设计？"

1. **物料成本** — 从采购入库记录取单价（FIFO/加权平均）
2. **人工成本** — 从工序报工 × 工时费率
3. **制造费用** — 设备折旧、水电分摊
4. **成本卷积** — 从 BOM 底层物料向上卷积到成品
5. **成本分析** — 实际成本 vs 标准成本差异分析

### 13.4 "一个新客户要做非标定制产品，系统怎么支持？"

1. 产品管理 → 新增定制产品（非标 SKU，单独编码）
2. BOM 管理 → 配置定制 BOM（可能和标准产品共用部分物料）
3. 工序管理 → 如果有特殊工艺，新增临时工序
4. 销售订单 → 关联定制产品
5. 生产工单 → 单独排产（避免和标准品混批）

---

# 第二部分：前端（~25% 篇幅）

---

## 十四、Vue 3 架构设计

### 14.1 技术选型

| 技术 | 版本 | 选型理由 |
|------|------|----------|
| Vue 3 | 3.x | Composition API 逻辑复用更好，`<script setup>` 简洁 |
| Element Plus | 2.x | 企业级组件库，中文文档好，适合后台管理系统 |
| Pinia | 3.x | Vue 3 官方推荐，TypeScript 友好，比 Vuex 更轻量 |
| Vue Router | 4.x | 支持 Composition API 的 `useRouter`/`useRoute` |
| ECharts | 6.x | 国内最成熟的可视化库，支持 7+ 图表类型 |
| Axios | — | 请求/响应拦截器，比 fetch 更方便 |
| Tailwind CSS | 3.x | 原子化 CSS，快速开发，配合 `darkMode: 'class'` |
| driver.js | — | 用户引导/产品 Tour，首次使用引导 |
| Vue CLI 5 | — | Webpack 构建，生态成熟 |

### 14.2 项目结构

```
src/
├── api/index.js              # Axios 实例 + 拦截器
├── main.js                   # 入口：注册全局组件/指令/插件
├── App.vue                   # 根组件
├── router/index.js           # 路由配置 + 导航守卫
├── stores/user.js            # Pinia 用户状态
├── directives/permission.js  # v-permission 自定义指令
├── utils/permission.js       # hasPermission 等工具函数
├── composables/              # 组合式函数（useTheme/useBreadcrumb/useAppTour）
│   ├── useTheme.js           # 主题切换（5 套预设 + 暗黑模式）
│   ├── useBreadcrumb.js      # 面包屑导航
│   └── useAppTour.js         # driver.js 引导
├── styles/tailwind.css       # Tailwind + 自定义组件样式 + 暗黑模式覆盖
├── layout/Index.vue          # 主布局（侧边栏 + 顶栏 + 内容区）
├── components/
│   └── SidebarMenu.vue       # 递归菜单组件
└── views/                    # 28 个页面，按模块组织
    ├── login/                # 登录/注册/忘记密码
    ├── dashboard/            # 仪表盘
    ├── system/               # 系统管理（用户/角色/菜单/个人中心/设置）
    ├── base/                 # 基础数据（产品/工序/客户/仓库）
    ├── sale/                 # 销售（订单/发货）
    ├── inventory/            # 库存
    ├── production/           # 生产（工单/报工/质检）
    ├── bi/                   # BI 分析
    ├── ai/                   # AI 助手
    ├── knowledge/            # 知识库
    ├── attendance/           # 考勤
    └── error/                # 403 等错误页
```

### 14.3 面试追问

**Q: 为什么用 `<script setup>` 而不是 Options API？**

> 1. 更少样板代码（不需要 `data()`, `methods`, `computed` 分开写）2. 逻辑复用方便（抽 composable 即可）3. 更好的 TypeScript 类型推断 4. 编译性能更好。

**Q: 为什么用 Pinia 而不是 Vuex？**

> Pinia 是 Vuex 5 的替代品：没有 mutations（直接改 state），完全的 TypeScript 支持，不需要嵌套模块（每个 store 独立），API 更简洁。Vue 官方已将 Pinia 列为默认状态管理方案。

**Q: Vue CLI 和 Vite 你倾向哪个？**

> 开发体验上 Vite 强很多（冷启动秒开、HMR 即时更新）。但 Vue CLI 的 Webpack 生态更成熟（兼容性好、loader 多）。如果是新项目我选 Vite，老项目迁移要考虑兼容成本。

---

## 十五、路由与导航守卫

### 15.1 路由设计

```javascript
// Hash 模式（createWebHashHistory），所有路由以 #/ 开头
// 优势：不需要服务端配置 fallback，部署到任意静态服务器都可以

const routes = [
  // 布局包裹的路由
  {
    path: '/',
    component: Layout,  // 侧边栏 + 顶栏 + 内容区
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', meta: { permission: 'dashboard:view' } },
      { path: 'sale/order', meta: { permission: 'sale:order:list' } },
      // ... 22 个业务路由，全部懒加载
    ]
  },
  // 独立页面（无布局）
  { path: '/login', component: () => import('@/views/login/Index.vue') },
  { path: '/register' },
  { path: '/forgot-password' },
  { path: '/403' },
]
```

### 15.2 导航守卫（beforeEach）完整逻辑

```javascript
router.beforeEach((to, from, next) => {
  // 第一步：公开页面放行
  if (['/login', '/register', '/forgot-password', '/403'].includes(to.path)) {
    // 特殊情况：已登录 + 记住我 → 跳过登录页
    if (to.path === '/login' && hasTokenInStorage()) {
      return next('/')
    }
    return next()
  }

  // 第二步：未登录 → 去登录
  if (!hasTokenInStorage()) {
    return next('/login')
  }

  // 第三步：权限检查
  const required = to.meta.permission
  if (required) {
    const roles = getRolesFromStorage()
    const isAdmin = roles.some(r => r.code === 'admin')
    const permissions = getPermissionsFromStorage()

    if (!isAdmin && !permissions.includes(required)) {
      return next('/403')
    }
  }

  next()
})
```

### 15.3 面试追问

**Q: 为什么路由权限用 `meta.permission` 而不是后端接口来判断？**

> 前端路由守卫是"第一道防线"，防止用户直接输入 URL 访问没权限的页面。它从 sessionStorage/localStorage 读取登录时后端返回的权限列表做判断。后端拦截器是"最终防线"。两者配合，前端拦截提供更好的用户体验（不发起无效请求），后端拦截保证安全性。

**Q: Hash 模式和 History 模式有什么区别？**

| Hash | History |
|------|---------|
| URL: `xxx.com/#/sale/order` | URL: `xxx.com/sale/order` |
| 不需要服务端配置 | 需要服务端配置 fallback |
| SEO 不友好 | SEO 友好（但后台系统不需要） |
| 兼容性好 | HTML5 API |

后台管理系统用 Hash 模式足够了，简单省事。

**Q: 路由懒加载怎么实现？**

> `component: () => import('@/views/sale/order/Index.vue')` — 每个页面都是动态 import，Webpack 会为每个页面生成独立的 chunk，用户访问时才加载。优点是首屏加载更快。

---

## 十六、状态管理 Pinia

### 16.1 唯一的 Store：useUserStore

```javascript
// stores/user.js — 管理认证相关的所有状态
export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(getStoredToken())
  const user = ref(parseStored('user'))
  const roles = ref(parseStored('roles'))
  const menus = ref(parseStored('menus'))
  const permissions = ref(parseStored('permissions'))

  // Actions
  async function login(username, password, captchaKey, captchaAnswer, rememberMe) {
    const res = await api.post('/auth/login', { ... })
    // 存入 Pinia state
    token.value = res.data.token
    // 根据 rememberMe 决定存 sessionStorage 还是 localStorage
    const storage = rememberMe ? localStorage : sessionStorage
    storage.setItem('token', token.value)
    // ...
  }

  function logout() {
    // 清除 state + 清除两个 storage
    // 但保留 remember_me 和 remember_username
  }

  return { token, user, roles, menus, permissions, login, logout, isRemembered }
})
```

### 16.2 "记住我"的双 Storage 策略

```
勾选"记住我" → token/user/roles/menus/permissions 都存 localStorage（持久化）
不勾选       → 全部存 sessionStorage（关闭浏览器自动清除）
登录页回填   → 从 localStorage 读取 remember_me + remember_username
登出         → 清除两个 storage，但保留 remember_me 和 remember_username
```

### 16.3 面试追问

**Q: 为什么只有一个 Store？不需要拆成多个吗？**

> 当前业务简单，认证状态是唯一需要全局共享的状态。如果后续加了购物车、通知、WebSocket 连接管理等复杂状态，就拆成 userStore / cartStore / notificationStore 等。Pinia 没有 modules 的概念，每个 store 是独立的，天然就是拆分的。

**Q: token 为什么存在 sessionStorage/localStorage 而不是 Cookie？**

> Cookie 会自动随请求发送（可能被 CSRF 攻击利用），且大小限制 4KB。Storage 不会自动发送，前端通过 Axios 拦截器手动加到 Authorization Header，更安全可控。缺点是 SSR 场景不支持（后台系统不需要 SSR）。

---

## 十七、权限控制三层防护

### 17.1 第一层：路由守卫

```javascript
// 在 router.beforeEach 中检查
if (to.meta.permission && !hasPermission(to.meta.permission)) {
  next('/403')  // 没权限 → 跳转 403 页面
}
```

### 17.2 第二层：v-permission 自定义指令

```javascript
// directives/permission.js
export default {
  mounted(el, binding) {
    const perm = binding.value  // e.g. 'system:user:add'
    if (perm && !hasPermission(perm)) {
      el.parentNode?.removeChild(el)  // 直接 DOM 移除，不是隐藏
    }
  }
}

// main.js 注册
app.directive('permission', permissionDirective)

// 使用（在 .vue 文件中）：
<el-button v-permission="'system:user:add'" type="primary">新增用户</el-button>
```

### 17.3 第三层：编程式权限函数

```javascript
// utils/permission.js
export function hasPermission(perm) {
  if (!perm) return true
  const userStore = useUserStore()
  // admin 角色绕行
  if (userStore.roles?.some(r => r.code === 'admin')) return true
  return userStore.permissions?.includes(perm) ?? false
}

export function hasAnyPermission(...perms) {
  return perms.some(p => hasPermission(p))  // 任一有权限即可
}

export function hasAllPermissions(...perms) {
  return perms.every(p => hasPermission(p))  // 全部有权限才行
}

// 使用场景：
// <el-tab-pane v-if="hasAnyPermission('inventory:in', 'inventory:out')">
```

### 17.4 面试追问

**Q: v-permission 为什么是 `removeChild` 而不是 `v-if` 或 `display:none`？**

> 安全性：`display:none` 用户可以通过浏览器开发者工具改成 `display:block` 看到按钮。`removeChild` 从 DOM 中彻底移除，用户无法恢复。

**Q: v-permission 的局限性是什么？**

> `mounted` 只在首次挂载时执行一次。如果权限动态变化（用户权限被管理员修改了），已挂载的元素不会被恢复。解决方案：同时使用 `updated` 钩子，或在权限变更时强制刷新页面。

**Q: 当前代码中 v-permission 实际用了多少？**

> 指令定义好了、全局注册了，但搜索所有 `.vue` 文件发现实际使用率为 0。实际起作用的权限控制是路由守卫 + 后端拦截器。这是前端权限覆盖的已知缺口。

---

## 十八、HTTP 层与拦截器

### 18.1 Axios 实例配置

```javascript
// api/index.js
const api = axios.create({
  baseURL: '/api',         // webpack devServer 代理到 localhost:8081
  timeout: 15000           // 15 秒超时
})

// 请求拦截器：自动加 Token
api.interceptors.request.use(config => {
  const token = sessionStorage.getItem('token') || localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一错误处理
api.interceptors.response.use(
  res => res.data,          // 自动解包，组件直接拿到 data
  err => {
    if (err.response?.status === 401) {
      // 清存储 → 跳登录
      sessionStorage.clear()
      localStorage.clear()
      router.push('/login')
    }
    ElMessage.error(err.response?.data?.message || '请求失败')
    return Promise.reject(err)
  }
)
```

### 18.2 面试追问

**Q: 响应拦截器里 `res.data` 解包有什么好处？**

> 组件中 `const res = await api.get('/xxx')` 拿到的直接是 `{ code: 200, data: {...}, message: 'success' }`，不需要再 `.then(res => res.data)`。减少重复代码。

**Q: 15 秒超时够吗？哪些接口可能超时？**

> 大部分 CRUD 接口在 1 秒内完成。可能超时的场景：Dashboard 聚合查询（全表扫）、BI 导出（Excel 生成）、文件上传（大文件）。生产环境建议：普通接口 15s，导出类接口 60s，上传类接口 120s。

**Q: 如果 Token 过期了，用户正在填写表单，怎么避免数据丢失？**

> 当前实现是 401 直接跳登录，表单数据会丢失。改进方案：1）Token 过期前主动刷新（refresh token 机制）；2）401 时弹出重新登录框而非直接跳转；3）表单数据暂存 sessionStorage。

---

## 十九、组件设计模式

### 19.1 SidebarMenu 递归组件

```vue
<!-- components/SidebarMenu.vue -->
<template>
  <template v-for="menu in menuList" :key="menu.id">
    <!-- 目录类型：el-sub-menu + 递归 -->
    <el-sub-menu v-if="menu.type === 1 && menu.children?.length" :index="menu.path">
      <template #title>
        <component :is="menu.icon" />  <!-- 动态图标 -->
        <span>{{ menu.name }}</span>
      </template>
      <SidebarMenu :menu-list="menu.children" />  <!-- 自己调用自己！ -->
    </el-sub-menu>

    <!-- 菜单类型：el-menu-item -->
    <el-menu-item v-else-if="menu.type === 2 && menu.visible !== 0" :index="menu.path">
      <component :is="menu.icon" />
      <span>{{ menu.name }}</span>
    </el-menu-item>
  </template>
</template>
```

**亮点：** 菜单从后端返回的树形 JSON 递归渲染，不限层级深度。后端控制了菜单结构和权限过滤，前端只负责渲染。

### 19.2 ElTable → 移动端卡片 自适应

大多数列表页都做了桌面/移动端适配：
```vue
<!-- 桌面端：标准 el-table -->
<div class="hidden md:block">
  <el-table :data="list" ...>...</el-table>
</div>
<!-- 移动端：卡片列表 -->
<div class="md:hidden">
  <el-card v-for="item in list" ...>...</el-card>
</div>
```

### 19.3 面试追问

**Q: 递归组件的 `name` 属性为什么必需？**

> Vue 组件需要 `name` 才能在模板中引用自己。`<script setup>` 默认没有 name，要么加第二个 `<script>` 声明 name，要么把组件文件名作为隐式 name。本项目用文件名 `SidebarMenu` 作为组件名来递归。

**Q: 动态图标 `<component :is="menu.icon" />` 是怎么实现的？**

> `main.js` 中把 Element Plus 所有图标都注册为全局组件：`for (const [key, component] of Object.entries(ElementPlusIconsVue)) { app.component(key, component) }`。后端返回 `icon: "User"`，前端 `<component :is="'User'" />` 就渲染出对应图标。

---

## 二十、ECharts 数据可视化

### 20.1 Dashboard 使用了哪些图表类型？

| 图表 | ECharts 类型 | 展示内容 |
|------|------------|---------|
| KPI 卡片 | 自定义（纯 CSS） | 待处理订单、生产中工单、今日入库、SKU 数 |
| 销售趋势 | `line`（双 Y 轴） | 每日销售额（柱状）+ 每日订单数（折线） |
| 库存结构 | `pie`（环形） | 各仓库库存占比 |
| 周转分析 | `bar` | 各产品类别周转天数 |
| 生产进度 | `bar`（横向） | 各工单完成百分比 |
| 交付率 | `gauge`（仪表盘） | 订单交付率 |
| 产品排行 | `bar`（横向） | Top 产品销售额 |
| 客户排行 | `bar`（横向） | Top 客户贡献 |

### 20.2 数据加载模式

```javascript
// Dashboard 页面 — 并行请求所有图表数据
onMounted(async () => {
  const [summary, sales, production, delivery, turnover, structure, ranking, bigScreen] =
    await Promise.all([
      api.get('/dashboard/summary'),
      api.get('/dashboard/sales-trend?days=30'),
      api.get('/dashboard/production-progress'),
      api.get('/dashboard/delivery-rate'),
      api.get('/dashboard/inventory-turnover?days=30'),
      api.get('/dashboard/inventory-structure'),
      api.get('/dashboard/sales-ranking?limit=10'),
      api.get('/dashboard/big-screen'),
    ])
  // 分别绑定到各图表...
})
```

### 20.3 面试追问

**Q: 为什么用 Promise.all 而不是串行请求？**

> Dashboard 有 8 个独立的数据接口，串行会等 8 个 RTT。并行请求总耗时 = max(各接口耗时)，用户感知速度大幅提升。

**Q: ECharts 在 Vue 中的生命周期管理怎么做？**

> 在 `onMounted` 中 `echarts.init()` + `setOption()`。在 `onUnmounted` 中 `dispose()` 销毁实例。窗口 resize 时需要 `chart.resize()`，用 `window.addEventListener('resize', handleResize)` 并在 `onUnmounted` 中移除监听。

**Q: 仪表盘数据量大时 ECharts 性能如何优化？**

> 1. 使用 `large: true` 开启大数据量优化 2. `sampling: 'lttb'` 降采样 3. 后端分页/SQL 聚合减少前端数据量 4. 非当前 tab 的图表 `v-if` 懒初始化。

---

## 二十一、SSE 实时推送前端

### 21.1 实现

```javascript
// Layout/Index.vue
const connectAlertStream = () => {
  const token = sessionStorage.getItem('token') || localStorage.getItem('token')
  // EventSource 不支持自定义 Header，通过 query param 传 token
  const sse = new EventSource(`/api/bi/alerts/stream?token=${token}`)

  sse.addEventListener('alertCount', (event) => {
    unreadAlertCount.value = parseInt(event.data)
  })

  sse.onerror = () => {
    // 断线重连在 5 秒后重试
    sse.close()
    setTimeout(connectAlertStream, 5000)
  }
}

onMounted(() => { connectAlertStream() })
```

### 21.2 面试追问

**Q: EventSource 为什么用 query param 传 token？**

> 浏览器原生 `EventSource` API 不支持自定义 HTTP Header，无法传 `Authorization: Bearer xxx`。通过 `?token=xxx` query param 传 token 是业界常见的 workaround。后端 `AuthInterceptor` 做了兼容：先从 Header 取 token，取不到再从 `request.getParameter("token")` 取。

**Q: SSE 断线重连怎么做？**

> `EventSource` 默认有自动重连机制，但重连间隔不可控。`onerror` 回调中手动 `sse.close()` + `setTimeout(重连, 5000)`，实现可控的指数退避重连。

---

## 二十二、主题与暗黑模式

### 22.1 5 套主题预设

```javascript
// composables/useTheme.js
const THEME_PRESETS = [
  { name: '天空蓝', primary: '#409EFF', cssClass: 'sky' },    // Element Plus 默认
  { name: '翡翠绿', primary: '#10B981', cssClass: 'emerald' },
  { name: '紫罗兰', primary: '#8B5CF6', cssClass: 'violet' },
  { name: '琥珀色', primary: '#F59E0B', cssClass: 'amber' },
  { name: '玫瑰红', primary: '#F43F5E', cssClass: 'rose' },
]

// 通过 CSS 变量注入主题色
function applyTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme.cssClass)
  // 动态覆盖 Element Plus 的 --el-color-primary 系列变量
  document.documentElement.style.setProperty('--el-color-primary', theme.primary)
  // 同时计算 light-3, light-5, dark-2 等衍生色
}
```

### 22.2 暗黑模式

```css
/* tailwind.css */
.dark .el-table { background-color: #1f2937; }
.dark .el-input__inner { background-color: #374151; border-color: #4b5563; }
/* 覆盖 Element Plus 所有组件的暗色样式 */
```

### 22.3 面试追问

**Q: 为什么用 CSS 变量做主题，而不是 Element Plus 的 SCSS 变量？**

> CSS 变量可以运行时动态切换（不需要重新编译），配合 `data-theme` 属性和 `class` 切换即可。SCSS 变量在编译时就固化了。

**Q: 暗黑模式切换后，ECharts 图表颜色怎么跟着变？**

> 切换暗黑模式后，需要重新 `setOption` 更新图表的颜色方案。或者在切换时监听 `useTheme().isDark` 变化，调用 `chart.dispose()` + `echarts.init(dom, 'dark')` 重建图表。

---

## 二十三、响应式设计

### 23.1 断点策略

| 断点 | Tailwind 类 | 设备 |
|------|------------|------|
| < 768px | `block md:hidden` | 手机 |
| ≥ 768px | `hidden md:block` | 平板/桌面 |

### 23.2 移动端适配案例

```vue
<!-- 表格 → 卡片（所有列表页统一模式） -->
<div class="hidden md:block">
  <el-table :data="list" stripe border>...</el-table>
</div>
<div class="md:hidden space-y-3">
  <el-card v-for="item in list" :key="item.id" shadow="hover">
    <div class="flex justify-between">
      <span class="font-bold">{{ item.name }}</span>
      <el-tag>{{ item.status }}</el-tag>
    </div>
  </el-card>
</div>

<!-- 对话框 → 移动端底部弹出 -->
<el-dialog :class="{ 'mobile-bottom-sheet': isMobile }" ...>
```

### 23.3 面试追问

**Q: 移动端为什么要用卡片替代表格？**

> 表格在小屏上需要横向滚动，用户体验很差。卡片布局是纵向堆叠，适配手机屏幕宽度，关键是信息层级要做取舍（卡片只放最重要的 3-4 个字段）。

---

## 二十四、前端性能优化

### 24.1 当前已做的优化

| 优化项 | 实现 |
|--------|------|
| 路由懒加载 | `() => import(...)`，按模块拆 chunk |
| 图标按需加载 | 通过全局注册而非全量引入 Element Plus |
| Dashboard 并行请求 | `Promise.all` 替代串行 await |
| Toast 去抖 | ElMessage 的错误提示不会重复弹出 |

### 24.2 可以做的优化

| 优化项 | 方案 |
|--------|------|
| 列表虚拟滚动 | 数据 > 1000 条时用 `el-table-v2` 虚拟化 |
| 图表懒加载 | 非首屏图表用 `v-if` + IntersectionObserver |
| keep-alive 缓存 | Tab 切换保留页面状态 |
| 图片懒加载 | `loading="lazy"` |
| Webpack → Vite | 冷启动 10x 提升 |
| 打包分析 | `webpack-bundle-analyzer` 排查大依赖 |

### 24.3 面试追问

**Q: 首页加载慢怎么排查？**

> 1. Chrome DevTools Network 看哪个资源大/慢 2. `webpack-bundle-analyzer` 看包体积 3. 检查是否有同步的大依赖（如 moment.js → 换 dayjs）4. 检查首屏是否加载了非首屏的图表库。

---

## 二十五、前端综合分析题

### 25.1 "用户说页面打开很慢，你怎么排查？"

1. **Network 面板** — 看是接口慢还是资源加载慢
2. **如果是接口慢** — 看哪个接口、返回数据量多大、后端 SQL 是否有 N+1
3. **如果是资源慢** — 看 vendor.js 多大、有没有重复打包、CDN 是否可用
4. **首屏优化** — 检查是否所有路由都懒加载了、Dashboard 的 8 个接口是否并行

### 25.2 "怎么设计一个让后端开发也能用的前端权限体系？"

> 约定优于配置：1）路由的 `meta.permission` 和后端 `@RequirePermission("xxx")` 用同一个字符串；2）菜单树由后端返回，前端只渲染；3）v-permission 指令让非前端开发也能在模板中加权限控制。核心是：后端定义权限、前端消费权限，不重复定义。

### 25.3 "如果要给这个系统加国际化，怎么做？"

> 1. Vue I18n 插件 2. 后端返回的菜单名/提示信息需要多语言字段 3. Element Plus 自带国际化配置（已在用 `zhCn`）4. ECharts 图表的 tooltip/axis 文本需要切换时重新 setOption。

---

# 第三部分：后端（~25% 篇幅）

---

## 二十六、权限体系全解析

> 详细权限分析请参考原文档。此处保留核心面试要点：

### 26.1 四层架构总结

```
数据库(5表 RBAC) → 拦截器(认证+鉴权) → 注解(@RequirePermission) → 前端(路由+指令+函数)
```

### 26.2 核心面试题

**Q: 权限怎么做？** → 30 秒版：
> RBAC 五表模型 + 双拦截器链。用户登录后从 DB 加载权限集合缓存到 Redis。AuthInterceptor 负责 Token 认证，PermissionInterceptor 负责读取 @RequirePermission 注解做鉴权。admin 角色在权限集合中额外加入 "admin" 标记实现全通。

**Q: 为什么不用 Spring Security？**
> 当前规模下，自研的双拦截器比 Spring Security 的 Filter Chain + Provider + UserDetailsService 更轻量灵活。如果未来需要 OAuth2/SAML/LDAP，再引入 Spring Security。

**Q: 权限缓存为什么要 24 小时 TTL？**
> 权限变更频率低，24h 可以减少 DB 查询。每次请求会刷新 TTL，活跃用户不会过期。如果管理员修改了权限，需要手动清除对应用户的 Redis 缓存。

---

## 二十七、数据库设计

### 27.1 关键设计决策

| 决策 | 选择 | 理由 |
|------|------|------|
| ID 策略 | 自增（AUTO） | 简单、索引性能好 |
| 逻辑删除 | @TableLogic | 数据可恢复，适合主数据 |
| 外键约束 | 无 | 应用层保障，简化 DDL |
| 订单号生成 | 业务代码生成（SO+日期+随机数） | 有业务含义可读 |
| 金额计算 | 应用层（非 DB 触发器） | 可测试、可调试 |
| 库存定位 | 四维唯一键 | 精确库存管理 |

### 27.2 面试追问

**Q: 数据库最大的问题是什么？**
> 缺少非唯一索引。`WHERE status = ?`、`ORDER BY create_time` 这些高频查询全是全表扫描。百万级数据时 Dashboard 会明显变慢。另外没有外键约束，数据完整性完全依赖应用层。

---

## 二十八、MyBatis-Plus 实践

### 28.1 已用 vs 未用

| 特性 | 状态 | 说明 |
|------|------|------|
| LambdaQueryWrapper | ✅ 大量使用 | 类型安全的动态查询 |
| @TableLogic | ✅ 12 个实体 | 全局逻辑删除 |
| IdType.AUTO | ✅ 全局配置 | 自增主键 |
| PaginationInnerInterceptor | ❌ 未配置 | 分页插件缺失 |
| MetaObjectHandler | ❌ 未实现 | createTime 不自动填充 |
| OptimisticLockerInnerInterceptor | ❌ 未使用 | 无乐观锁 |
| BlockAttackInnerInterceptor | ❌ 未使用 | 无防全表更新 |

### 28.2 面试追问

**Q: 为什么不用 MyBatis-Plus 的分页插件？**
> 漏配了。应该在 `MybatisPlusInterceptor` 中 `addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL))`。当前 KnowledgeService 手动调 `selectCount` + `LIMIT` 绕过了这个限制。

**Q: `@TableField(fill = FieldFill.INSERT)` 为什么不生效？**
> 缺少 `MetaObjectHandler` 实现类。需要 `implements MetaObjectHandler`，在 `insertFill` 方法中 `setFieldValByName("createTime", LocalDateTime.now(), metaObject)`。

---

## 二十九、事务与并发

### 29.1 事务使用清单（13 个方法）

| Service | @Transactional 方法 | 场景 |
|---------|-------------------|------|
| InventoryService | stockIn, stockOut | 库存增减 + 写流水 |
| SaleOrderService | create, delete | 订单 + 行项目 |
| WorkOrderService | create, startWork, complete, finishAndStockIn | 工单流转 |
| KnowledgeService | upload, delete | 文档 + 切片 |
| SysUserServiceImpl | assignRoles | 删旧角色 + 插新角色 |
| SysRoleServiceImpl | assignMenus | 删旧菜单 + 插新菜单 |

### 29.2 面试追问

**Q: 库存扣减并发安全怎么保证？**
> 当前只依赖 `@Transactional` 默认隔离级别。高并发下可能超卖。改进方案：乐观锁（`version` 字段 + 重试）或悲观锁（`SELECT FOR UPDATE`）或 Redis 分布式锁。

**Q: delete-then-insert 的事务风险？**
> 在 READ COMMITTED 隔离级别下，两个并发操作可能读到对方删除后的空数据。升级到 REPEATABLE READ（MySQL 默认）可解决。

---

## 三十、Redis 缓存

### 30.1 使用场景

| Key | Value | TTL | 用途 |
|-----|-------|-----|------|
| `token:<uuid>` | userId | 24h | 登录状态 |
| `perm:<userId>` | Set\<String\> | 24h | 权限集合 |
| `captcha:image:<uuid>` | 验证码 | 3min | 图片验证码 |
| `captcha:math:<uuid>` | 计算结果 | 3min | 数学验证码 |

### 30.2 面试追问

**Q: 权限缓存怎么避免缓存雪崩？**
> 当前所有 key 都是 24h 固定 TTL。改进：加随机偏移（`24h + random(0, 2h)`），避免同一时刻大量 key 同时过期。

**Q: admin 改了一个用户的权限后，怎么立即生效？**
> 1. 管理员操作时主动删除 `perm:<userId>` key 2. 或者提供"强制下线"功能删除 `token:<uuid>` 3. 或者缩短 TTL 到 1h 但不刷新。

---

## 三十一、AI 集成

### 31.1 架构

```
用户提问（中文自然语言）
      ↓
  AiChatService
      ↓
  ┌───┼──────────┐
  ↓   ↓          ↓
RAG 检索   System Prompt   10 个 @Tool 方法
(知识库)   (DeepSeek)     (实时数据查询)
```

### 31.2 面试追问

**Q: Function Calling 的反射实现有什么坑？**
> 需要编译参数 `-parameters` 保留方法参数名，否则 LLM 传的参数名和 Java 方法的参数名对不上。如果没有 `-parameters`，需要通过 `@P("参数描述")` 注解来指定参数名。

**Q: RAG 当前实现有什么问题？**
> 使用关键词 `contains()` 匹配而非向量相似度搜索。`all-minilm-l6-v2` 依赖已经引入了但没启用。升级到向量检索能显著提升语义匹配效果。

**Q: 为什么要限制 3 轮 Tool Calling？**
> 防止 LLM 进入死循环（调工具 → 看结果 → 再调 → ...），同时控制单次对话的 Token 消耗。

---

## 三十二、SSE 与定时任务

### 32.1 SSE 连接管理

```java
// CopyOnWriteArrayList 管理连接池
private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

// 广播
public static void broadcast(int unreadCount) {
    for (SseEmitter emitter : emitters) {
        try { emitter.send(SseEmitter.event().name("alertCount").data(unreadCount)); }
        catch (IOException e) { emitters.remove(emitter); }  // 清理死连接
    }
}
```

### 32.2 面试追问

**Q: CopyOnWriteArrayList 比 ArrayList + synchronized 好在哪里？**
> 读多写少的场景（连接列表读取频繁、新增/移除偶发），写时复制保证读操作无锁，性能更好。

**Q: 定时任务单线程有什么风险？**
> `@Scheduled(cron = "0 5 * * * ?")` 和 `@Scheduled(cron = "0 30 * * * ?")` 共用默认的单线程池。报表任务如果执行超过 25 分钟，告警扫描会延迟。

---

## 三十三、异常处理与统一返回

### 33.1 三层异常处理器

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    BusinessException  → warn 日志 + 返回业务状态码和消息       // 预期内
    BindException      → 提取字段校验错误拼接                   // 参数校验
    Exception          → error 日志 + "系统繁忙，请稍后重试"     // 兜底
}
```

### 33.2 统一返回体

```json
{ "code": 200, "message": "success", "data": {...} }
{ "code": 400, "message": "name: 不能为空; age: 必须大于0", "data": null }
{ "code": 403, "message": "权限不足: sale:order:delete", "data": null }
{ "code": 500, "message": "系统繁忙，请稍后重试", "data": null }
```

---

## 三十四、安全与部署

### 34.1 安全措施清单

| 措施 | 实现 |
|------|------|
| 密码存储 | BCrypt（MD5 兼容迁移） |
| 认证 | Redis Token 24h |
| 鉴权 | 双拦截器 + 注解 |
| 验证码 | 图片 + 数学双验证码 |
| SQL 注入 | MyBatis-Plus `#{}` 参数化 |
| CORS | CorsFilter |
| 通用错误提示 | 500 不暴露内部信息 |

### 34.2 面试追问

**Q: 当前安全方面有什么不足？**
> 1. 文件上传无大小限制/类型白名单 2. 注册无邮箱验证 3. 无 API 限流 4. CORS 允许所有来源 5. 无 XSS 过滤 6. Druid 监控页面未配置访问密码。

---

## 三十五、后端综合分析题

### 35.1 "系统突然变慢了，你怎么排查？"

1. **Druid 监控** — 看哪个 SQL 执行变慢、慢查询日志
2. **Redis 监控** — 是否命中率下降、内存是否满
3. **JVM 监控** — GC 频率、堆内存、线程数
4. **数据库连接池** — active 连接是否打满（max 20）
5. **定时任务** — 是否在执行大批量操作
6. **最近变更** — 是否新上了什么功能

### 35.2 "如果要支持 10 倍的用户量，你会改什么？"

1. **数据库索引** — 先加非唯一索引
2. **缓存** — Dashboard 数据缓存、菜单树缓存
3. **连接池** — Druid max-active 20 → 50，Lettuce 连接池扩容
4. **定时任务** — 线程池扩容，避免排队
5. **读写分离** — 只读查询走从库
6. **分库分表** — 流水表按时间分表
7. **异步化** — 邮件发送、报表生成异步执行

---

## 三十六、项目 Gap 与改进方向

### 36.1 按影响程度排序

| 优先级 | Gap | 影响 |
|--------|-----|------|
| 🔴 高 | 数据库无索引 | 全表扫描，百万级数据时系统不可用 |
| 🔴 高 | 无乐观锁 | 库存并发扣减可能超卖 |
| 🔴 高 | 告警规则表是空壳 | bi_alert_rule 的 CRUD 没接入扫描逻辑 |
| 🟡 中 | v-permission 未使用 | 前端按钮权限没生效 |
| 🟡 中 | MetaObjectHandler 缺失 | createTime/updateTime 不自动填充 |
| 🟡 中 | 无分页插件 | MyBatis-Plus 分页功能不工作 |
| 🟡 中 | RAG 是关键词匹配 | 不是向量检索，语义匹配差 |
| 🟡 中 | SaleOrder 软删除 + Item 硬删除 | 订单恢复后丢失明细 |
| 🟢 低 | 无 API 限流 | 可能被恶意刷接口 |
| 🟢 低 | CORS 允许所有来源 | 生产需收紧 |

### 36.2 Gap 的面试回答策略

> 面试被问"有什么不足"时，主动提 2-3 个影响大的 Gap，并且说出改进方案。展示你能"批判性地审视自己的代码"。

---

## 三十七、面试应答模板

### "介绍这个项目"（2 分钟）

> 我做的是一个面向中小型制造企业的工贸一体 MES 系统。业务上覆盖销售接单 → 生产工单 → 库存管理 → 质检 → BI 经营分析的全链路，同时集成了 AI 智能助手，让管理者可以用自然语言查询系统数据。
>
> 技术上用了 Spring Boot 3 + MyBatis-Plus + Vue 3 + Element Plus。权限系统是自研的 RBAC 五表模型 + 双拦截器 + 自定义注解，没依赖 Spring Security。AI 那部分用 LangChain4j 集成了 DeepSeek 大模型，通过 10 个 @Tool 方法让 AI 能查实时业务数据。实时告警用 SSE 推送到前端。
>
> 我做这个项目最大的收获是理解了制造业的业务逻辑——不只是 CRUD，要理解工单怎么流转、库存怎么用 FIFO 扣减、质检和报工怎么协同。技术上最大的挑战是库存扣减的并发安全和 AI Tool Calling 的参数反射匹配。

### "权限系统怎么做的？"（1 分钟）

> 分了四层。第一层数据库五表 RBAC——用户、角色、菜单/权限加两张关联表。第二层后端双拦截器——AuthInterceptor 用 Redis Token 做认证，PermissionInterceptor 反射读取 @RequirePermission 注解做鉴权。第三层注解驱动——开发者在 Controller 方法上加 @RequirePermission("sale:order:delete") 即可精准控制。第四层前端三层防护——路由守卫、v-permission 指令、hasPermission 函数。admin 角色有特权标记直接放行。

### "这个系统的业务价值是什么？"（1 分钟）

> 对工厂老板来说，以前想知道"今天生产了多少、库存还有多少、哪些订单快超期了"需要打电话问车间主任和仓库管理员。现在打开 Dashboard 一目了然。AI 助手更进一步——直接问"最近哪些订单有问题"就能得到答案。对车间主任来说，以前排产靠 Excel，不知道每个工单在哪个工序、做了多少。现在系统里有完整的工序流转和报工记录。对质检来说，以前质检记录在纸质单上，出了问题很难查。现在所有质检数据数字化，还能自动告警。

### "项目有什么不足？"（30 秒）

> 主要有三点：一是数据库缺少非唯一索引，Dashboard 的聚合查询目前是全表扫描，数据量大的时候会慢。二是告警扫描规则是硬编码的，bi_alert_rule 表有 CRUD 但没真正接入扫描逻辑。三是库存扣减目前的并发保护不够，高并发下可能超卖，需要引入乐观锁。这些问题我都清楚改进方向。

---

## 三十八、快速问答卡片

### 业务

- **工贸一体？** 既做生产也做销售，系统打通两端的业务流程
- **工单状态？** 待生产(1) → 生产中(2) → 已完成(3) → 已入库(4)
- **订单状态？** 待审核 → 已审核 → 生产中 → 部分发货 → 已完成 / 已取消
- **库存定位？** product × warehouse × location × batch 四维
- **出库模式？** FIFO 先进先出，按批次最早优先扣减
- **BOM 结构？** 单层（成品→直接物料），不支持多层递归
- **质检类型？** 来料检(IQC) / 过程检(IPQC) / 完工检(FQC)
- **Dashboard KPI？** 待处理订单 / 生产中工单 / 今日入库 / SKU数 / 库存周转天数 / 交付率
- **告警场景？** 库存不足(quantity<10) / 订单超期 / 不良率>5%
- **AI 能干什么？** 查产品、查库存、查订单、查工单、系统健康巡检

### 前端

- **框架？** Vue 3 Composition API + Element Plus + Pinia + ECharts 6 + Tailwind CSS
- **路由？** Hash 模式（`#/`），懒加载，beforeEach 权限守卫
- **状态管理？** 单个 Pinia Store（useUserStore），token/user/roles/menus/permissions
- **记住我？** localStorage（持久化）vs sessionStorage（会话级）
- **权限？** 路由守卫（meta.permission）+ v-permission 指令 + hasPermission 函数
- **HTTP？** Axios 实例，baseURL=/api，请求拦截器加 Bearer Token，响应拦截器解包+401跳转
- **图表？** ECharts，Dashboard 8 个图表 Promise.all 并行加载
- **SSE？** EventSource + query param 传 token + 5 秒自动重连
- **主题？** 5 套预设（CSS 变量）+ 暗黑模式 + Element Plus 全组件暗色覆盖
- **响应式？** 768px 断点，桌面表格 ↔ 移动端卡片
- **构建？** Vue CLI 5（Webpack），devServer 代理 8080 → 8081

### 后端

- **框架？** Spring Boot 3.4.0 + Java 17 + MyBatis-Plus 3.5.9
- **权限？** 五表 RBAC + AuthInterceptor + PermissionInterceptor + @RequirePermission
- **认证？** 自研 Token（Redis，24h TTL），非 Spring Security，非 JWT
- **数据库？** MySQL 8.0 + Druid 连接池（5-20）
- **缓存？** Redis（Lettuce + GenericJackson2JsonRedisSerializer）
- **AI？** LangChain4j 0.36 + DeepSeek API + 10 个 @Tool 方法 + 3 轮 Tool Calling
- **文档？** Knife4j 4.5（OpenAPI 3），中文 UI，/doc.html
- **密码？** BCrypt（MD5 兼容迁移）
- **事务？** @Transactional 默认 REQUIRED，13 个方法
- **分页？** 插件未配置（KnowledgeService 手动分页）
- **索引？** 仅有主键和唯一键（缺非唯一索引）
- **日志？** 无自定义配置，使用 Spring Boot 默认 Logback

---

> **面试核心建议：不要背答案。理解 Why > 记住 What。被问到时先说结论（30 秒），如果面试官追问再展开（2 分钟）。主动提到 Gap 和改进思路，展示工程素养。**
