<template>
  <div class="h-screen w-full relative overflow-hidden bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex flex-col">
    <!-- 背景装饰 -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -right-40 w-96 h-96 rounded-full bg-gradient-to-br from-emerald-500/20 to-teal-600/10 blur-3xl animate-pulse"></div>
      <div class="absolute -bottom-40 -left-40 w-96 h-96 rounded-full bg-gradient-to-tr from-green-500/20 to-cyan-600/10 blur-3xl animate-pulse" style="animation-delay: 2s;"></div>
    </div>
    <div class="absolute inset-0 opacity-[0.03] pointer-events-none" style="background-image: linear-gradient(rgba(255,255,255,0.1) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.1) 1px, transparent 1px); background-size: 40px 40px;"></div>

    <div class="relative z-10 flex-1 flex items-center justify-center px-4 py-6 overflow-y-auto">
      <div class="w-full max-w-5xl flex flex-col lg:flex-row items-center gap-8 lg:gap-12">

        <!-- 左侧 -->
        <div class="lg:w-1/2 flex flex-col justify-center text-white space-y-8 px-4">
          <div class="flex items-center gap-4">
            <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-emerald-400 to-teal-600 flex items-center justify-center shadow-lg shadow-emerald-500/30">
              <span class="text-white text-xl font-extrabold">ZY</span>
            </div>
            <div>
              <h1 class="text-2xl font-bold tracking-tight">造易 ZaoYi</h1>
              <p class="text-sm text-slate-400">制造变容易</p>
            </div>
          </div>
          <h2 class="text-4xl lg:text-5xl font-bold leading-tight">
            <span class="bg-gradient-to-r from-emerald-400 to-teal-600 bg-clip-text text-transparent">创建账号</span>
          </h2>
          <div class="space-y-4">
            <div class="flex gap-3 items-start">
              <div class="w-8 h-8 rounded-full bg-emerald-500/20 flex items-center justify-center shrink-0 text-emerald-400 font-bold text-sm">1</div>
              <div><strong class="text-white">设置用户名</strong><p class="text-sm text-slate-400">给自己起一个名字吧</p></div>
            </div>
            <div class="flex gap-3 items-start">
              <div class="w-8 h-8 rounded-full bg-emerald-500/20 flex items-center justify-center shrink-0 text-emerald-400 font-bold text-sm">2</div>
              <div><strong class="text-white">填写邮箱</strong><p class="text-sm text-slate-400">邮箱可用于登录和找回密码</p></div>
            </div>
            <div class="flex gap-3 items-start">
              <div class="w-8 h-8 rounded-full bg-emerald-500/20 flex items-center justify-center shrink-0 text-emerald-400 font-bold text-sm">3</div>
              <div><strong class="text-white">图形验证</strong><p class="text-sm text-slate-400">输入图片验证码后获取邮箱验证码</p></div>
            </div>
            <div class="flex gap-3 items-start">
              <div class="w-8 h-8 rounded-full bg-emerald-500/20 flex items-center justify-center shrink-0 text-emerald-400 font-bold text-sm">4</div>
              <div><strong class="text-white">设置密码</strong><p class="text-sm text-slate-400">完成注册即可登录</p></div>
            </div>
          </div>
        </div>

        <!-- 右侧表单 -->
        <div class="lg:w-1/2 flex items-center justify-center">
          <div class="w-full max-w-md bg-white/95 backdrop-blur-xl rounded-3xl shadow-2xl shadow-black/20 p-8 lg:p-10">
            <div class="text-center mb-8">
              <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-emerald-500 to-teal-600 mb-4 shadow-lg shadow-emerald-500/30">
                <el-icon color="white" :size="32"><UserFilled /></el-icon>
              </div>
              <h2 class="text-2xl font-bold text-slate-800">邮箱注册</h2>
              <p class="text-sm text-slate-500 mt-2">邮箱即账号，注册后直接登录</p>
            </div>

            <el-form :model="form" :rules="rules" ref="formRef" class="space-y-4">
              <!-- 用户名 -->
              <el-form-item prop="username">
                <el-input v-model="form.username" placeholder="请输入用户名" size="large" class="login-input">
                  <template #prefix><el-icon class="text-slate-400"><User /></el-icon></template>
                </el-input>
              </el-form-item>

              <!-- 昵称 -->
              <el-form-item prop="nickname">
                <el-input v-model="form.nickname" placeholder="请输入昵称（选填，默认同用户名）" size="large" class="login-input" maxlength="20">
                  <template #prefix><el-icon class="text-slate-400"><Edit /></el-icon></template>
                </el-input>
              </el-form-item>

              <!-- 角色选择 -->
              <el-form-item prop="roleId">
                <el-select v-model="form.roleId" placeholder="请选择角色" size="large" class="w-full" popper-class="register-role-popper">
                  <template #prefix><el-icon class="text-slate-400"><Avatar /></el-icon></template>
                  <el-option v-for="r in roleList" :key="r.id" :label="r.name" :value="r.id">
                    <span>{{ r.name }}</span>
                    <span class="text-xs text-slate-400 ml-2" v-if="r.description">{{ r.description }}</span>
                  </el-option>
                </el-select>
              </el-form-item>

              <!-- 邮箱 -->
              <el-form-item prop="email">
                <el-input v-model="form.email" placeholder="请输入邮箱" size="large" class="login-input">
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
                    class="h-10 w-[100px] rounded-xl cursor-pointer border border-gray-200 hover:border-emerald-400 active:scale-95 transition-all shrink-0 flex items-center justify-center bg-gray-50"
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
                                 text-emerald-600 bg-emerald-50 border-emerald-200 hover:bg-emerald-100 active:scale-95
                                 disabled:opacity-50 disabled:cursor-not-allowed"
                          :disabled="sendCooldown > 0" @click="sendEmailCode">
                    {{ sendCooldown > 0 ? sendCooldown + 's 后重发' : '获取验证码' }}
                  </button>
                </div>
              </el-form-item>

              <!-- 密码 -->
              <el-form-item prop="password">
                <el-input v-model="form.password" type="password" placeholder="密码至少 6 位" size="large" show-password class="login-input" @keyup.enter="handleRegister">
                  <template #prefix><el-icon class="text-slate-400"><Lock /></el-icon></template>
                </el-input>
              </el-form-item>

              <!-- 注册按钮 -->
              <el-form-item class="mb-0">
                <button type="button"
                  class="w-full h-12 rounded-xl font-semibold text-white
                         bg-gradient-to-r from-emerald-500 to-teal-600
                         hover:from-emerald-600 hover:to-teal-700
                         active:from-emerald-700 active:to-teal-800
                         transition-all duration-300
                         flex items-center justify-center gap-2
                         disabled:opacity-60 disabled:cursor-not-allowed
                         shadow-lg shadow-emerald-500/30 hover:shadow-xl hover:shadow-emerald-500/40
                         transform hover:-translate-y-0.5 active:translate-y-0"
                  :disabled="loading" @click="handleRegister">
                  <el-icon v-if="loading" class="animate-spin"><Loading /></el-icon>
                  <span>{{ loading ? '注册中...' : '注 册' }}</span>
                </button>
              </el-form-item>
            </el-form>

            <p class="text-center mt-6 text-sm text-slate-500">
              已有账号？<router-link to="/login" class="text-emerald-600 hover:text-emerald-700 font-medium">去登录</router-link>
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
import { Loading, Message, Picture, Key, Lock, User, Edit, Avatar, UserFilled, RefreshRight } from '@element-plus/icons-vue'
import api from '@/api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const sendCooldown = ref(0)
const captchaImage = ref('')
const imageCaptchaKey = ref('')
let cooldownTimer = null

