<template>
  <el-container class="h-screen bg-slate-100 dark:bg-slate-950 overflow-hidden">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '240px'" class="transition-all duration-300 ease-in-out relative">
      <div class="h-full bg-gradient-to-b from-slate-900 to-slate-800 text-white flex flex-col shadow-xl">
        <!-- Logo区域 -->
        <div class="h-16 flex items-center px-4 border-b border-slate-700/50 shrink-0">
          <div class="w-10 h-10 rounded-xl gradient-primary-br flex items-center justify-center shadow-primary shrink-0 text-white text-sm font-extrabold">
            ZY
          </div>
          <Transition name="fade">
            <div v-show="!isCollapse" class="ml-3 overflow-hidden">
              <h1 class="text-base font-bold leading-tight">造易 ZaoYi</h1>
              <p class="text-xs text-slate-400 dark:text-slate-300">制造变容易</p>
            </div>
          </Transition>
        </div>

        <!-- 菜单区域 -->
        <el-scrollbar class="flex-1 py-3">
          <el-menu
            :default-active="route.path"
            :collapse="isCollapse"
            :collapse-transition="false"
            background-color="transparent"
            text-color="#94a3b8"
            active-text-color="#38bdf8"
            router
            class="border-r-0 bg-transparent"
          >
            <SidebarMenu :menu-list="userStore.menus" />
          </el-menu>
        </el-scrollbar>

        <!-- 底部折叠按钮 -->
        <div class="p-3 border-t border-slate-700/50 shrink-0">
          <button
            data-tour="sidebar-toggle"
            @click="isCollapse = !isCollapse"
            class="w-full flex items-center justify-center gap-2 py-2 rounded-lg text-slate-400 dark:text-slate-300 hover:text-white hover:bg-white dark:bg-slate-800/10 transition-all duration-200"
          >
            <el-icon :size="18" :class="{ 'rotate-180': isCollapse }" class="transition-transform duration-300">
              <Fold />
            </el-icon>
            <Transition name="fade">
              <span v-show="!isCollapse" class="text-sm">收起菜单</span>
            </Transition>
          </button>
        </div>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="flex flex-col overflow-hidden min-w-0">
      <!-- 顶部导航栏 -->
      <el-header class="h-16 flex items-center justify-between bg-white dark:bg-slate-800 border-b border-slate-200 dark:border-slate-700 px-6 shadow-sm shrink-0">
        <!-- 左侧：面包屑 -->
        <div class="flex items-center gap-4 min-w-0" data-tour="breadcrumb">
          <el-breadcrumb separator="/" class="truncate">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <template v-if="breadcrumbMode === 'hierarchical' && hierarchicalPath.length">
              <el-breadcrumb-item
                v-for="(item, i) in hierarchicalPath"
                :key="i"
                :to="item.path ? { path: item.path } : undefined"
              >{{ item.name }}</el-breadcrumb-item>
            </template>
            <el-breadcrumb-item v-else-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <!-- 右侧：用户信息 -->
        <div class="flex items-center gap-3 shrink-0">
          <!-- 产品指引 -->
          <button
            @click="startTour"
            class="relative p-2 rounded-lg text-slate-500 dark:text-slate-300 hover:text-sky-500 hover:bg-sky-50 dark:hover:bg-sky-900 transition-all duration-200"
            title="操作指引"
          >
            <el-icon :size="20"><QuestionFilled /></el-icon>
            <span v-if="!tourShown" class="absolute top-1 right-1 w-2 h-2 bg-sky-400 rounded-full animate-pulse"></span>
          </button>

          <!-- 全屏按钮 -->
          <button
            data-tour="fullscreen"
            @click="toggleFullscreen"
            class="p-2 rounded-lg text-slate-500 dark:text-slate-300 hover:text-slate-700 dark:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:bg-slate-800 dark:hover:bg-slate-700 transition-all duration-200"
          >
            <el-icon :size="20"><FullScreen /></el-icon>
          </button>

          <div class="w-px h-6 bg-slate-200 dark:bg-slate-700"></div>

          <!-- 用户头像和信息 -->
          <el-dropdown trigger="click" placement="bottom-end" @command="handleDropdownCommand">
            <div class="flex items-center gap-3 cursor-pointer p-1.5 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-700 dark:bg-slate-900 dark:hover:bg-slate-700 transition-all duration-200" data-tour="user-menu">
              <div class="w-9 h-9 rounded-xl gradient-primary-br flex items-center justify-center text-white font-medium shadow-md shadow-primary">
                {{ userAvatar }}
              </div>
              <div class="hidden md:block">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-200 leading-tight">
                  {{ userStore.user?.nickname || userStore.user?.username || '用户' }}
                </p>
                <p class="text-xs text-slate-400 dark:text-slate-300">{{ userStore.roles.map(r => r.name).join('、') || '未分配角色' }}</p>
              </div>
              <el-icon :size="14" class="text-slate-400 dark:text-slate-300"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="w-48">
                <el-dropdown-item command="profile">
                  <el-icon class="mr-2"><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon class="mr-2"><Setting /></el-icon>系统设置
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon class="mr-2 text-red-500"><SwitchButton /></el-icon>
                  <span class="text-red-500">退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容 -->
      <el-main class="flex-1 overflow-auto bg-slate-50 dark:bg-slate-900 p-6 min-w-0">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FullScreen, ArrowDown, User, Setting, SwitchButton, QuestionFilled } from '@element-plus/icons-vue'
