<template>
  <div class="min-h-screen bg-slate-50 dark:bg-slate-900">
    <PortalNavbar />
    <div class="max-w-3xl mx-auto px-4 py-8">
      <h2 class="text-xl font-bold text-slate-800 dark:text-slate-200 mb-6">我的订单</h2>

      <!-- 状态筛选标签 -->
      <div class="flex flex-wrap gap-2 mb-6">
        <button
          v-for="tab in statusTabs" :key="tab.value"
          @click="statusFilter = tab.value; page = 1; fetchData()"
          :class="statusFilter === tab.value ? 'bg-sky-500 text-white' : 'bg-white dark:bg-slate-800 text-slate-600 dark:text-slate-300 border-slate-200 dark:border-slate-600 hover:border-sky-300'"
          class="px-4 py-1.5 rounded-full text-sm border transition"
        >{{ tab.label }}</button>
      </div>

      <div v-if="loading" class="text-center py-10 text-slate-400 dark:text-slate-500">加载中...</div>
      <div v-else-if="orders.length === 0" class="text-center py-16">
        <p class="text-5xl mb-4">📋</p>
        <p class="text-slate-400 dark:text-slate-500">暂无订单</p>
        <router-link to="/portal/products" class="text-sky-500 hover:text-sky-600 text-sm mt-2 inline-block">去逛逛 →</router-link>
      </div>
      <div v-else class="space-y-3">
        <div v-for="o in orders" :key="o.id" class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border dark:border-slate-700 p-4 hover:shadow-md transition">
          <div class="flex justify-between items-start mb-2">
            <div class="cursor-pointer flex-1" @click="$router.push(`/portal/orders/${o.id}`)">
              <p class="font-medium text-slate-800 dark:text-slate-200">{{ o.orderNo }}</p>
              <p class="text-xs text-slate-400 dark:text-slate-500">{{ o.orderDate }}</p>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-sm px-3 py-1 rounded-full" :class="statusClass(o.status)">{{ o.statusText }}</span>
              <!-- 待审核时可取消 -->
              <el-button
                v-if="o.status === 1"
                text type="danger" size="small"
                @click.stop="cancelOrder(o)"
              >取消</el-button>
            </div>
          </div>
          <div class="flex justify-between items-end cursor-pointer" @click="$router.push(`/portal/orders/${o.id}`)">
            <p class="text-sm text-slate-500 dark:text-slate-400 truncate flex-1 mr-4">{{ o.items?.map(i => i.productName).join('、') }}</p>
            <span class="text-sky-600 font-bold">¥{{ o.totalAmount }}</span>
          </div>
        </div>
      </div>
      <div class="flex justify-center mt-6">
        <el-pagination v-if="total > pageSize" background layout="prev, pager, next" :total="total" :page-size="pageSize" v-model:current-page="page" @current-change="fetchData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api/portal'
import PortalNavbar from './PortalNavbar.vue'
import { statusClass } from '@/utils/portal'

const orders = ref([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const pageSize = 10
const statusFilter = ref(null)

const statusTabs = [
  { label: '全部', value: null },
  { label: '待付款', value: 1 },
  { label: '已支付', value: 2 },
  { label: '生产中', value: 3 },
  { label: '已发货', value: 4 },
  { label: '已完成', value: 5 },
  { label: '已取消', value: 6 }
]

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize }
    if (statusFilter.value) params.status = statusFilter.value
    const res = await api.get('/orders', { params })
    orders.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch { /* ignore */ }
  finally { loading.value = false }
}

async function cancelOrder(o) {
  try {
    await ElMessageBox.confirm(`确定取消订单 ${o.orderNo}？`, '取消订单', { type: 'warning', confirmButtonText: '确定取消' })
    await api.put(`/orders/${o.id}/cancel`)
    ElMessage.success('订单已取消')
    fetchData()
  } catch { /* ignore */ }
}
</script>
