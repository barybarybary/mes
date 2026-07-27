/**
 * 侧边栏菜单配置（前端静态驱动）
 * 权限由 userStore.permissions + roles 控制可见性
 */
export const menuConfig = [
  {
    id: 'dashboard',
    name: '首页概览',
    icon: 'DataAnalysis',
    path: '/dashboard'
  },
  {
    id: 'system',
    name: '系统管理',
    icon: 'Setting',
    children: [
      { name: '个人中心', path: '/system/profile' },
      { name: '用户管理', path: '/system/user', permission: 'system:user:list' },
      { name: '角色管理', path: '/system/role', permission: 'system:role:list' },

      { name: '系统设置', path: '/system/settings' }
    ]
  },
  {
    id: 'base',
    name: '基础数据',
    icon: 'Document',
    children: [
      { name: '产品管理', path: '/base/product', permission: 'base:product:list' },
      { name: '工序管理', path: '/base/process', permission: 'base:process:list' },
      { name: '客户管理', path: '/base/customer', permission: 'base:customer:list' },
      { name: '仓库管理', path: '/base/warehouse', permission: 'base:warehouse:list' },
      { name: '设备管理', path: '/base/equipment', permission: 'base:equipment:list' },
      { name: '供应商管理', path: '/base/supplier', permission: 'base:supplier:list' }
    ]
  },
  {
    id: 'sale',
    name: '销售管理',
    icon: 'Sell',
    children: [
      { name: '销售订单', path: '/sale/order', permission: 'sale:order:list' },
      { name: '发货管理', path: '/sale/delivery', permission: 'sale:delivery:list' }
    ]
  },
  {
    id: 'inventory',
    name: '库存管理',
    icon: 'Box',
    children: [
      { name: '库存总览', path: '/inventory', permission: 'inventory:stock:list' },
      { name: '库存流水', path: '/inventory/transaction', permission: 'inventory:transaction:list' }
    ]
  },
  {
    id: 'production',
    name: '生产管理',
    icon: 'Monitor',
    children: [
      { name: '生产工单', path: '/production/work-order', permission: 'production:work-order:list' },
      { name: '报工管理', path: '/production/report', permission: 'production:report:list' },
      { name: '质检管理', path: '/production/qc', permission: 'production:qc:list' },
      { name: '质检标准', path: '/production/qc-standard', permission: 'production:qc:list' },
      { name: '工序SOP', path: '/production/process-sop', permission: 'production:work-order:list' }
    ]
  },
  {
    id: 'knowledge',
    name: '知识库',
    icon: 'Reading',
    path: '/knowledge',
    permission: 'knowledge:doc:list'
  },
  {
    id: 'ai',
    name: 'AI助手',
    icon: 'ChatDotRound',
    path: '/ai',
    permission: 'ai:chat:list'
  }
]
