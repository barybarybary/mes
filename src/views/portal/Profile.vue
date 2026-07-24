<template>
  <div class="min-h-screen bg-slate-50 dark:bg-slate-900">
    <PortalNavbar />
    <div class="max-w-2xl mx-auto px-4 py-8">
      <h2 class="text-xl font-bold text-slate-800 dark:text-slate-200 mb-6">个人中心</h2>

      <div class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border dark:border-slate-700 p-6 mb-6">
        <h3 class="font-medium text-slate-700 dark:text-slate-300 mb-4">基本信息</h3>
        <el-form :model="profile" label-width="80px" label-position="left">
          <el-form-item label="用户名"><el-input :model-value="profile.username" disabled /></el-form-item>
          <el-form-item label="公司名称"><el-input v-model="profile.companyName" /></el-form-item>
          <el-form-item label="联系人"><el-input v-model="profile.contactName" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="profile.phone" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="profile.email" /></el-form-item>
          <el-form-item label="地址"><el-input v-model="profile.address" /></el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存修改</el-button>
        </el-form>
      </div>

      <div class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border dark:border-slate-700 p-6">
        <h3 class="font-medium text-slate-700 dark:text-slate-300 mb-4">修改密码</h3>
        <el-form :model="pwdForm" label-width="80px" label-position="left">
          <el-form-item label="原密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
          <el-button type="warning" :loading="changing" @click="changePwd">修改密码</el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api/portal'
import PortalNavbar from './PortalNavbar.vue'

const profile = reactive({ username: '', companyName: '', contactName: '', phone: '', email: '', address: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '' })
const saving = ref(false)
const changing = ref(false)

onMounted(async () => {
  try {
    const res = await api.get('/profile')
    Object.assign(profile, res.data)
  } catch { /* ignore */ }
})

async function save() {
  saving.value = true
  try {
    await api.put('/profile', {
      companyName: profile.companyName, contactName: profile.contactName,
      phone: profile.phone, email: profile.email, address: profile.address
    })
    ElMessage.success('保存成功')
  } catch { /* ignore */ }
  finally { saving.value = false }
}

async function changePwd() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (pwdForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  changing.value = true
  try {
    await api.put('/password', pwdForm)
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
  } catch { /* ignore */ }
  finally { changing.value = false }
}
</script>
