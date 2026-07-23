<template>
  <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
    <!-- 头部 -->
    <div class="px-4 sm:px-6 py-4 sm:py-5 border-b border-slate-100 dark:border-slate-700 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 sm:gap-4">
      <div>
        <h2 class="text-base sm:text-lg font-semibold text-slate-800 dark:text-slate-200">产品管理</h2>
        <p class="text-xs text-slate-400 dark:text-slate-300 mt-0.5 sm:mt-1">管理产品基础信息和物料清单</p>
      </div>
      <div class="flex items-center gap-2 sm:gap-3">
        <el-input v-model="keyword" placeholder="搜索产品" clearable class="flex-1 sm:w-56" @change="fetchData">
          <template #prefix><el-icon class="text-slate-400 dark:text-slate-300"><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="openDialog()" class="h-10 px-3 sm:px-5 rounded-xl font-medium text-sm shrink-0">
          <el-icon class="sm:mr-1"><Plus /></el-icon><span class="hidden sm:inline">新增产品</span>
        </el-button>
      </div>
    </div>

    <!-- 桌面端表格 -->
    <div class="p-4 sm:p-6 hidden md:block">
      <el-table :data="list" border stripe v-loading="loading" class="page-table">
        <el-table-column prop="code" label="产品编码" width="130">
          <template #default="{ row }"><el-tag type="info" effect="light" size="small">{{ row.code }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="name" label="产品名称" min-width="160">
          <template #default="{ row }"><span class="font-medium text-slate-700 dark:text-slate-600">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="spec" label="规格型号" min-width="150" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="price" label="参考售价" width="120" align="right">
          <template #default="{ row }"><span class="font-semibold text-emerald-600">¥{{ row.price?.toFixed(2) }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-3">
              <button class="action-link primary" @click="openDialog(row)">编辑</button>
              <button class="action-link success" @click="openBomDialog(row)">BOM</button>
              <button class="action-link danger" @click="del(row.id)">删除</button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="mt-5 flex justify-end">
        <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize" layout="prev, pager, next, total" background @current-change="fetchData" />
      </div>
    </div>

    <!-- 移动端卡片列表 -->
    <div class="p-3 sm:p-4 md:hidden" v-loading="loading">
      <div v-if="list.length === 0 && !loading" class="text-center py-16 text-slate-400 dark:text-slate-300">
        <el-icon :size="48" class="mb-3"><Document /></el-icon>
        <p class="text-sm">暂无产品数据</p>
      </div>
      <div v-else class="space-y-3">
        <div
          v-for="row in list"
          :key="row.id"
          class="product-card bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-4 active:bg-slate-50 dark:bg-slate-900 transition-colors"
        >
          <!-- 顶部：编码标签 + 产品名 -->
          <div class="flex items-start justify-between gap-3 mb-3">
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <el-tag type="info" effect="light" size="small">{{ row.code }}</el-tag>
              </div>
              <h3 class="text-sm font-semibold text-slate-800 dark:text-slate-200 truncate">{{ row.name }}</h3>
            </div>
            <span class="text-sm font-bold text-emerald-600 whitespace-nowrap shrink-0">¥{{ row.price?.toFixed(2) }}</span>
          </div>
          <!-- 中间：规格 + 单位 -->
          <div class="flex items-center gap-4 text-xs text-slate-500 dark:text-slate-300 mb-3">
            <span v-if="row.spec" class="inline-flex items-center gap-1">
              <span class="text-slate-400 dark:text-slate-300">规格</span>
              <span class="text-slate-600 dark:text-slate-600 font-medium">{{ row.spec }}</span>
            </span>
            <span class="inline-flex items-center gap-1">
              <span class="text-slate-400 dark:text-slate-300">单位</span>
              <span class="text-slate-600 dark:text-slate-600 font-medium">{{ row.unit }}</span>
            </span>
          </div>
          <!-- 底部操作栏 -->
          <div class="flex items-center gap-2 pt-3 border-t border-slate-100 dark:border-slate-700">
            <button class="mobile-action-btn primary" @click="openDialog(row)">
              <el-icon :size="14"><Edit /></el-icon>编辑
            </button>
            <button class="mobile-action-btn success" @click="openBomDialog(row)">
              <el-icon :size="14"><Setting /></el-icon>BOM
            </button>
            <button class="mobile-action-btn danger" @click="del(row.id)">
              <el-icon :size="14"><Delete /></el-icon>删除
            </button>
          </div>
        </div>
      </div>
      <!-- 移动端分页 -->
      <div class="mt-4 flex justify-center" v-if="total > pageSize">
        <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize" layout="prev, pager, next" background small @current-change="fetchData" />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editing.id ? '编辑产品' : '新增产品'"
      width="560px"
      class="custom-dialog responsive-dialog"
    >
      <el-form :model="form" label-width="90px" label-position="right" class="responsive-form">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12"><el-form-item label="产品编码"><el-input v-model="form.code" placeholder="请输入产品编码" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="产品名称"><el-input v-model="form.name" placeholder="请输入产品名称" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12"><el-form-item label="规格型号"><el-input v-model="form.spec" placeholder="请输入规格型号" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="计量单位"><el-input v-model="form.unit" placeholder="如：个、件、kg" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="参考售价"><el-input-number v-model="form.price" :precision="2" :min="0" class="w-full" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" /></el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false" class="rounded-xl px-4 sm:px-5">取消</el-button>
          <el-button type="primary" @click="save" class="rounded-xl px-4 sm:px-5">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- BOM 弹窗 -->
    <el-dialog
      v-model="bomDialogVisible"
      title="BOM 物料清单"
      width="750px"
      class="custom-dialog responsive-dialog"
    >
      <p class="text-xs sm:text-sm text-slate-500 dark:text-slate-300 mb-4">配置该产品的物料组成结构。</p>
      <!-- 桌面端 BOM 表格 -->
      <div class="hidden sm:block">
        <el-table :data="bomList" border class="page-table">
          <el-table-column label="物料" min-width="250">
            <template #default="{ row }">
              <el-select v-model="row.materialId" filterable placeholder="选择物料" class="w-full">
                <el-option v-for="p in products" :key="p.id" :value="p.id" :label="p.name + ' (' + p.code + ')'" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="用量" width="140">
            <template #default="{ row }"><el-input-number v-model="row.quantity" :min="0" :precision="3" size="small" class="w-full" /></template>
          </el-table-column>
          <el-table-column label="单位" width="120">
            <template #default="{ row }"><el-input v-model="row.unit" size="small" placeholder="单位" /></template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ $index }"><button class="action-link danger" @click="bomList.splice($index, 1)">删除</button></template>
          </el-table-column>
        </el-table>
      </div>
      <!-- 移动端 BOM 卡片列表 -->
      <div class="sm:hidden space-y-3">
        <div v-for="(item, idx) in bomList" :key="idx" class="flex items-center gap-2 bg-slate-50 dark:bg-slate-900 rounded-xl p-3">
          <div class="flex-1 min-w-0 space-y-2">
            <el-select v-model="item.materialId" filterable placeholder="选择物料" size="small" class="w-full">
              <el-option v-for="p in products" :key="p.id" :value="p.id" :label="p.name + ' (' + p.code + ')'" />
            </el-select>
            <div class="flex gap-2">
              <el-input-number v-model="item.quantity" :min="0" :precision="3" size="small" placeholder="用量" class="flex-1" />
              <el-input v-model="item.unit" size="small" placeholder="单位" class="w-20" />
            </div>
          </div>
          <button class="action-link danger shrink-0 text-xs" @click="bomList.splice(idx, 1)">删除</button>
        </div>
      </div>
      <el-button class="mt-4 w-full sm:w-auto" @click="bomList.push({ materialId: null, quantity: 1, unit: '个' })">
        <el-icon class="mr-1"><Plus /></el-icon>添加物料
      </el-button>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="bomDialogVisible = false" class="rounded-xl px-4 sm:px-5">取消</el-button>
          <el-button type="primary" @click="saveBom" class="rounded-xl px-4 sm:px-5">保存BOM</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Setting, Document } from '@element-plus/icons-vue'
