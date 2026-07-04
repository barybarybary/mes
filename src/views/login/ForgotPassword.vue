<template>
  <div class="h-screen w-full relative overflow-hidden bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex flex-col">
    <!-- 背景装饰 -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -right-40 w-96 h-96 rounded-full bg-gradient-to-br from-amber-500/20 to-orange-600/10 blur-3xl animate-pulse"></div>
      <div class="absolute -bottom-40 -left-40 w-96 h-96 rounded-full bg-gradient-to-tr from-yellow-500/20 to-red-600/10 blur-3xl animate-pulse" style="animation-delay: 2s;"></div>
    </div>
    <div class="absolute inset-0 opacity-[0.03] pointer-events-none" style="background-image: linear-gradient(rgba(255,255,255,0.1) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.1) 1px, transparent 1px); background-size: 40px 40px;"></div>

    <div class="relative z-10 flex-1 flex items-center justify-center px-4 py-6 overflow-y-auto">
      <div class="w-full max-w-5xl flex flex-col lg:flex-row items-center gap-8 lg:gap-12">

        <!-- 左侧 -->
        <div class="lg:w-1/2 flex flex-col justify-center text-white space-y-8 px-4">
          <div class="flex items-center gap-4">
            <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-amber-400 to-orange-600 flex items-center justify-center shadow-lg shadow-amber-500/30">
              <span class="text-white text-xl font-extrabold">ZY</span>
            </div>
            <div>
              <h1 class="text-2xl font-bold tracking-tight">造易 ZaoYi</h1>
              <p class="text-sm text-slate-400">制造变容易</p>
            </div>
          </div>
          <h2 class="text-4xl lg:text-5xl font-bold leading-tight">
            <span class="bg-gradient-to-r from-amber-400 to-orange-600 bg-clip-text text-transparent">重置密码</span>
          </h2>
          <div class="space-y-4">
            <div class="flex gap-3 items-start">
              <div class="w-8 h-8 rounded-full bg-amber-500/20 flex items-center justify-center shrink-0 text-amber-400 font-bold text-sm">1</div>
              <div><strong class="text-white">填写邮箱</strong><p class="text-sm text-slate-400">输入注册时使用的邮箱</p></div>
            </div>
            <div class="flex gap-3 items-start">
              <div class="w-8 h-8 rounded-full bg-amber-500/20 flex items-center justify-center shrink-0 text-amber-400 font-bold text-sm">2</div>
              <div><strong class="text-white">验证身份</strong><p class="text-sm text-slate-400">输入图形验证码后获取邮箱验证码</p></div>
            </div>
            <div class="flex gap-3 items-start">
              <div class="w-8 h-8 rounded-full bg-amber-500/20 flex items-center justify-center shrink-0 text-amber-400 font-bold text-sm">3</div>
              <div><strong class="text-white">设置新密码</strong><p class="text-sm text-slate-400">输入新的登录密码</p></div>
            </div>
          </div>
        </div>

        <!-- 右侧表单 -->
        <div class="lg:w-1/2 flex items-center justify-center">
          <div class="w-full max-w-md bg-white/95 backdrop-blur-xl rounded-3xl shadow-2xl shadow-black/20 p-8 lg:p-10">
            <div class="text-center mb-8">
              <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-amber-500 to-orange-600 mb-4 shadow-lg shadow-amber-500/30">
                <el-icon color="white" :size="32"><Lock /></el-icon>
              </div>
              <h2 class="text-2xl font-bold text-slate-800">忘记密码</h2>
              <p class="text-sm text-slate-500 mt-2">通过邮箱验证重置密码</p>
            </div>

            <el-form :model="form" :rules="rules" ref="formRef" class="space-y-4">
              <!-- 邮箱 -->
              <el-form-item prop="email">
                <el-input v-model="form.email" placeholder="请输入注册邮箱" size="large" class="login-input">
                  <template #prefix><el-icon class="text-slate-400"><Message /></el-icon></template>
                </el-input>
              </el-form-item>

              <!-- 图形验证码 -->
              <el-form-item prop="imageCaptcha">
                <div class="flex gap-2 w-full">
                  <el-input v-model="form.imageCaptcha" placeholder="图形验证码" size="large" class="login-input flex-1" maxlength="6">
                    <template #prefix><el-icon class="text-slate-400"><Picture /></el-icon></template>
                  </el-input>
                  <div
                    class="h-10 w-[100px] rounded-xl cursor-pointer border border-gray-200 hover:border-amber-400 active:scale-95 transition-all shrink-0 flex items-center justify-center bg-gray-50"
                    @click="loadImageCaptcha" title="点击刷新"
                  >
                    <img v-if="captchaImage" :src="captchaImage" class="h-full w-full rounded-xl object-cover" alt="验证码" @error="captchaImage = ''" />
                    <el-icon v-else color="#9ca3af" :size="22"><RefreshRight /></el-icon>
                  </div>
                </div>
              </el-form-item>

              <!-- 邮箱验证码 -->
              <el-form-item prop="emailCode">
                <div class="flex gap-2 w-full">
                  <el-input v-model="form.emailCode" placeholder="邮箱验证码" size="large" class="login-input flex-1" maxlength="6">
                    <template #prefix><el-icon class="text-slate-400"><Key /></el-icon></template>
                  </el-input>
                  <button type="button" class="h-10 px-4 rounded-xl font-medium text-sm shrink-0 transition-all duration-200 border
                                 text-amber-600 bg-amber-50 border-amber-200 hover:bg-amber-100 active:scale-95
                                 disabled:opacity-50 disabled:cursor-not-allowed"
                          :disabled="sendCooldown > 0" @click="sendEmailCode">
                    {{ sendCooldown > 0 ? sendCooldown + 's 后重发' : '获取验证码' }}
                  </button>
                </div>
              </el-form-item>

              <!-- 新密码 -->
              <el-form-item prop="password">
                <el-input v-model="form.password" type="password" placeholder="新密码，至少 6 位" size="large" show-password class="login-input" @keyup.enter="handleReset">
                  <template #prefix><el-icon class="text-slate-400"><Lock /></el-icon></template>
                </el-input>
              </el-form-item>

              <!-- 确认密码 -->
              <el-form-item prop="confirmPassword">
                <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入新密码" size="large" show-password class="login-input" @keyup.enter="handleReset">
                  <template #prefix><el-icon class="text-slate-400"><Lock /></el-icon></template>
                </el-input>
              </el-form-item>

              <!-- 重置按钮 -->
              <el-form-item class="mb-0">
                <button type="button"
                  class="w-full h-12 rounded-xl font-semibold text-white
                         bg-gradient-to-r from-amber-500 to-orange-600
                         hover:from-amber-600 hover:to-orange-700
                         active:from-amber-700 active:to-orange-800
                         transition-all duration-300
                         flex items-center justify-center gap-2
                         disabled:opacity-60 disabled:cursor-not-allowed
                         shadow-lg shadow-amber-500/30 hover:shadow-xl hover:shadow-amber-500/40
                         transform hover:-translate-y-0.5 active:translate-y-0"
                  :disabled="loading" @click="handleReset">
                  <el-icon v-if="loading" class="animate-spin"><Loading /></el-icon>
                  <span>{{ loading ? '重置中...' : '重置密码' }}</span>
                </button>
              </el-form-item>
            </el-form>

            <p class="text-center mt-6 text-sm text-slate-500">
              想起密码了？<router-link to="/login" class="text-amber-600 hover:text-amber-700 font-medium">返回登录</router-link>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, Message, Picture, Key, Lock, RefreshRight } from '@element-plus/icons-vue'
