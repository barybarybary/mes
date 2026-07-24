-- 库存预警表
CREATE TABLE IF NOT EXISTS stock_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL COMMENT '产品ID',
    product_name VARCHAR(100) COMMENT '产品名称',
    current_qty DECIMAL(10,3) DEFAULT 0 COMMENT '当前库存',
    threshold_qty DECIMAL(10,3) DEFAULT 10 COMMENT '预警阈值',
    status TINYINT DEFAULT 0 COMMENT '0=未处理 1=已处理',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    resolve_time DATETIME COMMENT '处理时间'
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT '库存预警记录';
