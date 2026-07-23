-- ============================================
-- 客户门户模块
-- ============================================

-- 客户表（门户注册用户）
CREATE TABLE IF NOT EXISTS portal_customer (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    company_name VARCHAR(100) COMMENT '公司名称',
    contact_name VARCHAR(50) COMMENT '联系人',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    address VARCHAR(255) COMMENT '地址',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_portal_username (username)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT '门户客户表';

-- 测试客户（密码: 123456，BCrypt加密）
INSERT INTO portal_customer (username, password, company_name, contact_name, phone, email, address) VALUES
('customer1', '$2a$10$5pNvGV8R.r/S9sw0UQ5BwOm6N9kvtmqyOajH/Yd53NO8Ao8FVCh7m', '星辰科技有限公司', '张总', '13800001111', 'zhang@xingchen.com', '上海市浦东新区张江路100号'),
('customer2', '$2a$10$5pNvGV8R.r/S9sw0UQ5BwOm6N9kvtmqyOajH/Yd53NO8Ao8FVCh7m', '远航制造集团', '李经理', '13900002222', 'li@yuanhang.com', '深圳市南山区科技园路200号');

