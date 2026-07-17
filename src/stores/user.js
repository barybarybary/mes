import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api'

// 后端菜单英文 → 中文翻译映射（递归遍历菜单树时自动替换）
const NAME_MAP = {
  'Dashboard': '首页',
  'System': '系统管理',
  'System Profile': '个人中心',
  'System User': '用户管理',
  'System Role': '角色管理',
  'System Menu': '菜单管理',
  'System Settings': '系统设置',
  'Base Data': '基础数据',
  'Product': '产品管理',
  'Process': '工序管理',
  'Customer': '客户管理',
  'Warehouse': '仓库管理',
  'Sales': '销售管理',
  'Sale Order': '销售订单',
  'Delivery': '发货管理',
  'Inventory': '库存管理',
  'Inventory Transaction': '库存流水',
  'Production': '生产管理',
  'Work Order': '生产工单',
  'Work Report': '报工管理',
  'QC Record': '质检管理',
  'Knowledge': '知识库',
  'BI': 'BI报表',
  'Business Overview': '经营分析',
  'Pivot Analysis': '多维交叉',
  'Alert Center': '预警中心',
  'Report Export': '报表导出',
  'AI Chat': 'AI助手',
  'Attendance': '考勤打卡'
}

function translateMenu(menu) {
  if (!menu) return menu
  if (NAME_MAP[menu.name]) {
    menu.name = NAME_MAP[menu.name]
  }
  if (menu.children?.length) {
    menu.children.forEach(translateMenu)
  }
  return menu
}

function getStorage() {
  // 如果用户勾选了"记住我"，优先使用 localStorage（跨会话）
  if (localStorage.getItem('remember_me') === 'true') return localStorage
  // 否则优先 sessionStorage（关闭浏览器即清除，更安全）
  if (sessionStorage.getItem('token')) return sessionStorage
  return localStorage
}

export const useUserStore = defineStore('user', () => {
  const storage = getStorage()
  const token = ref(storage.getItem('token') || '')
  const user = ref(JSON.parse(storage.getItem('user') || 'null'))
  const roles = ref(JSON.parse(storage.getItem('roles') || '[]'))
  const menus = ref((JSON.parse(storage.getItem('menus') || '[]')).map(translateMenu))
  const permissions = ref(JSON.parse(storage.getItem('permissions') || '[]'))

  async function login(username, password, captchaKey, captchaAnswer, rememberMe = false) {
    const res = await api.post('/auth/login', { username, password, captchaKey, captchaAnswer })
    const { token: t, user: u, roles: r, menus: m, permissions: p } = res.data

    // 清除两个存储中的旧数据
    sessionStorage.removeItem('token')
    localStorage.removeItem('token')

    token.value = t
    user.value = u
    roles.value = r || []
    menus.value = (m || []).map(translateMenu)
    permissions.value = p || []

    if (rememberMe) {
      // 记住我：localStorage 持久化 + sessionStorage 当前会话
      const save = function(s, k, v) { s.setItem(k, v) }
      ;[localStorage, sessionStorage].forEach(function(s) {
        save(s, 'token', t)
        save(s, 'user', JSON.stringify(u))
        save(s, 'roles', JSON.stringify(r || []))
        save(s, 'menus', JSON.stringify(m || []))
        save(s, 'permissions', JSON.stringify(p || []))
      })
      localStorage.setItem('remember_me', 'true')
    } else {
      // 不记住：只存 sessionStorage
      sessionStorage.setItem('token', t)
      sessionStorage.setItem('user', JSON.stringify(u))
      sessionStorage.setItem('roles', JSON.stringify(r || []))
      sessionStorage.setItem('menus', JSON.stringify(m || []))
      sessionStorage.setItem('permissions', JSON.stringify(p || []))
      localStorage.removeItem('remember_me')
    }
    return res
  }

  function isRemembered() {
    return localStorage.getItem('remember_me') === 'true' && !!(localStorage.getItem('token') || sessionStorage.getItem('token'))
  }

  function logout() {
    token.value = ''
    user.value = null
    roles.value = []
    menus.value = []
    permissions.value = []

    // 清除两个 storage 中的认证数据，但保留 remember_me 和 remember_username
    const rememberMe = localStorage.getItem('remember_me')
    const rememberUsername = localStorage.getItem('remember_username')

    localStorage.clear()
    sessionStorage.clear()

    if (rememberMe) localStorage.setItem('remember_me', rememberMe)
    if (rememberUsername) localStorage.setItem('remember_username', rememberUsername)
  }

  return { token, user, roles, menus, permissions, login, logout, isRemembered }
})
