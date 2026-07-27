<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <main class="flex-1 max-w-4xl mx-auto px-4 sm:px-6 py-8 w-full">
      <div class="flex items-center gap-2 text-sm text-slate-400 mb-6">
        <router-link to="/portal" class="hover:text-sky-500 transition-colors no-underline">首页</router-link>
        <el-icon :size="12"><ArrowRight /></el-icon>
        <span class="text-slate-600">个人中心</span>
      </div>

      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-2xl md:text-3xl font-bold text-slate-800">个人中心</h1>
          <p class="text-slate-500 mt-1">管理您的账户信息</p>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div class="portal-card p-6 hover:shadow-card-hover transition-all duration-300 hover:-translate-y-1">
          <div class="flex items-center gap-4">
            <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-sky-500 to-blue-600 flex items-center justify-center shadow-lg shadow-sky-500/25">
              <el-icon :size="28" color="white"><CollectionTag /></el-icon>
            </div>
            <div>
              <p class="text-sm text-slate-400">订单数量</p>
              <p class="text-2xl font-bold text-slate-800 mt-0.5">{{ stats.orderCount || 0 }}</p>
            </div>
          </div>
          <router-link to="/portal/orders" class="text-sm text-sky-500 hover:text-sky-600 flex items-center gap-1 mt-4 font-medium">
            查看全部订单
            <el-icon :size="14"><ArrowRight /></el-icon>
          </router-link>
        </div>

        <div class="portal-card p-6 hover:shadow-card-hover transition-all duration-300 hover:-translate-y-1">
          <div class="flex items-center gap-4">
            <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center shadow-lg shadow-emerald-500/25">
              <el-icon :size="28" color="white"><Goods /></el-icon>
            </div>
            <div>
              <p class="text-sm text-slate-400">进行中订单</p>
              <p class="text-2xl font-bold text-slate-800 mt-0.5">{{ stats.activeCount || 0 }}</p>
            </div>
          </div>
          <router-link to="/portal/orders?status=3" class="text-sm text-emerald-500 hover:text-emerald-600 flex items-center gap-1 mt-4 font-medium">
            查看进行中
            <el-icon :size="14"><ArrowRight /></el-icon>
          </router-link>
        </div>

        <div class="portal-card p-6 hover:shadow-card-hover transition-all duration-300 hover:-translate-y-1">
          <div class="flex items-center gap-4">
            <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-amber-500 to-orange-600 flex items-center justify-center shadow-lg shadow-amber-500/25">
              <el-icon :size="28" color="white"><ShoppingCart /></el-icon>
            </div>
            <div>
              <p class="text-sm text-slate-400">购物车</p>
              <p class="text-2xl font-bold text-slate-800 mt-0.5">{{ stats.cartCount || 0 }}</p>
            </div>
          </div>
          <router-link to="/portal/cart" class="text-sm text-amber-500 hover:text-amber-600 flex items-center gap-1 mt-4 font-medium">
            去购物车
            <el-icon :size="14"><ArrowRight /></el-icon>
          </router-link>
        </div>
      </div>

      <div class="portal-card p-6 md:p-8">
        <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
          <span class="w-1 h-6 bg-gradient-to-b from-sky-500 to-blue-600 rounded-full"></span>
          账户信息
        </h2>

        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="max-w-xl">
          <el-form-item label="公司名称" prop="companyName">
            <el-input v-model="form.companyName" placeholder="请输入公司名称" size="large" />
          </el-form-item>

          <el-form-item label="联系人" prop="contactName">
            <el-input v-model="form.contactName" placeholder="请输入联系人姓名" size="large" />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" size="large" />
          </el-form-item>

          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="请输入邮箱" size="large" />
          </el-form-item>

          <el-form-item label="地址">
            <el-input v-model="form.address" type="textarea" :rows="3" placeholder="请输入地址" />
          </el-form-item>

          <el-form-item>
            <button type="button" class="portal-btn-primary" :disabled="submitting" @click="handleSubmit">
              <el-icon v-if="submitting" class="animate-spin"><Loading /></el-icon>
              {{ submitting ? '保存中...' : '保存修改' }}
            </button>
          </el-form-item>
        </el-form>
      </div>

      <div class="portal-card p-6 md:p-8 mt-6">
        <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
          <span class="w-1 h-6 bg-gradient-to-b from-sky-500 to-blue-600 rounded-full"></span>
          修改密码
        </h2>

        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" class="max-w-xl">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" size="large" />
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" size="large" />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请确认新密码" size="large" />
          </el-form-item>

          <el-form-item>
            <button type="button" class="portal-btn-secondary" :disabled="passwordSubmitting" @click="handleChangePassword">
              <el-icon v-if="passwordSubmitting" class="animate-spin"><Loading /></el-icon>
              {{ passwordSubmitting ? '修改中...' : '修改密码' }}
            </button>
          </el-form-item>
        </el-form>
      </div>
    </main>

    <PortalFooter />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowRight, Loading, CollectionTag, Goods, ShoppingCart } from '@element-plus/icons-vue'
import api from '@/api/portal'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'

const form = reactive({
  companyName: '',
  contactName: '',
  phone: '',
  email: '',
  address: ''
})

const rules = {
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const formRef = ref()
const passwordFormRef = ref()
const submitting = ref(false)
const passwordSubmitting = ref(false)
const stats = ref({})

async function fetchProfile() {
  try {
    const res = await api.get('/profile')
    Object.assign(form, res.data || {})
  } catch (err) { console.error(err) }
}

async function fetchStats() {
  try {
    const res = await api.get('/profile/stats')
    stats.value = res.data || {}
  } catch (err) { console.error(err) }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await api.put('/profile', form)
      ElMessage.success('保存成功')
    } catch (err) { console.error(err) } finally {
      submitting.value = false
    }
  })
}

async function handleChangePassword() {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    passwordSubmitting.value = true
    try {
      await api.put('/profile/password', passwordForm)
      ElMessage.success('密码修改成功')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } catch (err) { console.error(err) } finally {
      passwordSubmitting.value = false
    }
  })
}

onMounted(() => {
  fetchProfile()
  fetchStats()
})
</script>
