<template>
  <div class="space-y-6">
    <!-- Hero 头部卡片 -->
    <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 p-5 sm:p-6">
      <div class="flex flex-col sm:flex-row items-center sm:items-start gap-5">
        <!-- 头像 -->
        <div class="w-20 h-20 sm:w-22 sm:h-22 rounded-full bg-gradient-to-br from-sky-400 to-blue-600 flex items-center justify-center text-white text-3xl font-bold shadow-lg shadow-sky-500/30 ring-4 ring-sky-50 shrink-0">
          {{ userAvatar }}
        </div>

        <!-- 中间信息 -->
        <div class="flex-1 min-w-0 text-center sm:text-left">
          <h2 class="text-xl sm:text-2xl font-bold text-slate-800 dark:text-slate-200">
            {{ displayNickname || user?.username || '未设置' }}
          </h2>
          <div class="flex flex-wrap items-center justify-center sm:justify-start gap-x-4 gap-y-1 mt-2 text-sm text-slate-500 dark:text-slate-300">
            <span class="inline-flex items-center gap-1.5">
              <el-icon :size="14" class="text-slate-400 dark:text-slate-300"><User /></el-icon>
              {{ user?.username || '-' }}
            </span>
            <span v-if="user?.email" class="inline-flex items-center gap-1.5">
              <el-icon :size="14" class="text-slate-400 dark:text-slate-300"><Message /></el-icon>
              {{ user.email }}
            </span>
            <span v-if="user?.phone" class="inline-flex items-center gap-1.5">
              <el-icon :size="14" class="text-slate-400 dark:text-slate-300"><Phone /></el-icon>
              {{ user.phone }}
            </span>
            <span v-if="roles.length" class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-sky-50 text-sky-600 border border-sky-100">
              {{ roles[0] }}
            </span>
          </div>
        </div>

        <!-- 右侧按钮 -->
        <el-button type="primary" class="rounded-xl shadow-lg shadow-sky-500/20 shrink-0" @click="openEditDialog">
          <el-icon class="mr-1.5"><Edit /></el-icon>编辑资料
        </el-button>
      </div>
    </div>

    <!-- 统计卡片行 -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-5 shadow-sm border border-slate-100 dark:border-slate-700 card-hover flex items-center gap-4">
        <div class="w-10 h-10 rounded-xl bg-violet-100 flex items-center justify-center shrink-0">
          <el-icon :size="20" color="#7c3aed"><Avatar /></el-icon>
        </div>
        <div>
          <p class="text-xl font-bold text-slate-800 dark:text-slate-200 leading-none">{{ roles.length }}</p>
          <p class="text-xs text-slate-400 dark:text-slate-300 mt-1.5">角色</p>
        </div>
      </div>
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-5 shadow-sm border border-slate-100 dark:border-slate-700 card-hover flex items-center gap-4">
        <div class="w-10 h-10 rounded-xl bg-emerald-100 flex items-center justify-center shrink-0">
          <el-icon :size="20" color="#059669"><Key /></el-icon>
        </div>
        <div>
          <p class="text-xl font-bold text-slate-800 dark:text-slate-200 leading-none">{{ permissions.length }}</p>
          <p class="text-xs text-slate-400 dark:text-slate-300 mt-1.5">权限</p>
        </div>
      </div>
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-5 shadow-sm border border-slate-100 dark:border-slate-700 card-hover flex items-center gap-4">
        <div class="w-10 h-10 rounded-xl bg-sky-100 flex items-center justify-center shrink-0">
          <el-icon :size="20" color="#0284c7"><Calendar /></el-icon>
        </div>
        <div>
          <p class="text-sm font-medium text-slate-700 dark:text-slate-600 leading-none truncate max-w-[100px]">{{ user?.createTime || '-' }}</p>
          <p class="text-xs text-slate-400 dark:text-slate-300 mt-1.5">注册时间</p>
        </div>
      </div>
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-5 shadow-sm border border-slate-100 dark:border-slate-700 card-hover flex items-center gap-4">
        <div class="w-10 h-10 rounded-xl bg-amber-100 flex items-center justify-center shrink-0">
          <el-icon :size="20" color="#d97706"><Clock /></el-icon>
        </div>
        <div>
          <p class="text-sm font-medium text-slate-700 dark:text-slate-600 leading-none truncate max-w-[100px]">{{ user?.lastLoginTime || '首次登录' }}</p>
          <p class="text-xs text-slate-400 dark:text-slate-300 mt-1.5">最后登录</p>
        </div>
      </div>
    </div>

    <!-- 主体：双栏布局 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左栏 -->
      <div class="lg:col-span-2 space-y-6">
        <!-- 基本资料 -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
          <div class="px-5 sm:px-6 py-4 border-b border-slate-50 dark:border-slate-800">
            <h3 class="text-base font-semibold text-slate-800 dark:text-slate-200 flex items-center gap-2.5">
              <el-icon :size="18" color="#64748b"><User /></el-icon>基本资料
            </h3>
          </div>
          <div class="p-5 sm:p-6">
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-5">
              <div class="flex items-center justify-between py-2 border-b border-slate-50 dark:border-slate-800">
                <span class="text-sm text-slate-400 dark:text-slate-300">用户名</span>
                <span class="text-sm font-medium text-slate-700 dark:text-slate-600">{{ user?.username || '-' }}</span>
              </div>
              <div class="flex items-center justify-between py-2 border-b border-slate-50 dark:border-slate-800">
                <span class="text-sm text-slate-400 dark:text-slate-300">昵称</span>
                <span class="text-sm font-medium text-slate-700 dark:text-slate-600">{{ displayNickname || '-' }}</span>
              </div>
              <div class="flex items-center justify-between py-2 border-b border-slate-50 dark:border-slate-800">
                <span class="text-sm text-slate-400 dark:text-slate-300">邮箱</span>
                <span class="text-sm font-medium text-slate-700 dark:text-slate-600 truncate max-w-[160px]">{{ user?.email || '-' }}</span>
              </div>
              <div class="flex items-center justify-between py-2 border-b border-slate-50 dark:border-slate-800">
                <span class="text-sm text-slate-400 dark:text-slate-300">手机号</span>
                <span class="text-sm font-medium text-slate-700 dark:text-slate-600">{{ user?.phone || '-' }}</span>
              </div>
              <div class="flex items-center justify-between py-2 border-b border-slate-50 dark:border-slate-800">
                <span class="text-sm text-slate-400 dark:text-slate-300">注册时间</span>
                <span class="text-sm font-medium text-slate-700 dark:text-slate-600">{{ user?.createTime || '-' }}</span>
              </div>
              <div class="flex items-center justify-between py-2 border-b border-slate-50 dark:border-slate-800">
                <span class="text-sm text-slate-400 dark:text-slate-300">最后登录</span>
                <span class="text-sm font-medium text-slate-700 dark:text-slate-600">{{ user?.lastLoginTime || '-' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 修改密码 -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
          <div class="px-5 sm:px-6 py-4 border-b border-slate-50 dark:border-slate-800">
            <h3 class="text-base font-semibold text-slate-800 dark:text-slate-200 flex items-center gap-2.5">
              <el-icon :size="18" color="#64748b"><Lock /></el-icon>修改密码
            </h3>
          </div>
          <div class="p-5 sm:p-6">
            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="90px" class="max-w-lg">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
              </el-form-item>
              <!-- 密码强度 -->
              <div v-if="passwordForm.newPassword" class="mb-4 ml-0 sm:ml-[90px]">
                <div class="flex items-center gap-2.5">
                  <div class="flex-1 h-1.5 rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
                    <div class="h-full rounded-full transition-all duration-300" :class="pwdStrength.barClass" :style="{ width: pwdStrength.width }"></div>
                  </div>
                  <span class="text-xs font-medium w-10" :class="pwdStrength.color">{{ pwdStrength.text }}</span>
                </div>
              </div>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" @keyup.enter="handleChangePassword" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="pwdLoading" @click="handleChangePassword" class="rounded-xl shadow-lg shadow-sky-500/20">确认修改</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>

      <!-- 右栏 -->
      <div class="space-y-6">
        <!-- 角色信息 -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
          <div class="px-5 py-4 border-b border-slate-50 dark:border-slate-800">
            <h3 class="text-base font-semibold text-slate-800 dark:text-slate-200 flex items-center gap-2.5">
              <el-icon :size="18" color="#64748b"><Avatar /></el-icon>角色信息
              <span class="text-xs text-slate-400 dark:text-slate-300 font-normal">{{ roles.length ? `(${roles.length})` : '' }}</span>
            </h3>
          </div>
          <div class="p-5">
            <template v-if="roles.length">
              <div class="flex flex-wrap gap-2">
                <span v-for="r in roles" :key="r"
                      class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium bg-sky-50 text-sky-700 border border-sky-100">
                  <span class="w-1.5 h-1.5 rounded-full bg-sky-400"></span>
                  {{ r }}
                </span>
              </div>
            </template>
            <p v-else class="text-sm text-slate-400 dark:text-slate-300 py-4 text-center">暂无角色</p>
          </div>
        </div>

        <!-- 权限列表 -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
          <div class="px-5 py-4 border-b border-slate-50 dark:border-slate-800">
            <h3 class="text-base font-semibold text-slate-800 dark:text-slate-200 flex items-center gap-2.5">
              <el-icon :size="18" color="#64748b"><Key /></el-icon>权限列表
              <span class="text-xs text-slate-400 dark:text-slate-300 font-normal">({{ permissions.length }})</span>
            </h3>
          </div>
          <div class="p-5">
            <template v-if="permissions.length">
              <div class="max-h-56 overflow-y-auto space-y-1">
                <div v-for="p in permissions" :key="p"
                     class="flex items-center gap-2.5 py-1.5 text-sm text-slate-600 dark:text-slate-600 hover:bg-slate-50 dark:hover:bg-slate-700 dark:bg-slate-900 rounded-lg px-2 -mx-2 transition-colors">
                  <el-icon :size="14" color="#10b981"><Select /></el-icon>
                  {{ p }}
                </div>
              </div>
            </template>
            <p v-else class="text-sm text-slate-400 dark:text-slate-300 py-4 text-center">暂无权限</p>
          </div>
        </div>

        <!-- 当前会话 -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
          <div class="px-5 py-4 border-b border-slate-50 dark:border-slate-800">
            <h3 class="text-base font-semibold text-slate-800 dark:text-slate-200 flex items-center gap-2.5">
              <el-icon :size="18" color="#64748b"><Monitor /></el-icon>当前会话
              <span class="w-2 h-2 rounded-full bg-emerald-400 shrink-0" title="在线"></span>
            </h3>
          </div>
          <div class="p-5">
            <div class="space-y-4 text-sm">
              <div class="flex items-center justify-between">
                <span class="text-slate-400 dark:text-slate-300">浏览器</span>
                <span class="font-medium text-slate-700 dark:text-slate-600">{{ browserInfo }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-slate-400 dark:text-slate-300">操作系统</span>
                <span class="font-medium text-slate-700 dark:text-slate-600">{{ osInfo }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-slate-400 dark:text-slate-300">登录 IP</span>
                <span class="font-medium text-slate-700 dark:text-slate-600">-</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑个人资料" width="480px" class="profile-dialog">
      <div class="flex justify-center mb-6">
        <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-sky-400 to-blue-600 flex items-center justify-center text-white text-2xl font-bold shadow-lg shadow-sky-500/30 ring-4 ring-sky-50">
          {{ userAvatar }}
        </div>
      </div>
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false" class="rounded-xl">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleSaveProfile" class="rounded-xl shadow-lg shadow-sky-500/20">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Message, Edit, Lock, Avatar, Key, Monitor, Calendar, Clock, Phone, Select } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const userStore = useUserStore()
const user = computed(() => userStore.user)
const roles = computed(() => {
  // 优先从 userStore.roles（登录时返回的角色数组）取
  const r = userStore.roles
  if (Array.isArray(r)) return r.map(item => item?.name || item)
  // 兼容旧格式
  const u = userStore.user
  if (!u) return []
  if (Array.isArray(u.roles)) return u.roles.map(item => item?.name || item)
  if (u.roleName) return [u.roleName]
  if (u.role) return Array.isArray(u.role) ? u.role.map(item => item?.name || item) : [u.role]
  return []
})

const permissions = computed(() => {
  const p = userStore.permissions
  if (Array.isArray(p)) {
    return p.map(item => {
      // 字符串直接返回
      if (typeof item === 'string') return item
      // 对象提取 name / code / permission 字段
      if (item && typeof item === 'object') return item.name || item.code || item.permission || String(item)
      return String(item)
    })
  }
  if (typeof p === 'string') {
    // 过滤掉异常值
    if (!p || p === 'undefined' || p === 'null') return []
    return p.split(',').map(s => s.trim()).filter(Boolean)
  }
  return []
})

// 过滤掉纯数字的昵称（后端可能把用户ID当昵称返回）
const isNumericNickname = (val) => {
  if (!val) return true
  return /^\d+$/.test(String(val))
}

const displayNickname = computed(() => {
  const nick = user.value?.nickname
  if (!nick || isNumericNickname(nick)) return ''
  return nick
})

const userAvatar = computed(() => {
  const name = displayNickname.value || user.value?.username || 'U'
  return name.charAt(0).toUpperCase()
})

// 浏览器/系统信息
const browserInfo = computed(() => {
  const ua = navigator.userAgent
  if (ua.includes('Edg')) return 'Edge'
  if (ua.includes('Chrome')) return 'Chrome'
  if (ua.includes('Firefox')) return 'Firefox'
  if (ua.includes('Safari')) return 'Safari'
  return '其他'
})

const osInfo = computed(() => {
  const ua = navigator.userAgent
  if (ua.includes('Windows')) return 'Windows'
  if (ua.includes('Mac')) return 'macOS'
  if (ua.includes('Linux')) return 'Linux'
  if (ua.includes('Android')) return 'Android'
  if (ua.includes('iPhone') || ua.includes('iPad')) return 'iOS'
  return '其他'
})

// 密码强度
const pwdStrength = computed(() => {
  const len = passwordForm.newPassword.length
  if (!len) return { width: '0%', text: '', color: '', barClass: '' }
  if (len < 6) return { width: '25%', text: '太短', color: 'text-red-500', barClass: 'bg-red-400' }
  if (len < 8) return { width: '50%', text: '一般', color: 'text-amber-500', barClass: 'bg-amber-400' }
  if (len < 10) return { width: '75%', text: '良好', color: 'text-sky-500', barClass: 'bg-sky-400' }
  return { width: '100%', text: '优秀', color: 'text-emerald-500', barClass: 'bg-emerald-400' }
})

// 编辑弹窗
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive({ nickname: '', email: '', phone: '' })

function openEditDialog() {
  if (user.value) {
    editForm.nickname = isNumericNickname(user.value.nickname) ? '' : (user.value.nickname || '')
    editForm.email = user.value.email || ''
    editForm.phone = user.value.phone || ''
  }
  editDialogVisible.value = true
}

onMounted(() => {
  if (user.value) {
    editForm.nickname = isNumericNickname(user.value.nickname) ? '' : (user.value.nickname || '')
    editForm.email = user.value.email || ''
    editForm.phone = user.value.phone || ''
  }
})

async function handleSaveProfile() {
  editLoading.value = true
  try {
    const res = await api.put('/user/profile', { ...editForm })
    if (res.code === 200) {
      const u = { ...userStore.user, ...editForm }
      userStore.user = u
      localStorage.setItem('user', JSON.stringify(u))
      ElMessage.success('资料更新成功')
      editDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch {
    ElMessage.error('网络错误')
  } finally {
    editLoading.value = false
  }
}

// 修改密码
const passwordFormRef = ref()
const pwdLoading = ref(false)
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleChangePassword() {
  await passwordFormRef.value.validate()
  pwdLoading.value = true
  try {
    const res = await api.put('/user/password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      setTimeout(() => {
        userStore.logout()
        window.location.hash = '#/login'
      }, 1500)
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch {
    ElMessage.error('网络错误')
  } finally {
    pwdLoading.value = false
  }
}
</script>

<style scoped>
.profile-dialog :deep(.el-dialog) {
  border-radius: 16px;
}
.profile-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f1f5f9;
}
.profile-dialog :deep(.el-dialog__body) {
  padding: 24px;
}
.profile-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px 20px;
  border-top: 1px solid #f1f5f9;
}
</style>
