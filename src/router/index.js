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
      // 销售管理
      { path: '/sale/order', name: 'SaleOrder', component: () => import('@/views/sale/order/Index.vue'), meta: { title: '销售订单', permission: 'sale:order:list' }},
      { path: '/sale/delivery', name: 'SaleDelivery', component: () => import('@/views/sale/delivery/Index.vue'), meta: { title: '发货管理', permission: 'sale:delivery:list' }},
      // 生产
      { path: '/production/work-order', name: 'WorkOrder', component: () => import('@/views/production/work-order/Index.vue'), meta: { title: '生产工单', permission: 'production:work-order:list' }},
      { path: '/production/report', name: 'WorkReport', component: () => import('@/views/production/report/Index.vue'), meta: { title: '报工管理', permission: 'production:report:list' }},
      { path: '/production/qc', name: 'QcRecord', component: () => import('@/views/production/qc/Index.vue'), meta: { title: '质检管理', permission: 'production:qc:list' }},
      { path: '/production/qc-standard', name: 'QcStandard', component: () => import('@/views/production/qc-standard/Index.vue'), meta: { title: '质检标准', permission: 'production:qc:list' }},
      { path: '/production/process-sop', name: 'ProcessSop', component: () => import('@/views/production/process-sop/Index.vue'), meta: { title: '工序SOP', permission: 'production:work-order:list' }},
      // 库存
      { path: '/inventory', name: 'Inventory', component: () => import('@/views/inventory/Index.vue'), meta: { title: '库存管理', permission: 'inventory:stock:list' }},
      { path: '/inventory/transaction', name: 'InventoryTransaction', component: () => import('@/views/inventory/Transaction.vue'), meta: { title: '库存流水', permission: 'inventory:transaction:list' }},
      // 知识库
      { path: '/knowledge', name: 'Knowledge', component: () => import('@/views/knowledge/Index.vue'), meta: { title: '知识库', permission: 'knowledge:doc:list' }},
      // AI助手
      { path: '/ai', name: 'AiChat', component: () => import('@/views/ai/Index.vue'), meta: { title: 'AI助手', permission: 'ai:chat:list' }},
    ]
  },
  { path: '/login', name: 'Login', component: () => import('@/views/login/Index.vue'), meta: { title: '登录' }},
  { path: '/register', name: 'Register', component: () => import('@/views/login/Register.vue'), meta: { title: '注册' }},
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('@/views/login/ForgotPassword.vue'), meta: { title: '忘记密码' }},
  { path: '/403', name: 'Forbidden', component: () => import('@/views/error/403.vue'), meta: { title: '无权限' }},

  // ========== 客户门户路由 ==========
  { path: '/portal', name: 'PortalHome', component: () => import('@/views/portal/Home.vue'), meta: { title: '客户门户' }},
  { path: '/portal/login', name: 'PortalLogin', component: () => import('@/views/portal/Login.vue'), meta: { title: '客户登录' }},
  { path: '/portal/register', name: 'PortalRegister', component: () => import('@/views/portal/Register.vue'), meta: { title: '客户注册' }},
  { path: '/portal/products', name: 'PortalProducts', component: () => import('@/views/portal/ProductList.vue'), meta: { title: '产品列表' }},
  { path: '/portal/products/:id', name: 'PortalProductDetail', component: () => import('@/views/portal/ProductDetail.vue'), meta: { title: '产品详情' }},
  { path: '/portal/cart', name: 'PortalCart', component: () => import('@/views/portal/Cart.vue'), meta: { title: '购物车', portalAuth: true }},
  { path: '/portal/checkout', name: 'PortalCheckout', component: () => import('@/views/portal/Checkout.vue'), meta: { title: '确认订单', portalAuth: true }},
  { path: '/portal/orders', name: 'PortalOrders', component: () => import('@/views/portal/Orders.vue'), meta: { title: '我的订单', portalAuth: true }},
  { path: '/portal/orders/:id', name: 'PortalOrderDetail', component: () => import('@/views/portal/OrderDetail.vue'), meta: { title: '订单详情', portalAuth: true }},
  { path: '/portal/profile', name: 'PortalProfile', component: () => import('@/views/portal/Profile.vue'), meta: { title: '个人中心', portalAuth: true }},
  // 产品服务
  { path: '/portal/materials', name: 'PortalMaterials', component: () => import('@/views/portal/Materials.vue'), meta: { title: '原材料采购' }},
  { path: '/portal/semi-finished', name: 'PortalSemiFinished', component: () => import('@/views/portal/SemiFinished.vue'), meta: { title: '半成品定制' }},
  { path: '/portal/finished-products', name: 'PortalFinishedProducts', component: () => import('@/views/portal/FinishedProducts.vue'), meta: { title: '成品供应' }},
  { path: '/portal/parts-mall', name: 'PortalPartsMall', component: () => import('@/views/portal/PartsMall.vue'), meta: { title: '配件商城' }},
  { path: '/portal/mes-system', name: 'PortalMesSystem', component: () => import('@/views/portal/MesSystem.vue'), meta: { title: 'MES系统' }},
  // 关于我们
  { path: '/portal/about', name: 'PortalAbout', component: () => import('@/views/portal/About.vue'), meta: { title: '公司介绍' }},
  { path: '/portal/history', name: 'PortalHistory', component: () => import('@/views/portal/History.vue'), meta: { title: '发展历程' }},
  { path: '/portal/partners', name: 'PortalPartners', component: () => import('@/views/portal/Partners.vue'), meta: { title: '合作伙伴' }},
  { path: '/portal/news', name: 'PortalNews', component: () => import('@/views/portal/News.vue'), meta: { title: '新闻动态' }},
  { path: '/portal/careers', name: 'PortalCareers', component: () => import('@/views/portal/Careers.vue'), meta: { title: '加入我们' }},
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // ========== 客户门户路由守卫 ==========
  if (to.path.startsWith('/portal')) {
    const portalToken = sessionStorage.getItem('portal_token') || localStorage.getItem('portal_token')
    const portalPublic = ['/portal/login', '/portal/register', '/portal', '/portal/products', '/portal/materials', '/portal/semi-finished', '/portal/finished-products', '/portal/parts-mall', '/portal/mes-system', '/portal/about', '/portal/history', '/portal/partners', '/portal/news', '/portal/careers']
    const isPortalPublic = portalPublic.some(p => to.path === p || (p !== '/portal' && to.path.startsWith(p)))

    if (isPortalPublic) {
      next()
      return
    }
    if (!portalToken) {
      next('/portal/login')
      return
    }
    next()
    return
  }

  // ========== 后台管理路由守卫 ==========
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
