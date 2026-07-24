<template>
  <div>
    <!-- 标题栏 -->
    <div class="mb-6">
      <h2 class="text-xl font-bold text-slate-800 dark:text-slate-200">你好，{{ userName }}</h2>
      <p class="text-sm text-slate-400 mt-1">{{ currentDate }}</p>
    </div>

    <!-- KPI 卡片（6个，含订单通知和库存预警） -->
    <div class="grid grid-cols-2 lg:grid-cols-6 gap-4 mb-6">
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-5 shadow-sm border border-slate-100 dark:border-slate-700">
        <p class="text-sm text-slate-400 dark:text-slate-300 mb-2">待处理工单</p>
        <p class="text-3xl font-bold text-amber-500 tracking-tight">{{ cards.pendingOrders }}</p>
      </div>
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-5 shadow-sm border border-slate-100 dark:border-slate-700">
        <p class="text-sm text-slate-400 dark:text-slate-300 mb-2">生产中工单</p>
        <p class="text-3xl font-bold text-blue-500 tracking-tight">{{ cards.inProgressOrders }}</p>
      </div>
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-5 shadow-sm border border-slate-100 dark:border-slate-700">
        <p class="text-sm text-slate-400 dark:text-slate-300 mb-2">今日报工</p>
        <p class="text-3xl font-bold text-emerald-500 tracking-tight">{{ cards.todayOutput }}</p>
      </div>
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-5 shadow-sm border border-slate-100 dark:border-slate-700">
        <p class="text-sm text-slate-400 dark:text-slate-300 mb-2">不良品率</p>
        <p class="text-3xl font-bold tracking-tight" :class="defectColor">{{ cards.defectRate }}%</p>
        <p class="text-xs text-slate-400 mt-1">今日不良 {{ cards.todayDefect }}</p>
      </div>
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-5 shadow-sm border border-slate-100 dark:border-slate-700 cursor-pointer relative" :class="cards.unreadOrderNotifications > 0 ? 'border-sky-300 bg-sky-50 dark:bg-sky-900/20' : ''" @click="showNotifications = !showNotifications">
        <p class="text-sm text-slate-400 dark:text-slate-300 mb-2">📬 新订单通知</p>
        <p class="text-3xl font-bold tracking-tight" :class="cards.unreadOrderNotifications > 0 ? 'text-sky-500' : 'text-emerald-500'">{{ cards.unreadOrderNotifications ?? 0 }}</p>
        <p class="text-xs text-slate-400 mt-1" v-if="cards.unreadOrderNotifications > 0">有客户支付了新订单</p>
        <p class="text-xs text-slate-400 mt-1" v-else>无新通知</p>
        <span v-if="cards.unreadOrderNotifications > 0" class="absolute top-3 right-3 w-2.5 h-2.5 bg-red-500 rounded-full animate-pulse"></span>
      </div>
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-5 shadow-sm border border-slate-100 dark:border-slate-700 cursor-pointer" :class="cards.unresolvedAlerts > 0 ? 'border-red-200 bg-red-50 dark:bg-red-900/20' : ''" @click="$router.push('/inventory')">
        <p class="text-sm text-slate-400 dark:text-slate-300 mb-2">低库存预警</p>
        <p class="text-3xl font-bold tracking-tight" :class="cards.unresolvedAlerts > 0 ? 'text-red-500' : 'text-emerald-500'">{{ cards.unresolvedAlerts ?? cards.lowStockCount }}</p>
        <p class="text-xs text-slate-400 mt-1" v-if="cards.unresolvedAlerts > 0">点击前往库存页补货</p>
        <p class="text-xs text-slate-400 mt-1" v-else>库存充足</p>
      </div>
    </div>

    <!-- 低库存产品列表 -->
    <div v-if="lowStockProducts.length > 0" class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-red-200 mb-6">
      <div class="flex items-center justify-between mb-3">
        <div>
          <h3 class="font-semibold text-red-600">⚠️ 库存预警</h3>
          <p class="text-xs text-slate-400 mt-0.5">以下产品库存低于安全线（10），请及时补货</p>
        </div>
        <router-link to="/inventory" class="text-sky-500 text-sm hover:text-sky-600">前往库存管理 →</router-link>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
        <div v-for="p in lowStockProducts.slice(0, 6)" :key="p.productId" class="flex items-center justify-between p-3 bg-red-50 rounded-xl border border-red-100">
          <div class="min-w-0">
            <p class="text-sm font-medium text-slate-700 truncate">{{ p.productName }}</p>
            <p class="text-xs text-slate-400">{{ p.productCode }} | {{ p.warehouseName }}</p>
          </div>
          <span class="text-red-500 font-bold text-lg ml-3 shrink-0">{{ p.quantity }}</span>
        </div>
      </div>
      <p v-if="lowStockProducts.length > 6" class="text-xs text-slate-400 mt-3 text-center">
        还有 {{ lowStockProducts.length - 6 }} 个低库存产品…
      </p>
    </div>

    <!-- 最近订单通知 -->
    <div v-if="showNotifications && recentNotifications.length > 0" class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-sky-200 dark:border-sky-700 mb-6">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h3 class="font-semibold text-slate-800 dark:text-slate-200">📬 新订单通知</h3>
          <p class="text-xs text-slate-400 mt-0.5">客户已支付的订单</p>
        </div>
        <el-button text type="primary" size="small" @click="markAllRead">全部已读</el-button>
      </div>
      <div class="space-y-2">
        <div v-for="n in recentNotifications" :key="n.id" class="flex items-center justify-between p-3 bg-sky-50 dark:bg-sky-900/20 rounded-xl border border-sky-100 dark:border-sky-800">
          <div>
            <p class="text-sm font-medium text-slate-700 dark:text-slate-200">
              {{ n.customerName }} <span class="text-slate-400 font-normal text-xs ml-1">提交了新订单</span>
            </p>
            <p class="text-xs text-slate-400 mt-0.5">{{ n.orderNo }} · ¥{{ n.totalAmount }} · {{ formatTime(n.createTime) }}</p>
          </div>
          <el-button text type="primary" size="small" @click="markRead(n.id)">已读</el-button>
        </div>
      </div>
    </div>

    <!-- 产量趋势（全宽） -->
    <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-100 dark:border-slate-700 mb-6">
      <div class="mb-4">
        <h3 class="font-semibold text-slate-800 dark:text-slate-200">产量趋势</h3>
        <p class="text-xs text-slate-400 mt-0.5">近 7 天每日报工产量</p>
      </div>
      <div ref="trendChart" style="height: 320px"></div>
    </div>

    <!-- 下半区：生产进度 + 不良原因 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-6">
      <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-100 dark:border-slate-700">
        <div class="mb-4">
          <h3 class="font-semibold text-slate-800 dark:text-slate-200">生产进度</h3>
          <p class="text-xs text-slate-400 mt-0.5">进行中工单完成情况</p>
        </div>
        <div ref="progChart" style="height: 320px"></div>
      </div>

      <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-100 dark:border-slate-700">
        <div class="mb-4">
          <h3 class="font-semibold text-slate-800 dark:text-slate-200">不良原因</h3>
          <p class="text-xs text-slate-400 mt-0.5">近 30 天不良品原因分布</p>
        </div>
        <div v-if="defectCauses.length === 0" class="flex items-center justify-center h-80 text-slate-400 text-sm">
          暂无不良品记录
        </div>
        <div v-else ref="defectCauseChart" style="height: 320px"></div>
      </div>
    </div>

    <!-- 最近报工 -->
    <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-100 dark:border-slate-700">
      <div class="mb-4">
        <h3 class="font-semibold text-slate-800 dark:text-slate-200">最近报工</h3>
        <p class="text-xs text-slate-400 mt-0.5">最新 10 条报工记录</p>
      </div>
      <el-table :data="recentReports" stripe size="small">
        <el-table-column prop="orderNo" label="工单号" width="160" />
        <el-table-column prop="productName" label="产品" min-width="140" show-overflow-tooltip />
        <el-table-column prop="processName" label="工序" width="100" />
        <el-table-column prop="worker" label="报工人" width="90" />
        <el-table-column label="合格/总数" width="120" align="center">
          <template #default="{ row }">
            <span class="text-emerald-600 font-medium">{{ row.qualifiedQty ?? row.quantity }}</span>
            <span class="text-slate-400">/</span>
            <span>{{ row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="scrapQty" label="不良数" width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.scrapQty > 0" class="text-red-500 font-medium">{{ row.scrapQty }}</span>
            <span v-else class="text-slate-400">0</span>
          </template>
        </el-table-column>
        <el-table-column prop="reportDate" label="报工时间" width="110" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import api from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const userName = computed(() => userStore.user?.nickname || userStore.user?.username || '用户')
const currentDate = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
})

