<template>
  <div class="h-screen w-full flex bg-slate-50">
    <!-- Left: Brand -->
    <div class="hidden lg:flex lg:w-1/2 gradient-hero flex-col justify-center items-center text-white px-12 relative overflow-hidden">
      <div class="absolute inset-0 opacity-10" style="background-image: linear-gradient(rgba(255,255,255,0.2) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.2) 1px, transparent 1px); background-size: 60px 60px;"></div>
      <div class="relative z-10 text-center space-y-6">
        <div class="w-20 h-20 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center mx-auto">
          <span class="text-white text-3xl font-extrabold">ZY</span>
        </div>
        <h1 class="text-4xl font-bold">造易 ZaoYi</h1>
        <p class="text-xl text-sky-100">制造变容易</p>
        <p class="text-sky-200/80 text-sm max-w-sm">高品质制造，一站式采购平台<br>为制造企业提供优质原材料与配件</p>
      </div>
    </div>

    <!-- Right: Login form -->
    <div class="flex-1 flex items-center justify-center px-6">
      <div class="w-full max-w-sm">
        <div class="text-center mb-8 lg:hidden">
          <div class="w-14 h-14 rounded-xl gradient-primary flex items-center justify-center mx-auto mb-2">
            <span class="text-white text-xl font-extrabold">ZY</span>
          </div>
          <h2 class="text-xl font-bold text-slate-800">造易 ZaoYi</h2>
          <p class="text-sm text-slate-500">制造变容易</p>
        </div>

        <div class="text-center mb-8">
          <h3 class="text-2xl font-bold text-slate-800">客户登录</h3>
          <p class="text-sm text-slate-500 mt-1">欢迎回来，请登录您的账号</p>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              size="large"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>
          <el-form-item>
            <button
              type="button"
              class="w-full btn-primary h-11 text-base"
              :disabled="loading"
              @click="handleLogin"
            >
              <el-icon v-if="loading" class="animate-spin"><Loading /></el-icon>
              <span>{{ loading ? '登录中...' : '登 录' }}</span>
            </button>
          </el-form-item>
        </el-form>

        <div class="text-center text-sm text-slate-500">
          还没有账号？
          <router-link to="/portal/register" class="text-sky-500 hover:text-sky-600 font-medium no-underline">立即注册 →</router-link>
        </div>

        <!-- 测试账号提示 -->
        <div class="mt-6 p-4 rounded-xl bg-sky-50 border border-sky-100">
          <div class="flex items-center gap-2 text-sm text-sky-600 mb-2">
            <el-icon :size="16"><InfoFilled /></el-icon>
            <span>测试账号</span>
          </div>
          <div class="space-y-2 text-sm">
            <div class="flex items-center justify-between">
              <span class="text-slate-600">用户名：<code class="px-2 py-0.5 bg-white rounded text-sky-700">customer1</code></span>
              <span class="text-slate-600">密码：<code class="px-2 py-0.5 bg-white rounded text-sky-700">123456</code></span>
              <span class="text-xs text-slate-400">星辰科技</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-slate-600">用户名：<code class="px-2 py-0.5 bg-white rounded text-sky-700">customer2</code></span>
              <span class="text-slate-600">密码：<code class="px-2 py-0.5 bg-white rounded text-sky-700">123456</code></span>
              <span class="text-xs text-slate-400">远航制造</span>
            </div>
          </div>
        </div>

        <div class="mt-4 text-center">
          <router-link to="/portal" class="text-sm text-slate-400 hover:text-slate-600 no-underline">← 返回首页</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { usePortalStore } from '@/stores/portal'
import { ElMessage } from 'element-plus'
import { User, Lock, Loading } from '@element-plus/icons-vue'

const router = useRouter()
const portalStore = usePortalStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await portalStore.login(form.username, form.password)
    ElMessage.success('登录成功，欢迎回来！')
    router.push('/portal')
  } catch { /* ignore */
    // Error already shown by axios interceptor
  } finally {
    loading.value = false
  }
}
</script>
