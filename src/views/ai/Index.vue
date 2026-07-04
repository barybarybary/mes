<template>
  <div class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden h-[calc(100vh-180px)] flex flex-col">
    <div class="px-6 py-5 border-b border-slate-100 flex items-center justify-between bg-gradient-to-r from-indigo-50 via-purple-50 to-pink-50">
      <div class="flex items-center gap-4">
        <div class="w-12 h-12 rounded-2xl bg-gradient-to-br from-indigo-500 via-purple-500 to-pink-500 flex items-center justify-center text-white text-2xl shadow-lg shadow-purple-200">
          🤖
        </div>
        <div>
          <h2 class="text-lg font-semibold text-slate-800">MES AI 助手</h2>
          <p class="text-xs text-slate-500 mt-1">DeepSeek V4 Pro · 已接入知识库</p>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <el-tag type="success" effect="light" round size="small" class="flex items-center gap-1">
          <span class="w-1.5 h-1.5 bg-green-500 rounded-full animate-pulse"></span>
          在线
        </el-tag>
        <el-button text @click="clearChat" size="small" class="text-slate-400 hover:text-red-500">
          <el-icon><RefreshRight /></el-icon>
          清空对话
        </el-button>
      </div>
    </div>

    <div class="flex-1 overflow-y-auto p-6 space-y-5 bg-slate-50/50" ref="msgBox">
      <div v-for="(m, i) in messages" :key="i" :class="['flex', m.role === 'user' ? 'justify-end' : 'justify-start']">
        <div v-if="m.role === 'assistant'" class="flex gap-3 max-w-[75%]">
          <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white text-sm flex-shrink-0 shadow-md">
            🤖
          </div>
          <div class="bg-white rounded-2xl rounded-tl-md px-4 py-3 shadow-sm border border-slate-100">
            <div v-html="formatContent(m.content)" class="text-sm text-slate-700 leading-relaxed"></div>
            <div v-if="m.sources && m.sources.length > 0" class="mt-3 pt-3 border-t border-slate-100">
              <div class="text-xs text-slate-400 mb-2 flex items-center gap-1">
                <el-icon><Document /></el-icon>
                引用知识库文档
              </div>
              <div class="flex flex-wrap gap-2">
                <div v-for="(s, si) in m.sources" :key="si" class="px-2.5 py-1 bg-sky-50 text-sky-600 text-xs rounded-lg border border-sky-100">
                  {{ s.title || `文档 ${si + 1}` }}
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="flex gap-3 max-w-[75%] flex-row-reverse">
          <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-sky-400 to-blue-600 flex items-center justify-center text-white text-sm font-medium flex-shrink-0 shadow-md">
            {{ userAvatar }}
          </div>
          <div class="bg-gradient-to-br from-sky-500 to-blue-600 text-white rounded-2xl rounded-tr-md px-4 py-3 shadow-sm shadow-blue-200">
            <div class="text-sm leading-relaxed">{{ m.content }}</div>
          </div>
        </div>
      </div>

      <div v-if="thinking" class="flex gap-3 max-w-[75%]">
        <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white text-sm flex-shrink-0 shadow-md">
          🤖
        </div>
        <div class="bg-white rounded-2xl rounded-tl-md px-5 py-4 shadow-sm border border-slate-100">
          <div class="flex items-center gap-2">
            <span class="w-2 h-2 bg-indigo-400 rounded-full animate-bounce" style="animation-delay: 0ms"></span>
            <span class="w-2 h-2 bg-purple-400 rounded-full animate-bounce" style="animation-delay: 150ms"></span>
            <span class="w-2 h-2 bg-pink-400 rounded-full animate-bounce" style="animation-delay: 300ms"></span>
            <span class="text-sm text-slate-400 ml-2">AI 思考中...</span>
          </div>
        </div>
      </div>
    </div>

    <div class="p-5 border-t border-slate-100 bg-white">
      <div class="flex gap-3 items-end">
        <div class="flex-1 relative">
          <el-input
            v-model="input"
            type="textarea"
            :rows="2"
            placeholder="输入您的问题，按 Enter 发送，Shift + Enter 换行..."
            @keydown.enter.exact.prevent="send"
            :disabled="thinking"
            resize="none"
            class="ai-input"
          />
        </div>
        <el-button
          type="primary"
          size="large"
          :disabled="!input.trim() || thinking"
          @click="send"
          class="h-auto !py-3 px-6 rounded-xl font-medium bg-gradient-to-r from-indigo-500 to-purple-600 border-none hover:from-indigo-600 hover:to-purple-700 shadow-md shadow-purple-200"
        >
          <el-icon class="text-lg"><Promotion /></el-icon>
        </el-button>
      </div>
      <div class="flex items-center justify-between mt-3">
        <div class="flex gap-2">
          <span class="text-xs text-slate-400">快捷问题：</span>
          <el-button v-for="q in quickQuestions" :key="q" text size="small" class="text-xs text-slate-500 hover:text-indigo-500 !p-0 !h-auto" @click="askQuick(q)">
            {{ q }}
          </el-button>
        </div>
        <span class="text-xs text-slate-300">AI 生成内容仅供参考</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { RefreshRight, Document, Promotion } from '@element-plus/icons-vue'
