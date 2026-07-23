<template>
  <header class="bg-white border-b border-slate-200 sticky top-0 z-50">
    <div class="max-w-6xl mx-auto px-4 h-14 flex items-center justify-between">
      <!-- Logo -->
      <router-link to="/portal/home" class="flex items-center gap-2 text-sky-600 font-bold text-lg no-underline">
        <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-sky-400 to-blue-600 flex items-center justify-center text-white text-sm font-extrabold">ZY</div>
        造易商城
      </router-link>

      <!-- 导航链接 -->
      <nav class="hidden md:flex items-center gap-6 text-sm text-slate-600">
        <router-link to="/portal/home" class="hover:text-sky-500">首页</router-link>
        <router-link to="/portal/products" class="hover:text-sky-500">产品中心</router-link>
      </nav>

      <!-- 右侧 -->
      <div class="flex items-center gap-3">
        <router-link to="/portal/cart" class="relative p-2 text-slate-600 hover:text-sky-500">
          <el-icon :size="22"><ShoppingCart /></el-icon>
          <span v-if="cartCount > 0" class="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-xs rounded-full w-4 h-4 flex items-center justify-center">{{ cartCount }}</span>
        </router-link>
        <template v-if="customer">
          <el-dropdown trigger="click">
            <span class="flex items-center gap-1 cursor-pointer text-slate-700 text-sm">
              <el-icon :size="18"><User /></el-icon>
              {{ customer.contactName || customer.username }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/portal/orders')">我的订单</el-dropdown-item>
                <el-dropdown-item @click="$router.push('/portal/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout"><span class="text-red-500">退出登录</span></el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <router-link v-else to="/portal/login" class="text-sm text-sky-500 hover:text-sky-600">登录</router-link>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ShoppingCart, User } from '@element-plus/icons-vue'
import api from '@/api/portal'

const router = useRouter()
const cartCount = ref(0)

const customer = computed(() => {
  try { return JSON.parse(localStorage.getItem('portal_customer') || 'null') } catch { return null }
})

const token = computed(() => sessionStorage.getItem('portal_token') || localStorage.getItem('portal_token'))

onMounted(async () => {
  if (token.value) {
    try {
      const res = await api.get('/cart')
      const items = res.data || []
      cartCount.value = items.reduce((s, i) => s + i.quantity, 0)
    } catch { /* ignore */ }
  }
})

function handleLogout() {
  sessionStorage.removeItem('portal_token')
  localStorage.removeItem('portal_token')
  localStorage.removeItem('portal_customer')
  router.push('/portal/login')
}
</script>
