-- ============================================
-- 造易 v3 迁移脚本：修复 v2 中因 MySQL 兼容性问题
-- 未成功添加的列（ADD COLUMN IF NOT EXISTS 是 MariaDB 语法）
-- ============================================

-- 检查并添加 work_report 缺失列
-- 使用 INFORMATION_SCHEMA 做幂等检查，兼容 MySQL 5.7+/8.0

-- work_report.defect_reason
SET @col = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'mes1' AND TABLE_NAME = 'work_report' AND COLUMN_NAME = 'defect_reason');
SET @sql = IF(@col = 0,
    'ALTER TABLE work_report ADD COLUMN defect_reason VARCHAR(500) COMMENT ''不良原因'' AFTER scrap_qty',
    'SELECT ''[SKIP] work_report.defect_reason already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- work_report.report_type
SET @col = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'mes1' AND TABLE_NAME = 'work_report' AND COLUMN_NAME = 'report_type');
SET @sql = IF(@col = 0,
    'ALTER TABLE work_report ADD COLUMN report_type VARCHAR(20) DEFAULT ''NORMAL'' COMMENT ''NORMAL/REWORK/FIRST_PIECE'' AFTER defect_reason',
    'SELECT ''[SKIP] work_report.report_type already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- work_report.equipment_id
SET @col = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'mes1' AND TABLE_NAME = 'work_report' AND COLUMN_NAME = 'equipment_id');
SET @sql = IF(@col = 0,
    'ALTER TABLE work_report ADD COLUMN equipment_id BIGINT COMMENT ''设备ID'' AFTER report_type',
    'SELECT ''[SKIP] work_report.equipment_id already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- qc_record.work_order_process_id
SET @col = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'mes1' AND TABLE_NAME = 'qc_record' AND COLUMN_NAME = 'work_order_process_id');
SET @sql = IF(@col = 0,
    'ALTER TABLE qc_record ADD COLUMN work_order_process_id BIGINT COMMENT ''关联工单工序ID'' AFTER work_order_id',
    'SELECT ''[SKIP] qc_record.work_order_process_id already exists'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
