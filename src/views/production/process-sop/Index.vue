<template>
  <div class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-lg font-semibold text-slate-800">工序SOP</h2>
        <p class="text-xs text-slate-400 mt-1">管理工序与知识库文档的标准作业关联</p>
      </div>
      <el-button type="primary" @click="openDialog()" class="h-10 px-5 rounded-xl font-medium">
        <el-icon class="mr-1"><Plus /></el-icon>新增关联
      </el-button>
    </div>

    <div class="p-6">
      <el-table :data="list" border stripe v-loading="loading" class="page-table">
        <el-table-column prop="processName" label="关联工序" width="160">
          <template #default="{ row }">
            <span class="font-medium text-slate-700">{{ row.processName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="docTitle" label="关联文档" min-width="200">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.docTitle || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="SOP类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.sopType === 'standard'" type="primary" effect="light" size="small">标准作业</el-tag>
            <el-tag v-else-if="row.sopType === 'safety'" type="warning" effect="light" size="small">安全规范</el-tag>
            <el-tag v-else-if="row.sopType === 'quality'" type="success" effect="light" size="small">质量要求</el-tag>
            <el-tag v-else type="info" effect="light" size="small">{{ row.sopType || '其他' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
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

    <el-dialog v-model="visible" :title="editing.id ? '编辑工序SOP' : '新增工序SOP'" width="500px" class="custom-dialog">
      <el-form :model="form" label-width="100px" label-position="right">
        <el-form-item label="关联工序">
          <el-input v-model="form.processId" placeholder="请输入工序ID" />
        </el-form-item>
        <el-form-item label="关联文档">
          <el-input v-model="form.kbDocumentId" placeholder="请输入知识库文档ID" />
        </el-form-item>
        <el-form-item label="SOP类型">
          <el-select v-model="form.sopType" class="w-full" placeholder="请选择SOP类型">
            <el-option label="标准作业" value="standard" />
            <el-option label="安全规范" value="safety" />
            <el-option label="质量要求" value="quality" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sortOrder" :min="0" :controls-position="'right'" class="w-full" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const list = ref([])
const loading = ref(false)
const visible = ref(false)
const editing = ref({})
const form = reactive({
  processId: null,
  kbDocumentId: null,
  sopType: 'standard',
  sortOrder: 0
})

async function fetchData() {
  loading.value = true
  try {
    const r = await api.get('/production/process-sop')
    if (r.code === 200) list.value = r.data
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  editing.value = row || {}
  if (row) {
    Object.assign(form, {
      processId: row.processId,
      kbDocumentId: row.kbDocumentId,
      sopType: row.sopType || 'standard',
      sortOrder: row.sortOrder ?? 0
    })
  } else {
    Object.assign(form, {
      processId: null, kbDocumentId: null, sopType: 'standard', sortOrder: 0
    })
  }
  visible.value = true
}

async function save() {
  const d = { ...form }
  if (editing.value.id) {
    d.id = editing.value.id
    await api.put('/production/process-sop', d)
  } else {
    await api.post('/production/process-sop', d)
  }
  ElMessage.success('保存成功')
  visible.value = false
  fetchData()
}

async function del(id) {
  await ElMessageBox.confirm('确定要删除该工序SOP关联吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.delete(`/production/process-sop/${id}`)
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
