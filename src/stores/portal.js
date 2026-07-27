import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api/portal'

function getStorage() {
  if (sessionStorage.getItem('portal_token')) return sessionStorage
  return localStorage
}

export const usePortalStore = defineStore('portal', () => {
  const storage = getStorage()
  const token = ref(storage.getItem('portal_token') || '')
  const customer = ref(JSON.parse(storage.getItem('portal_customer') || 'null'))
  const cartCount = ref(0)

  // --- Auth ---

  async function login(username, password) {
    const res = await api.post('/login', { username, password })
    const { token: t, customer: c } = res.data
    token.value = t
    customer.value = c
    sessionStorage.setItem('portal_token', t)
    sessionStorage.setItem('portal_customer', JSON.stringify(c))
    await fetchCartCount()
    return res
  }

  async function register(form) {
    const res = await api.post('/register', form)
    return res
  }

  function logout() {
    token.value = ''
    customer.value = null
    cartCount.value = 0
    sessionStorage.removeItem('portal_token')
    sessionStorage.removeItem('portal_customer')
    localStorage.removeItem('portal_token')
    localStorage.removeItem('portal_customer')
  }

  // --- Profile ---

  async function fetchCustomer() {
    const res = await api.get('/profile')
    customer.value = res.data
    sessionStorage.setItem('portal_customer', JSON.stringify(res.data))
    return res.data
  }

  async function updateProfile(data) {
    const res = await api.put('/profile', data)
    customer.value = res.data
    sessionStorage.setItem('portal_customer', JSON.stringify(res.data))
    return res
  }

  async function changePassword(oldPassword, newPassword) {
    return await api.put('/password', { oldPassword, newPassword })
  }

  // --- Cart ---

  async function fetchCartCount() {
    try {
      const res = await api.get('/cart')
      const items = res.data || []
      cartCount.value = items.reduce((sum, item) => sum + item.quantity, 0)
    } catch { /* ignore */
      cartCount.value = 0
    }
  }

  async function addToCart(productId, quantity = 1) {
    await api.post('/cart/add', null, { params: { productId, quantity } })
    await fetchCartCount()
  }

  return {
    token,
    customer,
    cartCount,
    login,
    register,
    logout,
    fetchCustomer,
    updateProfile,
    changePassword,
    fetchCartCount,
    addToCart
  }
})
