<template>
  <div class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-lg font-semibold text-slate-800">设备台账</h2>
        <p class="text-xs text-slate-400 mt-1">管理生产设备基础信息</p>
      </div>
      <div class="flex items-center gap-3">
        <el-input v-model="keyword" placeholder="搜索设备名称或编号" clearable class="w-56" @change="fetchData">
          <template #prefix><el-icon class="text-slate-400"><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="openDialog()" class="h-10 px-5 rounded-xl font-medium">
          <el-icon class="mr-1"><Plus /></el-icon>新增设备
        </el-button>
      </div>
    </div>

    <div class="p-6">
      <el-table :data="list" border stripe v-loading="loading" class="page-table">
        <el-table-column prop="code" label="设备编号" width="140">
          <template #default="{ row }"><el-tag type="info" effect="light" size="small">{{ row.code }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="name" label="设备名称" min-width="180">
          <template #default="{ row }"><span class="font-medium text-slate-700">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="type" label="设备类型" width="120" />
        <el-table-column prop="workshop" label="所属车间" width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-3">
              <el-button type="primary" @click="openDialog(row)">编辑</el-button>
              <el-button type="danger" @click="del(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="mt-5 flex justify-end">
        <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize" layout="prev, pager, next, total" background @current-change="fetchData" />
      </div>
    </div>

    <el-dialog v-model="visible" :title="editing.id ? '编辑设备' : '新增设备'" width="600px" class="custom-dialog">
      <el-form :model="form" label-width="80px" label-position="right">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="设备编号"><el-input v-model="form.code" placeholder="请输入设备编号" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备名称"><el-input v-model="form.name" placeholder="请输入设备名称" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="设备类型"><el-input v-model="form.type" placeholder="请输入设备类型" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="所属车间"><el-input v-model="form.workshop" placeholder="请输入所属车间" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="所属产线"><el-input v-model="form.productionLine" placeholder="请输入所属产线（非必填）" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="购买日期"><el-date-picker v-model="form.purchaseDate" type="date" placeholder="选择购买日期" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="制造商"><el-input v-model="form.manufacturer" placeholder="请输入制造商" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="规格型号"><el-input v-model="form.specModel" placeholder="请输入规格型号" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" placeholder="请输入备注" :rows="3" /></el-form-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const list = ref([]), loading = ref(false), page = ref(1), total = ref(0), pageSize = ref(10), keyword = ref(''), visible = ref(false), editing = ref({}), form = reactive({})

function getStatusType(s) {
  const map = { ACTIVE: 'success', IDLE: 'info', REPAIR: 'warning', SCRAPPED: 'danger' }
  return map[s] || 'info'
}
function getStatusText(s) {
  const map = { ACTIVE: '启用', IDLE: '闲置', REPAIR: '维修中', SCRAPPED: '报废' }
  return map[s] || s
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (keyword.value) params.keyword = keyword.value
    const r = await api.get('/base/equipment', { params })
    if (r.code === 200) {
      const data = r.data
      if (Array.isArray(data)) { list.value = data; total.value = data.length }
      else { list.value = data?.list || data?.records || []; total.value = data?.total || 0 }
      console.log('设备数据加载成功:', list.value.length, '条, 总计:', total.value)
    } else {
      console.warn('设备API返回非200:', r)
    }
  } catch (e) {
    console.error('加载设备失败:', e)
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  editing.value = row || {}
  Object.assign(form, {
    code: row?.code || '',
    name: row?.name || '',
    type: row?.type || '',
    workshop: row?.workshop || '',
    productionLine: row?.productionLine || '',
    purchaseDate: row?.purchaseDate || '',
    manufacturer: row?.manufacturer || '',
    specModel: row?.specModel || '',
    remark: row?.remark || ''
  })
  visible.value = true
}

async function save() {
  const d = { ...form }
  if (editing.value.id) { d.id = editing.value.id; await api.put('/base/equipment', d) }
  else await api.post('/base/equipment', d)
  ElMessage.success('保存成功'); visible.value = false; fetchData()
}

async function del(id) {
  await ElMessageBox.confirm('确定要删除该设备吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.delete(`/base/equipment/${id}`); ElMessage.success('删除成功'); fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
:deep(.page-table th.el-table__cell) { background-color: #f8fafc !important; color: #475569 !important; font-weight: 600 !important; font-size: 13px !important; }
:deep(.custom-dialog .el-dialog) { border-radius: 16px !important; }
:deep(.custom-dialog .el-dialog__header) { padding: 20px 24px 16px !important; margin-right: 0 !important; border-bottom: 1px solid #f1f5f9; }
:deep(.custom-dialog .el-dialog__body) { padding: 24px !important; }
:deep(.custom-dialog .el-dialog__footer) { padding: 16px 24px 20px !important; border-top: 1px solid #f1f5f9; }
</style>
