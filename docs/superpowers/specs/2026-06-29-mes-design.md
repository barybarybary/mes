# 工贸一体 MES 系统设计文档

## 技术栈

| 层 | 选型 |
|---|---|
| 后端 | Spring Boot 3.4 + Java 17 |
| ORM | MyBatis-Plus |
| 消息队列 | RabbitMQ 3.13（异步邮件、事件驱动、审计日志） |
| AI | LangChain4j + RAG |
| 接口文档 | Knife4j |
| 前端 | Vue 3 + Element Plus + Tailwind CSS |
| 数据库 | MySQL 8.0 + Redis 7 |
| 向量库 | 暂用内存/PGVector |

## 模块清单

1. **基础数据** — 产品、BOM(单层)、工序、仓库/库位、客户
2. **系统管理** — 用户/角色/菜单权限(RBAC)、操作审计日志
3. **销售管理** — 客户、销售订单(状态流转)、发货单
4. **库存管理** — 入库/出库/调拨、批次号、库存查询、库存流水
5. **生产管理** — 工单、工序流转、报工、质检、完工入库
6. **知识库** — 文档分类、上传、切片、向量化
7. **AI 助手** — RAG 对话、智能搜索(LangChain4j)
8. **报表驾驶舱** — 销售趋势、库存周转、生产进度
9. **数据大屏** — 实时生产看板、KPI 轮播
10. **消息队列** — 异步邮件、BI 告警 SSE 推送、工单状态事件、审计日志

## 消息队列架构

### 拓扑设计

```
交换机                              队列                      路由键                  DLQ
═══════════════════════════════════════════════════════════════════════════════════════
mes.mail.exchange (direct)     mes.mail.queue           mail.send              mes.mail.dlq
                               mes.mail.dlq            (死信自动路由)           N/A

mes.event.exchange (topic)     mes.alert.queue          alert.*                (无)
                                                        notify.workorder.#

mes.audit.exchange (direct)    mes.audit.queue          audit.log              mes.audit.dlq
                               mes.audit.dlq            (死信自动路由)           N/A
```

### 关键设计

- **序列化**：JSON（Jackson2JsonMessageConverter），消息在管理界面可见
- **确认模式**：手动 ack，3 次重试后进入 DLQ
- **持久化**：消息和队列均持久化，支持 RabbitMQ 重启后恢复
- **降级**：MessageSender 内 try-catch，MQ 不可用时应用核心功能正常
- **邮件/审计含 DLQ**（业务数据不可丢失），**告警无 DLQ**（下次扫描重新生成）

### 异步邮件

```
RegisterController.sendCode() / BiScheduleService
  → MessageSender → RabbitMQ(mes.mail.queue)
  → MailQueueConsumer → MailService → SMTP
```

替代原有同步 SMTP 调用，验证码接口响应时间从 ~2s 降至 ~50ms。

### BI 告警推送

```
BiScheduleService.autoAlertScan()
  → BiAlertService.scanAndRecord()
  → MessageSender.sendEvent("alert.scan")
  → RabbitMQ(mes.alert.queue)
  → AlertEventConsumer → BiSseController.broadcast()
```

Service 层不再直接耦合 Controller，通过 MQ 解耦 SSE 广播。

### 工单状态事件

```
WorkOrderService.startWork/complete/finishAndStockIn()
  → MessageSender.sendEvent("notify.workorder.{status}")
  → RabbitMQ(mes.alert.queue)
  → WorkOrderEventConsumer
```

未来扩展：完工自动创建质检任务、入库推送库存更新通知。

### 审计日志

```
Controller/Service
  → MessageSender.sendAudit()
  → RabbitMQ(mes.audit.queue)
  → AuditLogConsumer → OperationLogMapper → operation_log 表
```

## 数据库核心表

- product, bom, process, warehouse, location, customer
- sys_user, sys_role, sys_menu, **operation_log**
- sale_order, sale_order_item, delivery, delivery_item
- inventory, inventory_batch, inventory_transaction
- work_order, work_order_process, work_report, qc_record
- kb_document, kb_chunk
- ai_conversation, ai_message
- bi_report_config, bi_alert_rule, bi_alert_record

## 分步实施

1. 环境搭建 + 基础数据 + RBAC
2. 销售 + 库存
3. 生产管理
4. 报表 + 大屏
5. 知识库 + AI
6. **消息队列** — RabbitMQ 集成、异步邮件、事件驱动、审计日志
