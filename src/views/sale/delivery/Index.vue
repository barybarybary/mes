<template>
  <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
    <!-- 头部 -->
    <div class="px-6 py-5 border-b border-slate-100 dark:border-slate-700 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-lg font-semibold text-slate-800 dark:text-slate-200">发货管理</h2>
        <p class="text-xs text-slate-400 mt-1">管理销售发货单、物流跟踪与签收确认</p>
      </div>
      <div class="flex items-center gap-3">
        <el-select v-model="filterStatus" placeholder="全部状态" clearable class="w-32" @change="fetchData">
          <el-option label="待发货" :value="1" />
          <el-option label="已发货" :value="2" />
          <el-option label="已签收" :value="3" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索发货单号/订单号" clearable class="w-56" @change="search">
          <template #prefix><el-icon class="text-slate-400"><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="openDialog()" class="h-10 px-5 rounded-xl font-medium">
          <el-icon class="mr-1"><Plus /></el-icon>新增发货
        </el-button>
      </div>
    </div>

    <!-- 桌面端表格 -->
    <div class="p-6 hidden md:block">
      <el-table :data="list" stripe v-loading="loading" class="page-table">
        <el-table-column prop="deliveryNo" label="发货单号" width="180">
          <template #default="{ row }">
            <span class="font-medium text-sky-600">{{ row.deliveryNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="关联订单" width="180">
          <template #default="{ row }">
            <span class="text-slate-500">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户名称" min-width="150" />
        <el-table-column label="发货状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="warning" effect="light" round size="small">待发货</el-tag>
            <el-tag v-else-if="row.status === 2" type="primary" effect="light" round size="small">已发货</el-tag>
            <el-tag v-else-if="row.status === 3" type="success" round size="small">已签收</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deliveryDate" label="发货日期" width="120" />
        <el-table-column prop="carrier" label="物流公司" width="130">
          <template #default="{ row }"><span class="text-slate-500">{{ row.carrier || '-' }}</span></template>
        </el-table-column>
        <el-table-column prop="trackingNo" label="物流单号" width="160">
          <template #default="{ row }">
            <span v-if="row.trackingNo" class="text-xs text-sky-500 font-mono">{{ row.trackingNo }}</span>
            <span v-else class="text-slate-300">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-2">
              <button class="action-link primary" @click="openDialog(row)">详情</button>
              <button v-if="row.status === 1" class="action-link success" @click="updateStatus(row.id, 2)">发货</button>
              <button v-if="row.status === 2" class="action-link warning" @click="updateStatus(row.id, 3)">签收</button>
              <button v-if="row.status === 1" class="action-link danger" @click="del(row.id)">删除</button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="mt-5 flex justify-end">
        <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize" layout="prev, pager, next, total" background @current-change="fetchData" />
      </div>
    </div>

    <!-- 移动端卡片列表 -->
    <div class="p-4 md:hidden" v-loading="loading">
      <div v-if="list.length === 0 && !loading" class="text-center py-16 text-slate-400">
        <el-icon :size="48" class="mb-3"><Document /></el-icon>
        <p class="text-sm">暂无发货记录</p>
      </div>
      <div v-else class="space-y-3">
        <div
          v-for="row in list" :key="row.id"
          class="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-600 p-4 active:bg-slate-50"
          @click="openDialog(row)"
        >
          <div class="flex items-start justify-between mb-2">
            <div>
              <div class="flex items-center gap-2 mb-1">
                <span class="text-sm font-semibold text-sky-600">{{ row.deliveryNo }}</span>
                <el-tag v-if="row.status === 1" type="warning" effect="light" round size="small">待发货</el-tag>
                <el-tag v-else-if="row.status === 2" type="primary" effect="light" round size="small">已发货</el-tag>
                <el-tag v-else-if="row.status === 3" type="success" round size="small">已签收</el-tag>
              </div>
              <p class="text-xs text-slate-500">订单：{{ row.orderNo }} | 客户：{{ row.customerName }}</p>
            </div>
          </div>
          <div class="flex flex-wrap gap-x-4 gap-y-1 text-xs text-slate-500 pt-2 border-t border-slate-100 dark:border-slate-600">
            <span>{{ row.deliveryDate || '-' }}</span>
            <span>物流：{{ row.carrier || '-' }}</span>
            <span v-if="row.trackingNo" class="text-sky-500 font-mono">{{ row.trackingNo }}</span>
          </div>
        </div>
      </div>
      <div class="mt-4 flex justify-center" v-if="total > pageSize">
        <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize" layout="prev, pager, next" background small @current-change="fetchData" />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="visible" :title="editing.id ? '发货详情' : '新增发货'" width="800px" class="custom-dialog">
      <el-form :model="form" label-width="85px" :disabled="form.status !== 1">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="销售订单">
              <el-select v-model="form.orderId" filterable class="w-full" placeholder="请选择订单" @change="onOrderChange">
                <el-option v-for="o in orders" :key="o.id" :value="o.id" :label="`${o.orderNo} - ${o.customerName}`" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发货日期">
              <el-date-picker v-model="form.deliveryDate" type="date" class="w-full" placeholder="选择发货日期" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物流公司">
              <el-select v-model="form.carrier" filterable allow-create class="w-full" placeholder="请选择或输入物流公司">
                <el-option label="顺丰速运" value="顺丰速运" />
                <el-option label="中通快递" value="中通快递" />
                <el-option label="圆通速递" value="圆通速递" />
                <el-option label="韵达快递" value="韵达快递" />
                <el-option label="京东物流" value="京东物流" />
                <el-option label="德邦物流" value="德邦物流" />
                <el-option label="安能物流" value="安能物流" />
                <el-option label="壹米滴答" value="壹米滴答" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物流单号">
              <el-input v-model="form.trackingNo" placeholder="请输入物流单号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>

      <el-divider content-position="left">
        <span class="font-semibold text-slate-700 dark:text-slate-300">发货明细</span>
      </el-divider>

      <el-table :data="items" class="page-table" :class="{ 'opacity-60': form.status !== 1 }">
        <el-table-column label="产品" min-width="200">
          <template #default="{ row }">
            <el-select v-model="row.productId" filterable class="w-full" placeholder="选择产品" :disabled="form.status !== 1">
              <el-option v-for="p in products" :key="p.id" :value="p.id" :label="p.name" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="发货数量" width="130">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" size="small" class="w-full" :disabled="form.status !== 1" />
          </template>
        </el-table-column>
        <el-table-column label="单位" width="80" align="center">
          <template #default="{ row }"><span class="text-sm text-slate-500">{{ row.unit || '个' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="70" align="center">
          <template #default="{ $index }">
            <button v-if="form.status === 1" class="action-link danger" @click="items.splice($index, 1)">删除</button>
          </template>
        </el-table-column>
      </el-table>
      <el-button v-if="form.status === 1" class="mt-4" @click="items.push({ productId: null, quantity: 1, unit: '个' })">
        <el-icon class="mr-1"><Plus /></el-icon>添加产品
      </el-button>

      <template #footer>
        <el-button @click="visible = false" class="rounded-xl px-5">关闭</el-button>
        <el-button v-if="editing.id && form.status === 1" type="primary" @click="save" class="rounded-xl px-5">保存修改</el-button>
        <el-button v-if="!editing.id" type="primary" @click="create" class="rounded-xl px-5">创建发货单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Document, Plus } from '@element-plus/icons-vue'
import api from '@/api'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const pageSize = ref(10)
const filterStatus = ref()
const keyword = ref('')

const visible = ref(false)
const editing = ref({})
const form = reactive({
  orderId: null,
  deliveryDate: '',
  carrier: '',
  trackingNo: '',
  remark: '',
  status: 1
})
const items = ref([])
const orders = ref([])
const products = ref([])

function search() {
  page.value = 1
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const r = await api.get('/sale/delivery', {
      params: {
        page: page.value,
        pageSize: pageSize.value,
        status: filterStatus.value || undefined,
        keyword: keyword.value || undefined
      }
    })
    if (r.code === 200) {
      list.value = r.data.list || r.data.records || []
      total.value = r.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

async function openDialog(row) {
  // 加载可发货的订单列表
  try {
    const ordersRes = await api.get('/sale/order', { params: { pageSize: 999, status: 2 } })
    orders.value = (ordersRes.data?.list || ordersRes.data?.records || []).filter(
      o => o.status === 2 || o.status === 3 || o.status === 4
    )
  } catch { orders.value = [] }

  products.value = (await api.get('/base/product', { params: { pageSize: 999 } })).data?.list || []

  if (row?.id) {
    editing.value = row
    const r = await api.get(`/sale/delivery/${row.id}`)
    const o = r.data
    Object.assign(form, {
      orderId: o.orderId,
      deliveryDate: o.deliveryDate,
      carrier: o.carrier || '',
      trackingNo: o.trackingNo || '',
      remark: o.remark || '',
      status: o.status
    })
    items.value = o.items || []
  } else {
    editing.value = {}
    Object.assign(form, {
      orderId: null,
      deliveryDate: '',
      carrier: '',
      trackingNo: '',
      remark: '',
      status: 1
    })
    items.value = []
  }
  visible.value = true
}

function onOrderChange(orderId) {
  const order = orders.value.find(o => o.id === orderId)
  if (order) {
    // 自动填充订单的产品明细
    items.value = (order.items || []).map(it => ({
      productId: it.productId,
      productName: it.productName,
      quantity: it.quantity - (it.deliveredQty || 0), // 剩余可发货量
      unit: it.unit || '个'
    })).filter(it => it.quantity > 0)
  }
}

async function create() {
  if (!form.orderId) {
    ElMessage.warning('请选择销售订单')
    return
  }
  if (!items.value.length) {
    ElMessage.warning('请至少添加一个发货产品')
    return
  }
  try {
    await api.post('/sale/delivery', { ...form, items: items.value })
    ElMessage.success('发货单创建成功')
    visible.value = false
    fetchData()
  } catch { /* error handled by interceptor */ }
}

async function save() {
  try {
    await api.put('/sale/delivery', { id: editing.value.id, ...form, items: items.value })
    ElMessage.success('保存成功')
    visible.value = false
    fetchData()
  } catch { /* error handled by interceptor */ }
}

async function updateStatus(id, status) {
  const labels = { 2: '确认发货', 3: '确认签收' }
  try {
    await ElMessageBox.confirm(
      `确定要${labels[status]}吗？`,
      '提示',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
    )
    await api.put(`/sale/delivery/${id}/status?status=${status}`)
    ElMessage.success(`${labels[status]}成功`)
    fetchData()
  } catch { /* user cancelled */ }
}

async function del(id) {
  try {
    await ElMessageBox.confirm('确定要删除该发货单吗？', '提示', {
      type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消'
    })
    await api.delete(`/sale/delivery/${id}`)
    ElMessage.success('已删除')
    fetchData()
  } catch { /* user cancelled */ }
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
</style>
