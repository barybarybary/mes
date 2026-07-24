<template>
  <div>
    <!-- 库存查询 -->
    <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden mb-6">
      <div class="px-6 py-5 border-b border-slate-100 dark:border-slate-700 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 class="text-lg font-semibold text-slate-800 dark:text-slate-200">库存管理</h2>
          <p class="text-xs text-slate-400 dark:text-slate-300 mt-1">当前库存查询，支持入库/出库操作</p>
        </div>
        <el-button type="primary" @click="openInDialog" class="h-10 px-5 rounded-xl font-medium">
          <el-icon class="mr-1"><Plus /></el-icon>产品入库
        </el-button>
      </div>
      <div class="p-6">
        <el-table :data="list" border stripe class="page-table">
          <el-table-column prop="productCode" label="产品编码" width="120" />
          <el-table-column prop="productName" label="产品名称" min-width="160">
            <template #default="{ row }"><span class="font-medium text-slate-700 dark:text-slate-600">{{ row.productName }}</span></template>
          </el-table-column>
          <el-table-column prop="warehouseName" label="仓库" width="120">
            <template #default="{ row }"><el-tag type="info" effect="light" size="small">{{ row.warehouseName }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="locationName" label="库位" width="100" />
          <el-table-column prop="batchNo" label="批次号" width="130">
            <template #default="{ row }">{{ row.batchNo || '-' }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="库存数量" width="130" align="center">
            <template #default="{ row }">
              <span :class="row.quantity > 10 ? 'text-green-600 font-bold' : 'text-red-500 font-bold'">
                {{ row.quantity }} {{ row.unit || '' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="lockedQty" label="锁定数量" width="100" align="center" />
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <button class="action-link danger" @click="openOutDialog(row)">出库</button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 库存流水 -->
    <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
      <div class="px-6 py-5 border-b border-slate-100 dark:border-slate-700">
        <h3 class="text-base font-semibold text-slate-800 dark:text-slate-200">库存流水</h3>
      </div>
      <div class="p-6">
        <el-table :data="transactions" border stripe class="page-table">
          <el-table-column label="类型" width="90" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.type === 'in' || row.quantity > 0" type="success" effect="light" size="small">入库</el-tag>
              <el-tag v-else type="danger" effect="light" size="small">出库</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="productId" label="产品ID" width="100" />
          <el-table-column label="数量" width="120" align="center">
            <template #default="{ row }">{{ Math.abs(row.quantity) }}</template>
          </el-table-column>
          <el-table-column label="库存变化" width="200" align="center">
            <template #default="{ row }">{{ row.beforeQty }} → {{ row.afterQty }}</template>
          </el-table-column>
          <el-table-column prop="batchNo" label="批次号" width="130" />
          <el-table-column prop="orderNo" label="关联单号" width="180" />
          <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
          <el-table-column prop="createTime" label="操作时间" width="170" />
        </el-table>
        <div class="flex justify-center mt-4">
          <el-pagination v-if="txTotal > 10" background layout="prev, pager, next" :total="txTotal" :page-size="10" v-model:current-page="txPage" @current-change="fetchTransactions" />
        </div>
      </div>
    </div>

    <!-- 入库弹窗 -->
    <el-dialog v-model="inVisible" title="产品入库" width="480px" class="custom-dialog">
      <el-form :model="inForm" label-width="80px" label-position="right">
        <el-form-item label="产品">
          <el-select v-model="inForm.productId" filterable placeholder="请选择产品" class="w-full">
            <el-option v-for="p in products" :key="p.id" :label="`[${p.code}] ${p.name}`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="inForm.warehouseId" placeholder="请选择仓库" class="w-full">
            <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量"><el-input-number v-model="inForm.quantity" :min="1" :precision="0" class="w-full" /></el-form-item>
        <el-form-item label="批次号"><el-input v-model="inForm.batchNo" placeholder="选填" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="inForm.remark" placeholder="选填" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inVisible = false">取消</el-button>
        <el-button type="primary" :loading="inLoading" @click="doStockIn">确认入库</el-button>
      </template>
    </el-dialog>

    <!-- 出库弹窗 -->
    <el-dialog v-model="outVisible" title="产品出库" width="480px" class="custom-dialog">
      <el-form :model="outForm" label-width="80px" label-position="right">
        <el-form-item label="产品"><el-input :model-value="`${outProduct.productCode} ${outProduct.productName}`" disabled /></el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="outForm.warehouseId" placeholder="请选择仓库" class="w-full">
            <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="出库数量"><el-input-number v-model="outForm.quantity" :min="1" :max="outProduct.quantity" :precision="0" class="w-full" /></el-form-item>
        <el-form-item label="批次号"><el-input v-model="outForm.batchNo" placeholder="选填，留空自动先进先出" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="outForm.remark" placeholder="选填" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="outVisible = false">取消</el-button>
        <el-button type="danger" :loading="outLoading" @click="doStockOut">确认出库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '@/api'

const list = ref([])
const transactions = ref([])
const txTotal = ref(0)
const txPage = ref(1)
const products = ref([])
const warehouses = ref([])

// ======= 入库 =======
const inVisible = ref(false)
const inLoading = ref(false)
const inForm = reactive({ productId: null, warehouseId: null, quantity: 1, batchNo: '', remark: '' })

// ======= 出库 =======
const outVisible = ref(false)
const outLoading = ref(false)
const outProduct = ref({})
const outForm = reactive({ productId: null, warehouseId: null, quantity: 1, batchNo: '', remark: '' })

onMounted(() => { fetchList(); fetchTransactions(); fetchProducts(); fetchWarehouses() })

async function fetchList() {
  try {
    const res = await api.get('/inventory')
    list.value = res.data || []
  } catch { /* ignore */ }
}

async function fetchTransactions() {
  try {
    const res = await api.get('/inventory/transactions', { params: { page: txPage.value, pageSize: 10 } })
    transactions.value = res.data?.list || []
    txTotal.value = res.data?.total || 0
  } catch { /* ignore */ }
}

async function fetchProducts() {
  try {
    const res = await api.get('/base/product', { params: { page: 1, pageSize: 999 } })
    products.value = res.data?.list || []
  } catch { /* ignore */ }
}

async function fetchWarehouses() {
  try {
    const res = await api.get('/base/warehouse')
    warehouses.value = Array.isArray(res.data) ? res.data : (res.data?.list || [])
  } catch { /* ignore */ }
}

function openInDialog() {
  inForm.productId = null
  inForm.warehouseId = warehouses.value[0]?.id || null
  inForm.quantity = 1
  inForm.batchNo = ''
  inForm.remark = ''
  inVisible.value = true
}

async function doStockIn() {
  if (!inForm.productId || !inForm.warehouseId || !inForm.quantity) {
    ElMessage.warning('请填写完整信息')
    return
  }
  inLoading.value = true
  try {
    const product = products.value.find(p => p.id === inForm.productId)
    const warehouse = warehouses.value.find(w => w.id === inForm.warehouseId)
    await api.post('/inventory/in', {
      productId: inForm.productId,
      warehouseId: inForm.warehouseId,
      quantity: inForm.quantity,
      batchNo: inForm.batchNo || null,
      type: '采购入库',
      orderNo: null,
      remark: inForm.remark || null
    })
    ElMessage.success(`「${product?.name}」入库 ${inForm.quantity} 件到「${warehouse?.name}」`)
    inVisible.value = false
    fetchList()
    fetchTransactions()
  } catch { /* ignore */ }
  finally { inLoading.value = false }
}

function openOutDialog(row) {
  outProduct.value = row
  outForm.warehouseId = row.warehouseId || warehouses.value[0]?.id || null
  outForm.quantity = 1
  outForm.batchNo = ''
  outForm.remark = ''
  outVisible.value = true
}

async function doStockOut() {
  if (!outForm.warehouseId || !outForm.quantity) {
    ElMessage.warning('请填写完整信息')
    return
  }
  outLoading.value = true
  try {
    await api.post('/inventory/out', {
      productId: outProduct.value.productId,
      warehouseId: outForm.warehouseId,
      quantity: outForm.quantity,
      batchNo: outForm.batchNo || null,
      type: '其它出库',
      orderNo: null,
      remark: outForm.remark || null
    })
    ElMessage.success(`「${outProduct.value.productName}」出库 ${outForm.quantity} 件`)
    outVisible.value = false
    fetchList()
    fetchTransactions()
  } catch { /* ignore */ }
  finally { outLoading.value = false }
}
</script>
