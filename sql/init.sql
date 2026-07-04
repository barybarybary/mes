-- ============================================
-- 工贸一体 MES 系统 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS mes1 DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE mes1;

-- ============================================
-- 1. 系统管理模块
-- ============================================

-- 用户表
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_time DATETIME COMMENT '最后登录时间',
    UNIQUE KEY uk_username (username)
) COMMENT '用户表';

-- 角色表
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL COMMENT '角色编码',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(200) COMMENT '描述',
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code)
) COMMENT '角色表';

-- 菜单表
CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    type TINYINT NOT NULL COMMENT '类型: 1目录 2菜单 3按钮',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    icon VARCHAR(50) COMMENT '图标',
    permission VARCHAR(100) COMMENT '权限标识',
    sort INT DEFAULT 0 COMMENT '排序',
    visible TINYINT DEFAULT 1 COMMENT '是否可见',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '菜单权限表';

-- 用户角色关联
CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL
) COMMENT '用户角色关联';

-- 角色菜单关联
CREATE TABLE sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL
) COMMENT '角色菜单关联';

-- ============================================
-- 2. 基础数据模块
-- ============================================

-- 产品分类
CREATE TABLE product_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '产品分类';

-- 产品表
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(32) NOT NULL COMMENT '产品编码',
    name VARCHAR(100) NOT NULL COMMENT '产品名称',
    category_id BIGINT COMMENT '分类ID',
    spec VARCHAR(200) COMMENT '规格型号',
    unit VARCHAR(20) COMMENT '单位',
    price DECIMAL(10,2) DEFAULT 0 COMMENT '参考售价',
    image_url VARCHAR(255) COMMENT '图片',
    remark VARCHAR(500) COMMENT '备注',
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code)
) COMMENT '产品表';

-- BOM表 (单层 - 成品与物料的对应关系)
CREATE TABLE bom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL COMMENT '成品ID',
    material_id BIGINT NOT NULL COMMENT '物料ID',
    quantity DECIMAL(10,3) NOT NULL COMMENT '用量',
    unit VARCHAR(20) COMMENT '单位',
    process_id BIGINT COMMENT '在哪个工序消耗',
    sort INT DEFAULT 0 COMMENT '排序',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT 'BOM物料清单';

-- 工序表
CREATE TABLE process (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(32) COMMENT '工序编码',
    name VARCHAR(100) NOT NULL COMMENT '工序名称',
    standard_hours DECIMAL(10,2) DEFAULT 0 COMMENT '标准工时(分钟)',
    price DECIMAL(10,2) DEFAULT 0 COMMENT '工价',
    sort INT DEFAULT 0 COMMENT '顺序',
    remark VARCHAR(255) COMMENT '备注',
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '工序表';

-- 客户表
CREATE TABLE customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(32) NOT NULL COMMENT '客户编码',
    name VARCHAR(100) NOT NULL COMMENT '客户名称',
    contact VARCHAR(50) COMMENT '联系人',
    phone VARCHAR(20) COMMENT '电话',
    email VARCHAR(100) COMMENT '邮箱',
    address VARCHAR(255) COMMENT '地址',
    remark VARCHAR(500) COMMENT '备注',
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code)
) COMMENT '客户表';

-- 仓库表
CREATE TABLE warehouse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(32) NOT NULL COMMENT '仓库编码',
    name VARCHAR(50) NOT NULL COMMENT '仓库名称',
    type VARCHAR(20) COMMENT '类型: material原料 finished成品 semi半成品',
    address VARCHAR(255),
    manager VARCHAR(50) COMMENT '负责人',
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '仓库表';

-- 库位表
CREATE TABLE warehouse_location (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    code VARCHAR(32) NOT NULL COMMENT '库位编码',
    name VARCHAR(50) COMMENT '库位名称',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '库位表';

-- ============================================
-- 3. 销售管理模块
-- ============================================

-- 销售订单
CREATE TABLE sale_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    order_date DATE NOT NULL COMMENT '订单日期',
    delivery_date DATE COMMENT '预计交期',
    status TINYINT DEFAULT 1 COMMENT '1待审核 2已审核 3生产中 4部分发货 5已完成 6已取消',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '订单金额',
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no)
) COMMENT '销售订单';

