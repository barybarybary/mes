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
              <p class="text-xs text-slate-400">制造变容易</p>
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
            class="w-full flex items-center justify-center gap-2 py-2 rounded-lg text-slate-400 hover:text-white hover:bg-white/10 transition-all duration-200"
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
          <!-- 考勤打卡 -->
          <el-popover placement="bottom-end" :width="240" trigger="click">
            <template #reference>
              <button class="relative p-2 rounded-lg text-slate-500 dark:text-slate-400 hover:text-sky-500 hover:bg-sky-50 dark:hover:bg-sky-900 transition-all duration-200" title="考勤打卡" data-tour="clock-in">
                <el-icon :size="20"><Clock /></el-icon>
                <span v-if="!todayClockIn" class="absolute top-1 right-1 w-2 h-2 bg-orange-500 rounded-full animate-pulse"></span>
              </button>
            </template>
            <div class="text-center py-2">
              <p class="text-sm font-semibold text-slate-700 mb-3">{{ currentTime }}</p>
              <div class="flex gap-2 justify-center">
                <el-button size="small" type="primary" :disabled="!!todayClockIn" @click="doClockIn" :loading="clockingIn">
                  {{ todayClockIn ? '已打卡 ' + todayClockIn : '上班打卡' }}
                </el-button>
                <el-button size="small" :type="todayClockOut ? 'success' : 'warning'" :disabled="!todayClockIn || !!todayClockOut" @click="doClockOut" :loading="clockingOut">
                  {{ todayClockOut ? '已签退 ' + todayClockOut : '下班打卡' }}
                </el-button>
              </div>
            </div>
          </el-popover>

          <!-- 通知铃铛 -->
          <el-popover placement="bottom-end" :width="340" trigger="click" @show="loadAlerts">
            <template #reference>
              <button class="relative p-2 rounded-lg text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-700 transition-all duration-200" data-tour="notification-bell">
                <el-icon :size="20"><Bell /></el-icon>
                <span v-if="unreadAlerts > 0" class="absolute -top-0.5 -right-0.5 min-w-[18px] h-[18px] flex items-center justify-center bg-red-500 text-white text-[10px] font-bold rounded-full px-1">{{ unreadAlerts > 99 ? '99+' : unreadAlerts }}</span>
              </button>
            </template>
            <div class="space-y-2 max-h-80 overflow-y-auto">
              <div v-if="alertItems.length === 0" class="text-center py-6 text-slate-400 text-sm">暂无新消息</div>
              <div v-for="a in alertItems" :key="a.id"
                   class="flex gap-2 p-2 rounded-lg hover:bg-slate-50 cursor-pointer transition-colors" @click="markAlertRead(a)">
                <div :class="['w-7 h-7 rounded-lg flex items-center justify-center shrink-0 mt-0.5', a.level==='critical'?'bg-red-100 text-red-500':a.level==='warning'?'bg-amber-100 text-amber-500':'bg-blue-100 text-blue-500']">
                  <el-icon :size="13"><WarningFilled /></el-icon>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-xs text-slate-700 line-clamp-2" :class="{ 'font-semibold': a.isRead === 0 }">{{ a.title }}</p>
                  <p class="text-[10px] text-slate-400 mt-0.5">{{ a.content?.substring(0, 40) }}{{ a.content?.length > 40 ? '...' : '' }}</p>
                  <p class="text-[10px] text-slate-300 mt-0.5">{{ formatTime(a.createTime) }}</p>
                </div>
              </div>
            </div>
          </el-popover>

          <!-- 产品指引 -->
          <button
            @click="startTour"
            class="relative p-2 rounded-lg text-slate-500 dark:text-slate-400 hover:text-sky-500 hover:bg-sky-50 dark:hover:bg-sky-900 transition-all duration-200"
            title="操作指引"
          >
            <el-icon :size="20"><QuestionFilled /></el-icon>
            <span v-if="!tourShown" class="absolute top-1 right-1 w-2 h-2 bg-sky-400 rounded-full animate-pulse"></span>
          </button>

          <!-- 全屏按钮 -->
          <button
            data-tour="fullscreen"
            @click="toggleFullscreen"
            class="p-2 rounded-lg text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-700 transition-all duration-200"
          >
            <el-icon :size="20"><FullScreen /></el-icon>
          </button>

          <div class="w-px h-6 bg-slate-200 dark:bg-slate-700"></div>

          <!-- 用户头像和信息 -->
          <el-dropdown trigger="click" placement="bottom-end" @command="handleDropdownCommand">
            <div class="flex items-center gap-3 cursor-pointer p-1.5 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-700 transition-all duration-200" data-tour="user-menu">
              <div class="w-9 h-9 rounded-xl gradient-primary-br flex items-center justify-center text-white font-medium shadow-md shadow-primary">
                {{ userAvatar }}
              </div>
              <div class="hidden md:block">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-200 leading-tight">
                  {{ userStore.user?.nickname || userStore.user?.username || '用户' }}
                </p>
                <p class="text-xs text-slate-400 dark:text-slate-500">{{ userStore.roles.map(r => r.name).join('、') || '未分配角色' }}</p>
              </div>
              <el-icon :size="14" class="text-slate-400"><ArrowDown /></el-icon>
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
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Clock, FullScreen, ArrowDown, User, Setting, SwitchButton, WarningFilled, QuestionFilled } from '@element-plus/icons-vue'
import SidebarMenu from '@/components/SidebarMenu.vue'
import { useTheme } from '@/composables/useTheme'
import { useBreadcrumb } from '@/composables/useBreadcrumb'
import { useAppTour } from '@/composables/useAppTour'
import api from '@/api'

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

