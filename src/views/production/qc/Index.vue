<template>
  <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100 dark:border-slate-700 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-lg font-semibold text-slate-800 dark:text-slate-200">质检管理</h2>
        <p class="text-xs text-slate-400 dark:text-slate-300 mt-1">管理质检记录和质量追溯</p>
      </div>
      <el-button type="primary" @click="openDialog()" class="h-10 px-5 rounded-xl font-medium">
        <el-icon class="mr-1"><Plus /></el-icon>新增记录
      </el-button>
    </div>

    <div class="p-6">
      <el-table :data="list" border stripe v-loading="loading" class="page-table">
        <el-table-column label="类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'incoming'" type="info" effect="light" size="small">来料检验</el-tag>
            <el-tag v-else-if="row.type === 'in_process'" type="warning" effect="light" size="small">过程检验</el-tag>
            <el-tag v-else type="success" effect="light" size="small">成品检验</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="batchNo" label="批次号" width="150">
          <template #default="{ row }">
            <el-tag type="info" effect="light" size="small">{{ row.batchNo }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkQty" label="检验数" width="90" align="right" />
        <el-table-column prop="okQty" label="合格数" width="90" align="right">
          <template #default="{ row }">
            <span class="text-emerald-600 font-medium">{{ row.okQty }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ngQty" label="不良数" width="90" align="right">
          <template #default="{ row }">
            <span class="text-red-500 font-medium">{{ row.ngQty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="合格率" width="120">
          <template #default="{ row }">
            <el-progress :percentage="Math.round(row.okQty / row.checkQty * 100)" :stroke-width="8" :status="row.okQty / row.checkQty >= 0.95 ? 'success' : row.okQty / row.checkQty >= 0.8 ? 'warning' : 'exception'" />
          </template>
        </el-table-column>
        <el-table-column label="结论" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.result === 1" type="success" effect="light" round size="small">合格</el-tag>
            <el-tag v-else-if="row.result === 2" type="danger" effect="light" round size="small">不合格</el-tag>
            <el-tag v-else type="warning" effect="light" round size="small">让步接收</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="inspector" label="检验人" width="100" />
        <el-table-column prop="ngDescription" label="不良描述" min-width="150" show-overflow-tooltip />
      </el-table>
      <div class="mt-5 flex justify-end">
        <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize" layout="prev, pager, next, total" background @current-change="fetchData" />
      </div>
    </div>

    <el-dialog v-model="visible" title="质检记录" width="640px" class="custom-dialog">
      <el-form :model="form" label-width="90px" label-position="right">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="检验类型">
              <el-select v-model="form.type" class="w-full">
                <el-option label="来料检验" value="incoming" />
                <el-option label="过程检验" value="in_process" />
                <el-option label="成品检验" value="final" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次号">
              <el-input v-model="form.batchNo" placeholder="请输入批次号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item label="检验数量">
              <el-input-number v-model="form.checkQty" :min="0" :controls-position="'right'" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="合格数">
              <el-input-number v-model="form.okQty" :min="0" :controls-position="'right'" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="不良数">
              <el-input-number v-model="form.ngQty" :min="0" :controls-position="'right'" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="检验结论">
              <el-select v-model="form.result" class="w-full">
                <el-option label="合格" :value="1" />
                <el-option label="不合格" :value="2" />
                <el-option label="让步接收" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检验人">
              <el-input v-model="form.inspector" placeholder="请输入检验人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="不良描述">
          <el-input v-model="form.ngDescription" type="textarea" :rows="2" placeholder="请输入不良描述" />
        </el-form-item>
        <el-form-item label="处理意见">
          <el-input v-model="form.disposition" placeholder="请输入处理意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false" class="rounded-xl px-5">取消</el-button>
        <el-button type="primary" @click="save" class="rounded-xl px-5">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const pageSize = ref(10)
const visible = ref(false)
const form = reactive({
  type: 'in_process',
  batchNo: '',
  checkQty: 0,
  okQty: 0,
  ngQty: 0,
  result: 1,
  inspector: '',
  ngDescription: '',
  disposition: ''
})

async function fetchData() {
  loading.value = true
  try {
    const r = await api.get('/production/qc', { params: { page: page.value, pageSize: pageSize.value } })
    if (r.code === 200) { list.value = r.data.list; total.value = r.data.total }
  } finally {
    loading.value = false
  }
}

function openDialog() {
  visible.value = true
}

async function save() {
  const d = { ...form }
  if (d.id) await api.put('/production/qc', d)
  else await api.post('/production/qc', d)
  ElMessage.success('保存成功')
  visible.value = false
  fetchData()
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
:deep(.custom-dialog .el-dialog) {
  border-radius: 16px !important;
}
:deep(.custom-dialog .el-dialog__header) {
  padding: 20px 24px 16px !important;
  margin-right: 0 !important;
  border-bottom: 1px solid #f1f5f9;
}
:deep(.custom-dialog .el-dialog__body) {
  padding: 24px !important;
}
:deep(.custom-dialog .el-dialog__footer) {
  padding: 16px 24px 20px !important;
  border-top: 1px solid #f1f5f9;
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
