-- 恢复库存管理菜单
UPDATE sys_menu SET visible = 1 WHERE path LIKE '/inventory%';
