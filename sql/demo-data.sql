-- Products (8 finished + 3 raw)
INSERT INTO product (code, name, category_id, spec, unit, price, status) VALUES
('P001', 'Precision Gear Shaft', NULL, 'D25x120mm 45#Steel', 'pcs', 85.00, 1),
('P002', 'Hydraulic Valve Body', NULL, 'HT250 Casting', 'set', 1200.00, 1),
('P003', 'Motor End Cover', NULL, 'D180x30mm Al Alloy', 'pcs', 45.00, 1),
('P004', 'Flange Connector', NULL, 'DN100 SS304', 'pcs', 320.00, 1),
('P005', 'Drive Shaft Bushing', NULL, 'D50xD30x80mm Bronze', 'pcs', 28.50, 1),
('P006', 'Bearing Housing', NULL, 'SN210 Cast Iron', 'set', 156.00, 1),
('P007', 'Control Panel Case', NULL, '450x300x120mm ABS', 'pcs', 68.00, 1),
('P008', 'Radiator Assembly', NULL, '600x400x50mm Al Extrusion', 'set', 480.00, 1),
('M001', 'Round Bar 45# Steel', NULL, 'D30mmx6m', 'bar', 180.00, 1),
('M002', 'Cast Iron Blank HT250', NULL, '200x200x100mm', 'block', 95.00, 1),
('M003', 'SS304 Plate', NULL, '3mmx1500x3000mm', 'sheet', 680.00, 1);

-- Processes
INSERT INTO process (code, name, standard_hours, price, sort, status) VALUES
('PR01', 'Cutting', 15, 8.00, 1, 1),
('PR02', 'Rough Turning', 30, 15.00, 2, 1),
('PR03', 'Finish Turning', 45, 22.00, 3, 1),
('PR04', 'Drilling', 20, 10.00, 4, 1),
('PR05', 'Milling', 40, 20.00, 5, 1),
('PR06', 'Grinding', 35, 18.00, 6, 1),
('PR07', 'Heat Treatment', 60, 25.00, 7, 1),
('PR08', 'Surface Finish', 25, 12.00, 8, 1),
('PR09', 'Assembly', 50, 28.00, 9, 1),
('PR10', 'QC Inspection', 20, 10.00, 10, 1);

-- Customers
INSERT INTO customer (code, name, contact, phone, email, address, status) VALUES
('C001', 'BYD Precision Mfg', 'Mr. Wang', '13800138001', 'wang@byd.com', 'Shenzhen Pingshan BYD Road 3009', 1),
('C002', 'SANY Heavy Industry', 'Mr. Li', '13800138002', 'li@sany.com', 'Changsha Xingsha Economic Zone', 1),
('C003', 'XCMG Group', 'Mr. Zhang', '13800138003', 'zhang@xcmg.com', 'Xuzhou Hi-Tech Road 68', 1),
('C004', 'Inovance Technology', 'Mr. Chen', '13800138004', 'chen@inovance.com', 'Shenzhen Longhua Guanlan Park', 1),
('C005', 'STEP Robotics', 'Mr. Liu', '13800138005', 'liu@step.com', 'Shanghai Jiading Boyuan Road 1333', 1);

-- Warehouses
INSERT INTO warehouse (code, name, type, address, manager, status) VALUES
('WH01', 'Raw Material WH', 'material', 'Building A Floor 1', 'Zhao', 1),
('WH02', 'Semi-Finished WH', 'semi', 'Building B Floor 2', 'Qian', 1),
('WH03', 'Finished Goods WH', 'finished', 'Building C Floor 1', 'Sun', 1);

-- Locations
INSERT INTO warehouse_location (warehouse_id, code, name) VALUES
(1, 'A-01', 'Zone A Rack 1'), (1, 'A-02', 'Zone A Rack 2'),
(2, 'B-01', 'Zone B Rack 1'), (2, 'B-02', 'Zone B Rack 2'),
(3, 'C-01', 'Zone C Rack 1'), (3, 'C-02', 'Zone C Rack 2');

-- Inventory
INSERT INTO inventory (product_id, warehouse_id, location_id, batch_no, quantity, unit) VALUES
(9, 1, 1, 'B20260601', 50, 'bar'),
(10, 1, 2, 'B20260605', 30, 'block'),
(11, 1, 1, 'B20260610', 20, 'sheet'),
(3, 3, 5, 'B20260615', 120, 'pcs'),
(1, 3, 5, 'B20260618', 80, 'pcs'),
(5, 2, 3, 'B20260620', 200, 'pcs');

-- Sale Orders
INSERT INTO sale_order (id, order_no, customer_id, order_date, delivery_date, status, total_amount, remark) VALUES
(1, 'SO20260601001', 1, '2026-06-01', '2026-06-15', 3, 85000.00, 'BYD monthly order'),
(2, 'SO20260605002', 2, '2026-06-05', '2026-06-20', 2, 156000.00, 'SANY urgent order'),
(3, 'SO20260610003', 3, '2026-06-10', '2026-06-25', 1, 48000.00, ''),
(4, 'SO20260615004', 4, '2026-06-15', '2026-06-30', 2, 93600.00, ''),
(5, 'SO20260620005', 5, '2026-06-20', '2026-07-05', 1, 27200.00, 'New customer first order');

