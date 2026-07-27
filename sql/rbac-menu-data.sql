-- ============================================
-- 造易 MES 完整菜单树 + 角色权限分配
-- 执行前会清空旧菜单和角色菜单关联
-- ============================================

USE mes1;

-- 清空旧数据
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM sys_role_menu;
DELETE FROM sys_menu;
ALTER TABLE sys_menu AUTO_INCREMENT = 1;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 仪表盘 (ID 100-109)
-- ============================================
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort) VALUES
(100, 0,  '仪表盘',   1, '/dashboard',          NULL,                       'DataAnalysis', NULL,             1),
(101, 100,'首页概览', 2, '/dashboard',          'dashboard/index',          NULL,           'dashboard:view', 2);

-- ============================================
-- 系统管理 (ID 1, 11-22)
-- ============================================
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort) VALUES
(1,  0,  '系统管理',   1, '/system',             NULL,                       'Setting',      NULL,                100),
(11, 1,  '用户管理',   2, '/system/user',        'system/user/index',        NULL,           'system:user:list',  101),
(14, 11, '新增用户',   3, NULL,                  NULL,                       NULL,           'system:user:add',   102),
(15, 11, '编辑用户',   3, NULL,                  NULL,                       NULL,           'system:user:edit',  103),
(16, 11, '删除用户',   3, NULL,                  NULL,                       NULL,           'system:user:delete',104),
(12, 1,  '角色管理',   2, '/system/role',        'system/role/index',        NULL,           'system:role:list',  105),
(17, 12, '新增角色',   3, NULL,                  NULL,                       NULL,           'system:role:add',   106),
(18, 12, '编辑角色',   3, NULL,                  NULL,                       NULL,           'system:role:edit',  107),
(19, 12, '删除角色',   3, NULL,                  NULL,                       NULL,           'system:role:delete',108),
(13, 1,  '菜单管理',   2, '/system/menu',        'system/menu/index',        NULL,           'system:menu:list',  109),
(20, 13, '新增菜单',   3, NULL,                  NULL,                       NULL,           'system:menu:add',   110),
(21, 13, '编辑菜单',   3, NULL,                  NULL,                       NULL,           'system:menu:edit',  111),
(22, 13, '删除菜单',   3, NULL,                  NULL,                       NULL,           'system:menu:delete',112),

-- ============================================
-- 基础数据 (ID 200-299)
-- ============================================
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort) VALUES
(200, 0,  '基础数据',   1, '/base',              NULL,                       'Document',     NULL,                 200),
-- 产品
(201, 200,'产品管理',   2, '/base/product',      'base/product/index',       NULL,           'base:product:list',  201),
(202, 201,'新增产品',   3, NULL,                  NULL,                       NULL,           'base:product:add',   202),
(203, 201,'编辑产品',   3, NULL,                  NULL,                       NULL,           'base:product:edit',  203),
(204, 201,'删除产品',   3, NULL,                  NULL,                       NULL,           'base:product:delete',204),
-- 工序
(205, 200,'工序管理',   2, '/base/process',      'base/process/index',       NULL,           'base:process:list',  205),
(206, 205,'新增工序',   3, NULL,                  NULL,                       NULL,           'base:process:add',   206),
(207, 205,'编辑工序',   3, NULL,                  NULL,                       NULL,           'base:process:edit',  207),
(208, 205,'删除工序',   3, NULL,                  NULL,                       NULL,           'base:process:delete',208),
-- 客户
(209, 200,'客户管理',   2, '/base/customer',     'base/customer/index',      NULL,           'base:customer:list', 209),
(210, 209,'新增客户',   3, NULL,                  NULL,                       NULL,           'base:customer:add',  210),
(211, 209,'编辑客户',   3, NULL,                  NULL,                       NULL,           'base:customer:edit', 211),
(212, 209,'删除客户',   3, NULL,                  NULL,                       NULL,           'base:customer:delete',212),
-- 仓库
(213, 200,'仓库管理',   2, '/base/warehouse',    'base/warehouse/index',     NULL,           'base:warehouse:list', 213),
(214, 213,'新增仓库',   3, NULL,                  NULL,                       NULL,           'base:warehouse:add',  214),
(215, 213,'编辑仓库',   3, NULL,                  NULL,                       NULL,           'base:warehouse:edit', 215),
(216, 213,'删除仓库',   3, NULL,                  NULL,                       NULL,           'base:warehouse:delete',216),
-- 设备
(217, 200,'设备管理',   2, '/base/equipment',   'base/equipment/index',     NULL,           'base:equipment:list', 217),
(218, 217,'新增设备',   3, NULL,                  NULL,                       NULL,           'base:equipment:add',  218),
(219, 217,'编辑设备',   3, NULL,                  NULL,                       NULL,           'base:equipment:edit', 219),
(220, 217,'删除设备',   3, NULL,                  NULL,                       NULL,           'base:equipment:delete',220),
-- 供应商
(221, 200,'供应商管理', 2, '/base/supplier',     'base/supplier/index',      NULL,           'base:supplier:list',  221),
(222, 221,'新增供应商', 3, NULL,                  NULL,                       NULL,           'base:supplier:add',   222),
(223, 221,'编辑供应商', 3, NULL,                  NULL,                       NULL,           'base:supplier:edit',  223),
(224, 221,'删除供应商', 3, NULL,                  NULL,                       NULL,           'base:supplier:delete',224);

