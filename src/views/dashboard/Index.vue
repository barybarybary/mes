<template>
  <div>
    <!-- 标题栏 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h2 class="text-xl font-bold text-slate-800 dark:text-slate-200">👋 你好，{{ userName }}</h2>
        <p class="text-sm text-slate-400 mt-1">{{ currentDate }}</p>
      </div>
      <el-button @click="refreshAll" :loading="loading" :icon="Refresh" round>
        刷新数据
      </el-button>
    </div>

    <!-- ====== KPI 统计卡片（6个） ====== -->
    <el-row :gutter="16" class="mb-5">
      <el-col :span="4" v-for="c in cards" :key="c.label">
        <div
          class="bg-white dark:bg-slate-800 rounded-2xl p-4 shadow-sm border border-slate-100 dark:border-slate-700 card-hover cursor-pointer"
          @click="c.onClick"
        >
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs text-slate-400 font-medium uppercase tracking-wide">{{ c.label }}</span>
            <div class="w-8 h-8 rounded-lg flex items-center justify-center" :style="{ background: c.bgColor }">
              <el-icon :size="16" :color="c.color"><component :is="c.icon" /></el-icon>
            </div>
          </div>
          <p class="text-2xl font-bold text-slate-800 dark:text-slate-100">{{ c.value }}</p>
          <div class="flex items-center gap-1 mt-1.5">
            <el-icon :size="12" :class="c.trend > 0 ? 'text-emerald-500' : 'text-red-500'">
              <component :is="c.trend > 0 ? 'Top' : 'Bottom'" />
            </el-icon>
            <span :class="['text-xs font-medium', c.trend > 0 ? 'text-emerald-500' : 'text-red-500']">
              {{ Math.abs(c.trend) }}%
            </span>
            <span class="text-xs text-slate-400">{{ c.trendLabel }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- ====== Row 1: 销售趋势（全宽） ====== -->
    <el-card class="mb-5 dark:bg-slate-800 dark:border-slate-700" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <div>
            <h3 class="font-semibold text-slate-800 dark:text-slate-200">📈 销售趋势</h3>
            <p class="text-xs text-slate-400 mt-0.5">每日销售金额变化</p>
          </div>
          <el-radio-group v-model="salesPeriod" size="small" @change="loadSalesTrend">
            <el-radio-button value="7">7天</el-radio-button>
            <el-radio-button value="30">30天</el-radio-button>
            <el-radio-button value="90">90天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="salesChart" style="height: 360px"></div>
    </el-card>

    <!-- ====== Row 2: 库存结构 + 库存周转 ====== -->
    <el-row :gutter="16" class="mb-5">
      <el-col :span="12">
        <el-card class="dark:bg-slate-800 dark:border-slate-700" shadow="never">
          <template #header>
            <div>
              <h3 class="font-semibold text-slate-800 dark:text-slate-200">🏗️ 库存结构分布</h3>
              <p class="text-xs text-slate-400 mt-0.5">各仓库 SKU 与库存量占比</p>
            </div>
          </template>
          <div ref="inventoryStructChart" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="dark:bg-slate-800 dark:border-slate-700" shadow="never">
          <template #header>
            <div class="flex items-center justify-between">
              <div>
                <h3 class="font-semibold text-slate-800 dark:text-slate-200">🔄 库存周转分析</h3>
                <p class="text-xs text-slate-400 mt-0.5">近{{ inventoryPeriod }}天周转情况</p>
              </div>
              <el-select v-model="inventoryPeriod" size="small" style="width:100px" @change="loadInventoryTurnover">
                <el-option :value="7" label="7天" />
                <el-option :value="30" label="30天" />
                <el-option :value="90" label="90天" />
              </el-select>
            </div>
          </template>
          <div ref="turnoverChart" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ====== Row 3: 生产进度 + 交付率 ====== -->
    <el-row :gutter="16" class="mb-5">
      <el-col :span="14">
        <el-card class="dark:bg-slate-800 dark:border-slate-700" shadow="never">
          <template #header>
            <div>
              <h3 class="font-semibold text-slate-800 dark:text-slate-200">🏭 生产进度</h3>
              <p class="text-xs text-slate-400 mt-0.5">各工单完成百分比</p>
            </div>
          </template>
          <div ref="progChart" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card class="dark:bg-slate-800 dark:border-slate-700" shadow="never">
          <template #header>
            <div>
              <h3 class="font-semibold text-slate-800 dark:text-slate-200">✅ 订单交付率</h3>
              <p class="text-xs text-slate-400 mt-0.5">整体订单履约情况</p>
            </div>
          </template>
          <div ref="rateChart" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ====== Row 4: 产品销售排行 + 客户排行 ====== -->
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card class="dark:bg-slate-800 dark:border-slate-700" shadow="never">
          <template #header>
            <div class="flex items-center justify-between">
              <div>
                <h3 class="font-semibold text-slate-800 dark:text-slate-200">🏆 产品销售排行</h3>
                <p class="text-xs text-slate-400 mt-0.5">近{{ rankingPeriod }}天 Top {{ rankingLimit }}</p>
              </div>
              <div class="flex items-center gap-2">
                <el-select v-model="rankingPeriod" size="small" style="width:90px" @change="loadSalesRanking">
                  <el-option :value="7" label="7天" />
                  <el-option :value="30" label="30天" />
                  <el-option :value="90" label="90天" />
                </el-select>
              </div>
            </div>
          </template>
          <div ref="productRankChart" style="height: 360px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="dark:bg-slate-800 dark:border-slate-700" shadow="never">
          <template #header>
            <div class="flex items-center justify-between">
              <div>
                <h3 class="font-semibold text-slate-800 dark:text-slate-200">🤝 客户贡献排行</h3>
                <p class="text-xs text-slate-400 mt-0.5">近{{ rankingPeriod }}天 Top {{ rankingLimit }}</p>
              </div>
            </div>
          </template>
          <div ref="customerRankChart" style="height: 360px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'
import api from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const userName = computed(() => userStore.user?.nickname || userStore.user?.username || '用户')
const currentDate = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
})

