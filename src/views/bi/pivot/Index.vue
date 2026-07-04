<template>
  <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
    <div class="px-4 sm:px-6 py-4 border-b border-slate-100 dark:border-slate-700">
      <h2 class="text-lg font-semibold text-slate-800 dark:text-slate-200">多维交叉分析</h2>
      <p class="text-xs text-slate-400 mt-1">从不同维度交叉查看业务数据，支持按年/月筛选</p>
    </div>
    <div class="p-4 sm:p-6">
      <el-tabs v-model="activeTab" @tab-change="loadTab">
        <el-tab-pane label="销售额×产品" name="sp" />
        <el-tab-pane label="销售额×客户" name="sc" />
        <el-tab-pane label="月份×品类矩阵" name="sm" />
        <el-tab-pane label="仓库×品类库存" name="iw" />
        <el-tab-pane label="月份×产量" name="pm" />
        <el-tab-pane label="客户×发货量" name="dc" />
      </el-tabs>

      <div class="flex items-center gap-3 mb-4 mt-2" v-if="showFilters">
        <span class="text-sm text-slate-500">年份:</span>
        <el-input-number v-model="year" :min="2020" :max="2030" size="small" class="w-28" v-if="showYear" />
        <span class="text-sm text-slate-500" v-if="showMonth">月份:</span>
        <el-input-number v-model="month" :min="1" :max="12" size="small" class="w-24" v-if="showMonth" />
        <el-button size="small" type="primary" @click="loadTab">查询</el-button>
      </div>

      <el-table :data="rows" stripe border size="small" v-loading="loading" max-height="480">
        <el-table-column v-for="col in columns" :key="col.prop" :prop="col.prop" :label="col.label" :width="col.width" :min-width="col.minWidth" :align="col.align || 'left'">
          <template v-if="col.fmt === 'money'" #default="{ row }">¥{{ Number(row[col.prop] || 0).toFixed(0) }}</template>
          <template v-else-if="col.fmt === 'qty'" #default="{ row }">{{ Number(row[col.prop] || 0).toFixed(1) }}</template>
        </el-table-column>
      </el-table>
      <div v-if="!loading && rows.length === 0" class="text-center py-12 text-slate-400">
        <p class="text-sm">该维度暂无数据，请调整筛选条件</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '@/api'

const activeTab = ref('sp')
const rows = ref([]), columns = ref([]), loading = ref(false)
const year = ref(2026), month = ref(null)
const showYear = ref(true), showMonth = ref(true), showFilters = ref(true)

const tabs = {
  sp: { url: '/bi/pivot/sales-by-product', cols: [
    { prop: 'product_name', label: '产品', minWidth: 180 },
    { prop: 'quantity', label: '销量', width: 100, align: 'right', fmt: 'qty' },
    { prop: 'amount', label: '销售额', width: 130, align: 'right', fmt: 'money' }
  ], y: true, m: true },
  sc: { url: '/bi/pivot/sales-by-customer', cols: [
    { prop: 'customer_name', label: '客户', minWidth: 180 },
    { prop: 'order_count', label: '订单数', width: 90, align: 'center' },
    { prop: 'amount', label: '销售额', width: 130, align: 'right', fmt: 'money' }
  ], y: true, m: true },
  sm: { url: '/bi/pivot/sales-by-month-category', cols: [
    { prop: 'category_name', label: '品类', width: 150 },
    { prop: 'month', label: '月份', width: 100 },
    { prop: 'amount', label: '销售额', width: 130, align: 'right', fmt: 'money' }
  ], y: true, m: false },
  iw: { url: '/bi/pivot/inventory-by-warehouse', cols: [
    { prop: 'warehouse_name', label: '仓库', width: 150 },
    { prop: 'category_name', label: '品类', width: 150 },
    { prop: 'quantity', label: '库存量', width: 110, align: 'right', fmt: 'qty' },
    { prop: 'sku_count', label: 'SKU数', width: 90, align: 'center' }
  ], y: false, m: false },
  pm: { url: '/bi/pivot/production-by-month', cols: [
    { prop: 'product_name', label: '产品', minWidth: 180 },
    { prop: 'month', label: '月份', width: 100 },
    { prop: 'quantity', label: '产量', width: 110, align: 'right', fmt: 'qty' }
  ], y: true, m: false },
  dc: { url: '/bi/pivot/delivery-by-customer', cols: [
    { prop: 'customer_name', label: '客户', minWidth: 180 },
    { prop: 'month', label: '月份', width: 100 },
    { prop: 'quantity', label: '发货量', width: 110, align: 'right', fmt: 'qty' }
  ], y: true, m: false }
}

async function loadTab() {
  const t = tabs[activeTab.value]
  columns.value = t.cols
  showYear.value = t.y; showMonth.value = t.m; showFilters.value = t.y || t.m
  loading.value = true
  try {
    const params = {}
    if (t.y) params.year = year.value
    if (t.m) params.month = month.value
    const res = await api.get(t.url, { params })
    if (res.code === 200) rows.value = res.data || []
    else rows.value = []
  } catch (e) { rows.value = [] }
  finally { loading.value = false }
}

loadTab()
</script>