-- ============================================
-- 销售管理 (ID 300-399)
-- ============================================
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort) VALUES
(300, 0,  '销售管理',   1, '/sale',              NULL,                       'Sell',         NULL,                 300),
-- 销售订单
(301, 300,'销售订单',   2, '/sale/order',        'sale/order/index',         NULL,           'sale:order:list',    301),
(302, 301,'新增订单',   3, NULL,                  NULL,                       NULL,           'sale:order:add',     302),
(303, 301,'编辑订单',   3, NULL,                  NULL,                       NULL,           'sale:order:edit',    303),
(304, 301,'删除订单',   3, NULL,                  NULL,                       NULL,           'sale:order:delete',  304),
(305, 301,'审核订单',   3, NULL,                  NULL,                       NULL,           'sale:order:audit',   305),
-- 发货
(306, 300,'发货管理',   2, '/sale/delivery',     'sale/delivery/index',      NULL,           'sale:delivery:list', 306),
(307, 306,'新增发货',   3, NULL,                  NULL,                       NULL,           'sale:delivery:add',  307),
(308, 306,'编辑发货',   3, NULL,                  NULL,                       NULL,           'sale:delivery:edit', 308),
(309, 306,'确认发货',   3, NULL,                  NULL,                       NULL,           'sale:delivery:ship', 309);

-- ============================================
-- 库存管理 (ID 400-499)
-- ============================================
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort) VALUES
(400, 0,  '库存管理',   1, '/inventory',         NULL,                       'Box',          NULL,                    400),
(401, 400,'库存总览',   2, '/inventory',         'inventory/index',          NULL,           'inventory:list',        401),
(402, 401,'库存调整',   3, NULL,                  NULL,                       NULL,           'inventory:adjust',      402),
(403, 400,'库存流水',   2, '/inventory/transaction','inventory/transaction/index',NULL,        'inventory:transaction:list',403);

-- ============================================
-- 生产管理 (ID 500-599)
-- ============================================
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort) VALUES
(500, 0,  '生产管理',   1, '/production',         NULL,                      'Monitor',      NULL,                          500),
-- 生产工单
(501, 500,'生产工单',   2, '/production/work-order','production/work-order/index',NULL,       'production:work-order:list',   501),
(502, 501,'新增工单',   3, NULL,                  NULL,                       NULL,           'production:work-order:add',     502),
(503, 501,'编辑工单',   3, NULL,                  NULL,                       NULL,           'production:work-order:edit',    503),
(504, 501,'删除工单',   3, NULL,                  NULL,                       NULL,           'production:work-order:delete',  504),
(505, 501,'开工',       3, NULL,                  NULL,                       NULL,           'production:work-order:start',   505),
(506, 501,'完工',       3, NULL,                  NULL,                       NULL,           'production:work-order:complete',506),
(507, 501,'入库',       3, NULL,                  NULL,                       NULL,           'production:work-order:stock-in',507),
-- 报工
(508, 500,'报工管理',   2, '/production/report',  'production/report/index',  NULL,           'production:report:list',        508),
(509, 508,'新增报工',   3, NULL,                  NULL,                       NULL,           'production:report:add',         509),
(510, 508,'编辑报工',   3, NULL,                  NULL,                       NULL,           'production:report:edit',        510),
-- 质检
(511, 500,'质检管理',   2, '/production/qc',      'production/qc/index',      NULL,           'production:qc:list',            511),
(512, 511,'新增质检',   3, NULL,                  NULL,                       NULL,           'production:qc:add',             512),
(513, 511,'编辑质检',   3, NULL,                  NULL,                       NULL,           'production:qc:edit',            513),
(514, 511,'删除质检',   3, NULL,                  NULL,                       NULL,           'production:qc:delete',          514);