const loading = ref(false)

// ====== KPI 卡片 ======
const cards = reactive([
  {
    label: '待处理订单', value: 0, trend: 12, trendLabel: '较昨日',
    icon: 'Document', color: '#f59e0b', bgColor: 'rgba(245,158,11,0.1)',
    onClick: () => {}
  },
  {
    label: '生产中工单', value: 0, trend: -5, trendLabel: '较昨日',
    icon: 'Monitor', color: '#3b82f6', bgColor: 'rgba(59,130,246,0.1)',
    onClick: () => {}
  },
  {
    label: '今日入库', value: 0, trend: 8, trendLabel: '较昨日',
    icon: 'Box', color: '#10b981', bgColor: 'rgba(16,185,129,0.1)',
    onClick: () => {}
  },
  {
    label: 'SKU 总数', value: 0, trend: 3, trendLabel: '较昨日',
    icon: 'Grid', color: '#8b5cf6', bgColor: 'rgba(139,92,246,0.1)',
    onClick: () => {}
  },
  {
    label: '库存周转天数', value: 0, trend: -2, trendLabel: '较上期',
    icon: 'Timer', color: '#14b8a6', bgColor: 'rgba(20,184,166,0.1)',
    onClick: () => {}
  },
  {
    label: '订单交付率', value: '0%', trend: 5, trendLabel: '较上期',
    icon: 'CircleCheck', color: '#f43f5e', bgColor: 'rgba(244,63,94,0.1)',
    onClick: () => {}
  }
])

// ====== 图表 refs ======
const salesChart = ref(null)
const progChart = ref(null)
const rateChart = ref(null)
const inventoryStructChart = ref(null)
const turnoverChart = ref(null)
const productRankChart = ref(null)
const customerRankChart = ref(null)

// ====== 筛选参数 ======
const salesPeriod = ref('30')
const inventoryPeriod = ref(30)
const rankingPeriod = ref(30)
const rankingLimit = ref(10)

// ====== 所有图表实例 ======
let chartInstances = []

