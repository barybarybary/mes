import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api/portal',
  timeout: 15000
})

// 请求拦截 — 使用 portal_token，不和后台 token 冲突
api.interceptors.request.use(config => {
  const token = sessionStorage.getItem('portal_token') || localStorage.getItem('portal_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截
api.interceptors.response.use(
  res => {
    const body = res.data
    if (body && body.code !== 200) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  err => {
    ElMessage.error(err.response?.data?.message || '请求失败')
    if (err.response?.status === 401) {
      // 清除门户 token，跳转门户登录页（不是后台登录页）
      sessionStorage.removeItem('portal_token')
      localStorage.removeItem('portal_token')
      location.hash = '#/portal/login'
    }
    return Promise.reject(err)
  }
)

export default api
