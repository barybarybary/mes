<template>
  <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
    <div class="px-6 py-5 border-b border-slate-100 dark:border-slate-700 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-lg font-semibold text-slate-800 dark:text-slate-200">角色管理</h2>
        <p class="text-xs text-slate-400 dark:text-slate-300 mt-1">管理系统角色和权限分配</p>
      </div>
      <el-button type="primary" @click="openDialog()" class="h-10 px-5 rounded-xl font-medium">
        <el-icon class="mr-1"><Plus /></el-icon>
        新增角色
      </el-button>
    </div>

    <div class="p-6">
      <el-table :data="list" class="page-table" stripe>
        <el-table-column prop="code" label="角色编码" width="150">
          <template #default="{ row }">
            <el-tag type="info" effect="light" size="small">{{ row.code }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="角色名称" min-width="150">
          <template #default="{ row }">
            <span class="font-medium text-slate-700 dark:text-slate-600">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="角色描述" min-width="250" show-overflow-tooltip />
        <el-table-column label="操作" width="190" align="center" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-3">
              <button class="action-link primary" @click="openDialog(row)">编辑</button>
              <button class="action-link warning" @click="openMenuDialog(row)">分配菜单</button>
              <button class="action-link danger" @click="del(row.id)">删除</button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="editing.id ? '编辑角色' : '新增角色'" width="480px" class="custom-dialog">
      <el-form :model="form" label-width="80px" label-position="right">
        <el-form-item label="角色编码">
          <el-input v-model="form.code" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false" class="rounded-xl px-5">取消</el-button>
        <el-button type="primary" @click="save" class="rounded-xl px-5">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="menuVisible" title="分配菜单权限" width="500px" class="custom-dialog">
      <p class="text-sm text-slate-500 dark:text-slate-300 mb-4">勾选该角色可以访问的菜单和功能。</p>
      <div class="border border-slate-200 dark:border-slate-700 rounded-xl p-4 max-h-96 overflow-y-auto">
        <el-tree :data="menuTree" show-checkbox node-key="id" ref="menuTreeRef" default-expand-all :props="{ label: 'name' }" />
      </div>
      <template #footer>
        <el-button @click="menuVisible = false" class="rounded-xl px-5">取消</el-button>
        <el-button type="primary" @click="saveMenus" class="rounded-xl px-5">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const list = ref([]), visible = ref(false), editing = ref({}), form = reactive({})
const menuVisible = ref(false), menuTree = ref([]), menuTreeRef = ref(), roleForMenu = ref()

async function fetch() { list.value = (await api.get('/system/role')).data }

function openDialog(row) {
  editing.value = row || {}
  Object.assign(form, { code: row?.code || '', name: row?.name || '', description: row?.description || '' })
  visible.value = true
}

async function save() {
  const d = { ...form }
  if (editing.value.id) { d.id = editing.value.id; await api.put('/system/role', d) }
  else await api.post('/system/role', d)
  ElMessage.success('保存成功')
  visible.value = false
  fetch()
}

async function del(id) {
  await ElMessageBox.confirm('确定要删除该角色吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.delete(`/system/role/${id}`)
  ElMessage.success('删除成功')
  fetch()
}

async function openMenuDialog(row) {
  roleForMenu.value = row.id
  menuTree.value = (await api.get('/system/menu/tree')).data || []
  menuVisible.value = true
}

async function saveMenus() {
  const ids = menuTreeRef.value.getCheckedKeys()
  await api.post(`/system/role/${roleForMenu.value}/menus`, { menuIds: ids })
  ElMessage.success('菜单分配成功')
  menuVisible.value = false
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
.action-link.warning { color: #f59e0b; }
.action-link.warning:hover { color: #b45309; }
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
