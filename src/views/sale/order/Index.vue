<template>
  <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100 dark:border-slate-700 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-lg font-semibold text-slate-800 dark:text-slate-200">销售订单</h2>
        <p class="text-xs text-slate-400 dark:text-slate-500 mt-1">管理客户销售订单，审核后转生产</p>
      </div>
      <div class="flex items-center gap-3">
        <el-input v-model="keyword" placeholder="搜索订单号／客户" clearable class="w-56" @change="fetchData">
          <template #prefix><el-icon class="text-slate-400"><Search /></el-icon></template>
        </el-input>
        <el-select v-model="filterStatus" placeholder="全部状态" clearable class="w-32" @change="fetchData">
          <el-option label="待付款" :value="1" />
          <el-option label="已支付" :value="2" />
          <el-option label="生产中" :value="3" />
          <el-option label="部分发货" :value="4" />
          <el-option label="已完成" :value="5" />
          <el-option label="已取消" :value="6" />
        </el-select>
        <el-button type="primary" @click="openDialog()" class="h-10 px-5 rounded-xl font-medium">
          <el-icon class="mr-1"><Plus /></el-icon>新增订单
        </el-button>
      </div>
    </div>

    <div class="p-6">
      <el-table :data="list" stripe v-loading="loading" class="page-table">
        <el-table-column prop="orderNo" label="订单号" width="180">
          <template #default="{ row }"><span class="font-medium text-sky-600">{{ row.orderNo }}</span></template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户" min-width="140" />
        <el-table-column prop="orderDate" label="订单日期" width="120" align="center" />
        <el-table-column prop="deliveryDate" label="预计交期" width="120" align="center" />
        <el-table-column prop="totalAmount" label="订单金额" width="130" align="right">
          <template #default="{ row }">
            <span class="font-semibold text-amber-600">¥{{ (row.totalAmount || 0).toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="105" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="info" effect="light" round size="small">待付款</el-tag>
            <el-tag v-else-if="row.status === 2" type="warning" effect="light" round size="small">已支付</el-tag>
            <el-tag v-else-if="row.status === 3" type="primary" effect="light" round size="small">生产中</el-tag>
            <el-tag v-else-if="row.status === 4" type="" effect="light" round size="small">部分发货</el-tag>
            <el-tag v-else-if="row.status === 5" type="success" effect="light" round size="small">已完成</el-tag>
            <el-tag v-else type="danger" effect="light" round size="small">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-2">
              <el-button type="primary" @click="openDialog(row)">详情</el-button>
              <el-button v-if="row.status === 1" type="warning" @click="updateStatus(row.id, 2)">确认收款</el-button>
              <el-button v-if="row.status === 2" type="primary" @click="convertToProduction(row.id)">转生产</el-button>
              <el-button v-if="row.status === 1" type="danger" @click="updateStatus(row.id, 6)">取消</el-button>
              <el-button v-if="row.status === 6" type="danger" @click="del(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="mt-5 flex justify-end">
        <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize" layout="prev, pager, next, total" background @current-change="fetchData" />
      </div>
    </div>

    <!-- 新增/详情弹窗 -->
    <el-dialog v-model="visible" :title="editing.id ? '订单详情' : '新增销售订单'" width="800px" class="custom-dialog">
      <el-form :model="form" label-width="90px" label-position="right">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户">
              <el-select v-model="form.customerId" filterable class="w-full" placeholder="选择客户">
                <el-option v-for="c in customers" :key="c.id" :value="c.id" :label="c.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计交期">
              <el-date-picker v-model="form.deliveryDate" type="date" class="w-full" placeholder="选择交期" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息" />
        </el-form-item>
      </el-form>

      <el-divider content-position="left">
        <span class="font-semibold text-sm">产品明细</span>
      </el-divider>

      <el-table :data="orderItems" class="page-table">
        <el-table-column label="产品" min-width="200">
          <template #default="{ row }">
            <el-select v-model="row.productId" filterable class="w-full" placeholder="选择产品" @change="onProductChange(row)">
              <el-option v-for="p in products" :key="p.id" :value="p.id" :label="`${p.name} (¥${p.price || 0})`" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="130" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="0" :controls-position="'right'" class="w-full" @change="calcItemAmount(row)" />
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120" align="right">
          <template #default="{ row }">{{ (row.price || 0).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column label="金额" width="130" align="right">
          <template #default="{ row }">
            <span class="font-medium text-amber-600">¥{{ (row.amount || 0).toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ $index }">
            <el-button type="danger" @click="orderItems.splice($index, 1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button class="mt-4" @click="orderItems.push({ productId: null, quantity: 1, price: 0, amount: 0 })" :text="true">
        <el-icon class="mr-1"><Plus /></el-icon>添加产品
      </el-button>

      <template #footer>
        <el-button @click="visible = false" class="rounded-xl px-5" :text="true">关闭</el-button>
        <el-button v-if="!editing.id" type="primary" @click="create" class="rounded-xl px-5">创建订单</el-button>
        <el-button v-if="editing.id && editing.status === 1" type="primary" @click="save" class="rounded-xl px-5">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const list = ref([]), loading = ref(false), page = ref(1), total = ref(0), pageSize = ref(10)
const filterStatus = ref(), keyword = ref('')
const visible = ref(false), editing = ref({}), form = reactive({}), orderItems = ref([])
const customers = ref([]), products = ref([])

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (filterStatus.value) params.status = filterStatus.value
    if (keyword.value) params.keyword = keyword.value
    const r = await api.get('/sale/order', { params })
    if (r.code === 200) { list.value = r.data.list || r.data.records || []; total.value = r.data.total || 0 }
  } finally { loading.value = false }
}

async function openDialog(row) {
  customers.value = (await api.get('/base/customer', { params: { pageSize: 999 } })).data?.list || (await api.get('/base/customer')).data || []
  products.value = (await api.get('/base/product', { params: { pageSize: 999 } })).data?.list || []
  if (row?.id) {
    editing.value = row
    const r = await api.get(`/sale/order/${row.id}`)
    const o = r.data
    Object.assign(form, { customerId: o.customerId, deliveryDate: o.deliveryDate, remark: o.remark })
    orderItems.value = (o.items || []).map(i => ({ productId: i.productId, quantity: i.quantity, price: i.price, amount: i.amount, unit: i.unit }))
  } else {
    editing.value = {}
    Object.assign(form, { customerId: null, deliveryDate: '', remark: '' })
    orderItems.value = []
  }
  visible.value = true
}

function onProductChange(item) {
  const p = products.value.find(x => x.id === item.productId)
  if (p) { item.price = p.price || 0; calcItemAmount(item) }
}

function calcItemAmount(item) {
  item.amount = (item.quantity || 0) * (item.price || 0)
}

async function create() {
  if (!form.customerId) { ElMessage.warning('请选择客户'); return }
  if (!orderItems.value.length) { ElMessage.warning('请至少添加一个产品'); return }
  const body = {
    customerId: form.customerId,
    orderDate: new Date().toISOString().split('T')[0],
    deliveryDate: form.deliveryDate || undefined,
    remark: form.remark || undefined,
    items: orderItems.value.map(i => ({ productId: i.productId, quantity: i.quantity }))
  }
  await api.post('/sale/order', body)
  ElMessage.success('订单创建成功'); visible.value = false; fetchData()
}

async function save() {
  const body = {
    id: editing.value.id,
    deliveryDate: form.deliveryDate || undefined,
    remark: form.remark || undefined,
    items: orderItems.value.map(i => ({ productId: i.productId, quantity: i.quantity }))
  }
  await api.put('/sale/order', body)
  ElMessage.success('修改保存成功'); visible.value = false; fetchData()
}

async function updateStatus(id, status) {
  const labels = { 2: '确认收款', 6: '取消订单' }
  await ElMessageBox.confirm(`确定${labels[status] || '执行此操作'}吗？`, '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.put(`/sale/order/${id}/status`, null, { params: { status } })
  ElMessage.success('操作成功'); fetchData()
}

async function convertToProduction(id) {
  await ElMessageBox.confirm('确定将该订单转为生产工单吗？将自动根据产品BOM生成工序。', '转生产确认', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  const r = await api.put(`/sale/order/${id}/to-production`)
  const count = r.data?.length || 0
  ElMessage.success(`已生成 ${count} 个生产工单，状态已更新为「生产中」`)
  fetchData()
}

async function del(id) {
  await ElMessageBox.confirm('确定要删除该订单吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.delete(`/sale/order/${id}`); ElMessage.success('已删除'); fetchData()
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