// ===== 考勤打卡 =====
const currentTime = ref('')
const todayClockIn = ref(null), todayClockOut = ref(null)
const clockingIn = ref(false), clockingOut = ref(false)

function updateClock() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}
async function fetchTodayAttendance() {
  try {
    const res = await api.get('/attendance/today')
    if (res.code === 200 && res.data) {
      todayClockIn.value = res.data.clockInTime
      todayClockOut.value = res.data.clockOutTime
    }
  } catch (e) { /* ignore */ }
}
async function doClockIn() {
  clockingIn.value = true
  try { await api.post('/attendance/clock-in', { time: currentTime.value }); fetchTodayAttendance(); ElMessage.success('打卡成功') }
  catch { ElMessage.error('打卡失败') }
  finally { clockingIn.value = false }
}
async function doClockOut() {
  clockingOut.value = true
  try { await api.post('/attendance/clock-out', { time: currentTime.value }); fetchTodayAttendance(); ElMessage.success('签退成功') }
  catch { ElMessage.error('签退失败') }
  finally { clockingOut.value = false }
}

// ===== 铃铛通知（SSE 实时推送） =====
const alertItems = ref([]), unreadAlerts = ref(0)
let sseConnection = null, clockTimer = null

async function loadAlerts() {
  try {
    const res = await api.get('/bi/alerts', { params: { page: 1, pageSize: 10, isRead: 0 } })
    if (res.code === 200) alertItems.value = res.data?.list || []
  } catch (e) { /* ignore */ }
}
function connectSSE() {
  const token = sessionStorage.getItem('token') || localStorage.getItem('token')
  if (!token) return
  const baseUrl = window.location.origin + '/api/bi/alerts/stream?token=' + encodeURIComponent(token)
  sseConnection = new EventSource(baseUrl)
  sseConnection.addEventListener('alertCount', function(e) {
    try { const d = JSON.parse(e.data); unreadAlerts.value = d.unread || 0 } catch(ex) { /* ignore parse error */ }
  })
  sseConnection.onerror = function() {
    sseConnection.close()
    setTimeout(connectSSE, 10000) // 断线10秒后重连
  }
}
async function markAlertRead(row) {
  if (row.isRead === 1) return
  try { await api.put('/bi/alerts/' + row.id + '/read'); row.isRead = 1; unreadAlerts.value = Math.max(0, unreadAlerts.value - 1); loadAlerts() } catch (e) { /* ignore */ }
}
function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = Math.floor((now - d) / 60000)
  if (diff < 1) return '刚刚'
  if (diff < 60) return diff + '分钟前'
  if (diff < 1440) return Math.floor(diff / 60) + '小时前'
  return d.toLocaleDateString()
}

// ===== 产品指引 =====
const tourSteps = [
  {
    element: '[data-tour="sidebar-toggle"]',
    popover: {
      title: '侧边栏切换',
      description: '点击此按钮可以收起或展开左侧导航菜单，收起后鼠标悬停菜单项可查看完整名称。',
      side: 'right', align: 'start',
      onHighlightStarted: () => { if (isCollapse.value) isCollapse.value = false }
    }
  },
  {
    element: '[data-tour="breadcrumb"]',
    popover: {
      title: '面包屑导航',
      description: '显示当前页面在系统中的位置路径，点击任意节点可快速跳转到对应页面。',
      side: 'bottom', align: 'start'
    }
  },
  {
    element: '[data-tour="clock-in"]',
    popover: {
      title: '考勤打卡',
      description: '点击此处可以进行上下班打卡。橙色指示灯表示今天尚未打卡，打卡完成后指示灯消失。',
      side: 'bottom', align: 'center'
    }
  },
  {
    element: '[data-tour="notification-bell"]',
    popover: {
      title: '消息通知',
      description: '系统预警和通知消息会实时推送到这里，红色角标数字表示未读消息数量。点击可查看详情并标记已读。',
      side: 'bottom', align: 'center'
    }
  },
  {
    element: '[data-tour="fullscreen"]',
    popover: {
      title: '全屏模式',
      description: '点击可以进入或退出浏览器全屏模式，获得更大的操作视野。按 Esc 键也可以退出全屏。',
      side: 'bottom', align: 'center'
    }
  },
  {
    element: '[data-tour="user-menu"]',
    popover: {
      title: '用户菜单',
      description: '点击头像或用户名可以打开菜单，在这里可以进入个人中心、系统设置，以及安全退出登录。',
      side: 'bottom', align: 'end'
    }
  }
]

function startTour() {
  startAppTour(tourSteps)
}

onMounted(() => {
  updateClock()
  clockTimer = setInterval(updateClock, 1000)
  fetchTodayAttendance()
  connectSSE()
})
onBeforeUnmount(() => {
  clearInterval(clockTimer)
  if (sseConnection) sseConnection.close()
})

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