function getChart(elRef) {
  if (!elRef) return null
  const dom = elRef
  // dispose if exists
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

// ====== 数据加载 ======
async function loadSummary() {
  try {
    const res = await api.get('/dashboard/summary')
    if (res.code === 200) {
      cards[0].value = res.data.pendingOrders ?? 0
      cards[1].value = res.data.inProgressOrders ?? 0
      cards[2].value = res.data.todayInCount ?? 0
      cards[3].value = res.data.skuCount ?? 0
    }
  } catch (e) {
    console.error('加载概览失败', e)
  }
}

async function loadSalesTrend() {
  try {
    const res = await api.get('/dashboard/sales-trend', { params: { days: salesPeriod.value } })
    if (res.code === 200 && res.data?.length) {
      const dates = res.data.map(d => d.date?.substring(5) || '')
      const amounts = res.data.map(d => d.amount || 0)
      await nextTick()
      renderLineChart(salesChart.value, dates, amounts)
    }
  } catch (e) {
    console.error('加载销售趋势失败', e)
  }
}

async function loadProductionProgress() {
  try {
    const res = await api.get('/dashboard/production-progress')
    if (res.code === 200 && res.data?.length) {
      const names = res.data.map(d => d.orderNo || '')
      const values = res.data.map(d => d.progress || 0)
      await nextTick()
      renderBarChart(progChart.value, names, values, '完成进度', '%')
    }
  } catch (e) {
    console.error('加载生产进度失败', e)
  }
}

async function loadDeliveryRate() {
  try {
    const res = await api.get('/dashboard/delivery-rate')
    if (res.code === 200) {
      cards[5].value = (res.data.rate ?? 0) + '%'
      await nextTick()
      renderGaugeChart(rateChart.value, res.data.rate ?? 0)
    }
  } catch (e) {
    console.error('加载交付率失败', e)
  }
}

async function loadInventoryStructure() {
  try {
    const res = await api.get('/dashboard/inventory-structure')
    if (res.code === 200 && res.data) {
      await nextTick()
      renderInventoryStructure(inventoryStructChart.value, res.data)
    }
  } catch (e) {
    console.error('加载库存结构失败', e)
  }
}

async function loadInventoryTurnover() {
  try {
    const res = await api.get('/dashboard/inventory-turnover', { params: { days: inventoryPeriod.value } })
    if (res.code === 200 && res.data) {
      cards[4].value = (res.data.turnoverDays ?? 0) + '天'
      await nextTick()
      renderTurnoverChart(turnoverChart.value, res.data)
    }
  } catch (e) {
    console.error('加载库存周转失败', e)
  }
}

async function loadSalesRanking() {
  try {
    const res = await api.get('/dashboard/sales-ranking', {
      params: { days: rankingPeriod.value, limit: rankingLimit.value }
    })
    if (res.code === 200 && res.data) {
      await nextTick()
      if (res.data.products?.length) {
        renderRankChart(productRankChart.value, res.data.products, 'productName', 'saleCount', '产品销售排行')
      }
      if (res.data.customers?.length) {
        renderRankChart(customerRankChart.value, res.data.customers, 'customerName', 'saleAmount', '客户贡献排行')
      }
    }
  } catch (e) {
    console.error('加载销售排行失败', e)
  }
}

// ====== 图表渲染 ======
function renderLineChart(el, dates, amounts) {
  if (!el) return
  const chart = getChart(el)
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30,41,59,0.9)',
      borderColor: '#334155',
      textStyle: { color: '#e2e8f0', fontSize: 12 }
    },
    grid: { left: 60, right: 30, top: 20, bottom: 30 },
    xAxis: {
      type: 'category', data: dates,
      axisLabel: { interval: Math.max(0, Math.floor(dates.length / 8) - 1), rotate: 30, fontSize: 10, color: '#94a3b8' },
      axisLine: { lineStyle: { color: '#e2e8f0' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { fontSize: 10, color: '#94a3b8', formatter: v => v >= 10000 ? (v / 10000).toFixed(0) + '万' : v },
      splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } }
    },
    series: [{
      data: amounts, type: 'line', smooth: true, symbol: 'circle', symbolSize: 4,
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(59,130,246,0.25)' },
        { offset: 1, color: 'rgba(59,130,246,0.02)' }
      ])},
      lineStyle: { color: '#3b82f6', width: 2 },
      itemStyle: { color: '#3b82f6' }
    }]
  })
}