import api from '@/api'

const list = ref([]), loading = ref(false), page = ref(1), total = ref(0), pageSize = ref(10), keyword = ref('')
const dialogVisible = ref(false), editing = ref({}), form = reactive({})
const bomDialogVisible = ref(false), bomProductId = ref(), bomList = ref([]), products = ref([])

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (keyword.value) params.keyword = keyword.value
    const res = await api.get('/base/product', { params })
    if (res.code === 200) {
      const data = res.data
      if (Array.isArray(data)) {
        list.value = data
        total.value = data.length
      } else {
        list.value = data?.list || data?.records || []
        total.value = data?.total || 0
      }
      console.log('产品数据加载成功:', list.value.length, '条, 总计:', total.value)
    } else {
      console.warn('产品API返回非200:', res)
    }
  } catch (e) {
    console.error('加载产品失败:', e)
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  editing.value = row || {}
  Object.assign(form, { code: row?.code || '', name: row?.name || '', spec: row?.spec || '', unit: row?.unit || '个', price: row?.price || 0, remark: row?.remark || '' })
  dialogVisible.value = true
}

async function save() {
  const data = { ...form }
  if (editing.value.id) { data.id = editing.value.id; await api.put('/base/product', data) }
  else await api.post('/base/product', data)
  ElMessage.success('保存成功'); dialogVisible.value = false; fetchData()
}

