<template>
  <div class="space-y-6">
    <!-- 统计卡片行 -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <!-- 总用户数 -->
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-100 card-hover flex items-center gap-4">
        <div class="w-11 h-11 rounded-xl flex items-center justify-center shrink-0" style="background: rgba(14,165,233,0.1);">
          <el-icon :size="22" color="#0ea5e9"><User /></el-icon>
        </div>
        <div>
          <p class="text-2xl font-bold text-slate-800 leading-none">{{ total }}</p>
          <p class="text-xs text-slate-400 mt-1.5">总用户数</p>
        </div>
      </div>

      <!-- 启用用户 -->
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-100 card-hover flex items-center gap-4">
        <div class="w-11 h-11 rounded-xl flex items-center justify-center shrink-0" style="background: rgba(16,185,129,0.1);">
          <el-icon :size="22" color="#10b981"><Select /></el-icon>
        </div>
        <div>
          <p class="text-2xl font-bold text-slate-800 leading-none">{{ list.filter(u => u.status === 1).length }}</p>
          <p class="text-xs text-slate-400 mt-1.5">已启用</p>
        </div>
      </div>

      <!-- 禁用用户 -->
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-100 card-hover flex items-center gap-4">
        <div class="w-11 h-11 rounded-xl flex items-center justify-center shrink-0" style="background: rgba(245,158,11,0.1);">
          <el-icon :size="22" color="#f59e0b"><CircleClose /></el-icon>
        </div>
        <div>
          <p class="text-2xl font-bold text-slate-800 leading-none">{{ list.filter(u => u.status === 0).length }}</p>
          <p class="text-xs text-slate-400 mt-1.5">已禁用</p>
        </div>
      </div>

      <!-- 管理员数量 -->
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-100 card-hover flex items-center gap-4">
        <div class="w-11 h-11 rounded-xl flex items-center justify-center shrink-0" style="background: rgba(139,92,246,0.1);">
          <el-icon :size="22" color="#8b5cf6"><Avatar /></el-icon>
        </div>
        <div>
          <p class="text-2xl font-bold text-slate-800 leading-none">{{ list.filter(u => u.role === 'admin' || u.roleName === 'admin').length }}</p>
          <p class="text-xs text-slate-400 mt-1.5">管理员</p>
        </div>
      </div>
    </div>

    <!-- 主内容卡片 -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
      <!-- 头部 -->
      <div class="px-6 py-5 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div class="flex items-start gap-3">
          <span class="w-1 h-6 rounded-full bg-gradient-to-b from-sky-400 to-blue-600 mt-1 shrink-0"></span>
          <div>
            <h2 class="text-lg font-semibold text-slate-800">用户管理</h2>
            <p class="text-xs text-slate-400 mt-1">管理系统用户账号和权限</p>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <el-input
            v-model="keyword"
            placeholder="搜索用户名/昵称"
            clearable
            class="w-64"
            @change="fetchData"
          >
            <template #prefix>
              <el-icon class="text-slate-400"><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="openDialog()" class="h-10 px-5 rounded-xl font-medium shadow-lg shadow-sky-500/20">
            <el-icon class="mr-1"><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </div>

      <!-- 表格区域 -->
      <div class="p-6">
        <el-table :data="list" v-loading="loading" class="page-table" stripe>
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="username" label="用户名" min-width="140">
            <template #default="{ row }">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-xl flex items-center justify-center text-white text-sm font-medium shadow-sm"
                     :class="avatarGradient(row.id)">
                  {{ row.username?.charAt(0).toUpperCase() }}
                </div>
                <span class="font-medium text-slate-700">{{ row.username }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="nickname" label="昵称" min-width="120" />
          <el-table-column label="角色" min-width="140">
            <template #default="{ row }">
              <template v-if="getUserRoles(row).length">
                <span
                  v-for="(r, i) in getUserRoles(row)"
                  :key="i"
                  class="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-xs font-medium bg-sky-50 text-sky-700 border border-sky-100"
                  :class="{ 'ml-1': i > 0 }"
                >{{ r }}</span>
              </template>
              <span v-else class="text-xs text-slate-400">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" width="140" />
          <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="110" align="center">
            <template #default="{ row }">
              <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium"
                    :class="row.status === 1 ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-100 text-slate-500'">
                <span class="w-1.5 h-1.5 rounded-full"
                      :class="row.status === 1 ? 'bg-emerald-400' : 'bg-slate-400'"></span>
                {{ row.status === 1 ? '启用' : '禁用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="{ row }">
              <div class="flex items-center justify-center gap-3">
                <button class="action-link primary" @click="openDialog(row)">编辑</button>
                <button class="action-link warning" @click="openRoleDialog(row)">角色</button>
                <button class="action-link danger" @click="del(row.id)">删除</button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="mt-5 flex justify-end">
          <el-pagination
            v-model:current-page="page"
            :total="total"
            :page-size="pageSize"
            layout="prev, pager, next, total"
            background
            @current-change="fetchData"
          />
        </div>
      </div>

      <!-- 编辑弹窗 -->
      <el-dialog v-model="dialogVisible" :title="editing.id ? '编辑用户' : '新增用户'" width="560px" class="user-dialog">
        <!-- 头像预览 (仅编辑模式) -->
        <div v-if="editing.id" class="flex flex-col items-center mb-6">
          <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-sky-400 to-blue-600 flex items-center justify-center text-white text-2xl font-bold shadow-lg shadow-sky-500/30 ring-4 ring-sky-50">
            {{ form.username?.charAt(0).toUpperCase() || 'U' }}
          </div>
          <p class="text-xs text-slate-400 mt-2">编辑用户 {{ form.username }}</p>
        </div>

        <el-form :model="form" label-width="80px" label-position="right">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="用户名">
                <el-input v-model="form.username" placeholder="请输入用户名">
                  <template #prefix>
                    <el-icon class="text-slate-400"><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="密码">
                <el-input v-model="form.password" type="password" :placeholder="editing.id ? '留空则不修改' : '请输入密码'" show-password>
                  <template #prefix>
                    <el-icon class="text-slate-400"><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="昵称">
                <el-input v-model="form.nickname" placeholder="请输入昵称">
                  <template #prefix>
                    <el-icon class="text-slate-400"><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号">
                <el-input v-model="form.phone" placeholder="请输入手机号">
                  <template #prefix>
                    <el-icon class="text-slate-400"><Phone /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="请输入邮箱">
              <template #prefix>
                <el-icon class="text-slate-400"><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="状态">
            <div class="flex items-center gap-3 p-3 rounded-xl bg-slate-50 border border-slate-100 w-full">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
              <span class="text-sm font-medium" :class="form.status === 1 ? 'text-emerald-600' : 'text-slate-500'">
                {{ form.status === 1 ? '账号已启用' : '账号已禁用' }}
              </span>
            </div>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false" class="rounded-xl px-5">取消</el-button>
          <el-button type="primary" @click="save" class="rounded-xl px-5 shadow-lg shadow-sky-500/20">保存</el-button>
        </template>
      </el-dialog>

      <!-- 角色弹窗 -->
      <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px" class="user-dialog">
        <div class="flex items-start gap-2 p-3 rounded-xl bg-sky-50 border border-sky-100 text-sm text-sky-700 mb-4">
          <el-icon :size="16" color="#0284c7" class="shrink-0 mt-0.5"><InfoFilled /></el-icon>
          <span>为用户分配系统角色，用户将拥有所选角色的所有权限。</span>
        </div>
        <div class="max-h-80 overflow-y-auto">
          <el-checkbox-group v-model="checkedRoles" class="flex flex-col gap-2.5">
            <el-checkbox v-for="r in roles" :key="r.id" :value="r.id" class="!mr-0">
              <div class="flex items-center gap-3 py-2 px-4 rounded-xl border-2 transition-all duration-150 w-full"
                   :class="checkedRoles.includes(r.id) ? 'border-sky-400 bg-sky-50/50' : 'border-slate-100 hover:border-slate-200 bg-white'">
                <span class="font-medium text-slate-700 text-sm">{{ r.name }}</span>
                <span class="text-xs text-slate-400 font-mono">{{ r.code }}</span>
              </div>
            </el-checkbox>
          </el-checkbox-group>
        </div>
        <template #footer>
          <el-button @click="roleDialogVisible = false" class="rounded-xl px-5">取消</el-button>
          <el-button type="primary" @click="saveRoles" class="rounded-xl px-5 shadow-lg shadow-sky-500/20">保存</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, User, Lock, Phone, Message, Select, CircleClose, Avatar, InfoFilled } from '@element-plus/icons-vue'
import api from '@/api'

const list = ref([]), loading = ref(false), page = ref(1), total = ref(0), pageSize = ref(10), keyword = ref('')
const dialogVisible = ref(false), editing = ref({}), form = reactive({})
const roleDialogVisible = ref(false), roleUserId = ref(), checkedRoles = ref([]), roles = ref([])

// 头像渐变色 (根据 ID 取模)
const avatarGradients = [
  'bg-gradient-to-br from-sky-400 to-blue-600',
  'bg-gradient-to-br from-emerald-400 to-teal-600',
  'bg-gradient-to-br from-violet-400 to-purple-600',
  'bg-gradient-to-br from-amber-400 to-orange-600',
]
function avatarGradient(id) {
  return avatarGradients[(id || 0) % 4]
}

function getUserRoles(user) {
  if (!user) return []
  // 数组格式: roles: ['admin', 'user'] 或 roles: [{ name: 'admin' }, ...]
  if (Array.isArray(user.roles) && user.roles.length) {
    return user.roles.map(r => (typeof r === 'string' ? r : r?.name || r?.roleName || ''))
  }
  // 单角色字符串
  if (user.roleName) return [user.roleName]
  if (user.role) {
    if (Array.isArray(user.role)) return user.role.map(r => (typeof r === 'string' ? r : r?.name || ''))
    return [user.role]
  }
  return []
}

async function fetchData() {
  loading.value = true
  try {
    const res = await api.get('/system/user', { params: { page: page.value, pageSize: pageSize.value, keyword: keyword.value } })
    if (res.code === 200) { list.value = res.data.list; total.value = res.data.total }
  } finally { loading.value = false }
}

function openDialog(row) {
  editing.value = row || {}
  Object.assign(form, {
    username: row?.username || '', nickname: row?.nickname || '', phone: row?.phone || '',
    email: row?.email || '', password: '', status: row?.status ?? 1
  })
  dialogVisible.value = true
}

async function save() {
  const data = { ...form }
  if (!editing.value.id) {
    await api.post('/system/user', data)
  } else {
    data.id = editing.value.id
    if (!data.password) delete data.password
    await api.put('/system/user', data)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchData()
}

async function del(id) {
  await ElMessageBox.confirm('确定要删除该用户吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.delete(`/system/user/${id}`)
  ElMessage.success('删除成功')
  fetchData()
}

async function openRoleDialog(row) {
  roleUserId.value = row.id
  checkedRoles.value = []
  roles.value = (await api.get('/system/role')).data || []
  dialogVisible.value = false
  roleDialogVisible.value = true
}

async function saveRoles() {
  await api.post(`/system/user/${roleUserId.value}/roles`, { roleIds: checkedRoles.value })
  ElMessage.success('角色分配成功')
  roleDialogVisible.value = false
}

onMounted(fetchData)
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

/* 弹窗样式 */
.user-dialog :deep(.el-dialog) {
  border-radius: 16px !important;
}
.user-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 16px !important;
  margin-right: 0 !important;
  border-bottom: 1px solid #f1f5f9;
}
.user-dialog :deep(.el-dialog__body) {
  padding: 24px !important;
}
.user-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px 20px !important;
  border-top: 1px solid #f1f5f9;
}

/* role checkbox 隐藏原生框，卡片式展示 */
:deep(.el-checkbox__input) {
  display: none !important;
}
</style>
