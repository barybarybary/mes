import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api'

function getStorage() {
  if (sessionStorage.getItem('token')) return sessionStorage
  return localStorage
}

export const useUserStore = defineStore('user', () => {
  const storage = getStorage()
  const token = ref(storage.getItem('token') || '')
  const user = ref(JSON.parse(storage.getItem('user') || 'null'))
  const roles = ref(JSON.parse(storage.getItem('roles') || '[]'))
  const permissions = ref(JSON.parse(storage.getItem('permissions') || '[]'))

  async function login(username, password, captchaKey, captchaAnswer, rememberMe = false) {
    const res = await api.post('/auth/login', { username, password, captchaKey, captchaAnswer })
    const { token: t, user: u, roles: r, permissions: p } = res.data

    const store = rememberMe ? localStorage : sessionStorage

    token.value = t
    user.value = u
    roles.value = r || []
    permissions.value = p || []

    store.setItem('token', t)
    store.setItem('user', JSON.stringify(u))
    store.setItem('roles', JSON.stringify(r || []))
    store.setItem('permissions', JSON.stringify(p || []))

    if (rememberMe) {
      localStorage.setItem('remember_me', 'true')
    } else {
      localStorage.removeItem('remember_me')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('roles')
      localStorage.removeItem('permissions')
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
    permissions.value = []

    const rememberMe = localStorage.getItem('remember_me')
    const rememberUsername = localStorage.getItem('remember_username')

    localStorage.clear()
    sessionStorage.clear()

    if (rememberMe) localStorage.setItem('remember_me', rememberMe)
    if (rememberUsername) localStorage.setItem('remember_username', rememberUsername)
  }

  return { token, user, roles, permissions, login, logout, isRemembered }
})
