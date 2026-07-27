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
            <SidebarMenu :menu-list="menuConfig" />
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

    <!-- ===== AI 悬浮对话面板 ===== -->
    <div class="fixed z-[9999]" :style="{ left: aiPos.x + 'px', top: aiPos.y + 'px' }">
      <!-- 悬浮球 -->
      <div
        @mousedown.prevent="startDrag"
        @touchstart.prevent="startDrag"
        @click="onBubbleClick"
        class="w-14 h-14 rounded-full bg-gradient-to-br from-blue-500 to-blue-700 flex items-center justify-center text-white text-2xl shadow-lg shadow-blue-300 dark:shadow-blue-900 cursor-grab active:cursor-grabbing transition-all duration-300 hover:scale-110 select-none"
        :class="{ 'scale-0 opacity-0 pointer-events-none': aiPanelOpen }"
        title="拖拽移动 · 点击打开"
      >
        💬
      </div>

      <!-- 对话面板 -->
      <transition name="panel-slide">
        <div
          v-if="aiPanelOpen"
          class="absolute bottom-0 right-0 w-[400px] h-[550px] bg-white dark:bg-slate-800 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-700 flex flex-col overflow-hidden"
        >
          <!-- 头部（拖拽抓手） -->
          <div
            @mousedown.prevent="startDrag"
            @touchstart.prevent="startDrag"
            class="bg-gradient-to-r from-blue-500 to-blue-700 px-4 py-3 flex items-center justify-between shrink-0 cursor-grab active:cursor-grabbing select-none"
          >
            <span class="text-white font-semibold text-sm">🤖 车间 AI 助手</span>
            <button @click="aiPanelOpen = false" @mousedown.stop @touchstart.stop class="text-white/80 hover:text-white text-lg leading-none transition-colors">&times;</button>
          </div>

          <!-- 消息区 -->
          <div ref="aiMsgBox" class="flex-1 overflow-y-auto p-4 space-y-3 bg-slate-50 dark:bg-slate-900">
            <div v-for="(m, i) in aiMessages" :key="i" :class="['flex', m.role === 'user' ? 'justify-end' : 'justify-start']">
              <div v-if="m.role === 'assistant'" class="bg-white dark:bg-slate-700 rounded-2xl rounded-tl-md px-4 py-2.5 max-w-[85%] shadow-sm border border-slate-100 dark:border-slate-600">
                <div v-html="m.content" class="text-sm text-slate-700 dark:text-slate-200 leading-relaxed markdown-body"></div>
              </div>
              <div v-else class="bg-gradient-to-br from-blue-500 to-blue-700 text-white rounded-2xl rounded-tr-md px-4 py-2.5 max-w-[85%] shadow-sm">
                <span class="text-sm">{{ m.content }}</span>
              </div>
            </div>
            <div v-if="aiThinking" class="flex justify-start">
              <div class="bg-white dark:bg-slate-700 rounded-2xl rounded-tl-md px-5 py-4 shadow-sm border border-slate-100 dark:border-slate-600">
                <div class="flex items-center gap-2">
                  <span class="w-2 h-2 bg-blue-400 rounded-full animate-bounce" style="animation-delay: 0ms"></span>
                  <span class="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style="animation-delay: 150ms"></span>
                  <span class="w-2 h-2 bg-blue-600 rounded-full animate-bounce" style="animation-delay: 300ms"></span>
                  <span class="text-sm text-slate-400 ml-2">思考中...</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 快捷问题 -->
          <div class="px-3 pt-2 pb-1 flex flex-wrap gap-1.5 shrink-0 bg-slate-50 dark:bg-slate-900">
            <el-tag
              v-for="q in floatingQuickQuestions" :key="q"
              @click="askFloatingAi(q)"
              class="cursor-pointer hover:bg-blue-100 dark:hover:bg-blue-900"
              size="small" type="info"
            >{{ q }}</el-tag>
          </div>

          <!-- 输入区 -->
          <div class="p-3 border-t border-slate-100 dark:border-slate-700 shrink-0 flex items-center gap-2 bg-white dark:bg-slate-800">
            <el-input
              v-model="aiInput"
              placeholder="输入问题..."
              size="small"
              @keydown.enter.exact="sendFloatingAi"
              :disabled="aiThinking"
              class="flex-1"
            />
            <el-button type="primary" size="small" :disabled="!aiInput.trim() || aiThinking" @click="sendFloatingAi" circle>
              <el-icon><Promotion /></el-icon>
            </el-button>
          </div>
        </div>
      </transition>
    </div>
  </template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FullScreen, ArrowDown, User, Setting, SwitchButton, QuestionFilled, Promotion } from '@element-plus/icons-vue'
import SidebarMenu from '@/components/SidebarMenu.vue'
import { menuConfig } from '@/config/menus'
import { useTheme } from '@/composables/useTheme'
import { useBreadcrumb } from '@/composables/useBreadcrumb'
import { useAppTour } from '@/composables/useAppTour'
import api from '@/api'
import { marked } from 'marked'

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

// ===== AI 浮动对话面板 =====
const aiPanelOpen = ref(false)
const aiInput = ref('')
const aiMessages = ref([])
const aiThinking = ref(false)
const aiConversationId = ref(null)
const aiMsgBox = ref(null)