import api from '@/api'

const input = ref(''), messages = ref([]), thinking = ref(false), msgBox = ref()
const conversationId = ref(null)

const userName = computed(() => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  return user.nickname || user.username || '用户'
})

const userAvatar = computed(() => {
  return userName.value.charAt(0).toUpperCase()
})

const quickQuestions = ['今天生产进度如何？', '库存不足的物料有哪些？', '如何创建工单？', '质检流程是什么？']

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
  if (!input.value.trim() || thinking.value) return
  const q = input.value.trim()
  input.value = ''
  messages.value.push({ role: 'user', content: q })
  scrollBottom()
  thinking.value = true
  try {
    const userId = JSON.parse(localStorage.getItem('user') || '{}').id || 1
    const res = await api.post('/ai/chat', { userId, conversationId: conversationId.value, question: q })
    if (res.code === 200) {
      conversationId.value = res.data.conversationId
      messages.value.push({ role: 'assistant', content: res.data.content, sources: res.data.sources })
    }
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，服务暂时不可用，请稍后再试。' })
  } finally {
    thinking.value = false
    scrollBottom()
  }
}

function formatContent(text) {
  if (!text) return ''
  let html = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  html = html.replace(/```(\w*)\n([\s\S]*?)```/g, '<pre class="bg-slate-800 text-green-300 p-3 rounded-xl my-2 text-xs overflow-x-auto"><code>$2</code></pre>')
  html = html.replace(/`([^`]+)`/g, '<code class="bg-slate-100 text-rose-500 px-1.5 py-0.5 rounded text-xs">$1</code>')
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong class="text-slate-800">$1</strong>')
  html = html.replace(/\n/g, '<br>')
  return html
}

function clearChat() {
  messages.value = []
  conversationId.value = null
  messages.value.push({
    role: 'assistant',
    content: '对话已清空！我是 MES 系统的 AI 助手，接入了知识库，可以帮你解答生产管理、工艺流程、库存管理等问题。请问有什么可以帮你的？'
  })
  ElMessage.success('对话已清空')
}

onMounted(() => {
  messages.value.push({
    role: 'assistant',
    content: '你好！我是 MES 系统的 AI 助手 👋 我接入了知识库，可以帮你解答生产管理、工艺流程、库存管理等问题。请问有什么可以帮你的？'
  })
})
</script>

<style scoped>
:deep(.ai-input .el-textarea__inner) {
  border-radius: 12px !important;
  border-color: #e2e8f0 !important;
  padding: 12px 16px !important;
  font-size: 14px !important;
  transition: all 0.2s ease;
}

:deep(.ai-input .el-textarea__inner:focus) {
  border-color: #818cf8 !important;
  box-shadow: 0 0 0 3px rgba(129, 140, 248, 0.1) !important;
}
</style>
