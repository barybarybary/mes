<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <div>
        <h2 class="text-xl font-bold text-slate-800 dark:text-slate-200">预警中心</h2>
        <p class="text-sm text-slate-400 mt-1">库存预警、交期预警、质量异常实时监控</p>
      </div>
      <div class="flex items-center gap-3">
        <el-select v-model="alertLevel" class="w-28" @change="fetchData">
          <el-option label="全部级别" value="" />
          <el-option label="严重" value="critical" />
          <el-option label="警告" value="warning" />
          <el-option label="提示" value="info" />
        </el-select>
        <el-button @click="fetchData" :loading="loading" :icon="Refresh" round>刷新</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="mb-5">
      <el-col :span="6" v-for="s in stats" :key="s.label">
        <div class="rounded-2xl p-5 shadow-sm border card-hover" :class="s.bgClass">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-xs font-medium uppercase opacity-70">{{ s.label }}</p>
              <p class="text-3xl font-bold mt-2">{{ s.value }}</p>
            </div>
            <el-icon :size="32" class="opacity-40"><component :is="s.icon" /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 预警列表 -->
    <el-card shadow="never" class="dark:bg-slate-800 dark:border-slate-700">
      <template #header><h3 class="font-semibold text-slate-800 dark:text-slate-200">预警明细</h3></template>
      <el-table :data="alerts" stripe v-loading="loading" class="page-table">
        <el-table-column label="级别" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.level === 'critical'" type="danger" round size="small">严重</el-tag>
            <el-tag v-else-if="row.level === 'warning'" type="warning" round size="small">警告</el-tag>
            <el-tag v-else type="info" round size="small">提示</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.category === 'order'" type="danger" effect="light" round size="small">订单</el-tag>
            <el-tag v-else-if="row.category === 'stock'" type="warning" effect="light" round size="small">库存</el-tag>
            <el-tag v-else-if="row.category === 'quality'" type="primary" effect="light" round size="small">质量</el-tag>
            <el-tag v-else type="info" effect="light" round size="small">{{ row.category || '其他' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="预警标题" min-width="160" show-overflow-tooltip>
          <template #default="{ row }"><span class="font-medium text-slate-700 dark:text-slate-200">{{ row.title }}</span></template>
        </el-table-column>
        <el-table-column prop="content" label="详情" min-width="260" show-overflow-tooltip>
          <template #default="{ row }"><span class="text-sm text-slate-500">{{ row.content }}</span></template>
        </el-table-column>
        <el-table-column prop="createTime" label="触发时间" width="170" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isRead === 0" type="danger" round size="small">未处理</el-tag>
            <el-tag v-else type="success" round size="small">已处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <button v-if="row.isRead === 0" class="action-link warning" @click="ack(row)">确认</button>
            <span v-else class="text-xs text-slate-400">—</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="mt-5 flex justify-end">
        <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize" layout="prev, pager, next, total" background @current-change="fetchData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import api from '@/api'

const loading = ref(false), alertLevel = ref(''), page = ref(1), total = ref(0), pageSize = ref(15)
const alerts = ref([])

const stats = reactive([
  { label: '严重预警', value: 0, icon: 'WarningFilled', bgClass: 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800 text-red-700 dark:text-red-300' },
  { label: '警告', value: 0, icon: 'Warning', bgClass: 'bg-amber-50 dark:bg-amber-900/20 border-amber-200 dark:border-amber-800 text-amber-700 dark:text-amber-300' },
  { label: '提示', value: 0, icon: 'InfoFilled', bgClass: 'bg-blue-50 dark:bg-blue-900/20 border-blue-200 dark:border-blue-800 text-blue-700 dark:text-blue-300' },
  { label: '已处理', value: 0, icon: 'CircleCheckFilled', bgClass: 'bg-emerald-50 dark:bg-emerald-900/20 border-emerald-200 dark:border-emerald-800 text-emerald-700 dark:text-emerald-300' }
])

async function fetchData() {
  loading.value = true
  try {
    const res = await api.get('/bi/alerts', {
      params: { page: page.value, pageSize: pageSize.value, level: alertLevel.value || undefined }
    })
    if (res.code === 200) {
      const data = res.data
      alerts.value = (data?.list || []).map(function(r) {
        return {
          id: r.id,
          category: r.category || '',
          title: r.title || '',
          content: r.content || '',
          level: r.level || 'info',
          isRead: r.isRead !== undefined ? r.isRead : 0,
          createTime: r.createTime || ''
        }
      })
      total.value = data?.total || 0

      // 统计各类别数量
      const allRecords = alerts.value
      stats[0].value = allRecords.filter(function(a) { return a.level === 'critical' }).length
      stats[1].value = allRecords.filter(function(a) { return a.level === 'warning' }).length
      stats[2].value = allRecords.filter(function(a) { return a.level === 'info' }).length
      stats[3].value = allRecords.filter(function(a) { return a.isRead === 1 }).length
    }
    // 加载未读数
    const cnt = await api.get('/bi/alerts/count')
    if (cnt.code === 200) {
      stats[0].value = cnt.data.unread || 0
    }
  } catch (e) {
    console.error('Alert load error:', e)
  }
  finally { loading.value = false }
}

async function ack(row) {
  await api.put('/bi/alerts/' + row.id + '/read')
  ElMessage.success('已确认')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.action-link {
  background: none; border: none; padding: 0; font-size: 13px; font-weight: 500; cursor: pointer; transition: color 0.15s; outline: none;
}
.action-link.warning { color: #f59e0b; }
.action-link.warning:hover { color: #b45309; }
</style>
