<template>
  <div class="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-sky-50 to-blue-100 px-4">
    <!-- 返回首页 -->
    <router-link to="/portal/home" class="mb-6 text-sky-500 hover:text-sky-600 text-sm flex items-center gap-1">
      ← 返回首页
    </router-link>

    <div class="bg-white rounded-2xl shadow-xl p-8 w-full max-w-md">
      <div class="text-center mb-6">
        <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-sky-400 to-blue-600 flex items-center justify-center mx-auto mb-3 text-white text-lg font-extrabold">ZY</div>
        <h2 class="text-xl font-bold text-slate-800">造易商城</h2>
        <p class="text-sm text-slate-500 mt-1">客户登录</p>
      </div>

      <el-form :model="form" :rules="rules" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password size="large" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" class="w-full !rounded-lg" :loading="loading" @click="handleLogin">
          登 录
        </el-button>
      </el-form>

      <div class="mt-4 text-center text-sm text-slate-500">
        还没有账号？<router-link to="/portal/register" class="text-sky-500 hover:text-sky-600 font-medium">立即注册</router-link>
      </div>

      <!-- 测试账号 — 非常明显 -->
      <div style="margin-top: 20px; padding: 16px; background: #f0f9ff; border: 2px dashed #0ea5e9; border-radius: 12px;">
        <p style="margin: 0 0 10px 0; font-size: 13px; font-weight: 600; color: #0369a1; text-align: center;">🔑 测试账号（点击直接填入）</p>
        <div style="display: flex; gap: 8px;">
          <button
            v-for="acct in demoAccounts" :key="acct.user"
            type="button"
            @click="form.username = acct.user; form.password = acct.pwd"
            style="flex: 1; padding: 10px; border: 1px solid #bae6fd; border-radius: 8px; background: white; cursor: pointer; text-align: center;"
            @mouseover="$event.currentTarget.style.background='#e0f2fe'"
            @mouseout="$event.currentTarget.style.background='white'"
          >
            <div style="font-weight: 700; color: #0c4a6e; font-size: 14px;">{{ acct.user }}</div>
            <div style="color: #64748b; font-size: 12px; margin-top: 2px;">密码: {{ acct.pwd }}</div>
            <div style="color: #94a3b8; font-size: 11px; margin-top: 1px;">{{ acct.company }}</div>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api/portal'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const demoAccounts = [
  { user: 'customer1', pwd: '123456', company: '星辰科技' },
  { user: 'customer2', pwd: '123456', company: '远航制造' }
]

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
      ElMessage.success('欢迎回来，' + (customer.contactName || customer.username) + '！')
      router.push('/portal/home')
    }
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}
</script>
