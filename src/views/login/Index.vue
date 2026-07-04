<template>
  <div class="h-screen w-full relative overflow-hidden bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex flex-col">
    <!-- 背景装饰 -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -right-40 w-96 h-96 rounded-full bg-gradient-to-br from-sky-500/20 to-blue-600/10 blur-3xl animate-pulse"></div>
      <div class="absolute -bottom-40 -left-40 w-96 h-96 rounded-full bg-gradient-to-tr from-cyan-500/20 to-teal-600/10 blur-3xl animate-pulse" style="animation-delay: 2s;"></div>
      <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] rounded-full bg-gradient-to-br from-indigo-500/5 to-purple-600/5 blur-3xl"></div>
    </div>

    <!-- 网格背景 -->
    <div class="absolute inset-0 opacity-[0.03] pointer-events-none" style="background-image: linear-gradient(rgba(255,255,255,0.1) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.1) 1px, transparent 1px); background-size: 40px 40px;"></div>

    <div class="relative z-10 flex-1 flex items-center justify-center px-4 py-6 overflow-y-auto">
      <div class="w-full max-w-5xl flex flex-col lg:flex-row items-center gap-8 lg:gap-12">
        
        <!-- 左侧品牌区域 -->
        <div class="lg:w-1/2 flex flex-col justify-center text-white space-y-8 px-4">
          <!-- Logo -->
          <div class="flex items-center gap-4">
            <div class="w-14 h-14 rounded-2xl gradient-primary-br flex items-center justify-center shadow-lg shadow-primary">
              <span class="text-white text-xl font-extrabold">ZY</span>
            </div>
            <div>
              <h1 class="text-2xl font-bold tracking-tight">造易 ZaoYi</h1>
              <p class="text-sm text-slate-400">制造变容易</p>
            </div>
          </div>

          <!-- 主标题 -->
          <div class="space-y-4">
            <h2 class="text-4xl lg:text-5xl font-bold leading-tight">
              <span class="bg-gradient-to-r from-sky-400 to-blue-600 bg-clip-text text-transparent">
                造易 ZaoYi
              </span>
            </h2>
            <p class="text-lg text-slate-400 leading-relaxed">制造变容易</p>
          </div>

          <!-- 功能亮点 -->
          <div class="grid grid-cols-2 gap-4">
            <div class="flex items-start gap-3 p-4 rounded-xl bg-white/5 backdrop-blur-sm border border-white/10 hover:bg-white/10 transition-all duration-300">
              <div class="w-10 h-10 rounded-lg bg-sky-500/20 flex items-center justify-center shrink-0">
                <el-icon color="#38bdf8" :size="20"><DataAnalysis /></el-icon>
              </div>
              <div>
                <h3 class="font-medium text-sm">实时监控</h3>
                <p class="text-xs text-slate-400 mt-1">生产数据实时可视化</p>
              </div>
            </div>
            <div class="flex items-start gap-3 p-4 rounded-xl bg-white/5 backdrop-blur-sm border border-white/10 hover:bg-white/10 transition-all duration-300">
              <div class="w-10 h-10 rounded-lg bg-emerald-500/20 flex items-center justify-center shrink-0">
                <el-icon color="#34d399" :size="20"><Setting /></el-icon>
              </div>
              <div>
                <h3 class="font-medium text-sm">智能排产</h3>
                <p class="text-xs text-slate-400 mt-1">AI驱动生产调度</p>
              </div>
            </div>
            <div class="flex items-start gap-3 p-4 rounded-xl bg-white/5 backdrop-blur-sm border border-white/10 hover:bg-white/10 transition-all duration-300">
              <div class="w-10 h-10 rounded-lg bg-sky-500/20 flex items-center justify-center shrink-0">
                <el-icon color="#fbbf24" :size="20"><Document /></el-icon>
              </div>
              <div>
                <h3 class="font-medium text-sm">质量追溯</h3>
                <p class="text-xs text-slate-400 mt-1">全流程质量管控</p>
              </div>
            </div>
            <div class="flex items-start gap-3 p-4 rounded-xl bg-white/5 backdrop-blur-sm border border-white/10 hover:bg-white/10 transition-all duration-300">
              <div class="w-10 h-10 rounded-lg bg-purple-500/20 flex items-center justify-center shrink-0">
                <el-icon color="#a78bfa" :size="20"><ChatDotRound /></el-icon>
              </div>
              <div>
                <h3 class="font-medium text-sm">AI助手</h3>
                <p class="text-xs text-slate-400 mt-1">智能问答与分析</p>
              </div>
            </div>
          </div>

          <!-- 底部信息 -->
          <div class="flex items-center gap-4 pt-4 text-sm text-slate-500">
            <span>v0.1.0</span>
            <span class="w-1 h-1 rounded-full bg-slate-600"></span>
            <span>安全连接</span>
            <span class="w-1 h-1 rounded-full bg-slate-600"></span>
            <span>© 2026 MES System</span>
          </div>
        </div>

        <!-- 右侧登录表单 -->
        <div class="lg:w-1/2 flex items-center justify-center">
          <div class="w-full max-w-md bg-white/95 backdrop-blur-xl rounded-3xl shadow-2xl shadow-black/20 p-8 lg:p-10">
            <!-- 登录标题 -->
            <div class="text-center mb-8">
              <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl gradient-primary-br mb-4 shadow-lg shadow-primary">
                <el-icon color="white" :size="32"><User /></el-icon>
              </div>
              <h2 class="text-2xl font-bold text-slate-800">账号登录</h2>
              <p class="text-sm text-slate-500 mt-2">请输入您的账号信息</p>
            </div>

            <!-- 登录表单 -->
            <el-form :model="form" :rules="rules" ref="formRef" class="space-y-5">
              <el-form-item prop="username">
                <el-input
                  v-model="form.username"
                  placeholder="请输入用户名"
                  size="large"
                  class="login-input"
                >
                  <template #prefix>
                    <el-icon class="text-slate-400"><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item prop="password">
                <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  show-password
                  class="login-input"
                  @keyup.enter="handleLogin"
                >
                  <template #prefix>
                    <el-icon class="text-slate-400"><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <!-- 数字验证码 -->
              <el-form-item prop="captchaAnswer">
                <div class="flex gap-2 w-full">
                  <el-input
                    v-model="form.captchaAnswer"
                    placeholder="验证码计算结果"
                    size="large"
                    class="login-input flex-1"
                    @keyup.enter="handleLogin"
                  >
                    <template #prefix>
                      <el-icon class="text-slate-400"><Key /></el-icon>
                    </template>
                  </el-input>
                  <div
                    class="h-10 px-4 rounded-xl cursor-pointer select-none flex items-center justify-center font-bold text-lg text-blue-700
                           bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-100 hover:from-blue-100 hover:to-indigo-100
                           active:scale-95 transition-all duration-200 shrink-0 min-w-[120px]"
                    @click="loadMathCaptcha"
                    title="点击刷新"
                  >{{ captchaQuestion }}</div>
                </div>
              </el-form-item>

              <div class="flex items-center justify-between text-sm">
                <label class="flex items-center gap-2 cursor-pointer text-slate-600">
                  <el-checkbox v-model="rememberMe" size="small" />
                  <span>记住我</span>
                </label>
                <router-link to="/forgot-password" class="text-sky-600 hover:text-sky-700 transition-colors">忘记密码？</router-link>
              </div>

              <!-- 注册入口 -->
              <div class="text-center text-sm text-slate-500">
                还没有账号？
                <router-link to="/register" class="text-sky-600 hover:text-sky-700 font-medium transition-colors">立即注册</router-link>
              </div>

              <el-form-item class="mb-0">
                <button
                  type="button"
                  class="w-full h-12 rounded-xl font-semibold text-white
                         gradient-primary
                         hover:brightness-110
                         active:brightness-90
                         transition-all duration-300
                         flex items-center justify-center gap-2
                         disabled:opacity-60 disabled:cursor-not-allowed
                         shadow-lg shadow-primary hover:shadow-xl
                         transform hover:-translate-y-0.5 active:translate-y-0"
                  :disabled="loading"
                  @click="handleLogin"
                >
                  <el-icon v-if="loading" class="animate-spin"><Loading /></el-icon>
                  <span>{{ loading ? '登录中...' : '登 录' }}</span>
                </button>
              </el-form-item>
            </el-form>

            <!-- 默认账号提示 -->
            <div class="mt-6 p-4 rounded-xl bg-slate-50 border border-slate-100">
              <div class="flex items-center gap-2 text-sm text-slate-500">
                <el-icon color="#64748b" :size="16"><InfoFilled /></el-icon>
                <span>测试账号</span>
              </div>
              <div class="mt-2 flex items-center justify-between text-sm">
                <span class="text-slate-600">用户名：<code class="px-2 py-0.5 bg-slate-200/50 rounded text-slate-700">admin</code></span>
                <span class="text-slate-600">密码：<code class="px-2 py-0.5 bg-slate-200/50 rounded text-slate-700">admin123</code></span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Loading, Key } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const rememberMe = ref(false)
