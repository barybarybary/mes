<template>
  <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden h-[calc(100vh-180px)] flex">
    <!-- ====== 左侧：历史对话列表 ====== -->
    <div class="w-72 border-r border-slate-100 dark:border-slate-700 flex flex-col bg-slate-50 dark:bg-slate-900/50 shrink-0">
      <div class="p-4 border-b border-slate-100 dark:border-slate-700">
        <el-button type="primary" @click="newChat" class="w-full rounded-xl" size="default">
          <el-icon class="mr-1"><Plus /></el-icon>新建对话
        </el-button>
      </div>
      <div class="flex-1 overflow-y-auto p-3 space-y-2">
        <div
          v-for="conv in conversations"
          :key="conv.id"
          @click="selectConversation(conv)"
          :class="[
            'p-3 rounded-xl cursor-pointer transition-all',
            conversationId === conv.id
              ? 'bg-indigo-100 border border-indigo-200'
              : 'hover:bg-white dark:bg-slate-800 border border-transparent hover:border-slate-200 dark:border-slate-700'
          ]"
        >
          <div class="text-sm font-medium text-slate-700 dark:text-slate-600 truncate">{{ conv.title || '新对话' }}</div>
          <div class="text-xs text-slate-400 dark:text-slate-300 mt-1 flex items-center gap-2">
            <span>{{ conv.messageCount || 0 }} 条消息</span>
            <span>·</span>
            <span>{{ formatTime(conv.updateTime || conv.createTime) }}</span>
          </div>
        </div>
        <div v-if="conversations.length === 0" class="text-center py-10 text-slate-400 dark:text-slate-300 text-sm">
          暂无历史对话
        </div>
      </div>
    </div>

    <!-- ====== 右侧：对话区域 ====== -->
    <div class="flex-1 flex flex-col">
      <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-700 flex items-center justify-between bg-gradient-to-r from-indigo-50 via-purple-50 to-pink-50">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-500 via-purple-500 to-pink-500 flex items-center justify-center text-white text-xl shadow-lg shadow-purple-200">
            🤖
          </div>
          <div>
            <h2 class="text-base font-semibold text-slate-800 dark:text-slate-200">MES AI 助手</h2>
            <p class="text-xs text-slate-500 dark:text-slate-300">DeepSeek · 已接入知识库</p>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <el-tag type="success" effect="light" round size="small" class="flex items-center gap-1">
            <span class="w-1.5 h-1.5 bg-green-500 rounded-full animate-pulse"></span>
            在线
          </el-tag>
          <el-button text @click="clearChat" size="small" class="text-slate-400 dark:text-slate-300 hover:text-red-500">
            <el-icon><RefreshRight /></el-icon>
            清空
          </el-button>
        </div>
      </div>

      <div class="flex-1 overflow-y-auto p-6 space-y-5 bg-slate-50 dark:bg-slate-900/50" ref="msgBox">
        <div v-for="(m, i) in messages" :key="i" :class="['flex', m.role === 'user' ? 'justify-end' : 'justify-start']">
          <div v-if="m.role === 'assistant'" class="flex gap-3 max-w-[75%]">
            <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white text-sm flex-shrink-0 shadow-md">
              🤖
            </div>
            <div class="bg-white dark:bg-slate-800 rounded-2xl rounded-tl-md px-4 py-3 shadow-sm border border-slate-100 dark:border-slate-700">
              <div v-html="marked.parse(m.content)" class="text-sm text-slate-700 dark:text-slate-600 leading-relaxed ai-markdown"></div>
              <div v-if="m.sources && m.sources.length > 0" class="mt-3 pt-3 border-t border-slate-100 dark:border-slate-700">
                <div class="text-xs text-slate-400 dark:text-slate-300 mb-2 flex items-center gap-1">
                  <el-icon><Document /></el-icon>引用知识库
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
            <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-sky-400 to-blue-600 flex items-center justify-center text-white text-sm font-medium flex-shrink-0 shadow-md">
              {{ userAvatar }}
            </div>
            <div class="bg-gradient-to-br from-sky-500 to-blue-600 text-white rounded-2xl rounded-tr-md px-4 py-3 shadow-sm shadow-blue-200">
              <div class="text-sm leading-relaxed">{{ m.content }}</div>
            </div>
          </div>
        </div>

        <div v-if="thinking" class="flex gap-3 max-w-[75%]">
          <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white text-sm flex-shrink-0 shadow-md">
            🤖
          </div>
          <div class="bg-white dark:bg-slate-800 rounded-2xl rounded-tl-md px-5 py-4 shadow-sm border border-slate-100 dark:border-slate-700">
            <div class="flex items-center gap-2">
              <span class="w-2 h-2 bg-indigo-400 rounded-full animate-bounce" style="animation-delay: 0ms"></span>
              <span class="w-2 h-2 bg-purple-400 rounded-full animate-bounce" style="animation-delay: 150ms"></span>
              <span class="w-2 h-2 bg-pink-400 rounded-full animate-bounce" style="animation-delay: 300ms"></span>
              <span class="text-sm text-slate-400 dark:text-slate-300 ml-2">AI 思考中...</span>
            </div>
          </div>
        </div>
      </div>

      <div class="p-5 border-t border-slate-100 dark:border-slate-700 bg-white dark:bg-slate-800">
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
          <div class="flex gap-2 flex-wrap">
            <span class="text-xs text-slate-400 dark:text-slate-300">快捷问题：</span>
            <el-button v-for="q in quickQuestions" :key="q" text size="small" class="text-xs text-slate-500 dark:text-slate-300 hover:text-indigo-500 !p-0 !h-auto" @click="askQuick(q)">
              {{ q }}
            </el-button>
          </div>
          <span class="text-xs text-slate-300 dark:text-slate-500">AI 生成内容仅供参考</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { RefreshRight, Document, Promotion, Plus } from '@element-plus/icons-vue'