-- ============================================
-- 知识库 (ID 600-699)
-- ============================================
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort) VALUES
(600, 0,  '知识库',     1, '/knowledge',         NULL,                       'Reading',      NULL,                    600),
(601, 600,'文档管理',   2, '/knowledge',         'knowledge/index',          NULL,           'knowledge:doc:list',    601),
(602, 601,'上传文档',   3, NULL,                  NULL,                       NULL,           'knowledge:doc:upload',  602),
(603, 601,'删除文档',   3, NULL,                  NULL,                       NULL,           'knowledge:doc:delete',  603);

-- ============================================
-- AI助手 (ID 700-799)
-- ============================================
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort) VALUES
(700, 0,  'AI助手',     1, '/ai',                NULL,                       'ChatDotRound', NULL,                    700),
(701, 700,'AI对话',     2, '/ai',                'ai/index',                 NULL,           'ai:chat:list',          701),
(702, 701,'删除对话',   3, NULL,                  NULL,                       NULL,           'ai:chat:delete',        702);

-- ============================================
-- 角色-菜单分配
-- ============================================

-- 管理员 (role_id=1): 全部菜单
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu;

-- 销售 (role_id=2): 仪表盘 + 基础数据(客户) + 销售管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2,100),(2,101),                         -- 仪表盘
(2,200),(2,209),(2,210),(2,211),(2,212), -- 基础数据-客户
(2,300),(2,301),(2,302),(2,303),(2,304),(2,305), -- 销售订单
(2,306),(2,307),(2,308),(2,309);         -- 发货管理

-- 操作工 (role_id=3): 仪表盘 + 生产工单(查看+开工+完工+报工)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3,100),(3,101),                         -- 仪表盘
(3,500),(3,501),(3,505),(3,506),         -- 生产工单(查看+开工+完工)
(3,508),(3,509);                         -- 报工管理(查看+新增)

-- 运营 (role_id=5): 仪表盘 + 库存管理 + 知识库
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(5,100),(5,101),                         -- 仪表盘
(5,400),(5,401),(5,402),                 -- 库存
(5,600),(5,601),(5,602);                 -- 知识库

-- 采购 (role_id=6): 仪表盘 + 基础数据(产品+仓库)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6,100),(6,101),                         -- 仪表盘
(6,200),(6,201),(6,202),(6,203),(6,204), -- 产品
(6,213),(6,214),(6,215),(6,216);         -- 仓库

-- 质检员 (role_id=7): 仪表盘 + 质检管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(7,100),(7,101),                         -- 仪表盘
(7,500),(7,511),(7,512),(7,513),(7,514); -- 质检

-- 仓管员 (role_id=8): 仪表盘 + 库存管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(8,100),(8,101),                         -- 仪表盘
(8,400),(8,401),(8,402);                 -- 库存

-- 开发 (role_id=9): 仪表盘 + 系统管理 + 知识库 + AI
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(9,100),(9,101),                         -- 仪表盘
(9,1),(9,11),(9,12),(9,13),             -- 系统管理(用户+角色+菜单)
(9,14),(9,15),(9,16),                   -- 用户增删改
(9,17),(9,18),(9,19),                   -- 角色增删改
(9,20),(9,21),(9,22),                   -- 菜单增删改
(9,600),(9,601),(9,602),(9,603),         -- 知识库
(9,700),(9,701),(9,702);                 -- AI
