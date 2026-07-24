import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layout/Index.vue'),
    redirect: '/dashboard',
    children: [
      { path: '/dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/Index.vue'), meta: { title: '首页', permission: 'dashboard:view' }},
      // 系统管理
      { path: '/system/profile', name: 'SystemProfile', component: () => import('@/views/system/profile/Index.vue'), meta: { title: '个人中心' }},
      { path: '/system/user', name: 'SystemUser', component: () => import('@/views/system/user/Index.vue'), meta: { title: '用户管理', permission: 'system:user:list' }},
      { path: '/system/role', name: 'SystemRole', component: () => import('@/views/system/role/Index.vue'), meta: { title: '角色管理', permission: 'system:role:list' }},
      { path: '/system/settings', name: 'SystemSettings', component: () => import('@/views/system/settings/Index.vue'), meta: { title: '系统设置' }},
      // 基础数据
      { path: '/base/product', name: 'BaseProduct', component: () => import('@/views/base/product/Index.vue'), meta: { title: '产品管理', permission: 'base:product:list' }},
      { path: '/base/process', name: 'BaseProcess', component: () => import('@/views/base/process/Index.vue'), meta: { title: '工序管理', permission: 'base:process:list' }},
      { path: '/base/customer', name: 'BaseCustomer', component: () => import('@/views/base/customer/Index.vue'), meta: { title: '客户管理', permission: 'base:customer:list' }},
      { path: '/base/warehouse', name: 'BaseWarehouse', component: () => import('@/views/base/warehouse/Index.vue'), meta: { title: '仓库管理', permission: 'base:warehouse:list' }},
      { path: '/base/equipment', name: 'BaseEquipment', component: () => import('@/views/base/equipment/Index.vue'), meta: { title: '设备管理', permission: 'base:equipment:list' }},
      { path: '/base/supplier', name: 'BaseSupplier', component: () => import('@/views/base/supplier/Index.vue'), meta: { title: '供应商管理', permission: 'base:supplier:list' }},
      // 库存
      { path: '/inventory', name: 'Inventory', component: () => import('@/views/inventory/Index.vue'), meta: { title: '库存管理', permission: 'inventory:list' }},
      // 生产
      { path: '/production/work-order', name: 'WorkOrder', component: () => import('@/views/production/work-order/Index.vue'), meta: { title: '生产工单', permission: 'production:work-order:list' }},
      { path: '/production/report', name: 'WorkReport', component: () => import('@/views/production/report/Index.vue'), meta: { title: '报工管理', permission: 'production:report:list' }},
      { path: '/production/qc', name: 'QcRecord', component: () => import('@/views/production/qc/Index.vue'), meta: { title: '质检管理', permission: 'production:qc:list' }},
      // 知识库
      { path: '/knowledge', name: 'Knowledge', component: () => import('@/views/knowledge/Index.vue'), meta: { title: '知识库', permission: 'knowledge:doc:list' }},
      // AI助手
      { path: '/ai', name: 'AiChat', component: () => import('@/views/ai/Index.vue'), meta: { title: 'AI助手', permission: 'ai:chat:list' }},
      // 报表中心
      { path: '/report', name: 'ReportCenter', component: () => import('@/views/report/Index.vue'), meta: { title: '报表中心', permission: 'report:manage' }},
    ]
  },
  { path: '/login', name: 'Login', component: () => import('@/views/login/Index.vue'), meta: { title: '登录' }},
  { path: '/register', name: 'Register', component: () => import('@/views/login/Register.vue'), meta: { title: '注册' }},
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('@/views/login/ForgotPassword.vue'), meta: { title: '忘记密码' }},
  { path: '/403', name: 'Forbidden', component: () => import('@/views/error/403.vue'), meta: { title: '无权限' }},

  // ========== 客户门户（无后台布局） ==========
  { path: '/portal/login', name: 'PortalLogin', component: () => import('@/views/portal/Login.vue'), meta: { title: '客户登录' }},
  { path: '/portal/register', name: 'PortalRegister', component: () => import('@/views/portal/Register.vue'), meta: { title: '客户注册' }},
  { path: '/portal/home', name: 'PortalHome', component: () => import('@/views/portal/Home.vue'), meta: { title: '造易商城' }},
  { path: '/portal/products', name: 'PortalProducts', component: () => import('@/views/portal/Products.vue'), meta: { title: '产品中心' }},
  { path: '/portal/products/:id', name: 'PortalProductDetail', component: () => import('@/views/portal/ProductDetail.vue'), meta: { title: '产品详情' }},
  { path: '/portal/cart', name: 'PortalCart', component: () => import('@/views/portal/Cart.vue'), meta: { title: '购物车' }},
  { path: '/portal/orders', name: 'PortalOrders', component: () => import('@/views/portal/Orders.vue'), meta: { title: '我的订单' }},
  { path: '/portal/orders/:id', name: 'PortalOrderDetail', component: () => import('@/views/portal/OrderDetail.vue'), meta: { title: '订单详情' }},
  { path: '/portal/profile', name: 'PortalProfile', component: () => import('@/views/portal/Profile.vue'), meta: { title: '个人中心' }},
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token') || localStorage.getItem('token')
  const portalToken = sessionStorage.getItem('portal_token') || localStorage.getItem('portal_token')

  // ========== 后台管理页面 ==========
  const adminPublicPages = ['/login', '/register', '/forgot-password', '/403']
  if (adminPublicPages.includes(to.path)) {
    if (to.path === '/login' && token && localStorage.getItem('remember_me') === 'true') {
      next('/')
      return
    }
    next()
    return
  }

  // ========== 客户门户页面 ==========
  if (to.path.startsWith('/portal')) {
    const portalPublicPages = ['/portal/login', '/portal/register', '/portal/home', '/portal/products']
    const isPublic = portalPublicPages.some(p => to.path === p || (to.path.startsWith('/portal/products') && p.startsWith('/portal/products')))
    if (isPublic) {
      next()
      return
    }
    // 门户需登录的页面
    if (!portalToken) {
      next('/portal/login')
      return
    }
    next()
    return
  }

  // ========== 后台鉴权 ==========
  if (!token) {
    next('/login')
    return
  }
  const requiredPerm = to.meta.permission
  if (requiredPerm) {
    const permissions = JSON.parse(sessionStorage.getItem('permissions') || localStorage.getItem('permissions') || '[]')
    const roles = JSON.parse(sessionStorage.getItem('roles') || localStorage.getItem('roles') || '[]')
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
