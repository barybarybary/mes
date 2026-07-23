<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-sky-50 to-blue-100 py-8">
    <div class="bg-white rounded-2xl shadow-xl p-8 w-full max-w-md mx-4">
      <div class="text-center mb-6">
        <h2 class="text-xl font-bold text-slate-800">注册账号</h2>
        <p class="text-sm text-slate-500 mt-1">加入造易商城</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名 *" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码 *" show-password size="large" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码 *" show-password size="large" />
        </el-form-item>
        <el-form-item prop="companyName">
          <el-input v-model="form.companyName" placeholder="公司名称" size="large" />
        </el-form-item>
        <el-form-item prop="contactName">
          <el-input v-model="form.contactName" placeholder="联系人" size="large" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" size="large" />
        </el-form-item>
        <el-button type="primary" size="large" class="w-full !rounded-lg" :loading="loading" @click="handleRegister">
          注 册
        </el-button>
      </el-form>

      <div class="mt-4 text-center text-sm text-slate-500">
        已有账号？<router-link to="/portal/login" class="text-sky-500 hover:text-sky-600">立即登录</router-link>
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
const form = reactive({
  username: '', password: '', confirmPassword: '',
  companyName: '', contactName: '', phone: '', email: '', address: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次密码不一致'))
  else callback()
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }],
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

async function handleRegister() {
  loading.value = true
  try {
    const payload = { ...form }
    delete payload.confirmPassword
    const res = await api.post('/register', payload)
    if (res.code === 200 && res.data) {
      const { token, customer } = res.data
      sessionStorage.setItem('portal_token', token)
      localStorage.setItem('portal_token', token)
      localStorage.setItem('portal_customer', JSON.stringify(customer))
      ElMessage.success('注册成功！')
      router.push('/portal/home')
    }
  } catch { /* api 拦截器已提示 */ }
  finally { loading.value = false }
}
</script>