const cards = reactive({
  pendingOrders: 0,
  inProgressOrders: 0,
  todayOutput: 0,
  todayDefect: 0,
  defectRate: 0,
  unresolvedAlerts: 0,
  lowStockCount: 0,
  unreadOrderNotifications: 0
})

const recentReports = ref([])
const defectCauses = ref([])
const lowStockProducts = ref([])
const recentNotifications = ref([])
const showNotifications = ref(false)

const defectColor = computed(() => {
  const r = parseFloat(cards.defectRate)
  if (r > 5) return 'text-red-500'
  if (r > 2) return 'text-amber-500'
  return 'text-emerald-500'
})

const trendChart = ref(null)
const progChart = ref(null)
const defectCauseChart = ref(null)

let chartInstances = []

function getChart(el) {
  if (!el) return null
  const existing = echarts.getInstanceByDom(el)
  if (existing) existing.dispose()
  const instance = echarts.init(el)
  chartInstances.push(instance)
  return instance
}

function disposeAll() {
  chartInstances.forEach(c => { try { c.dispose() } catch (e) { /* ignore */ } })
  chartInstances = []
}

async function loadData() {
  try {
    const res = await api.get('/dashboard/mes-summary')
    if (res.code !== 200) return
    const d = res.data

    // KPI 卡片
    cards.pendingOrders = d.pendingOrders ?? 0
    cards.inProgressOrders = d.inProgressOrders ?? 0
    cards.todayOutput = d.todayOutput ?? 0
    cards.todayDefect = d.todayDefect ?? 0
    cards.defectRate = d.defectRate ?? 0
    cards.unresolvedAlerts = d.unresolvedAlerts ?? 0
    cards.lowStockCount = d.lowStockCount ?? 0
    cards.unreadOrderNotifications = d.unreadOrderNotifications ?? 0

    lowStockProducts.value = d.lowStockProducts || []
    recentNotifications.value = d.recentOrderNotifications || []
    if (cards.unreadOrderNotifications > 0) {
      showNotifications.value = true
    }

    // 最近报工表格
    recentReports.value = d.recentReportList || []

    await nextTick()

    // 产量趋势
    if (d.productionTrend?.length) {
      const dates = d.productionTrend.map(i => i.date?.substring(5) || '')
      const outputs = d.productionTrend.map(i => i.output || 0)
      renderTrend(trendChart.value, dates, outputs)
    }

    // 生产进度（显示工单号，tooltip 中展示产品名）
    if (d.orderProgress?.length) {
      const names = d.orderProgress.map(i => i.orderNo || '')
      const products = d.orderProgress.map(i => i.productName || '')
      const values = d.orderProgress.map(i => i.progress || 0)
      renderProgress(progChart.value, names, products, values)
    }

    // 不良原因
    defectCauses.value = d.defectCauseList || []
    if (defectCauses.value.length) {
      await nextTick()
      renderDefectCause(defectCauseChart.value, defectCauses.value)
    }
  } catch (e) {
    console.error('加载 MES 数据失败', e)
  }
}

