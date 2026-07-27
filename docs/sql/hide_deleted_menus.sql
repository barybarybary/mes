-- 隐藏已删除的 BI/报表中心菜单
UPDATE sys_menu SET visible = 0 WHERE path LIKE '/bi%';
UPDATE sys_menu SET visible = 0 WHERE path LIKE '/report%';

-- 同时也隐藏其他已删除模块的菜单（如果有的话）
-- 考勤
UPDATE sys_menu SET visible = 0 WHERE path LIKE '/attendance%';
-- 库存（已恢复，如需隐藏请取消注释）
-- UPDATE sys_menu SET visible = 0 WHERE path LIKE '/inventory%';
-- 销售（注意不要误伤 portal 的订单）
UPDATE sys_menu SET visible = 0 WHERE path IN ('/sale', '/sale/order', '/sale/delivery');