import api from '@/api'
import { marked } from 'marked'
marked.setOptions({ breaks: true, gfm: true })

const input = ref(''), messages = ref([]), thinking = ref(false), msgBox = ref()
const conversationId = ref(null), conversations = ref([])

const userName = computed(() => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  return user.nickname || user.username || '用户'
})

const userAvatar = computed(() => {
  return userName.value.charAt(0).toUpperCase()
})

const quickQuestions = ['今天生产进度如何？', '库存不足的物料有哪些？', '如何创建工单？', '质检流程是什么？']

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
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

async function loadConversations() {
  try {
    const userId = getUserId()
    const res = await api.get('/ai/conversations', { params: { userId } })
    if (res.code === 200) {
      conversations.value = res.data || []
    }
  } catch (e) {
    console.error('加载对话列表失败', e)
  }
}

function getUserId() {
  return JSON.parse(localStorage.getItem('user') || '{}').id || 1
}

async function selectConversation(conv) {
  conversationId.value = conv.id
  messages.value = []
  try {
    const res = await api.get(`/ai/conversations/${conv.id}/messages`)
    if (res.code === 200 && res.data) {
      messages.value = res.data.map(m => ({
        role: m.role,
        content: m.content,
        sources: m.sources ? (typeof m.sources === 'string' ? JSON.parse(m.sources) : m.sources) : null
      }))
    }
  } catch (e) {
    console.error('加载消息失败', e)
  }
  scrollBottom()
}

async function newChat() {
  conversationId.value = null
  messages.value = []
  messages.value.push({
    role: 'assistant',
    content: '你好！我是 MES 系统的 AI 助手 👋 我接入了知识库，可以帮你解答生产管理、工艺流程、库存管理等问题。请问有什么可以帮你的？'
  })
  await loadConversations()
}

async function send() {
  if (!input.value.trim() || thinking.value) return
  const q = input.value.trim()
  input.value = ''
  messages.value.push({ role: 'user', content: q })
  scrollBottom()
  thinking.value = true
  try {
    const userId = getUserId()
    const res = await api.post('/ai/chat', { userId, conversationId: conversationId.value, question: q })
    if (res.code === 200) {
      conversationId.value = res.data.conversationId
      messages.value.push({ role: 'assistant', content: res.data.content, sources: res.data.sources })
      await loadConversations()
    }
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，服务暂时不可用，请稍后再试。' })
  } finally {
    thinking.value = false
    scrollBottom()
  }
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
  loadConversations()
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

/* Markdown 渲染样式 */
:deep(.ai-markdown h1) { font-size: 1.4em; font-weight: 700; margin: 0.8em 0 0.4em; }
:deep(.ai-markdown h2) { font-size: 1.2em; font-weight: 600; margin: 0.6em 0 0.3em; }
:deep(.ai-markdown h3) { font-size: 1.1em; font-weight: 600; margin: 0.5em 0 0.2em; }
:deep(.ai-markdown ul, .ai-markdown ol) { padding-left: 1.5em; margin: 0.4em 0; }
:deep(.ai-markdown li) { margin: 0.2em 0; }
:deep(.ai-markdown table) { border-collapse: collapse; width: 100%; margin: 0.5em 0; font-size: 0.9em; }
:deep(.ai-markdown th) { background: #f8fafc; border: 1px solid #e2e8f0; padding: 6px 10px; text-align: left; font-weight: 600; }
:deep(.ai-markdown td) { border: 1px solid #e2e8f0; padding: 5px 10px; }
.dark :deep(.ai-markdown th) { background: #1e293b; border-color: #334155; }
.dark :deep(.ai-markdown td) { border-color: #334155; }
:deep(.ai-markdown pre) { background: #1e293b; color: #a5d6ff; padding: 12px; border-radius: 8px; overflow-x: auto; margin: 0.5em 0; font-size: 0.85em; }
:deep(.ai-markdown code) { font-family: monospace; font-size: 0.9em; }
:deep(.ai-markdown p > code, .ai-markdown li > code) { background: #f1f5f9; color: #e11d48; padding: 1px 5px; border-radius: 4px; }
.dark :deep(.ai-markdown p > code), .dark :deep(.ai-markdown li > code) { background: #1e293b; color: #fda4af; }
:deep(.ai-markdown blockquote) { border-left: 3px solid #818cf8; padding-left: 12px; margin: 0.5em 0; color: #64748b; }
:deep(.ai-markdown strong) { font-weight: 700; color: #1e293b; }
.dark :deep(.ai-markdown strong) { color: #f1f5f9; }
</style>
