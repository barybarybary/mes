<template>
  <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100 dark:border-slate-700 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-lg font-semibold text-slate-800 dark:text-slate-200">发货管理</h2>
        <p class="text-xs text-slate-400 dark:text-slate-500 mt-1">管理销售发货单，自动扣减库存并更新订单状态</p>
      </div>
      <div class="flex items-center gap-3">
        <el-button type="primary" @click="openCreateDialog()" class="h-10 px-5 rounded-xl font-medium">
          <el-icon class="mr-1"><Plus /></el-icon>创建发货
        </el-button>
      </div>
    </div>

    <div class="p-6">
      <el-table :data="list" stripe v-loading="loading" class="page-table">
        <el-table-column prop="deliveryNo" label="发货单号" width="170">
          <template #default="{ row }"><span class="font-medium text-sky-600">{{ row.deliveryNo }}</span></template>
        </el-table-column>
        <el-table-column prop="orderNo" label="关联订单" width="170">
          <template #default="{ row }"><span class="font-medium text-slate-600 dark:text-slate-400">{{ row.orderNo }}</span></template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户" min-width="140" />
        <el-table-column label="发货产品" min-width="220">
          <template #default="{ row }">
            <span v-if="row.items?.length" class="text-slate-600 dark:text-slate-400">
              {{ row.items.map(i => `${i.productName || '产品#' + i.productId} ×${i.quantity}`).join('、') }}
            </span>
            <span v-else class="text-slate-400">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="deliveryDate" label="发货日期" width="120" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="warning" effect="light" round size="small">待发货</el-tag>
            <el-tag v-else-if="row.status === 2" type="primary" effect="light" round size="small">运输中</el-tag>
            <el-tag v-else-if="row.status === 3" type="success" effect="light" round size="small">已签收</el-tag>
            <el-tag v-else type="info" effect="light" round size="small">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-2">
              <el-button type="primary" @click="openViewDialog(row)">详情</el-button>
              <el-button v-if="row.status === 1" type="success" @click="updateStatus(row.id, 3)">签收</el-button>
              <el-button type="danger" @click="del(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="mt-5 flex justify-end">
        <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize" layout="prev, pager, next, total" background @current-change="fetchData" />
      </div>
    </div>

    <!-- 创建发货弹窗 -->
    <el-dialog v-model="createVisible" title="创建发货单" width="700px" class="custom-dialog">
      <el-form :model="deliveryForm" label-width="80px" label-position="right">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="关联订单">
              <el-select v-model="deliveryForm.orderId" filterable class="w-full" placeholder="选择销售订单" @change="onOrderChange">
                <el-option v-for="o in deliverableOrders" :key="o.id" :value="o.id"
                  :label="`${o.orderNo} — ${o.customerName}`" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发货日期">
              <el-date-picker v-model="deliveryForm.deliveryDate" type="date" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-divider content-position="left">
        <span class="font-semibold text-sm">发货产品</span>
      </el-divider>

      <el-table :data="deliveryItems" class="page-table">
        <el-table-column label="产品" min-width="180">
          <template #default="{ row }">{{ row.productName || '产品#' + row.productId }}</template>
        </el-table-column>
        <el-table-column label="已订数量" width="100" align="right">
          <template #default="{ row }">{{ row.orderedQty || 0 }}</template>
        </el-table-column>
        <el-table-column label="已发数量" width="100" align="right">
          <template #default="{ row }">{{ row.deliveredQty || 0 }}</template>
        </el-table-column>
        <el-table-column label="本次发货" width="150" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="0" :max="(row.orderedQty || 0) - (row.deliveredQty || 0)"
              :controls-position="'right'" class="w-full" />
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="createVisible = false" class="rounded-xl px-5">取消</el-button>
        <el-button type="primary" @click="createDelivery" class="rounded-xl px-5">确认发货</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewVisible" title="发货单详情" width="600px" class="custom-dialog">
      <template v-if="viewDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="发货单号">{{ viewDetail.deliveryNo }}</el-descriptions-item>
          <el-descriptions-item label="关联订单">{{ viewDetail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="客户">{{ viewDetail.customerName }}</el-descriptions-item>
          <el-descriptions-item label="发货日期">{{ viewDetail.deliveryDate }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="viewDetail.status === 1" type="warning" effect="light" size="small">待发货</el-tag>
            <el-tag v-else-if="viewDetail.status === 2" type="primary" effect="light" size="small">运输中</el-tag>
            <el-tag v-else-if="viewDetail.status === 3" type="success" effect="light" size="small">已签收</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注">{{ viewDetail.remark || '—' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">
          <span class="text-sm font-semibold">发货明细</span>
        </el-divider>

        <el-table :data="viewDetail.items || []" class="page-table">
          <el-table-column label="产品" min-width="160">
            <template #default="{ row }">{{ row.productName || '产品#' + row.productId }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="100" align="right" />
          <el-table-column prop="batchNo" label="批次" width="120" />
        </el-table>
      </template>
      <template #footer>
        <el-button @click="viewVisible = false" class="rounded-xl px-5">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const list = ref([]), loading = ref(false), page = ref(1), total = ref(0), pageSize = ref(10)

// 创建发货
const createVisible = ref(false), deliveryForm = reactive({}), deliveryItems = ref([]), deliverableOrders = ref([])
async function openCreateDialog() {
  // 加载可发货的订单 (status 2已支付, 3生产中, 4部分发货)
  const r = await api.get('/sale/order', { params: { pageSize: 999 } })
  deliverableOrders.value = (r.data?.list || r.data?.records || []).filter(o => [2, 3, 4].includes(o.status))
  Object.assign(deliveryForm, { orderId: null, deliveryDate: new Date().toISOString().split('T')[0] })
  deliveryItems.value = []
  createVisible.value = true
}

async function onOrderChange(orderId) {
  if (!orderId) { deliveryItems.value = []; return }
  const r = await api.get(`/sale/order/${orderId}`)
  const o = r.data
  deliveryItems.value = (o.items || []).map(i => ({
    productId: i.productId,
    productName: i.productName || `产品#${i.productId}`,
    orderedQty: i.quantity || 0,
    deliveredQty: i.deliveredQty || 0,
    quantity: Math.max(0, (i.quantity || 0) - (i.deliveredQty || 0))
  }))
}

async function createDelivery() {
  if (!deliveryForm.orderId) { ElMessage.warning('请选择订单'); return }
  const items = deliveryItems.value.filter(i => i.quantity > 0)
  if (!items.length) { ElMessage.warning('请填写发货数量'); return }
  const order = deliverableOrders.value.find(o => o.id === deliveryForm.orderId)
  await api.post('/sale/delivery', {
    orderId: deliveryForm.orderId,
    customerId: order?.customerId,
    deliveryDate: deliveryForm.deliveryDate,
    status: 1,
    items: items.map(i => ({ productId: i.productId, quantity: i.quantity }))
  })
  ElMessage.success('发货单创建成功，已自动扣减库存'); createVisible.value = false; fetchData()
}

// 查看详情
const viewVisible = ref(false), viewDetail = ref(null)
async function openViewDialog(row) {
  const r = await api.get(`/sale/delivery/${row.id}`)
  viewDetail.value = r.data
  viewVisible.value = true
}

// 列表
async function fetchData() {
  loading.value = true
  try {
    const r = await api.get('/sale/delivery', { params: { page: page.value, pageSize: pageSize.value } })
    if (r.code === 200) { list.value = r.data.list || r.data.records || []; total.value = r.data.total || 0 }
  } finally { loading.value = false }
}

async function updateStatus(id, status) {
  await api.put(`/sale/delivery/${id}/status`, null, { params: { status } })
  ElMessage.success(status === 3 ? '已标记签收' : '状态已更新'); fetchData()
}

async function del(id) {
  await ElMessageBox.confirm('确定要删除该发货单吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.delete(`/sale/delivery/${id}`); ElMessage.success('已删除'); fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
:deep(.page-table th.el-table__cell) {
  background-color: #f8fafc !important; color: #475569 !important;
  font-weight: 600 !important; font-size: 13px !important;
}
html.dark :deep(.page-table th.el-table__cell) {
  background-color: #1e293b !important; color: #94a3b8 !important;
}

:deep(.custom-dialog .el-dialog) { border-radius: 16px !important; }
:deep(.custom-dialog .el-dialog__header) {
  padding: 20px 24px 16px !important; margin-right: 0 !important; border-bottom: 1px solid #f1f5f9;
}
html.dark :deep(.custom-dialog .el-dialog__header) { border-bottom-color: #334155 !important; }
:deep(.custom-dialog .el-dialog__body) { padding: 24px !important; }
:deep(.custom-dialog .el-dialog__footer) {
  padding: 16px 24px 20px !important; border-top: 1px solid #f1f5f9;
}
html.dark :deep(.custom-dialog .el-dialog__footer) { border-top-color: #334155 !important; }

:deep(.el-input-number) { width: 100% !important; }
:deep(.el-input-number .el-input-number__decrease),
:deep(.el-input-number .el-input-number__increase) {
  background: #f8fafc !important; border: 1px solid #e2e8f0 !important;
  color: #64748b !important; box-shadow: none !important; outline: none !important;
}
:deep(.el-input-number .el-input-number__decrease:hover),
:deep(.el-input-number .el-input-number__increase:hover) {
  background: #e2e8f0 !important; color: #334155 !important; border-color: #cbd5e1 !important;
}
:deep(.el-input-number .el-input-number__decrease:focus-visible),
:deep(.el-input-number .el-input-number__increase:focus-visible) {
  outline: none !important; box-shadow: none !important;
}
:deep(.el-input-number .el-input__wrapper) { box-shadow: none !important; }
html.dark :deep(.el-input-number .el-input-number__decrease),
html.dark :deep(.el-input-number .el-input-number__increase) {
  background: #334155 !important; border-color: #475569 !important; color: #94a3b8 !important;
}
html.dark :deep(.el-input-number .el-input-number__decrease:hover),
html.dark :deep(.el-input-number .el-input-number__increase:hover) {
  background: #475569 !important; color: #cbd5e1 !important; border-color: #64748b !important;
}
</style>
