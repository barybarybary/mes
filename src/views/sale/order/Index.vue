<template>
  <div class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-lg font-semibold text-slate-800">销售订单</h2>
        <p class="text-xs text-slate-400 mt-1">管理销售订单和订单流转</p>
      </div>
      <div class="flex items-center gap-3">
        <el-select v-model="filterStatus" placeholder="全部状态" clearable class="w-32" @change="fetchData">
          <el-option label="待审核" :value="1" />
          <el-option label="已审核" :value="2" />
          <el-option label="生产中" :value="3" />
          <el-option label="部分发货" :value="4" />
          <el-option label="已完成" :value="5" />
        </el-select>
        <el-button type="primary" @click="openDialog()" class="h-10 px-5 rounded-xl font-medium">
          <el-icon class="mr-1"><Plus /></el-icon>新增订单
        </el-button>
      </div>
    </div>

    <div class="p-6">
      <el-table :data="list" stripe v-loading="loading" class="page-table">
        <el-table-column prop="orderNo" label="订单号" width="180">
          <template #default="{ row }">
            <span class="font-medium text-sky-600">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户名称" min-width="150" />
        <el-table-column prop="totalAmount" label="订单金额" width="130" align="right">
          <template #default="{ row }">
            <span class="font-semibold text-emerald-600">¥{{ row.totalAmount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="warning" effect="light" round size="small">待审核</el-tag>
            <el-tag v-else-if="row.status === 2" type="success" effect="light" round size="small">已审核</el-tag>
            <el-tag v-else-if="row.status === 3" type="primary" effect="light" round size="small">生产中</el-tag>
            <el-tag v-else-if="row.status === 4" type="info" effect="light" round size="small">部分发货</el-tag>
            <el-tag v-else-if="row.status === 5" type="success" round size="small">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderDate" label="订单日期" width="120" />
        <el-table-column label="操作" width="210" align="center" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-2">
              <button class="action-link primary" @click="openDialog(row)">详情</button>
              <button v-if="row.status === 1" class="action-link success" @click="updateStatus(row.id, 2)">审核</button>
              <button v-if="row.status === 2" class="action-link warning" @click="updateStatus(row.id, 3)">转生产</button>
              <button v-if="row.status >= 2 && row.status < 5" class="action-link primary" @click="updateStatus(row.id, row.status + 1)">下一状态</button>
              <button class="action-link danger" @click="del(row.id)">删除</button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="mt-5 flex justify-end">
        <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize" layout="prev, pager, next, total" background @current-change="fetchData" />
      </div>
    </div>

    <el-dialog v-model="visible" :title="editing.id ? '订单详情' : '新增订单'" width="800px" class="custom-dialog">
      <el-form :model="form" label-width="80px" :disabled="form.status !== 1">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户">
              <el-select v-model="form.customerId" filterable class="w-full" placeholder="请选择客户">
                <el-option v-for="c in customers" :key="c.id" :value="c.id" :label="c.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交期">
              <el-date-picker v-model="form.deliveryDate" type="date" class="w-full" placeholder="选择交期" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
      </el-form>

      <el-divider content-position="left">
        <span class="font-semibold">订单明细</span>
      </el-divider>

      <el-table :data="items" class="page-table">
        <el-table-column label="产品" min-width="200">
          <template #default="{ row }">
            <el-select v-model="row.productId" filterable class="w-full" placeholder="选择产品">
              <el-option v-for="p in products" :key="p.id" :value="p.id" :label="p.name" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="130">
          <template #default="{ row }"><el-input-number v-model="row.quantity" :min="0" size="small" class="w-full" /></template>
        </el-table-column>
        <el-table-column label="单价" width="130">
          <template #default="{ row }"><el-input-number v-model="row.price" :min="0" :precision="2" size="small" class="w-full" /></template>
        </el-table-column>
        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }">
            <span class="font-medium text-emerald-600">¥{{ (row.quantity * row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="70" align="center">
          <template #default="{ $index }">
            <button class="action-link danger" @click="items.splice($index, 1)">删除</button>
          </template>
        </el-table-column>
      </el-table>
      <el-button class="mt-4" @click="items.push({ productId: null, quantity: 1, price: 0 })">
        <el-icon class="mr-1"><Plus /></el-icon>添加产品
      </el-button>

      <template #footer>
        <el-button @click="visible = false" class="rounded-xl px-5">关闭</el-button>
        <el-button v-if="editing.id" type="primary" @click="save" class="rounded-xl px-5">保存修改</el-button>
        <el-button v-if="!editing.id" type="primary" @click="create" class="rounded-xl px-5">创建订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const list = ref([]), loading = ref(false), page = ref(1), total = ref(0), pageSize = ref(10), filterStatus = ref()
const visible = ref(false), editing = ref({}), form = reactive({}), items = ref([])
const customers = ref([]), products = ref([])

async function fetchData() {
  loading.value = true
  try { const r = await api.get('/sale/order', { params: { page: page.value, pageSize: pageSize.value, status: filterStatus.value } }); if (r.code === 200) { list.value = r.data.list; total.value = r.data.total } } finally { loading.value = false }
}

async function openDialog(row) {
  customers.value = (await api.get('/base/customer', { params: { pageSize: 999 } })).data?.list || []
  products.value = (await api.get('/base/product', { params: { pageSize: 999 } })).data?.list || []
  if (row?.id) {
    editing.value = row
    const r = await api.get(`/sale/order/${row.id}`)
    const o = r.data
    Object.assign(form, { customerId: o.customerId, deliveryDate: o.deliveryDate, remark: o.remark, status: o.status })
    items.value = o.items || []
  } else {
    editing.value = {}; Object.assign(form, { customerId: null, deliveryDate: '', remark: '', status: 1 }); items.value = []
  }
  visible.value = true
}

async function create() { await api.post('/sale/order', { ...form, items: items.value }); ElMessage.success('创建成功'); visible.value = false; fetchData() }
async function save() { await api.put('/sale/order', { id: editing.value.id, ...form, items: items.value }); ElMessage.success('保存成功'); visible.value = false; fetchData() }
async function updateStatus(id, status) {
  await ElMessageBox.confirm('确定要更新订单状态吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.put(`/sale/order/${id}/status?status=${status}`); ElMessage.success('状态已更新'); fetchData()
}
async function del(id) {
  await ElMessageBox.confirm('确定要删除该订单吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.delete(`/sale/order/${id}`); ElMessage.success('已删除'); fetchData()
}
onMounted(fetchData)
</script>

<style scoped>
.action-link {
  background: none;
  border: none;
  padding: 0;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.15s;
  outline: none;
}
.action-link.primary { color: #3b82f6; }
.action-link.primary:hover { color: #1d4ed8; }
.action-link.success { color: #10b981; }
.action-link.success:hover { color: #047857; }
.action-link.warning { color: #f59e0b; }
.action-link.warning:hover { color: #b45309; }
.action-link.danger { color: #f43f5e; }
.action-link.danger:hover { color: #be123c; }

:deep(.page-table th.el-table__cell) { background-color: #f8fafc !important; color: #475569 !important; font-weight: 600 !important; font-size: 13px !important; }
:deep(.custom-dialog .el-dialog) { border-radius: 16px !important; }
:deep(.custom-dialog .el-dialog__header) { padding: 20px 24px 16px !important; margin-right: 0 !important; border-bottom: 1px solid #f1f5f9; }
:deep(.custom-dialog .el-dialog__body) { padding: 24px !important; }
:deep(.custom-dialog .el-dialog__footer) { padding: 16px 24px 20px !important; border-top: 1px solid #f1f5f9; }
</style>
