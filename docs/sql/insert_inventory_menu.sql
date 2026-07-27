-- 库存管理菜单
-- 先用这条排查：
-- SELECT id, parent_id, name, type, path, visible FROM sys_menu WHERE path LIKE '/inventory%' OR name = 'Inventory';

-- 1. 顶级目录
INSERT INTO sys_menu (parent_id, name, type, path, component, icon, permission, sort, visible, deleted, create_time, update_time)
VALUES (0, 'Inventory', 1, '/inventory', '', 'Box', '', 7, 1, 0, NOW(), NOW());

-- 2. 库存管理（前端路由 /inventory → views/inventory/Index.vue，权限需匹配 router meta.permission）
INSERT INTO sys_menu (parent_id, name, type, path, component, icon, permission, sort, visible, deleted, create_time, update_time)
VALUES ((SELECT id FROM sys_menu WHERE path = '/inventory' AND parent_id = 0), 'Stock', 2, '/inventory', 'inventory/Index', '', 'inventory:list', 1, 1, 0, NOW(), NOW());

-- 3. 库存流水
INSERT INTO sys_menu (parent_id, name, type, path, component, icon, permission, sort, visible, deleted, create_time, update_time)
VALUES ((SELECT id FROM sys_menu WHERE path = '/inventory' AND parent_id = 0), 'Transaction', 2, '/inventory/transaction', 'inventory/Index', '', 'inventory:list', 2, 1, 0, NOW(), NOW());

-- 4. 授权给 admin（role_id 一般是 1）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE path LIKE '/inventory%';
