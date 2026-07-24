<template>
  <div class="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-sky-50 to-blue-100 dark:from-slate-900 dark:to-slate-800 px-4">
    <!-- 返回首页 -->
    <router-link to="/portal/home" class="mb-6 text-sky-500 hover:text-sky-600 text-sm flex items-center gap-1">
      ← 返回首页
    </router-link>

    <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-xl p-8 w-full max-w-md">
      <div class="text-center mb-6">
        <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-sky-400 to-blue-600 flex items-center justify-center mx-auto mb-3 text-white text-lg font-extrabold">ZY</div>
        <h2 class="text-xl font-bold text-slate-800 dark:text-slate-200">造易商城</h2>
        <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">客户登录</p>
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

      <div class="mt-4 text-center text-sm text-slate-500 dark:text-slate-400">
        还没有账号？<router-link to="/portal/register" class="text-sky-500 hover:text-sky-600 font-medium">立即注册</router-link>
      </div>

      <!-- 测试账号 -->
      <div class="mt-5 p-4 bg-sky-50 dark:bg-sky-900/30 border-2 border-dashed border-sky-300 dark:border-sky-700 rounded-xl">
        <p class="mb-2.5 text-xs font-semibold text-sky-700 dark:text-sky-300 text-center">🔑 测试账号（点击直接填入）</p>
        <div class="flex gap-2">
          <button
            v-for="acct in demoAccounts" :key="acct.user"
            type="button"
            @click="form.username = acct.user; form.password = acct.pwd"
            class="flex-1 p-2.5 border border-sky-200 dark:border-sky-700 rounded-lg bg-white dark:bg-slate-700 cursor-pointer text-center hover:bg-sky-50 dark:hover:bg-slate-600 transition"
          >
            <div class="font-bold text-sky-800 dark:text-sky-200 text-sm">{{ acct.user }}</div>
            <div class="text-slate-500 dark:text-slate-400 text-xs mt-0.5">密码: {{ acct.pwd }}</div>
            <div class="text-slate-400 dark:text-slate-500 text-[11px] mt-0.5">{{ acct.company }}</div>
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
import { usePortalStore } from '@/stores/portal'

const router = useRouter()
const portalStore = usePortalStore()
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
      portalStore.saveLogin(token, customer)
      portalStore.fetchCartCount()
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
