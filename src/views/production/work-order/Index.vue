<template>
  <div class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-lg font-semibold text-slate-800">生产工单</h2>
        <p class="text-xs text-slate-400 mt-1">管理生产工单和工序安排</p>
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
              <el-button type="primary" @click="openDetail(row)">详情</el-button>
              <el-button v-if="row.status === 1" type="warning" @click="action(row.id, 'start')">开工</el-button>
              <el-button v-if="row.status === 2" type="success" @click="action(row.id, 'complete')">完工</el-button>
              <el-button v-if="row.status === 3" type="primary" @click="action(row.id, 'stock-in')">入库</el-button>
              <el-button type="danger" @click="del(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="mt-5 flex justify-end">
        <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize" layout="prev, pager, next, total" background @current-change="fetchData" />
      </div>
    </div>

    <!-- ========== 新增工单弹窗 ========== -->
    <el-dialog v-model="visible" :title="editing.id ? '编辑工单' : '新增工单'" width="750px" class="custom-dialog">
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
          <template #default="{ $index }"><span class="text-slate-500">{{ $index + 1 }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ $index }">
            <el-button type="danger" @click="processes.splice($index, 1)">删除</el-button>
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

    <!-- ========== 工单详情抽屉 ========== -->
    <el-drawer v-model="drawerVisible" title="工单详情" direction="rtl" size="700px" class="work-order-drawer">
      <template v-if="detail">
        <!-- 1. 基本信息卡片 -->
        <div class="bg-slate-50 dark:bg-slate-800 rounded-xl p-5 mb-5">
          <h4 class="text-sm font-semibold text-slate-800 dark:text-slate-200 mb-4">📋 基本信息</h4>
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span class="text-slate-400 text-xs">产品名称</span>
              <p class="font-medium text-slate-800 dark:text-slate-200 mt-0.5">{{ detail.productName || '-' }}</p>
            </div>
            <div>
              <span class="text-slate-400 text-xs">计划数量</span>
              <p class="font-medium text-slate-800 dark:text-slate-200 mt-0.5">{{ detail.quantity || 0 }}</p>
            </div>
            <div>
              <span class="text-slate-400 text-xs">交期</span>
              <p class="font-medium text-slate-800 dark:text-slate-200 mt-0.5">{{ detail.planEnd || '-' }}</p>
            </div>
            <div>
              <span class="text-slate-400 text-xs">不良品数</span>
              <p class="font-medium text-red-500 mt-0.5">{{ detail.defectQty || 0 }}</p>
            </div>
            <div class="col-span-2">
              <span class="text-slate-400 text-xs">完成进度</span>
              <div class="flex items-center gap-3 mt-1">
                <el-progress :percentage="Math.round((detail.finishedQty || 0) / (detail.quantity || 1) * 100)" :stroke-width="10" class="flex-1" />
                <span class="text-sm font-semibold text-slate-700 dark:text-slate-300 whitespace-nowrap">{{ detail.finishedQty || 0 }} / {{ detail.quantity || 0 }}</span>
              </div>
            </div>
            <div class="col-span-2 flex items-center gap-2">
              <span class="text-slate-400 text-xs">状态</span>
              <el-tag v-if="detail.status === 1" type="info" effect="light" round size="small">待生产</el-tag>
              <el-tag v-else-if="detail.status === 2" type="warning" effect="light" round size="small">生产中</el-tag>
              <el-tag v-else-if="detail.status === 3" type="success" effect="light" round size="small">已完成</el-tag>
              <el-tag v-else type="primary" effect="light" round size="small">已入库</el-tag>
            </div>
          </div>
          <!-- 操作按钮 -->
          <div class="mt-4 pt-4 border-t border-slate-200 dark:border-slate-700 flex gap-3">
            <el-button v-if="detail.status === 1" type="primary" @click="actionFromDrawer('start')" class="rounded-lg">
              开工
            </el-button>
            <el-button v-if="detail.status === 2" type="success" @click="actionFromDrawer('complete')" class="rounded-lg">
              完工
            </el-button>
            <el-button v-if="detail.status === 3" type="primary" @click="actionFromDrawer('stock-in')" class="rounded-lg">
              入库
            </el-button>
          </div>
        </div>

        <!-- 2. 工序列表 -->
        <div class="mb-5">
          <h4 class="text-sm font-semibold text-slate-800 dark:text-slate-200 mb-3">🔧 工序流转</h4>
          <div v-if="processList.length === 0" class="text-center py-8 text-slate-400 text-sm">
            暂无工序数据
          </div>
          <div v-for="(proc, idx) in processList" :key="proc.id || idx" class="bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700 rounded-xl p-4 mb-3">
            <div class="flex items-start gap-4">
              <!-- 序号圈 -->
              <div
                class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold shrink-0"
                :class="procStatusClass(proc)"
              >
                {{ idx + 1 }}
              </div>
              <!-- 工序信息 -->
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between mb-1">
                  <span class="font-semibold text-slate-800 dark:text-slate-200">{{ proc.processName || '-' }}</span>
                  <el-tag :type="procTagType(proc)" effect="light" round size="small">
                    {{ procStatusLabel(proc) }}
                  </el-tag>
                </div>
                <div class="text-xs text-slate-400 space-y-0.5">
                  <span v-if="proc.worker">负责人：{{ proc.worker }} · </span>
                  <span>完成：{{ proc.completedQty || 0 }} / {{ proc.plannedQty || 0 }}</span>
                </div>
                <el-progress
                  :percentage="Math.round((proc.completedQty || 0) / (proc.plannedQty || 1) * 100)"
                  :stroke-width="6"
                  class="mt-2"
                />
                <!-- 操作按钮：进行中 + 工单未完工 -->
                <div v-if="procIsActive(proc) && canOperate" class="mt-3 pt-3 border-t border-slate-100 dark:border-slate-700 flex gap-2">
                  <el-button type="primary" @click="openReportDialog(proc)" class="rounded-lg">
                    报工
                  </el-button>
                  <el-button type="warning" @click="openQcDialog(proc)" class="rounded-lg">
                    质检
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 3. 最近报工记录 -->
        <div>
          <h4 class="text-sm font-semibold text-slate-800 dark:text-slate-200 mb-3">📝 最近报工记录</h4>
          <div v-if="recentReports.length === 0" class="text-center py-6 text-slate-400 text-sm">
            暂无报工记录
          </div>
          <el-table v-else :data="recentReports" class="page-table">
            <el-table-column label="时间" width="160">
              <template #default="{ row }">{{ row.reportDate || '-' }}</template>
            </el-table-column>
            <el-table-column prop="worker" label="工人" width="100" />
            <el-table-column prop="qualifiedQty" label="良品数" width="80" align="right">
              <template #default="{ row }">
                <span class="text-emerald-600 font-medium">{{ row.qualifiedQty || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="scrapQty" label="不良数" width="80" align="right">
              <template #default="{ row }">
                <span class="text-red-500 font-medium">{{ row.scrapQty || 0 }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </el-drawer>

    <!-- ========== 报工弹窗 ========== -->
    <el-dialog v-model="reportVisible" title="工序报工" width="450px" class="custom-dialog">
      <el-form :model="reportForm" label-width="90px" label-position="right">
        <el-form-item label="工序">
          <el-input :model-value="currentProcess?.processName || ''" disabled />
        </el-form-item>
        <el-form-item label="良品数量">
          <el-input-number v-model="reportForm.qualifiedQty" :min="0" :controls-position="'right'" class="w-full" />
        </el-form-item>
        <el-form-item label="不良品数量">
          <el-input-number v-model="reportForm.scrapQty" :min="0" :controls-position="'right'" class="w-full" />
        </el-form-item>
        <el-form-item label="不良原因">
          <el-select v-model="reportForm.defectReason" class="w-full" placeholder="请选择不良原因">
            <el-option label="缺胶" value="缺胶" />
            <el-option label="尺寸超差" value="尺寸超差" />
            <el-option label="外观不良" value="外观不良" />
            <el-option label="设备故障" value="设备故障" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="报工人">
          <el-input v-model="reportForm.worker" placeholder="请输入报工人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportVisible = false" class="rounded-xl px-5" :text="true">取消</el-button>
        <el-button type="primary" @click="submitReport" class="rounded-xl px-5">提交报工</el-button>
      </template>
    </el-dialog>

    <!-- ========== 质检弹窗 ========== -->
    <el-dialog v-model="qcVisible" title="工序质检" width="500px" class="custom-dialog">
      <el-form :model="qcForm" label-width="90px" label-position="right">
        <el-form-item label="工序">
          <el-input :model-value="currentProcess?.processName || ''" disabled />
        </el-form-item>
        <el-form-item label="检验类型">
          <el-select v-model="qcForm.type" class="w-full" placeholder="请选择检验类型">
            <el-option label="工序检" value="in_process" />
            <el-option label="终检" value="final" />
          </el-select>
        </el-form-item>
        <el-form-item label="抽检数量">
          <el-input-number v-model="qcForm.checkQty" :min="0" :controls-position="'right'" class="w-full" @change="onCheckQtyChange" />
        </el-form-item>
        <el-form-item label="合格数量">
          <el-input-number v-model="qcForm.okQty" :min="0" :max="qcForm.checkQty" :controls-position="'right'" class="w-full" @change="onCheckQtyChange" />
        </el-form-item>
        <el-form-item label="不良数量">
          <el-input-number :model-value="qcForm.ngQty" :min="0" :controls-position="'right'" class="w-full" disabled />
        </el-form-item>
        <el-form-item label="检验结论">
          <el-select v-model="qcForm.result" class="w-full" placeholder="请选择检验结论">
            <el-option label="合格" :value="1" />
            <el-option label="让步接收" :value="3" />
            <el-option label="不合格" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="qcForm.result === 2" label="不良描述">
          <el-input v-model="qcForm.ngDescription" type="textarea" :rows="3" placeholder="请描述不良情况" />
        </el-form-item>
        <el-form-item label="检验人">
          <el-input v-model="qcForm.inspector" placeholder="请输入检验人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="qcVisible = false" class="rounded-xl px-5" :text="true">取消</el-button>
        <el-button type="primary" @click="submitQc" class="rounded-xl px-5">提交质检</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const list = ref([]), loading = ref(false), page = ref(1), total = ref(0), pageSize = ref(10), filterStatus = ref()
const visible = ref(false), editing = ref({}), form = reactive({}), processes = ref([])
const products = ref([]), allProcesses = ref([])

// ===== 详情抽屉 =====
const drawerVisible = ref(false)
const detail = ref(null)
const processList = ref([])
const recentReports = ref([])

const canOperate = computed(() => {
  if (!detail.value) return false
  // 工单未完工（待生产或生产中）时允许工序操作
  return detail.value.status === 1 || detail.value.status === 2
})

// ===== 报工弹窗 =====
const reportVisible = ref(false)
const currentProcess = ref(null)
const reportForm = reactive({
  workOrderId: null, workOrderProcessId: null, productId: null, processId: null,
  worker: '', qualifiedQty: 0, scrapQty: 0, defectReason: '',
  reportType: 'NORMAL', reportDate: ''
})

// ===== 质检弹窗 =====
const qcVisible = ref(false)
const qcForm = reactive({
  workOrderId: null, workOrderProcessId: null, productId: null,
  type: 'in_process', checkQty: 0, okQty: 0, ngQty: 0,
  result: 1, ngDescription: '', inspector: '',
  checkDate: ''
})

function todayStr() {
  return new Date().toISOString().split('T')[0]
}

// ===== 列表 =====
async function fetchData() {
  loading.value = true
  try { const r = await api.get('/production/work-order', { params: { page: page.value, pageSize: pageSize.value, status: filterStatus.value } }); if (r.code === 200) { list.value = r.data.list; total.value = r.data.total } } finally { loading.value = false }
}

// ===== 新增弹窗 =====
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

// ===== 列表操作按钮 =====
async function action(id, type) {
  await ElMessageBox.confirm('确定执行此操作吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.put(`/production/work-order/${id}/${type}`); ElMessage.success('操作成功'); fetchData()
}
async function del(id) {
  await ElMessageBox.confirm('确定要删除该工单吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.delete(`/production/work-order/${id}`); ElMessage.success('已删除'); fetchData()
}

// ===== 详情抽屉 =====
async function openDetail(row) {
  try {
    const r = await api.get(`/production/work-order/${row.id}`)
    if (r.code === 200) {
      detail.value = r.data
      processList.value = r.data.processes || []
    }
    // 加载最近报工记录
    const rr = await api.get('/production/report', { params: { workOrderId: row.id, pageSize: 10 } })
    if (rr.code === 200) {
      recentReports.value = rr.data?.list || rr.data || []
    }
    drawerVisible.value = true
  } catch (e) {
    ElMessage.error('加载工单详情失败')
  }
}

// ===== 抽屉内操作按钮 =====
async function actionFromDrawer(type) {
  if (!detail.value) return
  await ElMessageBox.confirm('确定执行此操作吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.put(`/production/work-order/${detail.value.id}/${type}`)
  ElMessage.success('操作成功')
  // 刷新详情
  const r = await api.get(`/production/work-order/${detail.value.id}`)
  if (r.code === 200) {
    detail.value = r.data
    processList.value = r.data.processes || []
  }
  fetchData()
}

// ===== 工序状态辅助 =====
function procStatus(proc) {
  // status: 0=待开始, 1=进行中, 2=已完成
  const s = proc.status
  if (s === 0 || s === 'pending') return 'pending'
  if (s === 1 || s === 'in_progress') return 'active'
  return 'done'
}

function procStatusClass(proc) {
  const s = procStatus(proc)
  if (s === 'done') return 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900 dark:text-emerald-300'
  if (s === 'active') return 'bg-blue-100 text-blue-600 dark:bg-blue-900 dark:text-blue-300'
  return 'bg-slate-100 text-slate-400 dark:bg-slate-700 dark:text-slate-500'
}

function procStatusLabel(proc) {
  const s = procStatus(proc)
  if (s === 'done') return '已完成'
  if (s === 'active') return '进行中'
  return '待开始'
}

function procTagType(proc) {
  const s = procStatus(proc)
  if (s === 'done') return 'success'
  if (s === 'active') return ''
  return 'info'
}

function procIsActive(proc) {
  return procStatus(proc) === 'active'
}

// ===== 报工弹窗 =====
function openReportDialog(proc) {
  currentProcess.value = proc
  reportForm.workOrderId = detail.value?.id
  reportForm.workOrderProcessId = proc.id
  reportForm.productId = detail.value?.productId
  reportForm.processId = proc.processId
  reportForm.worker = proc.worker || ''
  reportForm.qualifiedQty = 0
  reportForm.scrapQty = 0
  reportForm.defectReason = ''
  reportForm.reportType = 'NORMAL'
  reportForm.reportDate = todayStr()
  reportVisible.value = true
}

async function submitReport() {
  try {
    await api.post('/production/report', { ...reportForm })
    ElMessage.success('报工成功')
    reportVisible.value = false
    // 刷新详情
    openDetail({ id: detail.value.id })
  } catch (e) {
    // 错误已在拦截器处理
  }
}

// ===== 质检弹窗 =====
function openQcDialog(proc) {
  currentProcess.value = proc
  qcForm.workOrderId = detail.value?.id
  qcForm.workOrderProcessId = proc.id
  qcForm.productId = detail.value?.productId
  qcForm.type = 'in_process'
  qcForm.checkQty = 0
  qcForm.okQty = 0
  qcForm.ngQty = 0
  qcForm.result = 1
  qcForm.ngDescription = ''
  qcForm.inspector = proc.worker || ''
  qcForm.checkDate = todayStr()
  qcVisible.value = true
}

function onCheckQtyChange() {
  qcForm.ngQty = Math.max(0, (qcForm.checkQty || 0) - (qcForm.okQty || 0))
}

async function submitQc() {
  try {
    await api.post('/production/qc', { ...qcForm })
    ElMessage.success('质检提交成功')
    qcVisible.value = false
    // 刷新详情
    openDetail({ id: detail.value.id })
  } catch (e) {
    // 错误已在拦截器处理
  }
}

onMounted(fetchData)
</script>

<style scoped>
:deep(.page-table th.el-table__cell) { background-color: #f8fafc !important; color: #475569 !important; font-weight: 600 !important; font-size: 13px !important; }
:deep(.custom-dialog .el-dialog) { border-radius: 16px !important; }
:deep(.custom-dialog .el-dialog__header) { padding: 20px 24px 16px !important; margin-right: 0 !important; border-bottom: 1px solid #f1f5f9; }
:deep(.custom-dialog .el-dialog__body) { padding: 24px !important; }
:deep(.custom-dialog .el-dialog__footer) { padding: 16px 24px 20px !important; border-top: 1px solid #f1f5f9; }

:deep(.work-order-drawer .el-drawer__header) { padding: 20px 24px 16px !important; margin-bottom: 0 !important; border-bottom: 1px solid #f1f5f9; }
:deep(.work-order-drawer .el-drawer__body) { padding: 24px !important; }

:deep(.el-input-number) { width: 100% !important; }
:deep(.el-input-number .el-input-number__decrease) { background: #f8fafc !important; border: 1px solid #e2e8f0 !important; color: #64748b !important; box-shadow: none !important; outline: none !important; }
:deep(.el-input-number .el-input-number__increase) { background: #f8fafc !important; border: 1px solid #e2e8f0 !important; color: #64748b !important; box-shadow: none !important; outline: none !important; }
:deep(.el-input-number .el-input-number__decrease:hover) { background: #e2e8f0 !important; color: #334155 !important; border-color: #cbd5e1 !important; }
:deep(.el-input-number .el-input-number__increase:hover) { background: #e2e8f0 !important; color: #334155 !important; border-color: #cbd5e1 !important; }
:deep(.el-input-number .el-input-number__decrease:focus-visible) { outline: none !important; box-shadow: none !important; }
:deep(.el-input-number .el-input-number__increase:focus-visible) { outline: none !important; box-shadow: none !important; }
:deep(.el-input-number .el-input__wrapper) { box-shadow: none !important; }
</style>