INSERT INTO sale_order_item (order_id, product_id, quantity, unit, price, amount) VALUES
(1, 1, 500, 'pcs', 85.00, 42500.00),
(1, 2, 50, 'set', 850.00, 42500.00),
(2, 2, 130, 'set', 1200.00, 156000.00),
(3, 3, 800, 'pcs', 45.00, 36000.00),
(3, 4, 100, 'pcs', 120.00, 12000.00),
(4, 6, 400, 'set', 156.00, 62400.00),
(4, 5, 200, 'pcs', 28.50, 31200.00),
(5, 7, 400, 'pcs', 68.00, 27200.00);

-- Work Orders
INSERT INTO work_order (id, order_no, product_id, quantity, finished_qty, qualified_qty, source_type, source_no, status, plan_start, plan_end, actual_start) VALUES
(1, 'WO20260602001', 1, 500, 350, 340, 'sale_order', 'SO20260601001', 2, '2026-06-02', '2026-06-12', '2026-06-02 08:00:00'),
(2, 'WO20260606002', 2, 130, 130, 128, 'sale_order', 'SO20260605002', 3, '2026-06-06', '2026-06-18', '2026-06-06 08:30:00'),
(3, 'WO20260611003', 3, 800, 200, 195, 'sale_order', 'SO20260610003', 2, '2026-06-11', '2026-06-22', '2026-06-11 09:00:00'),
(4, 'WO20260616004', 6, 400, 0, 0, 'sale_order', 'SO20260615004', 1, '2026-06-16', '2026-06-28', NULL),
(5, 'WO20260621005', 4, 100, 0, 0, 'manual', NULL, 1, '2026-06-21', '2026-07-02', NULL);

-- Work Order Processes
INSERT INTO work_order_process (work_order_id, process_id, sort, plan_qty, finished_qty, qualified_qty, scrap_qty, status) VALUES
(1, 1, 1, 500, 500, 500, 0, 3),
(1, 2, 2, 500, 500, 498, 2, 3),
(1, 3, 3, 500, 350, 340, 10, 2),
(2, 1, 1, 130, 130, 130, 0, 3),
(2, 5, 2, 130, 130, 129, 1, 3),
(2, 9, 3, 130, 130, 128, 2, 3),
(3, 1, 1, 800, 500, 498, 2, 2),
(3, 2, 2, 800, 200, 195, 5, 2),
(3, 4, 3, 800, 0, 0, 0, 1);

-- Work Reports
INSERT INTO work_report (work_order_id, work_order_process_id, product_id, process_id, worker, quantity, qualified_qty, scrap_qty, report_date, remark) VALUES
(1, 1, 1, 1, 'Zhang San', 200, 200, 0, '2026-06-03', ''),
(1, 1, 1, 1, 'Zhang San', 300, 300, 0, '2026-06-04', ''),
(1, 2, 1, 2, 'Li Si', 250, 249, 1, '2026-06-05', 'Tool wear'),
(1, 2, 1, 2, 'Li Si', 250, 249, 1, '2026-06-06', ''),
(1, 3, 1, 3, 'Wang Wu', 200, 195, 5, '2026-06-08', 'Surface roughness NG'),
(1, 3, 1, 3, 'Wang Wu', 150, 145, 5, '2026-06-10', ''),
(2, 4, 2, 1, 'Zhao Liu', 130, 130, 0, '2026-06-07', ''),
(2, 5, 2, 5, 'Qian Qi', 130, 129, 1, '2026-06-10', 'Burr found'),
(2, 6, 2, 9, 'Zhou Ba', 130, 128, 2, '2026-06-15', 'Assembly gap oversize'),
(3, 7, 3, 1, 'Zhang San', 500, 498, 2, '2026-06-13', ''),
(3, 8, 3, 2, 'Li Si', 200, 195, 5, '2026-06-16', '');

-- QC Records
INSERT INTO qc_record (type, product_id, work_order_id, batch_no, check_qty, ok_qty, ng_qty, result, inspector, check_date, ng_description, disposition) VALUES
('incoming', 9, NULL, 'B20260601', 20, 20, 0, 1, 'QC-A', '2026-06-01', NULL, NULL),
('in_process', 1, 1, 'B20260615', 50, 48, 2, 1, 'QC-B', '2026-06-08', 'Surface Ra out of spec', 'Rework'),
('final', 2, 2, NULL, 10, 9, 1, 3, 'QC-C', '2026-06-16', 'Seal surface scratch', 'Accept with concession'),
('in_process', 3, 3, NULL, 30, 28, 2, 1, 'QC-B', '2026-06-14', 'Dimension out of spec 0.02mm', 'Rework');

-- Inventory Transactions
INSERT INTO inventory_transaction (product_id, warehouse_id, batch_no, type, quantity, before_qty, after_qty, order_no, remark) VALUES
(9, 1, 'B20260601', 'in', 50, 0, 50, 'PO20260601', 'Purchase receipt'),
(10, 1, 'B20260605', 'in', 30, 0, 30, 'PO20260602', 'Purchase receipt'),
(11, 1, 'B20260610', 'in', 20, 0, 20, 'PO20260603', 'Purchase receipt'),
(3, 3, 'B20260615', 'in', 200, 0, 200, 'WO20260603', 'Production finished'),
(3, 3, 'B20260615', 'out', -80, 200, 120, 'SO20260610003', 'Sales delivery'),
(1, 3, 'B20260618', 'in', 350, 0, 350, 'WO20260601', 'Production finished'),
(1, 3, 'B20260618', 'out', -270, 350, 80, 'SO20260601001', 'Sales delivery');
