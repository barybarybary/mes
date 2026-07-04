<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <div>
        <h2 class="text-xl font-bold text-slate-800 dark:text-slate-200">经营分析</h2>
        <p class="text-sm text-slate-400 mt-1">核心经营指标一览与趋势分析</p>
      </div>
      <el-button @click="loadAll" :loading="loading" :icon="Refresh" round>刷新</el-button>
    </div>

    <el-row :gutter="16" class="mb-5">
      <el-col :span="6" v-for="k in kpis" :key="k.label">
        <div class="bg-white dark:bg-slate-800 rounded-2xl p-5 shadow-sm border border-slate-100 dark:border-slate-700 card-hover">
          <div class="flex items-center justify-between mb-3">
            <span class="text-xs text-slate-400 font-medium uppercase">{{ k.label }}</span>
            <div class="w-9 h-9 rounded-lg flex items-center justify-center" :style="{ background: k.bg }">
              <el-icon :size="18" :color="k.color"><component :is="k.icon" /></el-icon>
            </div>
          </div>
          <p class="text-2xl font-bold text-slate-800 dark:text-slate-100">{{ k.value }}</p>
          <div class="flex items-center gap-2 mt-2 text-xs">
            <span :class="k.growth >= 0 ? 'text-emerald-500' : 'text-red-500'" class="font-medium">
              {{ k.growth >= 0 ? 'up' : 'down' }}{{ Math.abs(k.growth) }}%
            </span>
            <span class="text-slate-400">{{ k.subLabel }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mb-5">
      <el-col :span="16">
        <el-card shadow="never" class="dark:bg-slate-800 dark:border-slate-700">
          <template #header><h3 class="font-semibold text-slate-800 dark:text-slate-200">销售-生产-库存联动趋势</h3></template>
          <div ref="trendChart" style="height:380px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="dark:bg-slate-800 dark:border-slate-700">
          <template #header><h3 class="font-semibold text-slate-800 dark:text-slate-200">交付健康度</h3></template>
          <div ref="gaugeChart" style="height:380px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mb-5">
      <el-col :span="10">
        <el-card shadow="never" class="dark:bg-slate-800 dark:border-slate-700">
          <template #header><h3 class="font-semibold text-slate-800 dark:text-slate-200">库存周转分析</h3></template>
          <div class="grid grid-cols-2 gap-3 mb-3">
            <div class="bg-slate-50 rounded-xl p-3 text-center">
              <p class="text-xs text-slate-400">出库总量</p>
              <p class="text-xl font-bold text-slate-700">{{ turnover.outboundQty || 0 }}</p>
            </div>
            <div class="bg-slate-50 rounded-xl p-3 text-center">
              <p class="text-xs text-slate-400">入库总量</p>
              <p class="text-xl font-bold text-slate-700">{{ turnover.inboundQty || 0 }}</p>
            </div>
            <div class="bg-slate-50 rounded-xl p-3 text-center">
              <p class="text-xs text-slate-400">平均库存</p>
              <p class="text-xl font-bold text-slate-700">{{ turnover.avgStock || 0 }}</p>
            </div>
            <div class="bg-slate-50 rounded-xl p-3 text-center">
              <p class="text-xs text-slate-400">周转天数</p>
              <p class="text-xl font-bold text-blue-600">{{ turnover.turnoverDays || 0 }}</p>
            </div>
          </div>
          <div ref="pieChart" style="height:280px"></div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="never" class="dark:bg-slate-800 dark:border-slate-700">
          <template #header><h3 class="font-semibold text-slate-800 dark:text-slate-200">关键指标明细</h3></template>
          <el-table :data="detailTable" stripe size="small" max-height="300">
            <el-table-column prop="name" label="维度" min-width="120" />
            <el-table-column prop="orderCount" label="订单数" width="90" align="center" />
            <el-table-column prop="outputQty" label="产出量" width="100" align="right" />
            <el-table-column prop="stockQty" label="库存量" width="100" align="right" />
            <el-table-column prop="turnoverDays" label="周转天数" width="100" align="center" />
            <el-table-column label="健康度" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.health >= 80" type="success" round size="small">优秀</el-tag>
                <el-tag v-else-if="row.health >= 60" type="warning" round size="small">一般</el-tag>
                <el-tag v-else type="danger" round size="small">预警</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 产品销售排行 + 客户贡献排行 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never" class="dark:bg-slate-800 dark:border-slate-700">
          <template #header>
            <div class="flex items-center justify-between">
              <h3 class="font-semibold text-slate-800 dark:text-slate-200">产品销售排行</h3>
              <el-tag size="small" round>近12月</el-tag>
            </div>
          </template>
          <el-table :data="productRank" stripe size="small" max-height="400" v-loading="loadingRank">
            <el-table-column prop="product_name" label="产品" min-width="140" />
            <el-table-column prop="total_quantity" label="销量" width="90" align="right" />
            <el-table-column label="销售额" width="110" align="right">
              <template #default="{ row }">¥{{ Number(row.total_amount||0).toFixed(0) }}</template>
            </el-table-column>
            <el-table-column label="毛利额" width="110" align="right">
              <template #default="{ row }">¥{{ Number(row.gross_profit||0).toFixed(0) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="dark:bg-slate-800 dark:border-slate-700">
          <template #header>
            <div class="flex items-center justify-between">
              <h3 class="font-semibold text-slate-800 dark:text-slate-200">客户贡献排行</h3>
              <el-tag size="small" round>近12月</el-tag>
            </div>
          </template>
          <el-table :data="customerRank" stripe size="small" max-height="400" v-loading="loadingRank">
            <el-table-column prop="customer_name" label="客户" min-width="140" />
            <el-table-column prop="order_count" label="订单数" width="80" align="center" />
            <el-table-column label="销售额" width="110" align="right">
              <template #default="{ row }">¥{{ Number(row.total_amount||0).toFixed(0) }}</template>
            </el-table-column>
            <el-table-column label="占比" width="70" align="center">
              <template #default="{ row }">{{ row.percentage }}%</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'
import api from '@/api'

const loading = ref(false), loadingRank = ref(false)
const trendChart = ref(null), gaugeChart = ref(null), pieChart = ref(null)

const turnover = reactive({ outboundQty: 0, inboundQty: 0, avgStock: 0, turnoverDays: 0 })
const productRank = ref([])
const customerRank = ref([])

const kpis = reactive([
  { label: '总销售额', value: '0', growth: 0, subLabel: '较上期', icon: 'Money', color: '#3b82f6', bg: 'rgba(59,130,246,0.1)' },
  { label: '总订单数', value: '0', growth: 0, subLabel: '较上期', icon: 'Document', color: '#8b5cf6', bg: 'rgba(139,92,246,0.1)' },
  { label: '平均周转天数', value: '0天', growth: 0, subLabel: '较上期', icon: 'Timer', color: '#f59e0b', bg: 'rgba(245,158,11,0.1)' },
  { label: '交付及时率', value: '0%', growth: 0, subLabel: '较上期', icon: 'CircleCheck', color: '#10b981', bg: 'rgba(16,185,129,0.1)' }
])
const detailTable = ref([])

const chartInstances = []

function getChart(el) {
  if (!el) return null
  const existing = echarts.getInstanceByDom(el)
  if (existing) existing.dispose()
  const inst = echarts.init(el)
  chartInstances.push(inst)
  return inst
}

async function loadAll() {
  loading.value = true
  try {
    const [summary, trend, turnover, rate] = await Promise.all([
      api.get('/dashboard/summary'),
      api.get('/dashboard/sales-trend', { params: { days: 30 } }),
      api.get('/dashboard/inventory-turnover', { params: { days: 30 } }),
      api.get('/dashboard/delivery-rate')
    ])

    if (summary.code === 200) {
      const d = summary.data
      kpis[0].value = '¥' + ((d.totalSalesAmount || 0) / 10000).toFixed(1) + '万'
      kpis[0].growth = d.salesGrowth || 0
      kpis[1].value = d.pendingOrders ?? 0
      kpis[1].growth = d.orderGrowth || 0
      kpis[2].value = (turnover.data?.turnoverDays ?? 0) + '天'
      kpis[2].growth = -(turnover.data?.daysGrowth || 0)
      kpis[3].value = (rate.data?.rate ?? 0) + '%'
      kpis[3].growth = rate.data?.rateGrowth || 0

      detailTable.value = (d.warehouseMetrics || []).map(function(w) {
        return {
          name: w.name || w.warehouseName || '-',
          orderCount: w.orderCount || 0,
          outputQty: w.outputQty || 0,
          stockQty: w.stockQty || 0,
          turnoverDays: w.turnoverDays || 0,
          health: w.healthScore || 0
        }
      })
    }

    if (trend.code === 200 && trend.data?.length) {
      await nextTick()
      const chart = getChart(trendChart.value)
      if (chart) {
        const dates = trend.data.map(function(d) { return (d.date || '').substring(5) })
        chart.setOption({
          tooltip: { trigger: 'axis' },
          legend: { data: ['销售额', '入库量', '出库量'], bottom: 0 },
          grid: { left: 60, right: 30, top: 20, bottom: 40 },
          xAxis: { type: 'category', data: dates, axisLabel: { fontSize: 10, interval: Math.floor(dates.length / 5) } },
          yAxis: { type: 'value', axisLabel: { fontSize: 10, formatter: function(v) { return v >= 10000 ? (v / 10000).toFixed(0) + '万' : v } } },
          series: [
            { name: '销售额', type: 'line', data: trend.data.map(function(d) { return d.amount || 0 }), smooth: true, areaStyle: { opacity: 0.15 }, itemStyle: { color: '#3b82f6' } },
            { name: '入库量', type: 'line', data: trend.data.map(function(d) { return d.inQty || 0 }), smooth: true, itemStyle: { color: '#10b981' } },
            { name: '出库量', type: 'line', data: trend.data.map(function(d) { return d.outQty || 0 }), smooth: true, lineStyle: { type: 'dashed' }, itemStyle: { color: '#f59e0b' } }
          ]
        })
      }
    }

    if (rate.code === 200) {
      await nextTick()
      const chart = getChart(gaugeChart.value)
      if (chart) {
        const v = rate.data.rate || 0
        chart.setOption({
          series: [{
            type: 'gauge', radius: '80%', center: ['50%', '55%'],
            startAngle: 210, endAngle: -30, min: 0, max: 100,
            progress: { show: true, width: 14, roundCap: true, itemStyle: { color: v >= 80 ? '#10b981' : v >= 60 ? '#f59e0b' : '#f43f5e' } },
            axisLine: { lineStyle: { width: 14, color: [[0.6, '#f43f5e'], [0.8, '#f59e0b'], [1, '#10b981']] } },
            axisTick: { show: false }, splitLine: { show: false }, axisLabel: { show: false },
            detail: { valueAnimation: true, formatter: '{value}%', fontSize: 40, fontWeight: 'bold', offsetCenter: [0, '70%'] },
            data: [{ value: v, name: '交付率' }]
          }]
        })
      }
    }

    // 库存周转数据
    if (turnover.code === 200) {
      Object.assign(turnover, {
        outboundQty: Number(turnover.data.outboundQty || 0).toFixed(1),
        inboundQty: Number(turnover.data.inboundQty || 0).toFixed(1),
        avgStock: Number(turnover.data.avgStock || 0).toFixed(1),
        turnoverDays: turnover.data.turnoverDays || 0
      })
    }

    // 产品销售排行 + 客户贡献排行
    loadingRank.value = true
    try {
      const [prodRes, custRes] = await Promise.all([
        api.get('/bi/product-profit', { params: { limit: 10 } }),
        api.get('/bi/customer-value', { params: { limit: 10 } })
      ])
      if (prodRes.code === 200) productRank.value = prodRes.data || []
      if (custRes.code === 200) customerRank.value = custRes.data || []
    } finally { loadingRank.value = false }

    if (trend.code === 200) {
      await nextTick()
      const chart = getChart(pieChart.value)
      if (chart) {
        const map = {}
        trend.data.forEach(function(d) { var n = d.productName || d.category || 'other'; map[n] = (map[n] || 0) + (d.amount || 0) })
        const pieData = Object.entries(map).map(function(e) { return { name: e[0], value: e[1] } })
        if (pieData.length) {
          chart.setOption({
            tooltip: { trigger: 'item' },
            series: [{ type: 'pie', radius: ['55%', '80%'], center: ['50%', '50%'], itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 }, label: { formatter: '{b}\n{d}%' }, data: pieData }]
          })
        }
      }
    }
  } catch (e) { console.error('BI load error:', e) }
  finally { loading.value = false }
}

function onResize() { chartInstances.forEach(function(c) { try { c.resize() } catch { /* ignore */ } }) }
onMounted(function() { loadAll(); window.addEventListener('resize', onResize) })
onBeforeUnmount(function() { window.removeEventListener('resize', onResize); chartInstances.forEach(function(c) { try { c.dispose() } catch { /* ignore */ } }) })
</script>