function renderTrend(el, dates, outputs) {
  if (!el) return
  const chart = getChart(el)
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30,41,59,0.9)',
      borderColor: '#334155',
      textStyle: { color: '#e2e8f0', fontSize: 12 }
    },
    grid: { left: 55, right: 25, top: 10, bottom: 25 },
    xAxis: {
      type: 'category', data: dates,
      axisLabel: { fontSize: 10, color: '#94a3b8' },
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLabel: { fontSize: 10, color: '#94a3b8' },
      splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } }
    },
    series: [{
      data: outputs, type: 'line', smooth: true, symbol: 'circle', symbolSize: 6,
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(16,185,129,0.2)' },
        { offset: 1, color: 'rgba(16,185,129,0.01)' }
      ])},
      lineStyle: { color: '#10b981', width: 2 },
      itemStyle: { color: '#10b981' }
    }]
  })
}

function renderProgress(el, names, products, values) {
  if (!el || names.length === 0) return
  const chart = getChart(el)
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30,41,59,0.9)',
      borderColor: '#334155',
      textStyle: { color: '#e2e8f0', fontSize: 12 },
      formatter: params => {
        const i = params[0].dataIndex
        const pn = products[i] ? `<br/>产品: ${products[i]}` : ''
        return `${params[0].name}${pn}<br/>完成进度: <b>${params[0].value}%</b>`
      }
    },
    grid: { left: 10, right: 30, top: 5, bottom: 25 },
    xAxis: {
      type: 'category', data: names,
      axisLabel: { rotate: 30, fontSize: 10, color: '#94a3b8' },
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value', max: 100,
      axisLabel: { fontSize: 10, color: '#94a3b8', formatter: '{value}%' },
      splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } }
    },
    series: [{
      data: values, type: 'bar', barWidth: '50%',
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#60a5fa' },
          { offset: 1, color: '#3b82f6' }
        ])
      }
    }]
  })
}