import SidebarMenu from '@/components/SidebarMenu.vue'
import { useTheme } from '@/composables/useTheme'
import { useBreadcrumb } from '@/composables/useBreadcrumb'
import { useAppTour } from '@/composables/useAppTour'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)
const { breadcrumbMode } = useTheme()
const { hierarchicalPath } = useBreadcrumb()
const { tourShown, startTour: startAppTour } = useAppTour()

const userAvatar = computed(() => {
  const name = userStore.user?.username || userStore.user?.nickname || 'U'
  return name.charAt(0).toUpperCase()
})

function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// ===== 产品指引 =====
const tourSteps = [
  {
    element: '[data-tour="sidebar-toggle"]',
    popover: {
      title: '侧边栏切换',
      description: '点击此按钮可以收起或展开左侧导航菜单。',
      side: 'right', align: 'start',
      onHighlightStarted: () => { if (isCollapse.value) isCollapse.value = false }
    }
  },
  {
    element: '[data-tour="breadcrumb"]',
    popover: {
      title: '面包屑导航',
      description: '显示当前页面在系统中的位置，点击可快速跳转。',
      side: 'bottom', align: 'start'
    }
  },
  {
    element: '[data-tour="fullscreen"]',
    popover: {
      title: '全屏模式',
      description: '点击可进入/退出浏览器全屏，按 Esc 键也可退出。',
      side: 'bottom', align: 'center'
    }
  },
  {
    element: '[data-tour="user-menu"]',
    popover: {
      title: '用户菜单',
      description: '点击头像可进入个人中心、系统设置或退出登录。',
      side: 'bottom', align: 'end'
    }
  }
]

function startTour() {
  startAppTour(tourSteps)
}

function handleDropdownCommand(command) {
  if (command === 'profile') {
    router.push('/system/profile')
  } else if (command === 'settings') {
    router.push('/system/settings')
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

:deep(.el-menu) {
  border-right: none !important;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 44px !important;
  line-height: 44px !important;
  margin: 2px 8px !important;
  border-radius: 8px !important;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background-color: rgba(255, 255, 255, 0.06) !important;
}

  :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, color-mix(in srgb, var(--color-primary-400) 15%, transparent) 0%, color-mix(in srgb, var(--color-primary-400) 5%, transparent) 100%) !important;
  color: var(--color-primary-400) !important;
}

:deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: -8px;
  background: var(--color-primary-400);
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: #38bdf8;
  border-radius: 0 2px 2px 0;
}

:deep(.el-sub-menu .el-menu-item) {
  min-width: auto !important;
  padding-left: 48px !important;
}

:deep(.el-breadcrumb__inner) {
  font-size: 14px !important;
}

:deep(.el-dropdown-menu) {
  padding: 8px !important;
  border-radius: 12px !important;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.12) !important;
}

:deep(.el-dropdown-menu__item) {
  border-radius: 8px !important;
  margin: 2px 0 !important;
}
</style>
