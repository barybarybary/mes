<template>
  <div>
    <!-- 标题栏 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h2 class="text-xl font-bold text-slate-800 dark:text-slate-200">👋 你好，{{ userName }}</h2>
        <p class="text-sm text-slate-400 mt-1">{{ currentDate }}</p>
      </div>
      <el-button type="primary" @click="refreshAll" :loading="loading" :icon="Refresh" round>
        刷新数据
      </el-button>
    </div>

    <!-- AI 摘要栏 -->
    <div v-if="aiSummary" class="mb-5 bg-blue-50 dark:bg-blue-950 border border-blue-100 dark:border-blue-900 rounded-xl p-4 flex items-start gap-3">
      <span class="text-xl shrink-0">🤖</span>
      <div>
        <p class="text-xs text-blue-500 dark:text-blue-400 font-medium mb-1">AI 生产摘要</p>
        <p class="text-sm text-slate-700 dark:text-slate-300 leading-relaxed" v-html="aiSummary"></p>
      </div>
    </div>

    <!-- KPI 统计卡片 -->
    <el-row :gutter="16" class="mb-5">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <div class="bg-white dark:bg-slate-800 rounded-2xl p-4 shadow-sm border border-slate-100 dark:border-slate-700 card-hover cursor-pointer" @click="goPage(c.path)">
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs text-slate-400 font-medium uppercase tracking-wide">{{ c.label }}</span>
            <div class="w-8 h-8 rounded-lg flex items-center justify-center" :style="{ background: c.bgColor }">
              <el-icon :size="16" :color="c.color"><component :is="c.icon" /></el-icon>
            </div>
          </div>
          <p class="text-2xl font-bold text-slate-800 dark:text-slate-100">{{ c.value }}</p>
          <p v-if="c.subLabel" class="text-xs text-slate-400 mt-1">{{ c.subLabel }}</p>
        </div>
      </el-col>
    </el-row>

    <!-- Row 1: 产量趋势 + 不良原因 -->
    <el-row :gutter="16" class="mb-5">
      <el-col :span="12">
        <el-card class="dark:bg-slate-800 dark:border-slate-700" shadow="never">
          <template #header>
            <div>
              <h3 class="font-semibold text-slate-800 dark:text-slate-200">📈 产量趋势</h3>
              <p class="text-xs text-slate-400 mt-0.5">最近7天每日产量</p>
            </div>
          </template>
          <div ref="productionTrendChart" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="dark:bg-slate-800 dark:border-slate-700" shadow="never">
          <template #header>
            <div>
              <h3 class="font-semibold text-slate-800 dark:text-slate-200">⚠️ 不良原因分析</h3>
              <p class="text-xs text-slate-400 mt-0.5">最近30天不良品原因分布</p>
            </div>
          </template>
          <div ref="defectCauseChart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Row 2: 工单进度 -->
    <el-row :gutter="16" class="mb-5">
      <el-col :span="24">
        <el-card class="dark:bg-slate-800 dark:border-slate-700" shadow="never">
          <template #header>
            <div class="flex items-center justify-between">
              <div>
                <h3 class="font-semibold text-slate-800 dark:text-slate-200">🏭 在产工单进度</h3>
                <p class="text-xs text-slate-400 mt-0.5">进行中工单完成情况</p>
              </div>
              <el-button type="primary" @click="goPage('/production/work-order')">查看全部 →</el-button>
            </div>
          </template>
          <div ref="orderProgressChart" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Row 3: 两张表格 -->
    <el-row :gutter="16" class="mb-5">
      <!-- 待处理工单 -->
      <el-col :span="12">
        <el-card class="dark:bg-slate-800 dark:border-slate-700" shadow="never">
          <template #header>
            <div class="flex items-center justify-between">
              <div>
                <h3 class="font-semibold text-slate-800 dark:text-slate-200">📋 待处理工单</h3>
                <p class="text-xs text-slate-400 mt-0.5">状态为待生产的工单</p>
              </div>
              <el-button type="primary" @click="goPage('/production/work-order')">查看全部 →</el-button>
            </div>
          </template>
          <el-table :data="pendingOrderList" size="small" stripe max-height="340" class="page-table">
            <el-table-column prop="orderNo" label="工单号" width="160" show-overflow-tooltip />
            <el-table-column prop="productName" label="产品" min-width="120" show-overflow-tooltip />
            <el-table-column prop="quantity" label="计划数" width="80" align="center" />
            <el-table-column prop="planEnd" label="计划交期" width="110">
              <template #default="{ row }">
                <span :class="{ 'text-red-500 font-medium': isOverdue(row.planEnd) }">{{ row.planEnd || '-' }}</span>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!pendingOrderList.length" class="text-center py-8 text-slate-400 text-sm">暂无待处理工单</div>
        </el-card>
      </el-col>

      <!-- 最近报工 -->
      <el-col :span="12">
        <el-card class="dark:bg-slate-800 dark:border-slate-700" shadow="never">
          <template #header>
            <div class="flex items-center justify-between">
              <div>
                <h3 class="font-semibold text-slate-800 dark:text-slate-200">📝 最近报工</h3>
                <p class="text-xs text-slate-400 mt-0.5">最新10条报工记录</p>
              </div>
              <el-button type="primary" @click="goPage('/production/report')">查看全部 →</el-button>
            </div>
          </template>
          <el-table :data="recentReportList" size="small" stripe max-height="340" class="page-table">
            <el-table-column prop="worker" label="报工人" width="80" />
            <el-table-column prop="productName" label="产品" min-width="100" show-overflow-tooltip />
            <el-table-column prop="processName" label="工序" width="80" show-overflow-tooltip />
            <el-table-column label="合格/总数" width="100" align="center">
              <template #default="{ row }">
                <span :class="scrapClass(row)">{{ row.qualifiedQty || 0 }}/{{ row.quantity || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="reportDate" label="日期" width="100" />
          </el-table>
          <div v-if="!recentReportList.length" class="text-center py-8 text-slate-400 text-sm">暂无报工记录</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'
import api from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const userName = computed(() => userStore.user?.nickname || userStore.user?.username || '用户')
const currentDate = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
})

