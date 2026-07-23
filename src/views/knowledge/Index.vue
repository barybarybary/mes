<template>
  <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100 dark:border-slate-700 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-lg font-semibold text-slate-800 dark:text-slate-200">知识库管理</h2>
        <p class="text-xs text-slate-400 dark:text-slate-300 mt-1">管理文档知识库，支持多种格式上传和自动切片</p>
      </div>
      <div class="flex items-center gap-3">
        <el-select v-model="category" placeholder="全部分类" clearable class="w-36" @change="fetchData">
          <el-option label="作业指导书" value="sop" />
          <el-option label="规格书" value="spec" />
          <el-option label="设备手册" value="manual" />
          <el-option label="其他" value="other" />
        </el-select>
        <el-input
          v-model="keyword"
          placeholder="搜索文档"
          clearable
          class="w-48"
          @change="fetchData"
        >
          <template #prefix>
            <el-icon class="text-slate-400 dark:text-slate-300"><Search /></el-icon>
          </template>
        </el-input>
        <el-upload :show-file-list="false" :http-request="handleUpload" accept=".txt,.md,.pdf,.docx,.xlsx,.csv,.json">
          <el-button type="primary" class="h-10 px-5 rounded-xl font-medium">
            <el-icon class="mr-1"><Upload /></el-icon>
            上传文档
          </el-button>
        </el-upload>
      </div>
    </div>

    <div class="p-6">
      <el-table :data="list" v-loading="loading" class="page-table" stripe>
        <el-table-column prop="title" label="文档标题" min-width="200">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <div :class="['w-10 h-10 rounded-xl flex items-center justify-center text-white text-lg', getFileIconBg(row.fileType)]">
                {{ getFileIcon(row.fileType) }}
              </div>
              <div>
                <div class="font-medium text-slate-700 dark:text-slate-600">{{ row.title }}</div>
                <div class="text-xs text-slate-400 dark:text-slate-300 mt-0.5">{{ row.fileType?.toUpperCase() }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)" effect="light" round size="small">
              {{ getCategoryLabel(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="chunkCount" label="切片数" width="100" align="center">
          <template #default="{ row }">
            <span class="font-medium text-slate-600 dark:text-slate-600">{{ row.chunkCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="info" effect="light" round size="small">待处理</el-tag>
            <el-tag v-else-if="row.status === 3" type="success" effect="light" round size="small">已完成</el-tag>
            <el-tag v-else type="warning" effect="light" round size="small">处理中</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-3">
              <button class="action-link primary" @click="showChunks(row)">查看切片</button>
              <button class="action-link danger" @click="del(row.id)">删除</button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-5 flex justify-end">
        <el-pagination
          v-model:current-page="page"
          :total="total"
          :page-size="pageSize"
          layout="prev, pager, next, total"
          background
          @current-change="fetchData"
        />
      </div>
    </div>

    <el-dialog v-model="chunkVisible" title="文档切片" width="700px" class="custom-dialog">
      <p class="text-sm text-slate-500 dark:text-slate-300 mb-4">文档已自动切分为 {{ chunks.length }} 个片段，用于 AI 知识库检索。</p>
      <div class="max-h-96 overflow-y-auto space-y-3">
        <div v-for="(c, i) in chunks" :key="i" class="p-4 bg-slate-50 dark:bg-slate-900 rounded-xl border border-slate-100 dark:border-slate-700 hover:border-sky-200 hover:bg-sky-50/30 transition-all">
          <div class="flex items-center gap-2 mb-2">
            <span class="inline-flex items-center justify-center w-6 h-6 bg-sky-100 text-sky-600 text-xs font-medium rounded-full">
              {{ c.chunkIndex + 1 }}
            </span>
            <span class="text-xs text-slate-400 dark:text-slate-300">第 {{ c.chunkIndex + 1 }} 片</span>
          </div>
          <p class="text-sm text-slate-600 dark:text-slate-600 leading-relaxed">{{ c.content.substring(0, 300) }}{{ c.content.length > 300 ? '...' : '' }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Upload } from '@element-plus/icons-vue'
import api from '@/api'

const list = ref([]), loading = ref(false), page = ref(1), total = ref(0), pageSize = ref(10)
const category = ref(), keyword = ref('')
const chunkVisible = ref(false), chunks = ref([])

const categoryMap = {
  sop: { label: '作业指导书', type: 'primary' },
  spec: { label: '规格书', type: 'success' },
  manual: { label: '设备手册', type: 'warning' },
  other: { label: '其他', type: 'info' }
}

function getCategoryLabel(cat) {
  return categoryMap[cat]?.label || cat
}

function getCategoryType(cat) {
  return categoryMap[cat]?.type || 'info'
}

function getFileIcon(type) {
  const icons = { pdf: '📄', docx: '📝', txt: '📃', md: '📋', csv: '📊', json: '⚙️' }
  return icons[type] || '📁'
}

function getFileIconBg(type) {
  const colors = {
    pdf: 'bg-gradient-to-br from-red-400 to-red-600',
    docx: 'bg-gradient-to-br from-blue-400 to-blue-600',
    txt: 'bg-gradient-to-br from-slate-400 to-slate-600',
    md: 'bg-gradient-to-br from-purple-400 to-purple-600',
    csv: 'bg-gradient-to-br from-green-400 to-green-600',
    json: 'bg-gradient-to-br from-amber-400 to-amber-600'
  }
  return colors[type] || 'bg-gradient-to-br from-slate-400 to-slate-600'
}

async function fetchData() {
  loading.value = true
  try {
    const r = await api.get('/knowledge', { params: { page: page.value, pageSize: pageSize.value, category: category.value, keyword: keyword.value } })
    if (r.code === 200) { list.value = r.data.list; total.value = r.data.total }
  } finally { loading.value = false }
}

async function handleUpload({ file }) {
  const fd = new FormData()
  fd.append('file', file)
  fd.append('category', category.value || 'other')
  await api.post('/knowledge/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
  ElMessage.success('上传成功，文档已自动切片')
  fetchData()
}

async function showChunks(row) {
  chunks.value = (await api.get(`/knowledge/${row.id}/chunks`)).data || []
  chunkVisible.value = true
}

async function del(id) {
  await ElMessageBox.confirm('确定要删除该文档吗？删除后不可恢复。', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.delete(`/knowledge/${id}`)
  ElMessage.success('已删除')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.action-link {
  background: none;
  border: none;
  padding: 0;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.15s;
  outline: none;
}
.action-link.primary { color: #3b82f6; }
.action-link.primary:hover { color: #1d4ed8; }
.action-link.danger { color: #f43f5e; }
.action-link.danger:hover { color: #be123c; }

:deep(.page-table th.el-table__cell) {
  background-color: #f8fafc !important;
  color: #475569 !important;
  font-weight: 600 !important;
  font-size: 13px !important;
}

:deep(.custom-dialog .el-dialog) {
  border-radius: 16px !important;
}

:deep(.custom-dialog .el-dialog__header) {
  padding: 20px 24px 16px !important;
  margin-right: 0 !important;
  border-bottom: 1px solid #f1f5f9;
}

:deep(.custom-dialog .el-dialog__body) {
  padding: 24px !important;
}

:deep(.custom-dialog .el-dialog__footer) {
  padding: 16px 24px 20px !important;
  border-top: 1px solid #f1f5f9;
}
</style>
