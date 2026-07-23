<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-sky-50 to-blue-100">
    <div class="bg-white rounded-2xl shadow-xl p-8 w-full max-w-md mx-4">
      <div class="text-center mb-6">
        <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-sky-400 to-blue-600 flex items-center justify-center mx-auto mb-3 text-white text-lg font-extrabold">ZY</div>
        <h2 class="text-xl font-bold text-slate-800">造易商城</h2>
        <p class="text-sm text-slate-500 mt-1">客户登录</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password size="large" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" class="w-full !rounded-lg" :loading="loading" @click="handleLogin">
          登 录
        </el-button>
      </el-form>

      <div class="mt-4 text-center text-sm text-slate-500">
        还没有账号？<router-link to="/portal/register" class="text-sky-500 hover:text-sky-600">立即注册</router-link>
      </div>
      <div class="mt-3 text-center text-xs text-slate-400">
        测试账号: customer1 / 123456
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import api from '@/api/portal'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  loading.value = true
  try {
    const res = await api.post('/login', form)
    if (res.code === 200 && res.data) {
      const { token, customer } = res.data
      sessionStorage.setItem('portal_token', token)
      localStorage.setItem('portal_token', token)
      localStorage.setItem('portal_customer', JSON.stringify(customer))
      ElMessage.success(`欢迎回来，${customer.contactName || customer.username}！`)
      router.push('/portal/home')
    }
  } catch { /* api 拦截器已提示 */ }
  finally { loading.value = false }
}
</script>
