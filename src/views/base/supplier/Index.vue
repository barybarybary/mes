<template>
  <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100 dark:border-slate-700 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-lg font-semibold text-slate-800 dark:text-slate-200">供应商管理</h2>
        <p class="text-xs text-slate-400 dark:text-slate-300 mt-1">管理供应商基础信息</p>
      </div>
      <div class="flex items-center gap-3">
        <el-input v-model="keyword" placeholder="搜索供应商" clearable class="w-56" @change="fetchData">
          <template #prefix><el-icon class="text-slate-400 dark:text-slate-300"><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="openDialog()" class="h-10 px-5 rounded-xl font-medium">
          <el-icon class="mr-1"><Plus /></el-icon>新增供应商
        </el-button>
      </div>
    </div>

    <div class="p-6">
      <el-table :data="list" border stripe v-loading="loading" class="page-table">
        <el-table-column prop="code" label="供应商编码" width="120">
          <template #default="{ row }"><el-tag type="info" effect="light" size="small">{{ row.code }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="name" label="供应商名称" min-width="180">
          <template #default="{ row }"><span class="font-medium text-slate-700 dark:text-slate-200">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-3">
              <button class="action-link primary" @click="openDialog(row)">编辑</button>
              <button class="action-link danger" @click="del(row.id)">删除</button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="mt-5 flex justify-end">
        <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize" layout="prev, pager, next, total" background @current-change="fetchData" />
      </div>
    </div>

    <el-dialog v-model="visible" :title="editing.id ? '编辑供应商' : '新增供应商'" width="560px" class="custom-dialog">
      <el-form :model="form" label-width="80px" label-position="right">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="供应商编码"><el-input v-model="form.code" placeholder="请输入编码" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="供应商名称"><el-input v-model="form.name" placeholder="请输入名称" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="联系人"><el-input v-model="form.contact" placeholder="请输入联系人" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="form.phone" placeholder="请输入电话" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="地址"><el-input v-model="form.address" placeholder="请输入地址" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息" /></el-form-item>
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
import { Search, Plus } from '@element-plus/icons-vue'
import api from '@/api'

const list = ref([]), loading = ref(false), page = ref(1), total = ref(0), pageSize = ref(10), keyword = ref(''), visible = ref(false), editing = ref({}), form = reactive({})

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (keyword.value) params.keyword = keyword.value
    const r = await api.get('/base/supplier', { params })
    if (r.code === 200) {
      const data = r.data
      if (Array.isArray(data)) { list.value = data; total.value = data.length }
      else { list.value = data?.list || data?.records || []; total.value = data?.total || 0 }
    }
  } catch (e) {
    console.error('加载供应商失败:', e)
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  editing.value = row || {}
  Object.assign(form, {
    code: row?.code || '', name: row?.name || '', contact: row?.contact || '',
    phone: row?.phone || '', address: row?.address || '',
    status: row?.status ?? 1, remark: row?.remark || ''
  })
  visible.value = true
}

async function save() {
  const d = { ...form }
  if (editing.value.id) { d.id = editing.value.id; await api.put('/base/supplier', d) }
  else await api.post('/base/supplier', d)
  ElMessage.success('保存成功'); visible.value = false; fetchData()
}

async function del(id) {
  await ElMessageBox.confirm('确定要删除该供应商吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.delete(`/base/supplier/${id}`); ElMessage.success('删除成功'); fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.action-link { background: none; border: none; padding: 0; font-size: 13px; font-weight: 500; cursor: pointer; transition: color 0.15s; outline: none; }
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
