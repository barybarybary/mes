-- ============================================
-- 造易 v2 迁移脚本：轻量级 MES 改造
-- ============================================

-- 1. 设备台账表
CREATE TABLE IF NOT EXISTS equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL COMMENT '设备编号',
    name VARCHAR(100) COMMENT '设备名称',
    type VARCHAR(50) COMMENT '设备类型',
    workshop VARCHAR(100) COMMENT '所属车间',
    line VARCHAR(100) COMMENT '所属产线',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/IDLE/REPAIR/SCRAPPED',
    buy_date DATE COMMENT '购买日期',
    manufacturer VARCHAR(100) COMMENT '制造商',
    spec VARCHAR(200) COMMENT '规格型号',
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code)
) COMMENT '设备台账';

-- 2. 工单工序加字段（MySQL 8.0 兼容写法，使用预处理语句做幂等检查）
SET @col = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'work_order_process' AND COLUMN_NAME = 'assigned_to');
SET @sql = IF(@col = 0, 'ALTER TABLE work_order_process ADD COLUMN assigned_to BIGINT COMMENT ''指派给用户ID'' AFTER worker', 'SELECT ''[SKIP] assigned_to exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'work_order_process' AND COLUMN_NAME = 'equipment_id');
SET @sql = IF(@col = 0, 'ALTER TABLE work_order_process ADD COLUMN equipment_id BIGINT COMMENT ''设备ID'' AFTER assigned_to', 'SELECT ''[SKIP] equipment_id exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3. 报工记录加字段
SET @col = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'work_report' AND COLUMN_NAME = 'defect_reason');
SET @sql = IF(@col = 0, 'ALTER TABLE work_report ADD COLUMN defect_reason VARCHAR(500) COMMENT ''不良原因'' AFTER scrap_qty', 'SELECT ''[SKIP] defect_reason exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'work_report' AND COLUMN_NAME = 'report_type');
SET @sql = IF(@col = 0, 'ALTER TABLE work_report ADD COLUMN report_type VARCHAR(20) DEFAULT ''NORMAL'' COMMENT ''NORMAL/REWORK/FIRST_PIECE'' AFTER defect_reason', 'SELECT ''[SKIP] report_type exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'work_report' AND COLUMN_NAME = 'equipment_id');
SET @sql = IF(@col = 0, 'ALTER TABLE work_report ADD COLUMN equipment_id BIGINT COMMENT ''设备ID'' AFTER report_type', 'SELECT ''[SKIP] equipment_id exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4. 质检记录加工序关联
SET @col = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qc_record' AND COLUMN_NAME = 'work_order_process_id');
SET @sql = IF(@col = 0, 'ALTER TABLE qc_record ADD COLUMN work_order_process_id BIGINT COMMENT ''关联工单工序ID'' AFTER work_order_id', 'SELECT ''[SKIP] work_order_process_id exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 5. 质检标准表
CREATE TABLE IF NOT EXISTS qc_standard (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL COMMENT '产品ID',
    process_id BIGINT COMMENT '工序ID(可为空，空=终检标准)',
    item_name VARCHAR(100) NOT NULL COMMENT '检验项目',
    item_type VARCHAR(20) DEFAULT 'QUALITATIVE' COMMENT 'QUANTITATIVE/QUALITATIVE',
    unit VARCHAR(20) COMMENT '单位',
    spec_lower DECIMAL(10,4) COMMENT '规格下限',
    spec_upper DECIMAL(10,4) COMMENT '规格上限',
    spec_target DECIMAL(10,4) COMMENT '目标值',
    is_critical TINYINT DEFAULT 0 COMMENT '是否关键特性',
    sort_order INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '质检标准';

-- 6. 工序-SOP关联表
CREATE TABLE IF NOT EXISTS process_sop (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    process_id BIGINT NOT NULL COMMENT '工序ID',
    kb_document_id BIGINT NOT NULL COMMENT '知识库文档ID',
    sop_type VARCHAR(20) DEFAULT 'SOP' COMMENT 'SOP/SPEC/TROUBLESHOOTING',
    sort_order INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '工序SOP关联';

-- ============================================
-- 演示数据
-- ============================================

-- 设备演示数据
INSERT INTO equipment (code, name, type, workshop, status) VALUES
('M01', '密炼机', '混料设备', '混料车间', 'ACTIVE'),
('E01', '硫化机1号', '硫化设备', '硫化车间', 'ACTIVE'),
('E02', '硫化机2号', '硫化设备', '硫化车间', 'ACTIVE'),
('E03', '硫化机3号', '硫化设备', '硫化车间', 'ACTIVE'),
('G01', '打磨机1号', '打磨设备', '打磨车间', 'ACTIVE'),
('G02', '打磨机2号', '打磨设备', '打磨车间', 'ACTIVE');

-- 质检标准演示数据（刹车片AB-200的硫化工序）
INSERT INTO qc_standard (product_id, process_id, item_name, item_type, unit, spec_lower, spec_upper, spec_target, is_critical, sort_order) VALUES
(1, 2, '外观', 'QUALITATIVE', NULL, NULL, NULL, NULL, 1, 1),
(1, 2, '尺寸', 'QUANTITATIVE', 'mm', 119.90, 120.10, 120.00, 1, 2),
(1, 2, '硬度', 'QUANTITATIVE', 'HA', 73.0, 77.0, 75.0, 1, 3);

-- ============================================
-- 7. 供应商管理
-- ============================================

CREATE TABLE IF NOT EXISTS supplier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) COMMENT '供应商编码',
    name VARCHAR(100) COMMENT '供应商名称',
    contact VARCHAR(50) COMMENT '联系人',
    phone VARCHAR(50) COMMENT '联系电话',
    address VARCHAR(255) COMMENT '地址',
    remark VARCHAR(500) COMMENT '备注',
    status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '供应商';

INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort)
VALUES (230, 200, '供应商管理', 2, '/base/supplier', 'base/supplier/index', 'Avatar', 'base:supplier:list', 4);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 230 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE menu_id = 230);

-- ============================================
-- 8. 清理非MES菜单（设为不可见）
-- ============================================

-- 销售管理（ERP模块，MES不需要）
UPDATE sys_menu SET visible = 0 WHERE name IN ('Sales', 'Sale Order', 'Sale Delivery');

-- 库存管理（MES简化版不单独暴露）
UPDATE sys_menu SET visible = 0 WHERE name IN ('Inventory', 'Inventory Transaction');

-- 知识库（保留，MES需要SOP）
-- AI助手（保留，核心差异化）
-- 系统管理（保留）
-- 基础数据（保留）
-- 生产管理（保留，核心模块）