const captchaQuestion = ref('加载中...')
const captchaKey = ref('')

const form = reactive({ username: '', password: '', captchaAnswer: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaAnswer: [{ required: true, message: '请输入验证码计算结果', trigger: 'blur' }]
}

onMounted(() => {
  // 回填记住的用户名
  const savedUsername = localStorage.getItem('remember_username')
  if (savedUsername) {
    form.username = savedUsername
    rememberMe.value = true
  }
  loadMathCaptcha()
})

async function loadMathCaptcha() {
  try {
    const res = await fetch('/api/captcha/math')
    const data = await res.json()
    captchaKey.value = data.captchaKey
    captchaQuestion.value = data.question
  } catch {
    captchaQuestion.value = '点击刷新'
  }
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form.username, form.password, captchaKey.value, form.captchaAnswer.trim(), rememberMe.value)

    if (rememberMe.value) {
      localStorage.setItem('remember_username', form.username)
    } else {
      localStorage.removeItem('remember_username')
      localStorage.removeItem('remember_me')
    }
    
    ElMessage({
      message: '登录成功，欢迎回来！',
      type: 'success',
      duration: 2000
    })
    setTimeout(() => {
      router.push('/')
    }, 500)
  } catch (e) {
    ElMessage.error(e?.message || '登录失败，请检查账号密码')
    loadMathCaptcha()
    form.captchaAnswer = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

:deep(.login-input .el-input__wrapper) {
  border-radius: 0.75rem;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  padding: 4px 16px;
  transition: all 0.3s ease;
  background: #fafafa;
}

:deep(.login-input .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #cbd5e1 inset;
  background: #fff;
}

:deep(.login-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #0ea5e9 inset;
  background: #fff;
}

:deep(.el-checkbox__inner) {
  border-radius: 4px;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}
</style>
