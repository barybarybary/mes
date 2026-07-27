<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-sky-50/40 to-blue-50/30 flex items-center justify-center px-4 py-10">
    <div class="w-full max-w-2xl mx-auto">
      <div class="text-center mb-8">
        <router-link to="/portal" class="inline-flex items-center gap-3 text-2xl font-bold text-slate-800 no-underline hover:opacity-80 transition-opacity">
          <div class="w-11 h-11 rounded-2xl bg-gradient-to-br from-sky-500 to-blue-600 flex items-center justify-center shadow-lg shadow-sky-500/25">
            <span class="text-white font-bold text-lg">M</span>
          </div>
          <span>MES 制造门户</span>
        </router-link>
      </div>

      <div class="portal-card p-8 md:p-10">
        <div class="text-center mb-8">
          <h1 class="text-2xl md:text-3xl font-bold text-slate-800">注册账户</h1>
          <p class="text-slate-500 mt-2">创建您的客户账户，开启订购之旅</p>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @submit.prevent="handleSubmit">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-x-6">
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

            <el-form-item label="登录账号" prop="username" class="md:col-span-2">
              <el-input v-model="form.username" placeholder="请设置登录账号" size="large">
                <template #prefix>
                  <el-icon color="#94a3b8"><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="请设置密码" size="large">
                <template #prefix>
                  <el-icon color="#94a3b8"><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" size="large">
                <template #prefix>
                  <el-icon color="#94a3b8"><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="公司地址" prop="address" class="md:col-span-2">
              <el-input v-model="form.address" type="textarea" :rows="3" placeholder="请输入公司地址" />
            </el-form-item>

            <el-form-item prop="agreed" class="md:col-span-2 mb-2">
              <el-checkbox v-model="form.agreed">
                我已阅读并同意
                <a href="#" class="text-sky-500 hover:text-sky-600 no-underline">服务条款</a>
                和
                <a href="#" class="text-sky-500 hover:text-sky-600 no-underline">隐私政策</a>
              </el-checkbox>
            </el-form-item>
          </div>

          <el-form-item class="mt-4">
            <button
              type="submit"
              class="portal-btn-primary w-full !text-base !py-4"
              :disabled="submitting"
            >
              <el-icon v-if="submitting" class="animate-spin"><Loading /></el-icon>
              {{ submitting ? '注册中...' : '立即注册' }}
            </button>
          </el-form-item>
        </el-form>

        <div class="text-center mt-6 text-sm text-slate-500">
          已有账户？
          <router-link to="/portal/login" class="text-sky-500 hover:text-sky-600 font-medium no-underline">
            立即登录
          </router-link>
        </div>
      </div>

      <p class="text-center text-slate-400 text-xs mt-8">
        © {{ new Date().getFullYear() }} MES 制造门户. 保留所有权利.
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Loading } from '@element-plus/icons-vue'
import api from '@/api/portal'

const router = useRouter()
const form = reactive({
  companyName: '',
  contactName: '',
  phone: '',
  email: '',
  username: '',
  password: '',
  confirmPassword: '',
  address: '',
  agreed: false
})

const rules = {
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  username: [{ required: true, message: '请设置登录账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  agreed: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请阅读并同意服务条款和隐私政策'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

const formRef = ref()
const submitting = ref(false)

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await api.post('/register', form)
      ElMessage.success('注册成功，请等待审核')
      setTimeout(() => router.push('/portal/login'), 1500)
    } catch (err) {
        console.error(err)
      } finally {
      submitting.value = false
    }
  })
}
</script>