function renderBarChart(el, names, values, name, unit) {
  if (!el || names.length === 0) return
  const chart = getChart(el)
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30,41,59,0.9)',
      borderColor: '#334155',
      textStyle: { color: '#e2e8f0', fontSize: 12 },
      formatter: params => `${params[0].name}<br/>${name}: <b>${params[0].value}${unit || ''}</b>`
    },
    grid: { left: 80, right: 30, top: 10, bottom: 30 },
    xAxis: {
      type: 'category', data: names,
      axisLabel: { rotate: 30, fontSize: 10, color: '#94a3b8' },
      axisLine: { lineStyle: { color: '#e2e8f0' } }
    },
    yAxis: {
      type: 'value', max: 100,
      axisLabel: { fontSize: 10, color: '#94a3b8', formatter: `{value}${unit || ''}` },
      splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } }
    },
    series: [{
      data: values, type: 'bar', barWidth: '50%',
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#34d399' },
          { offset: 1, color: '#10b981' }
        ])
      }
    }]
  })
}

function renderGaugeChart(el, rate) {
  if (!el) return
  const chart = getChart(el)
  chart.setOption({
    series: [{
      type: 'gauge', radius: '85%', center: ['50%', '55%'],
      startAngle: 210, endAngle: -30,
      min: 0, max: 100,
      progress: { show: true, width: 16, roundCap: true, itemStyle: { color: '#10b981' } },
      axisLine: { lineStyle: { width: 16, color: [[1, '#f1f5f9']] } },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      detail: {
        valueAnimation: true, formatter: '{value}%', fontSize: 36, fontWeight: 'bold',
        offsetCenter: [0, '65%'], color: '#1e293b'
      },
      data: [{ value: rate, name: '交付率' }]
    }]
  })
}

function renderInventoryStructure(el, data) {
  if (!el) return
  const chart = getChart(el)

  // data expected: { warehouses: [{ name, skuCount, quantity }], totalSku, totalQuantity }
  const warehouses = data.warehouses || []
  // Pie chart for quantity distribution
  const pieData = warehouses.map(w => ({ name: w.name || w.warehouseName || '未知', value: w.quantity || 0 }))

  chart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(30,41,59,0.9)',
      borderColor: '#334155',
      textStyle: { color: '#e2e8f0', fontSize: 12 },
      formatter: params => `${params.name}<br/>库存量: <b>${params.value}</b> (${params.percent}%)`
    },
    legend: {
      orient: 'vertical', right: 10, top: 'center',
      textStyle: { fontSize: 11, color: '#64748b' },
      itemWidth: 10, itemHeight: 10, itemGap: 12
    },
    series: [
      {
        type: 'pie', radius: ['55%', '80%'], center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 14, fontWeight: 'bold' },
          scaleSize: 8
        },
        data: pieData,
        color: ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#f43f5e', '#14b8a6', '#ec4899', '#6366f1']
      }
    ]
  })

  // 额外数据展示（通过 tooltip 下方的统计）
  if (data.totalSku !== undefined || data.totalQuantity !== undefined) {
    // 在图表中央显示总览文字
    chart.setOption({
      graphic: [
        {
          type: 'text', left: '33%', top: '45%',
          style: {
            text: `SKU: ${data.totalSku ?? '-'}\n库存: ${data.totalQuantity ?? '-'}`,
            textAlign: 'center', fontSize: 12, fontWeight: 'bold',
            fill: '#64748b', lineHeight: 18
          }
        }
      ]
    })
  }
}