function renderDefectCause(el, causes) {
  if (!el || causes.length === 0) return
  const chart = getChart(el)
  const reversed = [...Array(causes.length)].map((_, i) => causes.length - 1 - i)
  const yData = reversed.map(i => causes[i].cause || '')
  const xData = reversed.map(i => causes[i].count || 0)

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30,41,59,0.9)',
      borderColor: '#334155',
      textStyle: { color: '#e2e8f0', fontSize: 12 }
    },
    grid: { left: 10, right: 30, top: 5, bottom: 20 },
    xAxis: {
      type: 'value',
      axisLabel: { fontSize: 10, color: '#94a3b8' },
      splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } }
    },
    yAxis: {
      type: 'category', data: yData,
      axisLabel: { fontSize: 10, color: '#475569' },
      axisLine: { show: false }, axisTick: { show: false }
    },
    series: [{
      type: 'bar', data: xData, barWidth: '55%',
      itemStyle: {
        borderRadius: [0, 5, 5, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#f87171' },
          { offset: 1, color: '#fca5a5' }
        ])
      },
      label: { show: true, position: 'right', fontSize: 10, color: '#64748b' }
    }]
  })
}

function handleResize() {
  chartInstances.forEach(c => { try { c.resize() } catch (e) { /* ignore */ } })
}

async function markRead(id) {
  try {
    await api.put(`/dashboard/order-notifications/${id}/read`)
    recentNotifications.value = recentNotifications.value.filter(n => n.id !== id)
    cards.unreadOrderNotifications = Math.max(0, cards.unreadOrderNotifications - 1)
    if (recentNotifications.value.length === 0) showNotifications.value = false
  } catch { /* ignore */ }
}

async function markAllRead() {
  try {
    await api.put('/dashboard/order-notifications/read-all')
    recentNotifications.value = []
    cards.unreadOrderNotifications = 0
    showNotifications.value = false
  } catch { /* ignore */ }
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getMonth() + 1}/${d.getDate()} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeAll()
})
</script>
