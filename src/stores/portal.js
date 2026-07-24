import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api/portal'

export const usePortalStore = defineStore('portal', () => {
  // ===== 状态 =====
  const cartCount = ref(0)
  const customer = ref(null)

  // ===== 计算属性 =====
  const token = computed(() =>
    sessionStorage.getItem('portal_token') || localStorage.getItem('portal_token')
  )

  const isLoggedIn = computed(() => !!token.value)

  // ===== 方法 =====

  /** 从 localStorage 恢复用户信息 */
  function loadCustomer() {
    try {
      customer.value = JSON.parse(localStorage.getItem('portal_customer') || 'null')
    } catch {
      customer.value = null
    }
  }

  /** 请求后端刷新购物车数量 */
  async function fetchCartCount() {
    if (!token.value) {
      cartCount.value = 0
      return
    }
    try {
      const res = await api.get('/cart')
      const items = res.data || []
      cartCount.value = items.reduce((s, i) => s + i.quantity, 0)
    } catch {
      /* 忽略，可能未登录 */
    }
  }

  /** 加购后手动增加角标（乐观更新） */
  function incrementCart(n) {
    cartCount.value += n
  }

  /** 下单/清空后重置 */
  function resetCart() {
    cartCount.value = 0
  }

  /** 退出登录 */
  function logout() {
    sessionStorage.removeItem('portal_token')
    localStorage.removeItem('portal_token')
    localStorage.removeItem('portal_customer')
    customer.value = null
    cartCount.value = 0
  }

  /** 保存登录信息 */
  function saveLogin(tokenVal, customerObj) {
    sessionStorage.setItem('portal_token', tokenVal)
    localStorage.setItem('portal_token', tokenVal)
    localStorage.setItem('portal_customer', JSON.stringify(customerObj))
    customer.value = customerObj
  }

  return { cartCount, customer, token, isLoggedIn, loadCustomer, fetchCartCount, incrementCart, resetCart, logout, saveLogin }
})
