<template>
  <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100 dark:border-slate-700 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-lg font-semibold text-slate-800 dark:text-slate-200">生产工单</h2>
        <p class="text-xs text-slate-400 dark:text-slate-500 mt-1">管理生产工单和工序安排</p>
      </div>
      <div class="flex items-center gap-3">
        <el-select v-model="filterStatus" placeholder="全部状态" clearable class="w-32" @change="fetchData">
          <el-option label="待生产" :value="1" />
          <el-option label="生产中" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已入库" :value="4" />
        </el-select>
        <el-button type="primary" @click="openDialog()" class="h-10 px-5 rounded-xl font-medium">
          <el-icon class="mr-1"><Plus /></el-icon>新增工单
        </el-button>
      </div>
    </div>

    <div class="p-6">
      <el-table :data="list" stripe v-loading="loading" class="page-table">
        <el-table-column prop="orderNo" label="工单号" width="180">
          <template #default="{ row }"><span class="font-medium text-sky-600">{{ row.orderNo }}</span></template>
        </el-table-column>
        <el-table-column prop="productName" label="产品名称" min-width="160" />
        <el-table-column prop="quantity" label="计划数量" width="110" align="right" />
        <el-table-column prop="finishedQty" label="完成数" width="100" align="right">
          <template #default="{ row }">
            <span class="font-medium text-emerald-600">{{ row.finishedQty || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="完成进度" min-width="150">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-progress :percentage="Math.round((row.finishedQty || 0) / row.quantity * 100)" :stroke-width="8" />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="info" effect="light" round size="small">待生产</el-tag>
            <el-tag v-else-if="row.status === 2" type="warning" effect="light" round size="small">生产中</el-tag>
            <el-tag v-else-if="row.status === 3" type="success" effect="light" round size="small">已完成</el-tag>
            <el-tag v-else type="primary" effect="light" round size="small">已入库</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-2">
              <button class="action-link primary" @click="openDialog(row)">详情</button>
              <button v-if="row.status === 1" class="action-link warning" @click="action(row.id, 'start')">开工</button>
              <button v-if="row.status === 2" class="action-link success" @click="action(row.id, 'complete')">完工</button>
              <button v-if="row.status === 3" class="action-link primary" @click="action(row.id, 'stock-in')">入库</button>
              <button class="action-link danger" @click="del(row.id)">删除</button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="mt-5 flex justify-end">
        <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize" layout="prev, pager, next, total" background @current-change="fetchData" />
      </div>
    </div>

    <el-dialog v-model="visible" :title="editing.id ? '工单详情' : '新增工单'" width="750px" class="custom-dialog">
      <el-form :model="form" label-width="90px" label-position="right">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品">
              <el-select v-model="form.productId" filterable class="w-full" placeholder="请选择产品">
                <el-option v-for="p in products" :key="p.id" :value="p.id" :label="p.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划数量">
              <el-input-number v-model="form.quantity" :min="0" :controls-position="'right'" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划开始">
              <el-date-picker v-model="form.planStart" type="date" class="w-full" placeholder="选择开始日期" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划结束">
              <el-date-picker v-model="form.planEnd" type="date" class="w-full" placeholder="选择结束日期" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>

      <el-divider content-position="left">
        <span class="font-semibold">工序列表</span>
      </el-divider>

      <el-table :data="processes" class="page-table">
        <el-table-column label="工序" min-width="200">
          <template #default="{ row }">
            <el-select v-model="row.processId" filterable class="w-full" placeholder="选择工序">
              <el-option v-for="p in allProcesses" :key="p.id" :value="p.id" :label="p.name" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="顺序" width="80" align="center">
          <template #default="{ $index }"><span class="text-slate-500 dark:text-slate-400">{{ $index + 1 }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ $index }">
            <button class="action-link danger" @click="processes.splice($index, 1)">删除</button>
          </template>
        </el-table-column>
      </el-table>
      <el-button class="mt-4" @click="processes.push({ processId: null })" :text="true">
        <el-icon class="mr-1"><Plus /></el-icon>添加工序
      </el-button>

      <template #footer>
        <el-button @click="visible = false" class="rounded-xl px-5" :text="true">关闭</el-button>
        <el-button v-if="!editing.id" type="primary" @click="create" class="rounded-xl px-5">创建工单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const list = ref([]), loading = ref(false), page = ref(1), total = ref(0), pageSize = ref(10), filterStatus = ref()
const visible = ref(false), editing = ref({}), form = reactive({}), processes = ref([])
const products = ref([]), allProcesses = ref([])

async function fetchData() {
  loading.value = true
  try { const r = await api.get('/production/work-order', { params: { page: page.value, pageSize: pageSize.value, status: filterStatus.value } }); if (r.code === 200) { list.value = r.data.list; total.value = r.data.total } } finally { loading.value = false }
}

async function openDialog(row) {
  products.value = (await api.get('/base/product', { params: { pageSize: 999 } })).data?.list || []
  allProcesses.value = (await api.get('/base/process')).data || []
  if (row?.id) {
    editing.value = row; const r = await api.get(`/production/work-order/${row.id}`); const o = r.data
    Object.assign(form, { productId: o.productId, quantity: o.quantity, planStart: o.planStart, planEnd: o.planEnd, remark: o.remark })
    processes.value = o.processes || []
  } else { editing.value = {}; Object.assign(form, { productId: null, quantity: 0, planStart: '', planEnd: '', remark: '' }); processes.value = [] }
  visible.value = true
}

async function create() { await api.post('/production/work-order', { ...form, processes: processes.value }); ElMessage.success('工单创建成功'); visible.value = false; fetchData() }
async function action(id, type) {
  await ElMessageBox.confirm('确定执行此操作吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.put(`/production/work-order/${id}/${type}`); ElMessage.success('操作成功'); fetchData()
}
async function del(id) {
  await ElMessageBox.confirm('确定要删除该工单吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.delete(`/production/work-order/${id}`); ElMessage.success('已删除'); fetchData()
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
.action-link.warning { color: #f59e0b; }
.action-link.warning:hover { color: #b45309; }
.action-link.success { color: #10b981; }
.action-link.success:hover { color: #047857; }
.action-link.danger { color: #f43f5e; }
.action-link.danger:hover { color: #be123c; }

:deep(.page-table th.el-table__cell) {
  background-color: #f8fafc !important;
  color: #475569 !important;
  font-weight: 600 !important;
  font-size: 13px !important;
}
html.dark :deep(.page-table th.el-table__cell) {
  background-color: #1e293b !important;
  color: #94a3b8 !important;
}

:deep(.custom-dialog .el-dialog) { border-radius: 16px !important; }
:deep(.custom-dialog .el-dialog__header) {
  padding: 20px 24px 16px !important;
  margin-right: 0 !important;
  border-bottom: 1px solid #f1f5f9;
}
html.dark :deep(.custom-dialog .el-dialog__header) {
  border-bottom-color: #334155 !important;
}
:deep(.custom-dialog .el-dialog__body) { padding: 24px !important; }
:deep(.custom-dialog .el-dialog__footer) {
  padding: 16px 24px 20px !important;
  border-top: 1px solid #f1f5f9;
}
html.dark :deep(.custom-dialog .el-dialog__footer) {
  border-top-color: #334155 !important;
}

:deep(.el-input-number) {
  width: 100% !important;
}
:deep(.el-input-number .el-input-number__decrease) {
  background: #f8fafc !important;
  border: 1px solid #e2e8f0 !important;
  color: #64748b !important;
  box-shadow: none !important;
  outline: none !important;
}
:deep(.el-input-number .el-input-number__increase) {
  background: #f8fafc !important;
  border: 1px solid #e2e8f0 !important;
  color: #64748b !important;
  box-shadow: none !important;
  outline: none !important;
}
:deep(.el-input-number .el-input-number__decrease:hover) {
  background: #e2e8f0 !important;
  color: #334155 !important;
  border-color: #cbd5e1 !important;
}
:deep(.el-input-number .el-input-number__increase:hover) {
  background: #e2e8f0 !important;
  color: #334155 !important;
  border-color: #cbd5e1 !important;
}
html.dark :deep(.el-input-number .el-input-number__decrease) {
  background: #334155 !important;
  border-color: #475569 !important;
  color: #94a3b8 !important;
}
html.dark :deep(.el-input-number .el-input-number__increase) {
  background: #334155 !important;
  border-color: #475569 !important;
  color: #94a3b8 !important;
}
html.dark :deep(.el-input-number .el-input-number__decrease:hover) {
  background: #475569 !important;
  color: #cbd5e1 !important;
  border-color: #64748b !important;
}
html.dark :deep(.el-input-number .el-input-number__increase:hover) {
  background: #475569 !important;
  color: #cbd5e1 !important;
  border-color: #64748b !important;
}
:deep(.el-input-number .el-input-number__decrease:focus-visible) {
  outline: none !important;
  box-shadow: none !important;
}
:deep(.el-input-number .el-input-number__increase:focus-visible) {
  outline: none !important;
  box-shadow: none !important;
}
:deep(.el-input-number .el-input__wrapper) {
  box-shadow: none !important;
}
</style>