function renderTurnoverChart(el, data) {
  if (!el) return
  const chart = getChart(el)

  // data expected: { turnoverRate, turnoverDays, periodDays, totalOutbound, avgInventory }
  const turnoverRate = data.turnoverRate ?? 0
  const turnoverDays = data.turnoverDays ?? 0
  const totalOutbound = data.totalOutbound ?? 0
  const avgInventory = data.avgInventory ?? 0

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30,41,59,0.9)',
      borderColor: '#334155',
      textStyle: { color: '#e2e8f0', fontSize: 12 }
    },
    grid: { left: 70, right: 30, top: 40, bottom: 30 },
    xAxis: {
      type: 'category',
      data: ['出库总量', '平均库存', '周转次数(年化)', '周转天数'],
      axisLabel: { fontSize: 10, color: '#94a3b8', interval: 0 },
      axisLine: { lineStyle: { color: '#e2e8f0' } }
    },
    yAxis: [
      {
        type: 'value', name: '数量',
        axisLabel: { fontSize: 10, color: '#94a3b8' },
        splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } }
      },
      {
        type: 'value', name: '次/天',
        axisLabel: { fontSize: 10, color: '#94a3b8' },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '数值', type: 'bar', barWidth: '40%',
        data: [
          { value: totalOutbound, itemStyle: { color: '#3b82f6', borderRadius: [6, 6, 0, 0] } },
          { value: avgInventory, itemStyle: { color: '#8b5cf6', borderRadius: [6, 6, 0, 0] } },
          { value: turnoverRate, itemStyle: { color: '#f59e0b', borderRadius: [6, 6, 0, 0] }, yAxisIndex: 1 },
          { value: turnoverDays, itemStyle: { color: '#f43f5e', borderRadius: [6, 6, 0, 0] }, yAxisIndex: 1 }
        ],
        label: {
          show: true, position: 'top', fontSize: 10, fontWeight: 'bold',
          color: '#64748b',
          formatter: p => {
            if (p.dataIndex === 0) return totalOutbound.toLocaleString()
            if (p.dataIndex === 1) return avgInventory.toLocaleString()
            if (p.dataIndex === 2) return turnoverRate.toFixed(1) + '次'
            return turnoverDays.toFixed(1) + '天'
          }
        }
      }
    ]
  })
}

function renderRankChart(el, data, nameKey, valueKey, title) {
  if (!el || !data?.length) return
  const chart = getChart(el)

  // 取 Top N，水平条形图（倒序以便从上到下）
  const sorted = [...data].sort((a, b) => (a[valueKey] || 0) - (b[valueKey] || 0))
  const names = sorted.map(d => d[nameKey] || '')
  const values = sorted.map(d => d[valueKey] || 0)

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30,41,59,0.9)',
      borderColor: '#334155',
      textStyle: { color: '#e2e8f0', fontSize: 12 },
      formatter: params => `${params[0].name}<br/>${title}: <b>${params[0].value?.toLocaleString() || params[0].value}</b>`
    },
    grid: { left: 110, right: 40, top: 5, bottom: 20 },
    xAxis: {
      type: 'value',
      axisLabel: { fontSize: 10, color: '#94a3b8', formatter: v => v >= 10000 ? (v / 10000).toFixed(0) + '万' : v },
      splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } }
    },
    yAxis: {
      type: 'category', data: names,
      axisLabel: { fontSize: 11, color: '#475569', width: 100, overflow: 'truncate' },
      axisLine: { show: false }, axisTick: { show: false }
    },
    series: [{
      type: 'bar', data: values, barWidth: '60%',
      itemStyle: {
        borderRadius: [0, 6, 6, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#6366f1' },
          { offset: 1, color: '#a78bfa' }
        ])
      },
      label: { show: true, position: 'right', fontSize: 10, color: '#64748b' }
    }]
  })
}

// ====== 全部刷新 ======
async function refreshAll() {
  loading.value = true
  try {
    await Promise.all([
      loadSummary(),
      loadSalesTrend(),
      loadProductionProgress(),
      loadDeliveryRate(),
      loadInventoryStructure(),
      loadInventoryTurnover(),
      loadSalesRanking()
    ])
  } finally {
    loading.value = false
  }
}

// ====== 响应式 resize ======
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