-- 销售订单明细
CREATE TABLE sale_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL COMMENT '数量',
    unit VARCHAR(20) COMMENT '单位',
    price DECIMAL(10,2) COMMENT '单价',
    amount DECIMAL(12,2) COMMENT '金额',
    delivered_qty DECIMAL(10,2) DEFAULT 0 COMMENT '已发货数量',
    remark VARCHAR(255)
) COMMENT '销售订单明细';

-- 发货单
CREATE TABLE delivery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_no VARCHAR(32) NOT NULL COMMENT '发货单号',
    order_id BIGINT COMMENT '关联订单',
    customer_id BIGINT NOT NULL,
    delivery_date DATE COMMENT '发货日期',
    status TINYINT DEFAULT 1 COMMENT '1待发货 2已发货 3已签收',
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_by BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_delivery_no (delivery_no)
) COMMENT '发货单';

-- 发货明细
CREATE TABLE delivery_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    batch_no VARCHAR(32) COMMENT '批次号',
    remark VARCHAR(255)
) COMMENT '发货明细';

-- ============================================
-- 4. 库存管理模块
-- ============================================

-- 库存表 (按产品+仓库+库位+批次汇总)
CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    location_id BIGINT COMMENT '库位ID',
    batch_no VARCHAR(32) COMMENT '批次号',
    quantity DECIMAL(10,3) NOT NULL DEFAULT 0 COMMENT '库存数量',
    locked_qty DECIMAL(10,3) DEFAULT 0 COMMENT '锁定数量',
    unit VARCHAR(20) COMMENT '单位',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stock (product_id, warehouse_id, location_id, batch_no)
) COMMENT '库存表';

-- 库存流水
CREATE TABLE inventory_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    batch_no VARCHAR(32),
    type VARCHAR(20) NOT NULL COMMENT '类型: in入库 out出库 transfer调拨 adjust调整',
    quantity DECIMAL(10,3) NOT NULL COMMENT '变动数量(正数入库,负数出库)',
    before_qty DECIMAL(10,3) COMMENT '变动前数量',
    after_qty DECIMAL(10,3) COMMENT '变动后数量',
    order_no VARCHAR(32) COMMENT '关联单号',
    remark VARCHAR(255),
    create_by BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '库存流水';

-- ============================================
-- 5. 生产管理模块
-- ============================================

-- 生产工单
CREATE TABLE work_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL COMMENT '工单号',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    quantity DECIMAL(10,2) NOT NULL COMMENT '计划数量',
    finished_qty DECIMAL(10,2) DEFAULT 0 COMMENT '已完成数量',
    qualified_qty DECIMAL(10,2) DEFAULT 0 COMMENT '合格数量',
    scrap_qty DECIMAL(10,2) DEFAULT 0 COMMENT '报废数量',
    source_type VARCHAR(20) COMMENT '来源: manual手动 sale_order销售订单',
    source_no VARCHAR(32) COMMENT '来源单号',
    status TINYINT DEFAULT 1 COMMENT '1待生产 2生产中 3已完成 4已入库',
    plan_start DATE COMMENT '计划开始',
    plan_end DATE COMMENT '计划结束',
    actual_start DATETIME COMMENT '实际开始',
    actual_end DATETIME COMMENT '实际结束',
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    create_by BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no)
) COMMENT '生产工单';