const form = reactive({ username: '', nickname: '', roleId: null, email: '', password: '', imageCaptcha: '', emailCode: '' })
const roleList = ref([])
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名 2-20 位', trigger: 'blur' }
  ],
  nickname: [
    { max: 20, message: '昵称最多 20 个字符', trigger: 'blur' }
  ],
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  imageCaptcha: [{ required: true, message: '请输入图形验证码', trigger: 'blur' }],
  emailCode: [{ required: true, message: '请输入邮箱验证码', trigger: 'blur' }]
}

onMounted(() => { loadImageCaptcha(); loadRoles() })

async function loadRoles() {
  try {
    const res = await api.get('/system/role')
    roleList.value = res.data || []
  } catch { /* ignore */ }
}

async function loadImageCaptcha() {
  captchaImage.value = ''
  try {
    const url = `/api/captcha/image?_t=${Date.now()}`
    const res = await fetch(url)
    const contentType = res.headers.get('content-type') || ''

    if (contentType.includes('application/json')) {
      // 后端返回 JSON：{ captchaKey, image (Base64) }
      const data = await res.json()
      imageCaptchaKey.value = data.captchaKey || data.captcha_key || data.key || data.captchaId || ''
      const img = data.image || data.img || data.base64 || data.data || ''
      captchaImage.value = img.startsWith('data:') ? img : `data:image/png;base64,${img}`
    } else {
      // 后端直接返回图片二进制，创建 Blob URL
      const blob = await res.blob()
      captchaImage.value = URL.createObjectURL(blob)
      // captchaKey 后端通过 cookie/session 维护
    }
  } catch {
    captchaImage.value = ''
  }
}

async function sendEmailCode() {
  if (!form.email) { ElMessage.warning('请先填写邮箱'); return }
  if (!form.imageCaptcha) { ElMessage.warning('请先输入图形验证码'); return }

  try {
    const data = await api.post('/register/send-code', {
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
    ElMessage.error('网络错误')
  }
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await api.post('/register', {
      username: form.username, nickname: form.nickname, roleId: form.roleId, email: form.email, password: form.password, code: form.emailCode
    })
    if (data.code === 200) {
      ElMessage.success('注册成功！即将跳转登录...')
      setTimeout(() => router.push('/login'), 1500)
    } else {
      ElMessage.error(data.message || '注册失败')
    }
  } catch {
    ElMessage.error('网络错误')
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
  box-shadow: 0 0 0 2px #10b981 inset;
  background: #fff;
}
</style>
