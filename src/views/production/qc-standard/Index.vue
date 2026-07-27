<template>
  <div class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-lg font-semibold text-slate-800">质检标准</h2>
        <p class="text-xs text-slate-400 mt-1">管理产品质量检验标准和规格</p>
      </div>
      <el-button type="primary" @click="openDialog()" class="h-10 px-5 rounded-xl font-medium">
        <el-icon class="mr-1"><Plus /></el-icon>新增标准
      </el-button>
    </div>

    <div class="p-6">
      <el-table :data="list" border stripe v-loading="loading" class="page-table">
        <el-table-column prop="itemName" label="检验项" min-width="150">
          <template #default="{ row }">
            <span class="font-medium text-slate-700">{{ row.itemName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="关联产品" width="140">
          <template #default="{ row }">
            <span class="text-slate-500">{{ row.productName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="processName" label="关联工序" width="140">
          <template #default="{ row }">
            <span class="text-slate-500">{{ row.processName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="检验类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.itemType === 'quantitative'" type="primary" effect="light" size="small">定量</el-tag>
            <el-tag v-else-if="row.itemType === 'qualitative'" type="success" effect="light" size="small">定性</el-tag>
            <el-tag v-else type="info" effect="light" size="small">{{ row.itemType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" align="center">
          <template #default="{ row }">
            <span class="text-slate-500">{{ row.unit || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="规格下限" width="110" align="right">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.specLower ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="规格上限" width="110" align="right">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.specUpper ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="目标值" width="100" align="right">
          <template #default="{ row }">
            <span class="text-blue-600 font-medium">{{ row.specTarget ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="关键项" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isCritical === 1" type="danger" effect="light" size="small">是</el-tag>
            <span v-else class="text-slate-400">否</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-3">
              <el-button type="primary" @click="openDialog(row)">编辑</el-button>
              <el-button type="danger" @click="del(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="editing.id ? '编辑质检标准' : '新增质检标准'" width="600px" class="custom-dialog">
      <el-form :model="form" label-width="90px" label-position="right">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="检验项名称">
              <el-input v-model="form.itemName" placeholder="请输入检验项名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检验类型">
              <el-select v-model="form.itemType" class="w-full" placeholder="请选择检验类型">
                <el-option label="定量" value="quantitative" />
                <el-option label="定性" value="qualitative" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="关联产品">
              <el-input v-model="form.productId" placeholder="请输入产品ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联工序">
              <el-input v-model="form.processId" placeholder="请输入工序ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="单位">
              <el-input v-model="form.unit" placeholder="如：mm" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否关键">
              <el-select v-model="form.isCritical" class="w-full">
                <el-option label="是" :value="1" />
                <el-option label="否" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序号">
              <el-input-number v-model="form.sortOrder" :min="0" :controls-position="'right'" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="规格下限">
              <el-input v-model="form.specLower" placeholder="下限" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="规格上限">
              <el-input v-model="form.specUpper" placeholder="上限" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="目标值">
              <el-input v-model="form.specTarget" placeholder="目标" />
            </el-form-item>
          </el-col>
        </el-row>
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

const list = ref([])
const loading = ref(false)
const visible = ref(false)
const editing = ref({})
const form = reactive({
  itemName: '',
  itemType: 'quantitative',
  productId: null,
  processId: null,
  unit: '',
  specLower: null,
  specUpper: null,
  specTarget: null,
  isCritical: 0,
  sortOrder: 0
})

async function fetchData() {
  loading.value = true
  try {
    const r = await api.get('/production/qc-standard')
    if (r.code === 200) list.value = r.data
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  editing.value = row || {}
  if (row) {
    Object.assign(form, {
      itemName: row.itemName || '',
      itemType: row.itemType || 'quantitative',
      productId: row.productId,
      processId: row.processId,
      unit: row.unit || '',
      specLower: row.specLower,
      specUpper: row.specUpper,
      specTarget: row.specTarget,
      isCritical: row.isCritical ?? 0,
      sortOrder: row.sortOrder ?? 0
    })
  } else {
    Object.assign(form, {
      itemName: '', itemType: 'quantitative', productId: null, processId: null,
      unit: '', specLower: null, specUpper: null, specTarget: null,
      isCritical: 0, sortOrder: 0
    })
  }
  visible.value = true
}

async function save() {
  const d = { ...form }
  if (editing.value.id) {
    d.id = editing.value.id
    await api.put('/production/qc-standard', d)
  } else {
    await api.post('/production/qc-standard', d)
  }
  ElMessage.success('保存成功')
  visible.value = false
  fetchData()
}

async function del(id) {
  await ElMessageBox.confirm('确定要删除该质检标准吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.delete(`/production/qc-standard/${id}`)
  ElMessage.success('删除成功')
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
</style>