-- 工单工序流转
CREATE TABLE work_order_process (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    process_id BIGINT NOT NULL COMMENT '工序ID',
    sort INT DEFAULT 0 COMMENT '工序顺序',
    plan_qty DECIMAL(10,2) COMMENT '计划数量',
    finished_qty DECIMAL(10,2) DEFAULT 0 COMMENT '完成数量',
    qualified_qty DECIMAL(10,2) DEFAULT 0 COMMENT '合格数量',
    scrap_qty DECIMAL(10,2) DEFAULT 0 COMMENT '报废数量',
    status TINYINT DEFAULT 1 COMMENT '1待加工 2加工中 3已完成',
    worker VARCHAR(50) COMMENT '操作工',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '工单工序';

-- 报工记录
CREATE TABLE work_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    work_order_process_id BIGINT COMMENT '工单工序ID',
    product_id BIGINT NOT NULL,
    process_id BIGINT,
    worker VARCHAR(50) NOT NULL COMMENT '报工人',
    quantity DECIMAL(10,2) NOT NULL COMMENT '报工数量',
    qualified_qty DECIMAL(10,2) DEFAULT 0 COMMENT '合格数',
    scrap_qty DECIMAL(10,2) DEFAULT 0 COMMENT '报废数',
    work_hours DECIMAL(10,2) COMMENT '工时',
    report_date DATE COMMENT '报工日期',
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '报工记录';

-- 质检记录
CREATE TABLE qc_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL COMMENT '类型: incoming来料 in_process过程 final成品',
    product_id BIGINT NOT NULL,
    work_order_id BIGINT COMMENT '工单ID',
    batch_no VARCHAR(32) COMMENT '批次号',
    check_qty DECIMAL(10,2) COMMENT '检验数量',
    ok_qty DECIMAL(10,2) DEFAULT 0 COMMENT '合格数量',
    ng_qty DECIMAL(10,2) DEFAULT 0 COMMENT '不合格数量',
    result TINYINT COMMENT '结论: 1合格 2不合格 3让步接收',
    inspector VARCHAR(50) COMMENT '检验人',
    check_date DATE COMMENT '检验日期',
    ng_description VARCHAR(500) COMMENT '不良描述',
    disposition VARCHAR(100) COMMENT '处理意见',
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '质检记录';

-- ============================================
-- 6. 知识库模块
-- ============================================

-- 知识库文档
CREATE TABLE kb_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '文档标题',
    category VARCHAR(50) COMMENT '分类: sop作业指导书 spec规格书 manual设备手册 other其他',
    file_name VARCHAR(200) COMMENT '原文件名',
    file_type VARCHAR(20) COMMENT '文件类型',
    file_size BIGINT COMMENT '文件大小',
    file_path VARCHAR(500) COMMENT '文件路径',
    content TEXT COMMENT '文本内容',
    status TINYINT DEFAULT 1 COMMENT '1待处理 2处理中 3已完成',
    chunk_count INT DEFAULT 0 COMMENT '切片数量',
    deleted TINYINT DEFAULT 0,
    create_by BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '知识库文档';

-- 知识库切片
CREATE TABLE kb_chunk (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL COMMENT '文档ID',
    chunk_index INT NOT NULL COMMENT '切片序号',
    content TEXT NOT NULL COMMENT '切片内容',
    embedding TEXT COMMENT '向量(JSON格式存储)',
    token_count INT COMMENT 'Token数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '知识库切片';

-- ============================================
-- 7. AI 助手模块
-- ============================================

-- AI 对话会话
CREATE TABLE ai_conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) COMMENT '对话标题',
    user_id BIGINT NOT NULL,
    model VARCHAR(50) COMMENT '使用的模型',
    message_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT 'AI对话会话';

-- AI 消息
CREATE TABLE ai_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'user / assistant',
    content TEXT NOT NULL,
    sources TEXT COMMENT 'RAG引用来源(JSON)',
    token_count INT COMMENT 'Token消耗',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT 'AI消息';

-- ============================================
-- 初始化数据
-- ============================================

-- 管理员用户 (密码: admin123, BCrypt加密)
INSERT INTO sys_user (username, password, nickname, status) VALUES
('admin', '$2b$12$tK44Camsm9ZM9FolgJh1OeTYMs21BJ/8HDa1oDDhbqLhAl4v.nowu', 'Administrator', 1);

-- 基础角色
INSERT INTO sys_role (code, name, description) VALUES
('admin',     '管理员',   '系统管理员，拥有全部权限'),
('sale',      '销售',     '销售订单、客户管理'),
('operator',  '操作工',   '生产报工、工序操作'),
('operation', '运营',     '仓库库存、数据报表、知识库管理'),
('purchase',  '采购',     '供应商管理、采购订单、物料询价'),
('qc',        '质检员',   '来料检验、过程检、成品检验、质检报告'),
('warehouse', '仓管员',   '库存出入库、盘点、调拨、库存预警'),
('dev',       '开发',     '系统开发、接口调试、技术文档维护');

-- 基础菜单
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort) VALUES
(1, 0, 'System', 1, '/system', NULL, 'Setting', NULL, 100),
(2, 0, 'Base Data', 1, '/base', NULL, 'Document', NULL, 200),
(3, 0, 'Sales', 1, '/sale', NULL, 'Sell', NULL, 300),
(4, 0, 'Inventory', 1, '/inventory', NULL, 'Box', NULL, 400),
(5, 0, 'Production', 1, '/production', NULL, 'Monitor', NULL, 500),
(6, 0, 'Knowledge', 1, '/knowledge', NULL, 'Reading', NULL, 600),
(7, 0, 'AI Assistant', 1, '/ai', NULL, 'ChatDotRound', NULL, 700),
(8, 0, 'Dashboard', 1, '/dashboard', NULL, 'DataAnalysis', NULL, 800),
(11, 1, 'User Management', 2, '/system/user', 'system/user/index', NULL, 'system:user:list', 1),
(12, 1, 'Role Management', 2, '/system/role', 'system/role/index', NULL, 'system:role:list', 2),
(13, 1, 'Menu Management', 2, '/system/menu', 'system/menu/index', NULL, 'system:menu:list', 3),
(14, 11, 'User Add', 3, NULL, NULL, NULL, 'system:user:add', 1),
(15, 11, 'User Edit', 3, NULL, NULL, NULL, 'system:user:edit', 2),
(16, 11, 'User Delete', 3, NULL, NULL, NULL, 'system:user:delete', 3);

