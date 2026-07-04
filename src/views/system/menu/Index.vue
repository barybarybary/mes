<template>
  <div class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-lg font-semibold text-slate-800">菜单管理</h2>
        <p class="text-xs text-slate-400 mt-1">管理系统菜单结构和权限标识</p>
      </div>
      <el-button type="primary" @click="openDialog()" class="h-10 px-5 rounded-xl font-medium">
        <el-icon class="mr-1"><Plus /></el-icon>
        新增菜单
      </el-button>
    </div>

    <div class="p-6">
      <el-table :data="list" class="page-table" row-key="id" default-expand-all border stripe>
        <el-table-column prop="name" label="菜单名称" min-width="200">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-icon v-if="row.icon" class="text-slate-500"><component :is="row.icon" /></el-icon>
              <span class="font-medium text-slate-700">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 1" type="info" effect="light" size="small">目录</el-tag>
            <el-tag v-else-if="row.type === 2" type="primary" effect="light" size="small">菜单</el-tag>
            <el-tag v-else type="success" effect="light" size="small">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="180" show-overflow-tooltip />
        <el-table-column prop="permission" label="权限标识" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-3">
              <button class="action-link primary" @click="openDialog(row)">编辑</button>
              <button class="action-link danger" @click="del(row.id)">删除</button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="editing.id ? '编辑菜单' : '新增菜单'" width="560px" class="custom-dialog">
      <el-form :model="form" label-width="90px" label-position="right">
        <el-form-item label="父级菜单">
          <el-tree-select v-model="form.parentId" :data="treeOptions" check-strictly node-key="id" :props="{ label: 'name', value: 'id' }" clearable class="w-full" placeholder="选择父级菜单（根节点则留空）" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="菜单名称">
              <el-input v-model="form.name" placeholder="请输入菜单名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单类型">
              <el-select v-model="form.type" class="w-full">
                <el-option label="目录" :value="1" />
                <el-option label="菜单" :value="2" />
                <el-option label="按钮" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="路由路径">
          <el-input v-model="form.path" placeholder="如：/system/user" />
        </el-form-item>
        <el-form-item label="组件路径">
          <el-input v-model="form.component" placeholder="如：system/user/Index" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="图标">
              <el-input v-model="form.icon" placeholder="Element图标名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="权限标识">
          <el-input v-model="form.permission" placeholder="如：system:user:add" />
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

const list = ref([]), visible = ref(false), editing = ref({}), form = reactive({}), treeOptions = ref([])

async function fetch() {
  list.value = (await api.get('/system/menu')).data
  treeOptions.value = [{ id: 0, name: '根节点', children: list.value }]
}

function openDialog(row) {
  editing.value = row || {}
  Object.assign(form, {
    parentId: row?.parentId || 0,
    name: row?.name || '',
    type: row?.type || 2,
    path: row?.path || '',
    component: row?.component || '',
    icon: row?.icon || '',
    permission: row?.permission || '',
    sort: row?.sort || 0
  })
  visible.value = true
}

async function save() {
  const d = { ...form }
  if (editing.value.id) { d.id = editing.value.id; await api.put('/system/menu', d) }
  else await api.post('/system/menu', d)
  ElMessage.success('保存成功')
  visible.value = false
  fetch()
}

async function del(id) {
  await ElMessageBox.confirm('确定要删除该菜单吗？删除后子菜单也会被删除。', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.delete(`/system/menu/${id}`)
  ElMessage.success('删除成功')
  fetch()
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
</style>