import api from '@/api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const sendCooldown = ref(0)
const captchaImage = ref('')
const imageCaptchaKey = ref('')
let cooldownTimer = null

const form = reactive({ email: '', password: '', confirmPassword: '', imageCaptcha: '', emailCode: '' })

const validateConfirmPass = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  email: [
    { required: true, message: '请输入注册邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPass, trigger: 'blur' }
  ],
  imageCaptcha: [{ required: true, message: '请输入图形验证码', trigger: 'blur' }],
  emailCode: [{ required: true, message: '请输入邮箱验证码', trigger: 'blur' }]
}

onMounted(() => { loadImageCaptcha() })

async function loadImageCaptcha() {
  captchaImage.value = ''
  try {
    const url = `/api/captcha/image?_t=${Date.now()}`
    const res = await fetch(url)
    const contentType = res.headers.get('content-type') || ''

    if (contentType.includes('application/json')) {
      const data = await res.json()
      imageCaptchaKey.value = data.captchaKey || data.captcha_key || data.key || data.captchaId || ''
      const img = data.image || data.img || data.base64 || data.data || ''
      captchaImage.value = img.startsWith('data:') ? img : `data:image/png;base64,${img}`
    } else {
      const blob = await res.blob()
      captchaImage.value = URL.createObjectURL(blob)
    }
  } catch {
    captchaImage.value = ''
  }
}

async function sendEmailCode() {
  if (!form.email) { ElMessage.warning('请先填写邮箱'); return }
  if (!form.imageCaptcha) { ElMessage.warning('请先输入图形验证码'); return }

  try {
    const data = await api.post('/password/send-code', {
      email: form.email, captchaKey: imageCaptchaKey.value, captchaCode: form.imageCaptcha
    })
    if (data.code === 200) {
      ElMessage.success('验证码已发送，请查收邮件')
      sendCooldown.value = 60
      cooldownTimer = setInterval(() => {
        sendCooldown.value--
        if (sendCooldown.value <= 0) { clearInterval(cooldownTimer); cooldownTimer = null }
      }, 1000)
    } else {
      ElMessage.error(data.message || '发送失败')
      loadImageCaptcha()
      form.imageCaptcha = ''
    }
  } catch {
    ElMessage.error('网络错误，请稍后重试')
  }
}

async function handleReset() {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await api.post('/password/reset', {
      email: form.email, password: form.password, code: form.emailCode
    })
    if (data.code === 200) {
      ElMessage.success('密码重置成功！即将跳转登录...')
      setTimeout(() => router.push('/login'), 1500)
    } else {
      ElMessage.error(data.message || '重置失败')
    }
  } catch {
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
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
  box-shadow: 0 0 0 2px #f59e0b inset;
  background: #fff;
}
</style>
