-- ============================================
-- 造易 MES v4: 智能报表 — 记录表 + 定时配置表 + 菜单
-- ============================================

USE mes1;

-- 报表记录表
CREATE TABLE IF NOT EXISTS report_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '生成用户ID',
    title VARCHAR(200) COMMENT '报表标题',
    report_type VARCHAR(50) NOT NULL COMMENT 'sales/production/inventory/summary',
    time_range VARCHAR(100) COMMENT '时间范围',
    file_bytes LONGBLOB COMMENT 'Excel 文件',
    file_name VARCHAR(200) COMMENT '文件名',
    file_size BIGINT COMMENT '文件大小(字节)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT 'AI生成的报表记录';

-- 报表定时发送配置表
CREATE TABLE IF NOT EXISTS report_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '创建用户ID',
    report_type VARCHAR(50) NOT NULL COMMENT '报表类型',
    report_title VARCHAR(200) COMMENT '报表标题',
    cron_expr VARCHAR(50) NOT NULL COMMENT 'cron 表达式',
    recipients VARCHAR(500) COMMENT '额外收件人邮箱(逗号分隔)',
    include_self TINYINT DEFAULT 1 COMMENT '包含创建者自己: 0否 1是',
    status TINYINT DEFAULT 1 COMMENT '0停用 1启用',
    last_run_time DATETIME COMMENT '上次执行时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) COMMENT '报表定时发送配置';

-- 菜单：报表中心 (使用较大ID避免与已有菜单冲突)
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort) VALUES
(170, 0,  '报表中心', 1, '/report',     NULL,                   'DocumentChecked', NULL,             17),
(171, 170,'报表中心', 2, '/report',     'report/index',         NULL,              'report:manage',  1);

-- 给管理员角色授权
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, 170 FROM sys_role r WHERE r.code = 'admin';

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, 171 FROM sys_role r WHERE r.code = 'admin';
