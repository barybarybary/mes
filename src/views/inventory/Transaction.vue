<template>
  <div class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
    <!-- 头部 -->
    <div class="px-4 sm:px-6 py-4 sm:py-5 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 sm:gap-4">
      <div class="flex items-start gap-3">
        <span class="w-1 h-6 rounded-full gradient-primary-r mt-1 shrink-0"></span>
        <div>
          <h2 class="text-base sm:text-lg font-semibold text-slate-800">库存流水</h2>
          <p class="text-xs text-slate-400 mt-0.5 sm:mt-1">出入库操作记录与追溯</p>
        </div>
      </div>
      <div class="flex items-center gap-2 sm:gap-3">
        <el-select v-model="filterType" placeholder="全部类型" clearable class="w-28" @change="search">
          <el-option label="入库" value="in" />
          <el-option label="出库" value="out" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索关联单号" clearable class="w-48" @change="search">
          <template #prefix><el-icon class="text-slate-400"><Search /></el-icon></template>
        </el-input>
      </div>
    </div>

    <!-- 桌面端表格 -->
    <div class="p-4 sm:p-6 hidden md:block">
      <el-table :data="list" border stripe v-loading="loading" class="page-table">
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'in'" type="success" effect="light" round size="small">入库</el-tag>
            <el-tag v-else type="warning" effect="light" round size="small">出库</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="产品" min-width="160">
          <template #default="{ row }">
            <span class="font-medium text-slate-700">{{ row.productName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="batchNo" label="批次号" width="150">
          <template #default="{ row }"><el-tag type="info" effect="light" size="small">{{ row.batchNo }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column label="数量" width="120" align="right">
          <template #default="{ row }">
            <span :class="row.type === 'in' ? 'text-emerald-600' : 'text-orange-500'" class="font-semibold">
              {{ row.type === 'in' ? '+' : '-' }}{{ row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="beforeQty" label="变更前" width="100" align="right" />
        <el-table-column prop="afterQty" label="变更后" width="100" align="right">
          <template #default="{ row }"><span class="font-medium">{{ row.afterQty }}</span></template>
        </el-table-column>
        <el-table-column prop="orderNo" label="关联单号" min-width="160" />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column prop="createTime" label="操作时间" width="170" />
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

    <!-- 移动端卡片列表 -->
    <div class="p-3 sm:p-4 md:hidden" v-loading="loading">
      <div v-if="list.length === 0 && !loading" class="text-center py-16 text-slate-400">
        <el-icon :size="48" class="mb-3"><Document /></el-icon>
        <p class="text-sm">暂无流水记录</p>
      </div>
      <div v-else class="space-y-3">
        <div
          v-for="row in list"
          :key="row.id"
          class="bg-white rounded-xl border border-slate-200 p-4 active:bg-slate-50"
        >
          <div class="flex items-start justify-between mb-2">
            <div class="flex items-center gap-2">
              <el-tag v-if="row.type === 'in'" type="success" effect="light" round size="small">入库</el-tag>
              <el-tag v-else type="warning" effect="light" round size="small">出库</el-tag>
              <span class="text-sm font-semibold text-slate-800">{{ row.productName }}</span>
            </div>
            <span :class="row.type === 'in' ? 'text-emerald-600' : 'text-orange-500'" class="font-semibold text-sm">
              {{ row.type === 'in' ? '+' : '-' }}{{ row.quantity }}
            </span>
          </div>
          <div class="flex flex-wrap gap-x-4 gap-y-1 text-xs text-slate-500 mb-2">
            <span>批次：{{ row.batchNo || '-' }}</span>
            <span>仓库：{{ row.warehouseName || '-' }}</span>
            <span>{{ row.beforeQty }} → {{ row.afterQty }}</span>
          </div>
          <div class="flex items-center justify-between text-xs text-slate-400 pt-2 border-t border-slate-100">
            <span>{{ row.orderNo || '-' }} / {{ row.operator || '-' }}</span>
            <span>{{ row.createTime }}</span>
          </div>
        </div>
      </div>
      <div class="mt-4 flex justify-center" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="page"
          :total="total"
          :page-size="pageSize"
          layout="prev, pager, next"
          background
          small
          @current-change="fetchData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, Document } from '@element-plus/icons-vue'
import api from '@/api'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const pageSize = ref(15)
const filterType = ref('')
const keyword = ref('')

function search() {
  page.value = 1
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const res = await api.get('/inventory/transactions', {
      params: {
        page: page.value,
        pageSize: pageSize.value,
        type: filterType.value || undefined,
        keyword: keyword.value || undefined,
      }
    })
    if (res.code === 200) {
      const data = res.data
      if (Array.isArray(data)) {
        list.value = data
        total.value = data.length
      } else {
        list.value = data.list || data.records || []
        total.value = data.total || 0
      }
    }
  } catch (e) {
    console.error('加载流水失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
:deep(.page-table th.el-table__cell) {
  background-color: #f8fafc !important;
  color: #475569 !important;
  font-weight: 600 !important;
  font-size: 13px !important;
}
</style>
