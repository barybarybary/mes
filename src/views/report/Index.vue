<template>
  <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100 dark:border-slate-700">
      <h2 class="text-lg font-semibold text-slate-800 dark:text-slate-200">报表中心</h2>
      <p class="text-xs text-slate-400 dark:text-slate-300 mt-1">AI 智能报表生成、下载与定时发送</p>
    </div>

    <el-tabs v-model="activeTab" class="px-6 pt-4">
      <!-- ==================== Tab 1: 报表记录 ==================== -->
      <el-tab-pane label="报表记录" name="records">
        <el-table :data="records" border stripe v-loading="loading" empty-text="暂无报表，请在AI助手中对话生成或点击「手动生成」">
          <el-table-column prop="title" label="标题" min-width="200" />
          <el-table-column prop="reportType" label="类型" width="110">
            <template #default="{ row }">
              <el-tag size="small" :type="typeTag(row.reportType)">{{ typeName(row.reportType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="timeRange" label="时间范围" width="130" />
          <el-table-column prop="fileSize" label="大小" width="90">
            <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="生成时间" width="170" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="downloadReport(row.id)" :icon="Download" text>下载</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ==================== Tab 2: 定时配置 ==================== -->
      <el-tab-pane label="定时发送" name="schedules">
        <div class="mb-4 flex justify-end">
          <el-button type="primary" @click="openScheduleDialog()" :icon="Plus" size="small">新增定时</el-button>
        </div>
        <el-table :data="schedules" border stripe v-loading="schedLoading" empty-text="暂无定时配置">
          <el-table-column prop="reportTitle" label="名称" min-width="150" />
          <el-table-column prop="reportType" label="类型" width="110">
            <template #default="{ row }">
              <el-tag size="small" :type="typeTag(row.reportType)">{{ typeName(row.reportType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="cronExpr" label="Cron" width="140">
            <template #default="{ row }">
              <code class="text-xs bg-slate-100 dark:bg-slate-700 px-1.5 py-0.5 rounded">{{ row.cronExpr }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="recipients" label="额外收件人" min-width="160" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button size="small" @click="openScheduleDialog(row)" text>编辑</el-button>
              <el-button size="small" type="danger" @click="deleteSchedule(row.id)" text>删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ==================== Tab 3: 手动生成 ==================== -->
      <el-tab-pane label="手动生成" name="generate">
        <el-form :model="genForm" label-width="100px" class="max-w-lg">
          <el-form-item label="报表类型">
            <el-select v-model="genForm.reportType" class="w-full">
              <el-option value="sales" label="销售报表" />
              <el-option value="production" label="生产报表" />
              <el-option value="inventory" label="库存报表" />
              <el-option value="summary" label="综合报表" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-select v-model="genForm.timeRange" class="w-full">
              <el-option value="本周" label="本周" />
              <el-option value="本月" label="本月" />
              <el-option value="上月" label="上月" />
              <el-option value="近7天" label="近7天" />
              <el-option value="近30天" label="近30天" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="doGenerate" :loading="genLoading">生成报表</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <!-- 定时配置弹窗 -->
    <el-dialog v-model="schedVisible" :title="editingSchedule ? '编辑定时' : '新增定时'" width="540px" class="custom-dialog">
      <el-form :model="schedForm" label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="schedForm.reportTitle" placeholder="如：每日生产报表" />
        </el-form-item>
        <el-form-item label="报表类型">
          <el-select v-model="schedForm.reportType" class="w-full">
            <el-option value="sales" label="销售报表" />
            <el-option value="production" label="生产报表" />
            <el-option value="inventory" label="库存报表" />
            <el-option value="summary" label="综合报表" />
          </el-select>
        </el-form-item>
        <el-form-item label="Cron表达式">
          <el-input v-model="schedForm.cronExpr" placeholder="0 9 * * * (每天9点)" />
          <p class="text-xs text-slate-400 mt-1">
            每天9点: <code>0 9 * * *</code> &nbsp; 每周一9点: <code>0 9 * * 1</code> &nbsp; 每月1号9点: <code>0 9 1 * *</code>
          </p>
        </el-form-item>
        <el-form-item label="额外收件人">
          <el-input v-model="schedForm.recipients" placeholder="邮箱逗号分隔，留空则只发给自己" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="schedForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="schedVisible = false" text>取消</el-button>
        <el-button type="primary" @click="saveSchedule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Plus } from '@element-plus/icons-vue'
import api from '@/api'

const activeTab = ref('records')

// ====== 报表记录 ======
const records = ref([]), loading = ref(false)
async function loadRecords() {
  loading.value = true
  try {
    const r = await api.get('/report/records')
    if (r.code === 200) records.value = r.data || []
  } finally { loading.value = false }
}

function downloadReport(id) {
  const token = sessionStorage.getItem('token') || localStorage.getItem('token')
  const a = document.createElement('a')
  a.href = `/api/report/download/${id}`
  // Use fetch for blob download
  fetch(`/api/report/download/${id}`, { headers: { 'Authorization': `Bearer ${token}` } })
    .then(res => {
      if (res.status === 403) { ElMessage.error('无权下载'); return }
      return res.blob().then(blob => {
        const url = URL.createObjectURL(blob)
        a.href = url
        const disp = res.headers.get('Content-Disposition') || ''
        const m = disp.match(/filename\*?=(?:UTF-8'')?([^;]+)/)
        a.download = m ? decodeURIComponent(m[1].replace(/"/g, '')) : 'report.xlsx'
        document.body.appendChild(a); a.click()
        document.body.removeChild(a); URL.revokeObjectURL(url)
      })
    })
    .catch(() => ElMessage.error('下载失败'))
}

// ====== 手动生成 ======
const genForm = reactive({ reportType: 'summary', timeRange: '本月' })
const genLoading = ref(false)
async function doGenerate() {
  genLoading.value = true
  try {
    const r = await api.post('/report/generate', { ...genForm })
    if (r.code === 200) {
      ElMessage.success('报表生成成功！')
      downloadReport(r.data.id)
      loadRecords()
    }
  } catch (e) {
    ElMessage.error('生成失败')
  } finally { genLoading.value = false }
}

// ====== 定时配置 ======
const schedules = ref([]), schedLoading = ref(false)
const schedVisible = ref(false), editingSchedule = ref(null)
const schedForm = reactive({ reportTitle: '', reportType: 'summary', cronExpr: '', recipients: '', status: 1 })

async function loadSchedules() {
  schedLoading.value = true
  try {
    const r = await api.get('/report/schedules')
    if (r.code === 200) schedules.value = r.data || []
  } finally { schedLoading.value = false }
}

function openScheduleDialog(row) {
  if (row) {
    editingSchedule.value = row
    Object.assign(schedForm, row)
  } else {
    editingSchedule.value = null
    Object.assign(schedForm, { reportTitle: '', reportType: 'summary', cronExpr: '', recipients: '', status: 1 })
  }
  schedVisible.value = true
}

async function saveSchedule() {
  try {
    if (editingSchedule.value) {
      await api.put(`/report/schedule/${editingSchedule.value.id}`, { ...schedForm })
    } else {
      await api.post('/report/schedule', { ...schedForm })
    }
    ElMessage.success('保存成功')
    schedVisible.value = false
    loadSchedules()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function deleteSchedule(id) {
  try {
    await ElMessageBox.confirm('确定删除此定时配置？', '确认', { type: 'warning' })
    await api.delete(`/report/schedule/${id}`)
    ElMessage.success('已删除')
    loadSchedules()
  } catch (e) { /* cancelled */ }
}

// ====== 工具 ======
function typeName(t) {
  return { sales: '销售', production: '生产', inventory: '库存', summary: '综合' }[t] || t
}
function typeTag(t) {
  return { sales: 'primary', production: 'success', inventory: 'warning', summary: '' }[t] || 'info'
}
function formatSize(bytes) {
  if (!bytes) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1048576).toFixed(1) + ' MB'
}

onMounted(() => { loadRecords(); loadSchedules() })
</script>

<style scoped>
:deep(.el-tabs__nav-wrap::after) { height: 1px; }
:deep(.custom-dialog .el-dialog) { border-radius: 16px !important; }
:deep(.custom-dialog .el-dialog__header) { padding: 20px 24px 16px !important; border-bottom: 1px solid #f1f5f9; }
:deep(.custom-dialog .el-dialog__body) { padding: 24px !important; }
:deep(.custom-dialog .el-dialog__footer) { padding: 16px 24px 20px !important; border-top: 1px solid #f1f5f9; }
</style>