async function del(id) {
  await ElMessageBox.confirm('确定要删除该产品吗？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await api.delete(`/base/product/${id}`); ElMessage.success('删除成功'); fetchData()
}

async function openBomDialog(row) {
  bomProductId.value = row.id
  try {
    const res = await api.get(`/base/product/${row.id}`)
    const data = res.data
    // 兼容多种返回格式
    if (data?.bomList) {
      bomList.value = data.bomList
    } else if (Array.isArray(data)) {
      bomList.value = data
    } else {
      bomList.value = data?.bomList || data?.list || data?.records || []
    }
  } catch (e) {
    console.error('加载BOM失败:', e)
    bomList.value = []
  }
  products.value = list.value
  bomDialogVisible.value = true
}

async function saveBom() {
  await api.post(`/base/product/${bomProductId.value}/bom`, bomList.value)
  ElMessage.success('BOM保存成功'); bomDialogVisible.value = false
}

onMounted(fetchData)
</script>

<style scoped>
/* ===== 操作链接（桌面端表格用） ===== */
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
.action-link.success { color: #10b981; }
.action-link.success:hover { color: #047857; }
.action-link.danger { color: #f43f5e; }
.action-link.danger:hover { color: #be123c; }

/* ===== 移动端卡片 ===== */
.product-card {
  -webkit-tap-highlight-color: transparent;
}
.product-card:active {
  background-color: #f8fafc;
}

/* ===== 移动端操作按钮 ===== */
.mobile-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  outline: none;
  -webkit-tap-highlight-color: transparent;
}
.mobile-action-btn:active {
  transform: scale(0.96);
}
.mobile-action-btn.primary {
  color: #3b82f6;
  border-color: #bfdbfe;
  background: #eff6ff;
}
.mobile-action-btn.primary:active {
  background: #dbeafe;
}
.mobile-action-btn.success {
  color: #10b981;
  border-color: #a7f3d0;
  background: #ecfdf5;
}
.mobile-action-btn.success:active {
  background: #d1fae5;
}
.mobile-action-btn.danger {
  color: #f43f5e;
  border-color: #fecdd3;
  background: #fff1f2;
}
.mobile-action-btn.danger:active {
  background: #ffe4e6;
}

/* ===== 响应式弹窗 ===== */
:deep(.responsive-dialog .el-dialog) {
  border-radius: 16px !important;
  margin: 0 auto !important;
}
:deep(.responsive-dialog .el-dialog__header) {
  padding: 20px 24px 16px !important;
  margin-right: 0 !important;
  border-bottom: 1px solid #f1f5f9;
}
:deep(.responsive-dialog .el-dialog__body) {
  padding: 24px !important;
}
:deep(.responsive-dialog .el-dialog__footer) {
  padding: 16px 24px 20px !important;
  border-top: 1px solid #f1f5f9;
}

@media (max-width: 639px) {
  :deep(.responsive-dialog) {
    width: 92% !important;
    max-width: 92vw !important;
  }
  :deep(.responsive-dialog .el-dialog) {
    border-radius: 16px 16px 0 0 !important;
    position: fixed !important;
    bottom: 0 !important;
    left: 50% !important;
    transform: translateX(-50%) !important;
    margin: 0 !important;
    width: 100% !important;
    max-width: 100vw !important;
    max-height: 90vh !important;
    display: flex !important;
    flex-direction: column !important;
  }
  :deep(.responsive-dialog .el-dialog__body) {
    flex: 1 !important;
    overflow-y: auto !important;
    padding: 16px !important;
  }
  :deep(.responsive-dialog .el-dialog__header) {
    padding: 16px !important;
  }
  :deep(.responsive-dialog .el-dialog__footer) {
    padding: 12px 16px !important;
  }
  /* 移动端表单标签置顶 */
  :deep(.responsive-form .el-form-item) {
    display: block !important;
  }
  :deep(.responsive-form .el-form-item__label) {
    display: block !important;
    text-align: left !important;
    width: auto !important;
    padding-bottom: 4px !important;
  }
  :deep(.responsive-form .el-form-item__content) {
    margin-left: 0 !important;
  }
}
</style>
