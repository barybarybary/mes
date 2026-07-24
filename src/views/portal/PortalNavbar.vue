<template>
  <header class="bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-700 sticky top-0 z-50">
    <div class="max-w-6xl mx-auto px-4 h-14 flex items-center justify-between">
      <!-- Logo -->
      <router-link to="/portal/home" class="flex items-center gap-2 text-sky-600 font-bold text-lg no-underline">
        <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-sky-400 to-blue-600 flex items-center justify-center text-white text-sm font-extrabold">ZY</div>
        造易商城
      </router-link>

      <!-- 导航链接 -->
      <nav class="hidden md:flex items-center gap-6 text-sm text-slate-600 dark:text-slate-300">
        <router-link to="/portal/home" class="hover:text-sky-500">首页</router-link>
        <router-link to="/portal/products" class="hover:text-sky-500">产品中心</router-link>
      </nav>

      <!-- 右侧 -->
      <div class="flex items-center gap-3">
        <router-link to="/portal/cart" class="relative p-2 text-slate-600 dark:text-slate-300 hover:text-sky-500">
          <el-icon :size="22"><ShoppingCart /></el-icon>
          <span v-if="portalStore.cartCount > 0" class="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-xs rounded-full w-4 h-4 flex items-center justify-center">{{ portalStore.cartCount }}</span>
        </router-link>
        <template v-if="portalStore.customer">
          <el-dropdown trigger="click">
            <span class="flex items-center gap-1 cursor-pointer text-slate-700 dark:text-slate-300 text-sm">
              <el-icon :size="18"><User /></el-icon>
              {{ portalStore.customer.contactName || portalStore.customer.username }}
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
  <!-- 浮动 AI 客服 -->
  <AiChatBubble />
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ShoppingCart, User } from '@element-plus/icons-vue'
import { usePortalStore } from '@/stores/portal'
import AiChatBubble from './AiChatBubble.vue'

const router = useRouter()
const portalStore = usePortalStore()

onMounted(() => {
  portalStore.loadCustomer()
  portalStore.fetchCartCount()
})

function handleLogout() {
  portalStore.logout()
  router.push('/portal/login')
}
</script>
