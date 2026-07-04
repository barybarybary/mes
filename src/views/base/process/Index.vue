<template>
  <div class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-lg font-semibold text-slate-800">工序管理</h2>
        <p class="text-xs text-slate-400 mt-1">管理生产工序和工价配置</p>
      </div>
      <el-button type="primary" @click="openDialog()" class="h-10 px-5 rounded-xl font-medium">
        <el-icon class="mr-1"><Plus /></el-icon>新增工序
      </el-button>
    </div>

    <div class="p-6">
      <el-table :data="list" border stripe class="page-table">
        <el-table-column prop="code" label="工序编码" width="130">
          <template #default="{ row }"><el-tag type="info" effect="light" size="small">{{ row.code }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="name" label="工序名称" min-width="180">
          <template #default="{ row }"><span class="font-medium text-slate-700">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="standardHours" label="标准工时" width="140" align="center">
          <template #default="{ row }"><span>{{ row.standardHours }} 分钟</span></template>
        </el-table-column>
        <el-table-column prop="price" label="工价" width="120" align="right">
          <template #default="{ row }"><span class="font-semibold text-emerald-600">¥{{ row.price?.toFixed(2) }}</span></template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-3">
              <button class="action-link primary" @click="openDialog(row)">编辑</button>
              <button class="action-link danger" @click="del(row.id)">删除</button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="editing.id ? '编辑工序' : '新增工序'" width="500px" class="custom-dialog">
      <el-form :model="form" label-width="100px" label-position="right">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="工序编码"><el-input v-model="form.code" placeholder="请输入工序编码" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="工序名称"><el-input v-model="form.name" placeholder="请输入工序名称" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="标准工时"><el-input-number v-model="form.standardHours" :min="0" :precision="1" class="w-full" /><span class="text-xs text-slate-400 ml-2">分钟</span></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="工价"><el-input-number v-model="form.price" :min="0" :precision="2" class="w-full" /><span class="text-xs text-slate-400 ml-2">元/件</span></el-form-item></el-col>
        </el-row>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
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

const list = ref([]), visible = ref(false), editing = ref({}), form = reactive({})

async function fetch() {
  try {
    const r = await api.get('/base/process')
    if (r.code === 200) {
      const data = r.data
      if (Array.isArray(data)) list.value = data
      else list.value = data?.list || data?.records || []
      console.log('工序数据加载成功:', list.value.length, '条')
    } else {
      console.warn('工序API返回非200:', r)
    }
  } catch (e) {
    console.error('加载工序失败:', e)
  }
}

function openDialog(row) {
  editing.value = row || {}
  Object.assign(form, { code: row?.code || '', name: row?.name || '', standardHours: row?.standardHours || 0, price: row?.price || 0, sort: row?.sort || 0 })
  visible.value = true
}

async function save() {
  const d = { ...form }
  if (editing.value.id) { d.id = editing.value.id; await api.put('/base/process', d) }
  else await api.post('/base/process', d)
  ElMessage.success('保存成功'); visible.value = false; fetch()
}

async function del(id) {
  await ElMessageBox.confirm('确定要删除该工序吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.delete(`/base/process/${id}`); ElMessage.success('删除成功'); fetch()
}

onMounted(fetch)
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
.action-link.danger { color: #f43f5e; }
.action-link.danger:hover { color: #be123c; }

:deep(.page-table th.el-table__cell) { background-color: #f8fafc !important; color: #475569 !important; font-weight: 600 !important; font-size: 13px !important; }
:deep(.custom-dialog .el-dialog) { border-radius: 16px !important; }
:deep(.custom-dialog .el-dialog__header) { padding: 20px 24px 16px !important; margin-right: 0 !important; border-bottom: 1px solid #f1f5f9; }
:deep(.custom-dialog .el-dialog__body) { padding: 24px !important; }
:deep(.custom-dialog .el-dialog__footer) { padding: 16px 24px 20px !important; border-top: 1px solid #f1f5f9; }
</style>
