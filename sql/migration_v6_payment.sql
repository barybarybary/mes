-- ============================================
-- 模拟支付 — 新增 paid 字段
-- ============================================
ALTER TABLE sale_order ADD COLUMN IF NOT EXISTS paid TINYINT DEFAULT 0 COMMENT '是否已付款: 0未付款 1已付款';
