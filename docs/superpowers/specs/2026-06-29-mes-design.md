# 工贸一体 MES 系统设计文档

## 技术栈

| 层 | 选型 |
|---|---|
| 后端 | Spring Boot 3.4 + Java 17 |
| ORM | MyBatis-Plus |
| AI | LangChain4j + RAG |
| 接口文档 | Knife4j |
| 前端 | Vue 3 + Element Plus + Tailwind CSS |
| 数据库 | MySQL 8.0 + Redis 7 |
| 向量库 | 暂用内存/PGVector |

## 模块清单

1. **基础数据** — 产品、BOM(单层)、工序、仓库/库位、客户
2. **系统管理** — 用户/角色/菜单权限(RBAC)
3. **销售管理** — 客户、销售订单(状态流转)、发货单
4. **库存管理** — 入库/出库/调拨、批次号、库存查询、库存流水
5. **生产管理** — 工单、工序流转、报工、质检、完工入库
6. **知识库** — 文档分类、上传、切片、向量化
7. **AI 助手** — RAG 对话、智能搜索(LangChain4j)
8. **报表驾驶舱** — 销售趋势、库存周转、生产进度
9. **数据大屏** — 实时生产看板、KPI 轮播

## 数据库核心表

- product, bom, process, warehouse, location, customer
- sys_user, sys_role, sys_menu
- sale_order, sale_order_item, delivery, delivery_item
- inventory, inventory_batch, inventory_transaction
- work_order, work_order_process, work_report, qc_record
- kb_document, kb_chunk
- ai_conversation, ai_message

## 分步实施

1. 环境搭建 + 基础数据 + RBAC
2. 销售 + 库存
3. 生产管理
4. 报表 + 大屏
5. 知识库 + AI