// ===== 拖拽移动 =====
const BUBBLE_SIZE = 56      // 悬浮球直径
const PANEL_W = 400         // 面板宽度
const PANEL_H = 550         // 面板高度
const DRAG_THRESHOLD = 5    // 超过此像素才算拖拽

const aiPos = ref({ x: 0, y: 0 })
const isDragging = ref(false)
const dragOffset = ref({ x: 0, y: 0 })   // 鼠标相对元素左上角的偏移
const hasMoved = ref(false)

function initAiPos() {
  aiPos.value = {
    x: window.innerWidth - BUBBLE_SIZE - 24,
    y: window.innerHeight - BUBBLE_SIZE - 24
  }
}

function clampPos(x, y, open) {
  if (open) {
    // 面板从容器(56x56)右下角向左上延伸 → 容器右下 = 面板右下
    return {
      x: Math.max(PANEL_W - BUBBLE_SIZE, Math.min(x, window.innerWidth - BUBBLE_SIZE)),
      y: Math.max(PANEL_H - BUBBLE_SIZE, Math.min(y, window.innerHeight - BUBBLE_SIZE))
    }
  }
  return {
    x: Math.max(0, Math.min(x, window.innerWidth - BUBBLE_SIZE)),
    y: Math.max(0, Math.min(y, window.innerHeight - BUBBLE_SIZE))
  }
}

function startDrag(e) {
  isDragging.value = true
  hasMoved.value = false
  const cx = e.touches ? e.touches[0].clientX : e.clientX
  const cy = e.touches ? e.touches[0].clientY : e.clientY
  dragOffset.value = { x: cx - aiPos.value.x, y: cy - aiPos.value.y }
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.addEventListener('touchmove', onDrag, { passive: false })
  document.addEventListener('touchend', stopDrag)
}

function onDrag(e) {
  if (!isDragging.value) return
  e.preventDefault()
  const cx = e.touches ? e.touches[0].clientX : e.clientX
  const cy = e.touches ? e.touches[0].clientY : e.clientY
  const nx = cx - dragOffset.value.x
  const ny = cy - dragOffset.value.y
  if (Math.abs(nx - aiPos.value.x) > DRAG_THRESHOLD || Math.abs(ny - aiPos.value.y) > DRAG_THRESHOLD) {
    hasMoved.value = true
  }
  const clamped = clampPos(nx, ny, aiPanelOpen.value)
  aiPos.value = clamped
}

function stopDrag() {
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', stopDrag)
}

function onBubbleClick() {
  if (hasMoved.value) return       // 拖拽过 → 不触发打开
  aiPanelOpen.value = !aiPanelOpen.value
}

const floatingQuickQuestions = ['今天生产情况', '哪些工单超期了', '最近不良率', '硫化操作指导']

function scrollAiBottom() {
  nextTick(() => {
    if (aiMsgBox.value) {
      aiMsgBox.value.scrollTop = aiMsgBox.value.scrollHeight
    }
  })
}

function askFloatingAi(q) {
  aiInput.value = q
  sendFloatingAi()
}

async function sendFloatingAi() {
  if (!aiInput.value.trim() || aiThinking.value) return
  const q = aiInput.value.trim()
  aiInput.value = ''
  aiMessages.value.push({ role: 'user', content: q })
  scrollAiBottom()
  aiThinking.value = true
  try {
    const userId = JSON.parse(localStorage.getItem('user') || '{}').id || 1
    const res = await api.post('/ai/chat', { userId, conversationId: aiConversationId.value, question: q })
    if (res.code === 200) {
      aiConversationId.value = res.data.conversationId
      aiMessages.value.push({ role: 'assistant', content: marked.parse(res.data.content || '') })
    }
  } catch (e) {
    aiMessages.value.push({ role: 'assistant', content: '抱歉，服务暂时不可用，请稍后再试。' })
  } finally {
    aiThinking.value = false
    scrollAiBottom()
  }
}

onMounted(() => {
  initAiPos()
  window.addEventListener('resize', onWindowResize)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', onWindowResize)
})

// 面板打开/关闭时重新 clamp（因为悬浮球和面板尺寸不同）
watch(aiPanelOpen, () => {
  aiPos.value = clampPos(aiPos.value.x, aiPos.value.y, aiPanelOpen.value)
})

function onWindowResize() {
  aiPos.value = clampPos(aiPos.value.x, aiPos.value.y, aiPanelOpen.value)
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

/* AI 浮动面板过渡动画 */
.panel-slide-enter-active,
.panel-slide-leave-active {
  transition: all 0.3s ease;
}

.panel-slide-enter-from,
.panel-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.markdown-body p { margin: 0.25em 0; }
.markdown-body code { background: #f1f5f9; color: #f43f5e; padding: 1px 6px; border-radius: 4px; font-size: 12px; }
.markdown-body pre { background: #1e293b; color: #86efac; padding: 12px; border-radius: 12px; margin: 8px 0; font-size: 12px; overflow-x: auto; }
.markdown-body pre code { background: transparent; color: inherit; padding: 0; font-size: inherit; }
.markdown-body strong { font-weight: 600; color: #1e293b; }
.dark .markdown-body strong { color: #e2e8f0; }
.dark .markdown-body code { background: #334155; color: #fb7185; }

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
