<template>
  <div class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-lg font-semibold text-slate-800">仓库管理</h2>
        <p class="text-xs text-slate-400 mt-1">管理仓库和库位信息</p>
      </div>
      <el-button type="primary" @click="openDialog()" class="h-10 px-5 rounded-xl font-medium">
        <el-icon class="mr-1"><Plus /></el-icon>新增仓库
      </el-button>
    </div>

    <div class="p-6">
      <el-table :data="list" border stripe class="page-table">
        <el-table-column prop="code" label="仓库编码" width="130">
          <template #default="{ row }"><el-tag type="info" effect="light" size="small">{{ row.code }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="name" label="仓库名称" min-width="160">
          <template #default="{ row }"><span class="font-medium text-slate-700">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="type" label="仓库类型" width="130" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'material'" type="warning" effect="light" size="small">原料仓</el-tag>
            <el-tag v-else-if="row.type === 'finished'" type="success" effect="light" size="small">成品仓</el-tag>
            <el-tag v-else type="info" effect="light" size="small">半成品仓</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="manager" label="负责人" width="100" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-3">
              <el-button type="primary" @click="openDialog(row)">编辑</el-button>
              <el-button type="success" @click="openLocDialog(row)">库位</el-button>
              <el-button type="danger" @click="del(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="editing.id ? '编辑仓库' : '新增仓库'" width="520px" class="custom-dialog">
      <el-form :model="form" label-width="80px" label-position="right">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="仓库编码"><el-input v-model="form.code" placeholder="请输入仓库编码" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="仓库名称"><el-input v-model="form.name" placeholder="请输入仓库名称" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="仓库类型">
          <el-select v-model="form.type" class="w-full">
            <el-option label="原料仓" value="material" />
            <el-option label="成品仓" value="finished" />
            <el-option label="半成品仓" value="semi" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="负责人"><el-input v-model="form.manager" placeholder="请输入负责人" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="地址"><el-input v-model="form.address" placeholder="请输入地址" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="visible = false" class="rounded-xl px-5">取消</el-button>
        <el-button type="primary" @click="save" class="rounded-xl px-5">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="locVisible" title="库位管理" width="520px" class="custom-dialog">
      <p class="text-sm text-slate-500 mb-4">管理该仓库下的库位信息。</p>
      <div class="max-h-80 overflow-y-auto border border-slate-200 rounded-xl">
        <div v-for="l in locations" :key="l.id" class="flex justify-between items-center px-4 py-3 border-b border-slate-100 last:border-0 hover:bg-slate-50 transition-colors">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded-lg bg-sky-50 flex items-center justify-center">
              <el-icon color="#0ea5e9" :size="16"><Location /></el-icon>
            </div>
            <div>
              <span class="font-medium text-slate-700 text-sm">{{ l.code }}</span>
              <span v-if="l.name" class="text-slate-400 text-sm ml-2">{{ l.name }}</span>
            </div>
          </div>
          <div class="flex gap-2">
            <el-button link type="primary" size="small" @click="editLocation(l)">编辑</el-button>
            <el-button link type="danger" size="small" @click="delLocation(l.id)">删除</el-button>
          </div>
        </div>
        <div v-if="locations.length === 0" class="text-center py-10 text-slate-400 text-sm">暂无库位</div>
      </div>
      <div class="flex gap-2 mt-4">
        <el-input v-model="locCode" placeholder="库位编码" size="default" class="flex-1" />
        <el-input v-model="locName" placeholder="名称(可选)" size="default" style="width:120px" />
        <el-button type="primary" @click="locEditingId ? saveEditLocation() : addLocation()">
          {{ locEditingId ? '更新' : '添加' }}
        </el-button>
        <el-button v-if="locEditingId" @click="cancelEditLocation">取消</el-button>
      </div>
      <template #footer>
        <el-button @click="locVisible = false" class="rounded-xl px-5">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const list = ref([]), visible = ref(false), editing = ref({}), form = reactive({})
const locVisible = ref(false), locWarehouseId = ref(), locations = ref([]), locCode = ref(''), locName = ref(''), locEditingId = ref(null)

async function fetch() {
  try {
    const r = await api.get('/base/warehouse')
    if (r.code === 200) {
      const data = r.data
      if (Array.isArray(data)) list.value = data
      else list.value = data?.list || data?.records || []
      console.log('仓库数据加载成功:', list.value.length, '条')
    } else {
      console.warn('仓库API返回非200:', r)
    }
  } catch (e) {
    console.error('加载仓库失败:', e)
  }
}

function openDialog(row) {
  editing.value = row || {}
  Object.assign(form, { code: row?.code || '', name: row?.name || '', type: row?.type || 'finished', address: row?.address || '', manager: row?.manager || '' })
  visible.value = true
}

async function save() {
  const d = { ...form }
  if (editing.value.id) { d.id = editing.value.id; await api.put('/base/warehouse', d) }
  else await api.post('/base/warehouse', d)
  ElMessage.success('保存成功'); visible.value = false; fetch()
}

async function del(id) {
  await ElMessageBox.confirm('确定要删除该仓库吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.delete(`/base/warehouse/${id}`); ElMessage.success('删除成功'); fetch()
}

async function openLocDialog(row) {
  locWarehouseId.value = row.id
  locEditingId.value = null
  locCode.value = ''
  locName.value = ''
  await loadLocations()
  locVisible.value = true
}

async function loadLocations() {
  try {
    const r = await api.get(`/base/warehouse/${locWarehouseId.value}/locations`)
    const data = r.data || r
    locations.value = Array.isArray(data) ? data : (data?.list || data?.records || [])
  } catch { locations.value = [] }
}

function editLocation(l) {
  locEditingId.value = l.id
  locCode.value = l.code || ''
  locName.value = l.name || ''
}

function cancelEditLocation() {
  locEditingId.value = null
  locCode.value = ''
  locName.value = ''
}

async function saveEditLocation() {
  if (!locCode.value) return
  await api.put(`/base/warehouse/locations/${locEditingId.value}`, { code: locCode.value, name: locName.value || null })
  ElMessage.success('库位已更新')
  cancelEditLocation()
  await loadLocations()
}

async function addLocation() {
  if (!locCode.value) return
  await api.post(`/base/warehouse/${locWarehouseId.value}/locations`, { code: locCode.value, name: locName.value || null })
  ElMessage.success('库位已添加')
  locCode.value = ''
  locName.value = ''
  await loadLocations()
}

async function delLocation(id) {
  await ElMessageBox.confirm('确定删除该库位吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.delete(`/base/warehouse/locations/${id}`)
  ElMessage.success('已删除')
  await loadLocations()
}

onMounted(fetch)
</script>

<style scoped>
:deep(.page-table th.el-table__cell) { background-color: #f8fafc !important; color: #475569 !important; font-weight: 600 !important; font-size: 13px !important; }
:deep(.custom-dialog .el-dialog) { border-radius: 16px !important; }
:deep(.custom-dialog .el-dialog__header) { padding: 20px 24px 16px !important; margin-right: 0 !important; border-bottom: 1px solid #f1f5f9; }
:deep(.custom-dialog .el-dialog__body) { padding: 24px !important; }
:deep(.custom-dialog .el-dialog__footer) { padding: 16px 24px 20px !important; border-top: 1px solid #f1f5f9; }
</style>
