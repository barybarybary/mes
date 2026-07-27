<template>
  <div class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 class="text-lg font-semibold text-slate-800">库存管理</h2>
          <p class="text-xs text-slate-400 mt-1">库存查询、出入库操作</p>
        </div>
      </div>
    </div>

    <div class="p-6">
      <el-tabs v-model="activeTab" class="custom-tabs" @tab-change="onTabChange">
        <el-tab-pane label="库存查询" name="stock">
          <div class="mb-4">
            <el-select v-model="filterProductId" placeholder="筛选产品" filterable clearable class="w-64" @change="onFilterChange">
              <el-option v-for="p in products" :key="p.id" :value="p.id" :label="p.name" />
            </el-select>
          </div>
          <el-table :data="stocks" border stripe class="page-table">
            <el-table-column prop="productName" label="产品名称" min-width="180">
              <template #default="{ row }"><span class="font-medium text-slate-700">{{ row.productName }}</span></template>
            </el-table-column>
            <el-table-column prop="batchNo" label="批次号" width="150">
              <template #default="{ row }"><el-tag type="info" effect="light" size="small">{{ row.batchNo }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="warehouseName" label="仓库" width="140" />
            <el-table-column prop="quantity" label="库存数量" width="130" align="right">
              <template #default="{ row }">
                <span class="font-semibold" :class="row.quantity > 0 ? 'text-emerald-600' : 'text-red-500'">{{ row.quantity }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="unit" label="单位" width="80" align="center" />
            <el-table-column prop="updateTime" label="更新时间" width="170" />
          </el-table>
          <div class="mt-5 flex justify-end">
            <el-pagination
              v-model:current-page="stockPage"
              :total="stockTotal"
              :page-size="stockPageSize"
              layout="prev, pager, next, total"
              background
              @current-change="fetchStock"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="入库操作" name="in">
          <div class="max-w-lg mx-auto py-6">
            <div class="text-center mb-6">
              <div class="w-16 h-16 mx-auto rounded-2xl bg-emerald-50 flex items-center justify-center mb-3">
                <el-icon color="#10b981" :size="32"><Bottom /></el-icon>
              </div>
              <h3 class="text-lg font-semibold text-slate-800">商品入库</h3>
              <p class="text-sm text-slate-400 mt-1">将商品入库到指定仓库</p>
            </div>
            <el-form :model="inForm" label-width="80px" label-position="right">
              <el-form-item label="产品">
                <el-select v-model="inForm.productId" filterable class="w-full" placeholder="请选择产品">
                  <el-option v-for="p in products" :key="p.id" :value="p.id" :label="p.name" />
                </el-select>
              </el-form-item>
              <el-form-item label="仓库">
                <el-select v-model="inForm.warehouseId" class="w-full" placeholder="请选择仓库">
                  <el-option v-for="w in warehouses" :key="w.id" :value="w.id" :label="w.name" />
                </el-select>
              </el-form-item>
              <el-form-item label="批次号">
                <el-input v-model="inForm.batchNo" placeholder="请输入批次号" />
              </el-form-item>
              <el-form-item label="入库数量">
                <el-input-number v-model="inForm.quantity" :min="0" :precision="2" class="w-full" />
              </el-form-item>
              <el-form-item label="备注">
                <el-input v-model="inForm.remark" placeholder="请输入备注" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" class="w-full h-11 rounded-xl font-medium" @click="doStockIn">
                  <el-icon class="mr-1"><Bottom /></el-icon>确认入库
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="出库操作" name="out">
          <div class="max-w-lg mx-auto py-6">
            <div class="text-center mb-6">
              <div class="w-16 h-16 mx-auto rounded-2xl bg-orange-50 flex items-center justify-center mb-3">
                <el-icon color="#f97316" :size="32"><Top /></el-icon>
              </div>
              <h3 class="text-lg font-semibold text-slate-800">商品出库</h3>
              <p class="text-sm text-slate-400 mt-1">从指定仓库出库商品</p>
            </div>
            <el-form :model="outForm" label-width="80px" label-position="right">
              <el-form-item label="产品">
                <el-select v-model="outForm.productId" filterable class="w-full" placeholder="请选择产品">
                  <el-option v-for="p in products" :key="p.id" :value="p.id" :label="p.name" />
                </el-select>
              </el-form-item>
              <el-form-item label="仓库">
                <el-select v-model="outForm.warehouseId" class="w-full" placeholder="请选择仓库">
                  <el-option v-for="w in warehouses" :key="w.id" :value="w.id" :label="w.name" />
                </el-select>
              </el-form-item>
              <el-form-item label="批次号">
                <el-input v-model="outForm.batchNo" placeholder="请输入批次号" />
              </el-form-item>
              <el-form-item label="出库数量">
                <el-input-number v-model="outForm.quantity" :min="0" :precision="2" class="w-full" />
              </el-form-item>
              <el-form-item label="备注">
                <el-input v-model="outForm.remark" placeholder="请输入备注" />
              </el-form-item>
              <el-form-item>
                <el-button type="danger" class="w-full h-11 rounded-xl font-medium" @click="doStockOut">
                  <el-icon class="mr-1"><Top /></el-icon>确认出库
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const activeTab = ref('stock'), stocks = ref([]), filterProductId = ref()
const products = ref([]), warehouses = ref([])
const stockPage = ref(1), stockTotal = ref(0), stockPageSize = ref(8), stockLoading = ref(false)
const inForm = reactive({ productId: null, warehouseId: null, batchNo: '', quantity: 0, remark: '' })
const outForm = reactive({ productId: null, warehouseId: null, batchNo: '', quantity: 0, remark: '' })

async function fetchStock() {
  stockLoading.value = true
  try {
    const r = await api.get('/inventory', {
      params: {
        page: stockPage.value,
        pageSize: stockPageSize.value,
        productId: filterProductId.value || undefined
      }
    })
    if (r.code === 200) {
      const data = r.data
      if (Array.isArray(data)) {
        stocks.value = data
        stockTotal.value = data.length
      } else {
        stocks.value = data.list || data.records || []
        stockTotal.value = data.total || 0
      }
    }
  } catch (e) {
    console.error('加载库存失败', e)
  } finally {
    stockLoading.value = false
  }
}
async function doStockIn() {
  if (!inForm.productId || !inForm.warehouseId || !inForm.quantity) {
    ElMessage.warning('请填写完整信息')
    return
  }
  await api.post('/inventory/in', inForm)
  ElMessage.success('入库成功')
  Object.assign(inForm, { productId: null, warehouseId: null, batchNo: '', quantity: 0, remark: '' })
  stockPage.value = 1; fetchStock()
}
async function doStockOut() {
  if (!outForm.productId || !outForm.warehouseId || !outForm.quantity) {
    ElMessage.warning('请填写完整信息')
    return
  }
  await api.post('/inventory/out', outForm)
  ElMessage.success('出库成功')
  Object.assign(outForm, { productId: null, warehouseId: null, batchNo: '', quantity: 0, remark: '' })
  stockPage.value = 1; fetchStock()
}

function onFilterChange() {
  stockPage.value = 1
  fetchStock()
}

function onTabChange(name) {
  if (name === 'stock') fetchStock()
}

onMounted(async () => {
  products.value = (await api.get('/base/product', { params: { pageSize: 999 } })).data?.list || []
  warehouses.value = (await api.get('/base/warehouse')).data || []
  fetchStock()
})
</script>

<style scoped>
:deep(.page-table th.el-table__cell) { background-color: #f8fafc !important; color: #475569 !important; font-weight: 600 !important; font-size: 13px !important; }
:deep(.custom-tabs .el-tabs__item) { font-weight: 500; height: 44px; }
:deep(.custom-tabs .el-tabs__active-bar) { height: 3px; }
</style>
