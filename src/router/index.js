import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layout/Index.vue'),
    redirect: '/dashboard',
    children: [
      { path: '/dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/Index.vue'), meta: { title: '首页', permission: 'dashboard:view' }},
      // 考勤
      { path: '/attendance', name: 'Attendance', component: () => import('@/views/attendance/Index.vue'), meta: { title: '考勤打卡', permission: 'attendance:view' }},
      // 系统管理
      { path: '/system/profile', name: 'SystemProfile', component: () => import('@/views/system/profile/Index.vue'), meta: { title: '个人中心' }},
      { path: '/system/user', name: 'SystemUser', component: () => import('@/views/system/user/Index.vue'), meta: { title: '用户管理', permission: 'system:user:list' }},
      { path: '/system/role', name: 'SystemRole', component: () => import('@/views/system/role/Index.vue'), meta: { title: '角色管理', permission: 'system:role:list' }},
      { path: '/system/menu', name: 'SystemMenu', component: () => import('@/views/system/menu/Index.vue'), meta: { title: '菜单管理', permission: 'system:menu:list' }},
      { path: '/system/settings', name: 'SystemSettings', component: () => import('@/views/system/settings/Index.vue'), meta: { title: '系统设置' }},
      // 基础数据
      { path: '/base/product', name: 'BaseProduct', component: () => import('@/views/base/product/Index.vue'), meta: { title: '产品管理', permission: 'base:product:list' }},
      { path: '/base/process', name: 'BaseProcess', component: () => import('@/views/base/process/Index.vue'), meta: { title: '工序管理', permission: 'base:process:list' }},
      { path: '/base/customer', name: 'BaseCustomer', component: () => import('@/views/base/customer/Index.vue'), meta: { title: '客户管理', permission: 'base:customer:list' }},
      { path: '/base/warehouse', name: 'BaseWarehouse', component: () => import('@/views/base/warehouse/Index.vue'), meta: { title: '仓库管理', permission: 'base:warehouse:list' }},
      // 销售
      { path: '/sale/order', name: 'SaleOrder', component: () => import('@/views/sale/order/Index.vue'), meta: { title: '销售订单', permission: 'sale:order:list' }},
      { path: '/sale/delivery', name: 'SaleDelivery', component: () => import('@/views/sale/delivery/Index.vue'), meta: { title: '发货管理', permission: 'sale:delivery:list' }},
      // 库存
      { path: '/inventory', name: 'Inventory', component: () => import('@/views/inventory/Index.vue'), meta: { title: '库存管理', permission: 'inventory:list' }},
      { path: '/inventory/transaction', name: 'InventoryTransaction', component: () => import('@/views/inventory/Transaction.vue'), meta: { title: '库存流水', permission: 'inventory:transaction:list' }},
      // 生产
      { path: '/production/work-order', name: 'WorkOrder', component: () => import('@/views/production/work-order/Index.vue'), meta: { title: '生产工单', permission: 'production:work-order:list' }},
      { path: '/production/report', name: 'WorkReport', component: () => import('@/views/production/report/Index.vue'), meta: { title: '报工管理', permission: 'production:report:list' }},
      { path: '/production/qc', name: 'QcRecord', component: () => import('@/views/production/qc/Index.vue'), meta: { title: '质检管理', permission: 'production:qc:list' }},
      // 知识库
      { path: '/knowledge', name: 'Knowledge', component: () => import('@/views/knowledge/Index.vue'), meta: { title: '知识库', permission: 'knowledge:doc:list' }},
      // BI报表
      { path: '/bi/overview', name: 'BiOverview', component: () => import('@/views/bi/overview/Index.vue'), meta: { title: '经营分析', permission: 'bi:view' }},
      { path: '/bi/pivot', name: 'BiPivot', component: () => import('@/views/bi/pivot/Index.vue'), meta: { title: '多维交叉', permission: 'bi:view' }},
      { path: '/bi/alert', name: 'BiAlert', component: () => import('@/views/bi/alert/Index.vue'), meta: { title: '预警中心', permission: 'bi:alert:manage' }},
      { path: '/bi/export', name: 'BiExport', component: () => import('@/views/bi/export/Index.vue'), meta: { title: '报表导出', permission: 'bi:export' }},
      // AI助手
      { path: '/ai', name: 'AiChat', component: () => import('@/views/ai/Index.vue'), meta: { title: 'AI助手', permission: 'ai:chat:list' }},
    ]
  },
  { path: '/login', name: 'Login', component: () => import('@/views/login/Index.vue'), meta: { title: '登录' }},
  { path: '/register', name: 'Register', component: () => import('@/views/login/Register.vue'), meta: { title: '注册' }},
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('@/views/login/ForgotPassword.vue'), meta: { title: '忘记密码' }},
  { path: '/403', name: 'Forbidden', component: () => import('@/views/error/403.vue'), meta: { title: '无权限' }},
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token') || localStorage.getItem('token')
  // 公开页面
  if (to.path === '/login' || to.path === '/register' || to.path === '/forgot-password' || to.path === '/403') {
    // 已登录且记住我 → 跳过登录页直接进系统
    if (to.path === '/login' && token && localStorage.getItem('remember_me') === 'true') {
      next('/')
      return
    }
    next()
    return
  }
  // 未登录
  if (!token) {
    next('/login')
    return
  }
  // 权限检查
  const requiredPerm = to.meta.permission
  if (requiredPerm) {
    const permissions = JSON.parse(sessionStorage.getItem('permissions') || localStorage.getItem('permissions') || '[]')
    const roles = JSON.parse(sessionStorage.getItem('roles') || localStorage.getItem('roles') || '[]')
    // 管理员跳过检查
    if (roles.some(r => r.code === 'admin')) {
      next()
      return
    }
    if (!permissions.includes(requiredPerm)) {
      next('/403')
      return
    }
  }
  next()
})

export default router
