<template>
  <!-- 浮动气泡 -->
  <div class="fixed bottom-6 right-6 z-50">
    <!-- 聊天窗口 -->
    <transition name="slide-up">
      <div v-if="open" class="bg-white dark:bg-slate-800 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-700 w-80 md:w-96 mb-4 flex flex-col overflow-hidden" style="height: 480px; max-height: calc(100vh - 120px);">
        <!-- 头部 -->
        <div class="bg-gradient-to-r from-sky-500 to-blue-600 text-white px-4 py-3 flex items-center justify-between shrink-0">
          <div class="flex items-center gap-2">
            <span class="text-xl">🤖</span>
            <div>
              <p class="text-sm font-semibold">AI 客服</p>
              <p class="text-xs text-sky-100">造易商城智能助手</p>
            </div>
          </div>
          <button @click="open = false" class="text-white/80 hover:text-white text-lg leading-none">&times;</button>
        </div>

        <!-- 消息区 -->
        <div ref="msgBox" class="flex-1 overflow-y-auto p-3 space-y-3 bg-slate-50 dark:bg-slate-900">
          <div v-for="(m, i) in messages" :key="i" :class="['flex', m.role === 'user' ? 'justify-end' : 'justify-start']">
            <div v-if="m.role === 'assistant'" class="flex gap-2 max-w-[85%]">
              <span class="text-lg shrink-0">🤖</span>
              <div class="bg-white dark:bg-slate-700 rounded-2xl rounded-tl-sm px-3 py-2 shadow-sm border dark:border-slate-600 text-sm text-slate-700 dark:text-slate-200 leading-relaxed" v-html="mdToHtml(m.content)"></div>
            </div>
            <div v-else class="bg-gradient-to-br from-sky-500 to-blue-600 text-white rounded-2xl rounded-tr-sm px-3 py-2 max-w-[85%] shadow-sm text-sm">
              {{ m.content }}
            </div>
          </div>

          <!-- 思考中 -->
          <div v-if="thinking" class="flex gap-2 max-w-[85%]">
            <span class="text-lg shrink-0">🤖</span>
            <div class="bg-white dark:bg-slate-700 rounded-2xl rounded-tl-sm px-4 py-3 shadow-sm border dark:border-slate-600">
              <div class="flex gap-1.5">
                <span class="w-2 h-2 bg-sky-400 rounded-full animate-bounce" style="animation-delay: 0ms"></span>
                <span class="w-2 h-2 bg-sky-400 rounded-full animate-bounce" style="animation-delay: 150ms"></span>
                <span class="w-2 h-2 bg-sky-400 rounded-full animate-bounce" style="animation-delay: 300ms"></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 快捷问题 -->
        <div class="px-3 py-2 border-t dark:border-slate-700 bg-white dark:bg-slate-800 shrink-0 flex gap-1.5 flex-wrap">
          <button v-for="q in quickQuestions" :key="q" @click="askQuick(q)" class="text-xs px-2 py-1 rounded-full bg-sky-50 dark:bg-sky-900/30 text-sky-600 dark:text-sky-400 hover:bg-sky-100 dark:hover:bg-sky-900/50 transition border border-sky-100 dark:border-sky-800 whitespace-nowrap">
            {{ q }}
          </button>
        </div>

        <!-- 输入区 -->
        <div class="border-t dark:border-slate-700 px-3 py-3 bg-white dark:bg-slate-800 shrink-0 flex gap-2 items-end">
          <el-input
            v-model="input"
            type="textarea"
            :rows="1"
            placeholder="输入问题..."
            :disabled="thinking"
            resize="none"
            @keydown.enter.exact.prevent="send"
            class="text-sm"
          />
          <el-button type="primary" size="small" :disabled="!input.trim() || thinking" @click="send" class="!rounded-lg shrink-0">
            发送
          </el-button>
        </div>
      </div>
    </transition>

    <!-- 浮动按钮 -->
    <button @click="toggle" class="w-14 h-14 bg-gradient-to-br from-sky-500 to-blue-600 rounded-full shadow-lg shadow-blue-200 dark:shadow-blue-900/50 flex items-center justify-center text-2xl hover:shadow-xl hover:scale-105 transition-all duration-200 relative">
      <span v-if="!open">💬</span>
      <span v-else>✕</span>
      <span v-if="!open && unreadCount > 0" class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-bold">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
    </button>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import api from '@/api/portal'
import { marked } from 'marked'

marked.setOptions({ breaks: true, gfm: true })

const open = ref(false)
const input = ref('')
const messages = ref([])
const thinking = ref(false)
const msgBox = ref(null)
const unreadCount = ref(0)

const quickQuestions = ['有什么产品？', '查库存', '如何下单？', '订单查询']

// 简单的 markdown 转 HTML
function mdToHtml(text) {
  if (!text) return ''
  try {
    return marked.parse(text)
  } catch {
    return text.replace(/\n/g, '<br>')
  }
}

function toggle() {
  open.value = !open.value
  if (open.value) {
    unreadCount.value = 0
    if (messages.value.length === 0) {
      messages.value.push({
        role: 'assistant',
        content: '你好！我是造易商城的 AI 客服 🤖\n\n可以帮你查询产品、库存、订单信息。有什么可以帮你的？'
      })
    }
    nextTick(scrollBottom)
  }
}

function scrollBottom() {
  nextTick(() => {
    if (msgBox.value) {
      msgBox.value.scrollTop = msgBox.value.scrollHeight
    }
  })
}

function askQuick(q) {
  input.value = q
  send()
}

async function send() {
  const q = input.value.trim()
  if (!q || thinking.value) return
  input.value = ''
  messages.value.push({ role: 'user', content: q })
  scrollBottom()
  thinking.value = true
  try {
    const res = await api.post('/ai/chat', { question: q })
    const content = res.data?.content || '抱歉，暂时无法回复。'
    messages.value.push({ role: 'assistant', content })
  } catch {
    messages.value.push({ role: 'assistant', content: '抱歉，AI 客服暂时不可用。' })
  } finally {
    thinking.value = false
    scrollBottom()
  }
}
</script>

<style scoped>
.slide-up-enter-active, .slide-up-leave-active {
  transition: all 0.25s ease;
}
.slide-up-enter-from, .slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

:deep(.el-textarea__inner) {
  border-radius: 10px !important;
  font-size: 13px !important;
  padding: 8px 12px !important;
}
</style>
