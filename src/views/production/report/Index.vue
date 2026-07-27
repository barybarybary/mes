<template>
  <div class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-lg font-semibold text-slate-800">报工管理</h2>
        <p class="text-xs text-slate-400 mt-1">管理工序报工和生产数据统计</p>
      </div>
      <el-button type="primary" @click="openDialog()" class="h-10 px-5 rounded-xl font-medium">
        <el-icon class="mr-1"><Plus /></el-icon>新增报工
      </el-button>
    </div>

    <div class="p-6">
      <el-table :data="list" border stripe v-loading="loading" class="page-table">
        <el-table-column prop="worker" label="报工人" width="120" />
        <el-table-column prop="workOrderNo" label="工单号" width="160">
          <template #default="{ row }"><span class="text-sky-600 font-medium">{{ row.workOrderNo || '-' }}</span></template>
        </el-table-column>
        <el-table-column prop="processName" label="工序" width="140" />
        <el-table-column prop="quantity" label="报工数量" width="120" align="right">
          <template #default="{ row }"><span class="font-medium">{{ row.quantity }}</span></template>
        </el-table-column>
        <el-table-column prop="qualifiedQty" label="合格数" width="100" align="right">
          <template #default="{ row }"><span class="text-emerald-600 font-medium">{{ row.qualifiedQty || 0 }}</span></template>
        </el-table-column>
        <el-table-column prop="scrapQty" label="报废数" width="100" align="right">
          <template #default="{ row }"><span class="text-red-500 font-medium">{{ row.scrapQty || 0 }}</span></template>
        </el-table-column>
        <el-table-column prop="reportDate" label="报工日期" width="120" />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      </el-table>
      <div class="mt-5 flex justify-end">
        <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="prev, pager, next, sizes, total" background @current-change="fetchData" @size-change="fetchData" />
      </div>
    </div>

    <el-dialog v-model="visible" title="工序报工" width="640px" class="custom-dialog">
      <el-form :model="form" label-width="90px" label-position="right">
        <el-form-item label="工单">
          <el-select v-model="form.workOrderId" filterable class="w-full" placeholder="请选择工单" @change="loadProcesses">
            <el-option v-for="w in workOrders" :key="w.id" :value="w.id" :label="w.orderNo + ' - ' + w.productName" />
          </el-select>
        </el-form-item>
        <el-form-item label="工序">
          <el-select v-model="form.workOrderProcessId" class="w-full" placeholder="请选择工序">
            <el-option v-for="p in woProcesses" :key="p.id" :value="p.id" :label="p.processName || ('工序' + p.sort)" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="报工人">
              <el-input v-model="form.worker" placeholder="请输入报工人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报工日期">
              <el-date-picker v-model="form.reportDate" type="date" class="w-full" placeholder="选择日期" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item label="报工数量">
              <el-input-number v-model="form.quantity" :min="0" :controls-position="'right'" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="合格数">
              <el-input-number v-model="form.qualifiedQty" :min="0" :controls-position="'right'" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="报废数">
              <el-input-number v-model="form.scrapQty" :min="0" :controls-position="'right'" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="使用设备">
          <el-select v-model="form.equipmentId" class="w-full" placeholder="选择设备（可选）" clearable>
            <el-option v-for="e in equipmentList" :key="e.id" :value="e.id" :label="e.name + ' (' + e.code + ')'" :disabled="e.status === 'SCRAPPED'" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false" class="rounded-xl px-5" :text="true">取消</el-button>
        <el-button type="primary" @click="save" class="rounded-xl px-5">提交报工</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const list = ref([]), loading = ref(false), page = ref(1), total = ref(0), pageSize = ref(10)
const visible = ref(false), form = reactive({ workOrderId: null, workOrderProcessId: null, worker: '', quantity: 0, qualifiedQty: 0, scrapQty: 0, reportDate: '', remark: '', equipmentId: null })
const workOrders = ref([]), woProcesses = ref([]), equipmentList = ref([])

async function fetchData() {
  loading.value = true
  try {
    const r = await api.get('/production/report', { params: { page: page.value, pageSize: pageSize.value } })
    if (r.code === 200) {
      list.value = r.data.list || []
      total.value = r.data.total || 0
    }
  } finally { loading.value = false }
}

async function openDialog() {
  workOrders.value = (await api.get('/production/work-order', { params: { pageSize: 999 } })).data?.list || []
  equipmentList.value = (await api.get('/base/equipment', { params: { pageSize: 999 } })).data?.list || []
  visible.value = true
}

async function loadProcesses(woId) {
  const r = await api.get(`/production/work-order/${woId}`)
  woProcesses.value = r.data?.processes || []
}

async function save() {
  await api.post('/production/report', { ...form, productId: null, processId: null })
  ElMessage.success('报工成功')
  visible.value = false
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
:deep(.page-table th.el-table__cell) { background-color: #f8fafc !important; color: #475569 !important; font-weight: 600 !important; font-size: 13px !important; }
:deep(.custom-dialog .el-dialog) { border-radius: 16px !important; }
:deep(.custom-dialog .el-dialog__header) { padding: 20px 24px 16px !important; margin-right: 0 !important; border-bottom: 1px solid #f1f5f9; }
:deep(.custom-dialog .el-dialog__body) { padding: 24px !important; }
:deep(.custom-dialog .el-dialog__footer) { padding: 16px 24px 20px !important; border-top: 1px solid #f1f5f9; }

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
:deep(.el-input-number .el-input-number__decrease:focus-visible),
:deep(.el-input-number .el-input-number__increase:focus-visible) {
  outline: none !important;
  box-shadow: none !important;
}
:deep(.el-input-number .el-input__wrapper) {
  box-shadow: none !important;
}
</style>
