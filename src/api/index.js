import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截
api.interceptors.request.use(config => {
  const token = sessionStorage.getItem('token') || localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截
api.interceptors.response.use(
  res => res.data,
  err => {
    ElMessage.error(err.response?.data?.message || '请求失败')
    if (err.response?.status === 401) {
      localStorage.clear()
      sessionStorage.clear()
      location.hash = '#/login'
    }
    return Promise.reject(err)
  }
)

export default api
