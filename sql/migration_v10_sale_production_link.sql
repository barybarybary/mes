-- ============================================
-- migration_v10: 销售订单 → 生产工单关联
-- 为 work_order 增加来源ID字段，支持高效追溯
-- ============================================

-- 1. 添加来源ID（用于高效JOIN查询，补充已有的 source_type/source_no）
ALTER TABLE work_order
    ADD COLUMN source_id BIGINT COMMENT '来源ID（如sale_order.id）' AFTER source_no;

-- 2. 添加来源明细ID（关联具体的 sale_order_item）
ALTER TABLE work_order
    ADD COLUMN source_item_id BIGINT COMMENT '来源明细ID（如sale_order_item.id）' AFTER source_id;

-- 3. 为 source_id 添加索引，方便按来源订单查询工单
CREATE INDEX idx_work_order_source_id ON work_order (source_id);