const loading = ref(false)
const aiSummary = ref('')
const pendingOrderList = ref([])
const recentReportList = ref([])

// KPI 卡片
const cards = reactive([
  { label: '在产工单', value: 0, subLabel: '单', icon: 'Monitor', color: '#3b82f6', bgColor: 'rgba(59,130,246,0.1)', path: '/production/work-order' },
  { label: '今日报工', value: 0, subLabel: '件', icon: 'Box', color: '#10b981', bgColor: 'rgba(16,185,129,0.1)', path: '/production/report' },
  { label: '今日不良', value: 0, subLabel: '件', icon: 'WarningFilled', color: '#f43f5e', bgColor: 'rgba(244,63,94,0.1)', path: '/production/qc' },
  { label: '不良率', value: '0%', subLabel: '%', icon: 'DataAnalysis', color: '#f59e0b', bgColor: 'rgba(245,158,11,0.1)', path: '' }
])

function goPage(path) { if (path) router.push(path) }
function isOverdue(date) { if (!date) return false; return new Date(date) < new Date() }
function scrapClass(row) {
  if (!row.quantity || row.quantity === 0) return ''
  return (row.scrapQty || 0) > 0 ? 'text-orange-500 font-medium' : 'text-emerald-600'
}

// 图表 refs
const productionTrendChart = ref(null)
const defectCauseChart = ref(null)
const orderProgressChart = ref(null)
let chartInstances = []

function getChart(elRef) {
  if (!elRef) return null
  const dom = elRef
  const existing = echarts.getInstanceByDom(dom)
  if (existing) existing.dispose()
  const instance = echarts.init(dom)
  chartInstances.push(instance)
  return instance
}

function disposeAll() {
  chartInstances.forEach(c => { try { c.dispose() } catch (e) { /* ignore */ } })
  chartInstances = []
}

async function loadMesSummary() {
  try {
    const res = await api.get('/dashboard/mes-summary')
    if (res.code === 200 && res.data) {
      const d = res.data
      cards[0].value = d.inProgressOrders ?? 0
      cards[1].value = d.todayOutput ?? 0
      cards[2].value = d.todayDefect ?? 0
      cards[3].value = (d.defectRate ?? 0) + '%'
      pendingOrderList.value = d.pendingOrderList || []
      recentReportList.value = d.recentReportList || []
      await nextTick()
      if (d.productionTrend?.length) renderProductionTrend(productionTrendChart.value, d.productionTrend)
      if (d.defectCauseList?.length) renderDefectCause(defectCauseChart.value, d.defectCauseList)
      if (d.orderProgress?.length) renderOrderProgress(orderProgressChart.value, d.orderProgress)
    }
  } catch (e) { console.error('加载MES概览失败', e) }
}

