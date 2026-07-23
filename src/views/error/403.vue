<template>
  <div class="min-h-screen bg-slate-100 dark:bg-slate-800 flex items-center justify-center">
    <div class="text-center">
      <div class="text-8xl font-extrabold text-slate-300 dark:text-slate-500 mb-4">403</div>
      <h1 class="text-2xl font-bold text-slate-700 dark:text-slate-600 mb-2">访问被拒绝</h1>
      <p class="text-slate-500 dark:text-slate-300 mb-4">您没有权限访问此页面</p>
      <p class="text-slate-400 dark:text-slate-300 text-sm mb-6">如需开通权限，请联系管理员</p>
      <div class="flex gap-3 justify-center">
        <el-button class="rounded-xl" @click="$router.back()">返回上一页</el-button>
        <el-button type="primary" class="rounded-xl" @click="$router.push('/dashboard')">返回首页</el-button>
        <el-button class="rounded-xl" @click="handleLogout">退出登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const $router = useRouter()

function handleLogout() {
  localStorage.clear()
  window.location.hash = '#/login'
}
</script>