-- 管理员拥有所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 管理员角色分配
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- ============================================
-- 8. BI 报表模块
-- ============================================

-- 定时报表配置
CREATE TABLE bi_report_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) COMMENT '报表名称',
    type VARCHAR(50) COMMENT '类型: sales/inventory/production',
    report_format VARCHAR(10) DEFAULT 'excel' COMMENT 'excel/pdf',
    cron_expr VARCHAR(50) COMMENT 'cron表达式',
    recipients VARCHAR(500) COMMENT '收件人邮箱(逗号分隔)',
    status TINYINT DEFAULT 1 COMMENT '0停用 1启用',
    last_run_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '定时报表配置';

-- 告警规则
CREATE TABLE bi_alert_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) COMMENT '规则名称',
    category VARCHAR(50) COMMENT '分类: stock/order/quality',
    metric VARCHAR(50) COMMENT '监控指标',
    operator VARCHAR(10) COMMENT '比较符: lt/gt/eq',
    threshold DECIMAL(12,2) COMMENT '阈值',
    level VARCHAR(20) DEFAULT 'warning' COMMENT 'info/warning/critical',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '告警规则';

-- 告警记录
CREATE TABLE bi_alert_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id BIGINT COMMENT '关联规则',
    title VARCHAR(200) COMMENT '告警标题',
    content TEXT COMMENT '告警详情',
    level VARCHAR(20),
    is_read TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '告警记录';

-- ============================================
-- 数据库变更: product 表加成本价
-- ============================================
ALTER TABLE product ADD COLUMN IF NOT EXISTS cost_price DECIMAL(10,2) DEFAULT 0 COMMENT '参考成本价';

-- ============================================
-- 预置告警规则
-- ============================================
INSERT INTO bi_alert_rule (name, category, metric, operator, threshold, level) VALUES
('库存低于安全线', 'stock', 'inventory_quantity', 'lt', 10, 'critical'),
('订单超期未交付', 'order', 'delivery_overdue', 'gt', 0, 'warning'),
('质检不合格率超标', 'quality', 'qc_ng_rate', 'gt', 5, 'warning');

-- BI 菜单
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort) VALUES
(9, 0, 'BI Reports', 1, '/bi', NULL, 'TrendCharts', NULL, 900),
(91, 9, 'Overview', 2, '/bi/overview', 'bi/overview/index', NULL, 'bi:view', 1),
(92, 9, 'Pivot Analysis', 2, '/bi/pivot', 'bi/pivot/index', NULL, 'bi:view', 2),
(93, 9, 'Alert Center', 2, '/bi/alert', 'bi/alert/index', NULL, 'bi:alert:manage', 3),
(94, 9, 'Report Export', 2, '/bi/export', 'bi/export/index', NULL, 'bi:export', 4);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE id >= 9 AND id < 100;