async function loadAiSummary() {
  try {
    const userId = JSON.parse(localStorage.getItem('user') || '{}').id || 1
    const res = await api.post('/ai/chat', { userId, question: '今天生产情况怎么样' })
    if (res.code === 200) aiSummary.value = (res.data.content || '').replace(/\n/g, '<br>')
  } catch (e) { aiSummary.value = '' }
}

function renderProductionTrend(el, data) {
  if (!el || !data?.length) return
  const chart = getChart(el)
  const dates = data.map(d => d.date?.substring(5) || '')
  const outputs = data.map(d => d.output || 0)
  chart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(30,41,59,0.9)', borderColor: '#334155', textStyle: { color: '#e2e8f0', fontSize: 12 } },
    grid: { left: 50, right: 20, top: 15, bottom: 25 },
    xAxis: { type: 'category', data: dates, axisLabel: { fontSize: 10, color: '#94a3b8' }, axisLine: { lineStyle: { color: '#e2e8f0' } } },
    yAxis: { type: 'value', axisLabel: { fontSize: 10, color: '#94a3b8' }, splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } } },
    series: [{
      data: outputs, type: 'line', smooth: true, symbol: 'circle', symbolSize: 5,
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(59,130,246,0.25)' }, { offset: 1, color: 'rgba(59,130,246,0.02)' }]) },
      lineStyle: { color: '#3b82f6', width: 2 }, itemStyle: { color: '#3b82f6' }
    }]
  })
}

function renderDefectCause(el, data) {
  if (!el || !data?.length) return
  const chart = getChart(el)
  const colors = ['#f43f5e', '#f59e0b', '#3b82f6', '#8b5cf6', '#10b981', '#ec4899', '#14b8a6', '#6366f1']
  chart.setOption({
    tooltip: { trigger: 'item', backgroundColor: 'rgba(30,41,59,0.95)', borderColor: '#334155', textStyle: { color: '#e2e8f0', fontSize: 12 }, formatter: p => `<b>${p.name}</b><br/>数量: <b>${p.value}</b> (${p.percent}%)` },
    legend: { bottom: 0, left: 'center', textStyle: { fontSize: 10, color: '#64748b' }, itemWidth: 10, itemHeight: 10 },
    series: [{
      type: 'pie', radius: ['45%', '72%'], center: ['50%', '45%'], avoidLabelOverlap: false,
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { scaleSize: 6, label: { show: true, fontSize: 13, fontWeight: 'bold' } },
      data: data.map((d, i) => ({ name: d.cause || d.name || '', value: d.count || d.value || 0, itemStyle: { color: colors[i % colors.length] } })),
      color: colors
    }]
  })
}

function renderOrderProgress(el, data) {
  if (!el || !data?.length) return
  const chart = getChart(el)
  const sorted = [...data].sort((a, b) => (a.progress || 0) - (b.progress || 0))
  const names = sorted.map(d => {
    const no = d.orderNo || ''
    const name = d.productName || ''
    return (no + ' ' + name).trim() || '-'
  })
  const values = sorted.map(d => d.progress || 0)
  chart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(30,41,59,0.9)', borderColor: '#334155', textStyle: { color: '#e2e8f0', fontSize: 12 }, formatter: params => `${params[0].name}<br/>完成进度: <b>${params[0].value}%</b>` },
    grid: { left: 150, right: 55, top: 8, bottom: 15 },
    xAxis: { type: 'value', max: 100, axisLabel: { fontSize: 10, color: '#94a3b8', formatter: '{value}%' }, splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } } },
    yAxis: { type: 'category', data: names, axisLabel: { fontSize: 10, color: '#475569', width: 130, overflow: 'truncate' }, axisLine: { show: false }, axisTick: { show: false } },
    series: [{
      type: 'bar', data: values, barWidth: '55%',
      itemStyle: { borderRadius: [0, 5, 5, 0], color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#10b981' }, { offset: 1, color: '#34d399' }]) },
      label: { show: true, position: 'right', fontSize: 10, color: '#64748b', formatter: '{c}%' }
    }]
  })
}

async function refreshAll() {
  loading.value = true
  try { await Promise.all([loadMesSummary(), loadAiSummary()]) }
  finally { loading.value = false }
}

function handleResize() {
  chartInstances.forEach(c => { try { c.resize() } catch (e) { /* ignore */ } })
}

onMounted(() => {
  refreshAll()
  window.addEventListener('resize', handleResize)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeAll()
})
</script>
