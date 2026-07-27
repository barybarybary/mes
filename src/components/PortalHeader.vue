<template>
  <header class="fixed top-0 left-0 right-0 z-50 glass border-b border-slate-200/60" style="height: 72px;">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 h-full flex items-center justify-between">
      <div class="flex items-center gap-10">
        <router-link to="/portal" class="flex items-center gap-3 no-underline group">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-sky-400 to-blue-600 flex items-center justify-center shadow-lg shadow-sky-500/25 group-hover:shadow-xl group-hover:shadow-sky-500/30 transition-all duration-300 group-hover:scale-105">
            <span class="text-white text-sm font-extrabold tracking-tight">ZY</span>
          </div>
          <div class="flex flex-col">
            <span class="text-lg font-bold text-slate-800 leading-tight">造易</span>
            <span class="text-[10px] text-slate-400 font-medium tracking-wider">ZAOYI MES</span>
          </div>
        </router-link>

        <nav class="hidden md:flex items-center gap-1">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-link px-4 py-2 rounded-lg"
            :class="{ 'nav-link-active bg-sky-50/50': isActive(item.path) }"
          >{{ item.label }}</router-link>
        </nav>
      </div>

      <div class="flex items-center gap-3">
        <div class="hidden sm:flex items-center gap-2 bg-slate-50 hover:bg-white border border-slate-200 focus-within:border-sky-400 focus-within:bg-white focus-within:shadow-md focus-within:shadow-sky-500/10 rounded-xl px-4 py-2 transition-all duration-300 group w-64">
          <el-icon color="#94a3b8" :size="18" class="group-focus-within:text-sky-500 transition-colors shrink-0"><Search /></el-icon>
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索产品..."
            class="flex-1 bg-transparent border-none outline-none text-sm text-slate-700 placeholder-slate-400 w-0 min-w-0"
            @keydown.enter="doSearch"
          />
          <button
            v-if="keyword"
            class="text-[10px] text-slate-400 hover:text-sky-500 border border-slate-200 hover:border-sky-300 rounded px-1.5 py-0.5 transition-colors shrink-0"
            @click.stop="doSearch"
          >搜索</button>
        </div>

        <router-link
          to="/portal/cart"
          class="relative p-2.5 rounded-xl text-slate-600 hover:text-sky-500 hover:bg-sky-50 transition-all duration-200 no-underline group"
        >
          <el-icon :size="22" class="group-hover:scale-110 transition-transform"><ShoppingCart /></el-icon>
          <span
            v-if="portalStore.cartCount > 0"
            class="absolute -top-0.5 -right-0.5 min-w-[20px] h-5 flex items-center justify-center bg-gradient-to-r from-rose-500 to-pink-600 text-white text-[11px] font-bold rounded-full shadow-md shadow-rose-500/30 px-1"
          >{{ portalStore.cartCount > 99 ? '99+' : portalStore.cartCount }}</span>
        </router-link>

        <template v-if="portalStore.token && portalStore.customer">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="flex items-center gap-2.5 cursor-pointer pl-2 pr-3 py-1.5 rounded-xl hover:bg-slate-50 transition-colors">
              <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-sky-400 to-blue-600 flex items-center justify-center text-white text-sm font-semibold shadow-md shadow-sky-500/25">
                {{ (portalStore.customer.companyName || portalStore.customer.username || 'U').charAt(0).toUpperCase() }}
              </div>
              <div class="hidden sm:block">
                <div class="text-sm font-semibold text-slate-700 leading-tight">
                  {{ portalStore.customer.companyName || portalStore.customer.username }}
                </div>
                <div class="text-xs text-slate-400">会员用户</div>
              </div>
              <el-icon :size="14" class="text-slate-400"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="!p-2 !rounded-xl !shadow-xl">
                <el-dropdown-item command="orders" class="!rounded-lg !my-0.5">
                  <el-icon><Document /></el-icon>
                  <span class="ml-2">我的订单</span>
                </el-dropdown-item>
                <el-dropdown-item command="profile" class="!rounded-lg !my-0.5">
                  <el-icon><User /></el-icon>
                  <span class="ml-2">个人中心</span>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout" class="!rounded-lg !my-0.5 !text-rose-500">
                  <el-icon><SwitchButton /></el-icon>
                  <span class="ml-2">退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link
            to="/portal/login"
            class="hidden sm:inline-flex items-center text-sm font-medium text-slate-600 hover:text-sky-500 transition-colors no-underline px-4 py-2 rounded-lg hover:bg-slate-50"
          >登录</router-link>
          <router-link
            to="/portal/register"
            class="inline-flex items-center gap-1.5 px-5 py-2.5 rounded-xl font-semibold text-sm text-white bg-gradient-to-r from-sky-500 to-blue-600 hover:from-sky-600 hover:to-blue-700 shadow-md shadow-sky-500/25 hover:shadow-lg hover:shadow-sky-500/30 transition-all duration-300 no-underline hover:-translate-y-0.5"
          >免费注册</router-link>
        </template>
      </div>
    </div>
  </header>
  <div style="height: 72px;"></div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { usePortalStore } from '@/stores/portal'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const portalStore = usePortalStore()

const keyword = ref('')

function doSearch() {
  const kw = keyword.value.trim()
  if (kw) {
    router.push({ path: '/portal/products', query: { keyword: kw } })
  } else {
    router.push('/portal/products')
  }
}

const navItems = [
  { path: '/portal', label: '首页' },
  { path: '/portal/products', label: '产品中心' },
  { path: '/portal/orders', label: '我的订单' },
]

function isActive(path) {
  if (path === '/portal') {
    return route.path === '/portal'
  }
  return route.path.startsWith(path)
}

onMounted(() => {
  if (portalStore.token) {
    portalStore.fetchCartCount()
  }
})

function handleCommand(cmd) {
  if (cmd === 'orders') {
    router.push('/portal/orders')
  } else if (cmd === 'profile') {
    router.push('/portal/profile')
  } else if (cmd === 'logout') {
    portalStore.logout()
    ElMessage.success('已退出登录')
    router.push('/portal')
  }
}
</script>
