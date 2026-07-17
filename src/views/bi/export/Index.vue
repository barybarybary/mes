<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <div>
        <h2 class="text-xl font-bold text-slate-800 dark:text-slate-200">报表导出</h2>
        <p class="text-sm text-slate-400 mt-1">一键导出经营报表，支持 Excel 格式</p>
      </div>
    </div>

    <!-- 导出卡片列表 -->
    <el-row :gutter="16" class="mb-5">
      <el-col :span="8" v-for="r in reports" :key="r.key">
        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-100 dark:border-slate-700 card-hover">
          <div class="w-12 h-12 rounded-xl flex items-center justify-center mb-4" :style="{ background: r.bg }">
            <el-icon :size="24" :color="r.color"><component :is="r.icon" /></el-icon>
          </div>
          <h3 class="font-semibold text-slate-800 dark:text-slate-200 mb-1">{{ r.title }}</h3>
          <p class="text-xs text-slate-400 mb-4">{{ r.desc }}</p>
          <div>
            <el-button size="small" type="primary" round @click="doExport(r.key)">
              <el-icon class="mr-1"><Download /></el-icon>导出 Excel
            </el-button>
          </div>
          <p v-if="r.lastExport" class="text-xs text-slate-400 mt-3">上次导出：{{ r.lastExport }}</p>
        </div>
      </el-col>
    </el-row>

    <!-- 导出历史 -->
    <el-card shadow="never" class="dark:bg-slate-800 dark:border-slate-700">
      <template #header><h3 class="font-semibold text-slate-800 dark:text-slate-200">导出记录</h3></template>
      <el-table :data="history" stripe v-loading="loading" class="page-table">
        <el-table-column prop="reportName" label="报表名称" min-width="180">
          <template #default="{ row }"><span class="font-medium text-slate-700 dark:text-slate-200">{{ row.reportName }}</span></template>
        </el-table-column>
        <el-table-column prop="format" label="格式" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="success" round size="small">Excel</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="text-sm text-sky-500 font-mono">{{ row.fileName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="导出时间" width="170" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <button class="action-link primary" @click="downloadFile(row)">下载</button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="history.length === 0 && !loading" class="text-center py-12 text-slate-400">
        <el-icon :size="48" class="mb-3"><Document /></el-icon>
        <p class="text-sm">暂无导出记录</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Document } from '@element-plus/icons-vue'
import api from '@/api'

const loading = ref(false)
const history = ref([])

const reports = reactive([
  { key: 'sales-summary', title: '销售汇总报表', desc: '销售额、订单数、客户排行汇总', icon: 'Sell', color: '#3b82f6', bg: 'rgba(59,130,246,0.1)', lastExport: '' },
  { key: 'inventory-report', title: '库存现状报表', desc: '各仓库存量、周转率、预警项', icon: 'Box', color: '#10b981', bg: 'rgba(16,185,129,0.1)', lastExport: '' },
  { key: 'production-report', title: '生产进度报表', desc: '工单状态、完成率、报工明细', icon: 'Monitor', color: '#8b5cf6', bg: 'rgba(139,92,246,0.1)', lastExport: '' }
])

async function doExport(reportKey) {
  try {
    const report = reports.find(function(r) { return r.key === reportKey })
    const typeMap = { 'sales-summary': 'sales', 'inventory-report': 'inventory', 'production-report': 'production' }
    const type = typeMap[reportKey] || reportKey
    const res = await api.get('/bi/export/' + type, {
      responseType: 'blob'
    })
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.download = reportKey + '_' + new Date().toISOString().slice(0, 10) + '.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success(report.title + '导出成功')
    if (report) report.lastExport = new Date().toLocaleString()
  } catch (e) {
    console.error('Export error:', e)
  }
}

async function fetchHistory() {
  loading.value = true
  try {
    const res = await api.get('/bi/schedule')
    if (res.code === 200) {
      const data = res.data
      history.value = (data || []).map(function(s) {
        return { id: s.id, reportName: s.name, format: s.reportFormat, fileName: s.type + '_report.xlsx', createTime: s.lastRunTime || s.createTime }
      })
    }
  } catch (e) {
    console.error('History load error:', e)
  }
  finally { loading.value = false }
}

function downloadFile(row) {
  const nameMap = { '库存': 'inventory', '生产': 'production', '销售': 'sales' }
  let type = 'sales'
  for (const key of Object.keys(nameMap)) {
    if (row.reportName && row.reportName.includes(key)) { type = nameMap[key]; break }
  }
  api.get('/bi/export/' + type, { responseType: 'blob' }).then(function(res) {
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.download = row.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  })
}

onMounted(fetchHistory)
</script>

<style scoped>
.action-link {
  background: none; border: none; padding: 0; font-size: 13px; font-weight: 500; cursor: pointer; transition: color 0.15s; outline: none;
}
.action-link.primary { color: #3b82f6; }
.action-link.primary:hover { color: #1d4ed8; }
</style>
