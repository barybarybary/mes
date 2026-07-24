-- 订单支付通知表
CREATE TABLE IF NOT EXISTS order_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(50) NOT NULL COMMENT '订单号',
    customer_name VARCHAR(100) COMMENT '客户名称/公司',
    total_amount DECIMAL(12,2) COMMENT '订单金额',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '通知时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单支付通知';
