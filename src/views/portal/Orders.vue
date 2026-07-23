<template>
  <div class="min-h-screen bg-slate-50">
    <PortalNavbar />
    <div class="max-w-3xl mx-auto px-4 py-8">
      <h2 class="text-xl font-bold text-slate-800 mb-6">我的订单</h2>

      <div v-if="loading" class="text-center py-10 text-slate-400">加载中...</div>
      <div v-else-if="orders.length === 0" class="text-center py-16">
        <p class="text-slate-400">暂无订单</p>
      </div>
      <div v-else class="space-y-3">
        <div v-for="o in orders" :key="o.id" class="bg-white rounded-xl shadow-sm border p-4 cursor-pointer hover:shadow-md transition" @click="$router.push(`/portal/orders/${o.id}`)">
          <div class="flex justify-between items-start mb-2">
            <div>
              <p class="font-medium text-slate-800">{{ o.orderNo }}</p>
              <p class="text-xs text-slate-400">{{ o.orderDate }}</p>
            </div>
            <span class="text-sm px-3 py-1 rounded-full" :class="statusClass(o.status)">{{ o.statusText }}</span>
          </div>
          <div class="flex justify-between items-end">
            <p class="text-sm text-slate-500 truncate flex-1 mr-4">{{ o.items?.map(i => i.productName).join('、') }}</p>
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
import api from '@/api/portal'
import PortalNavbar from './PortalNavbar.vue'

const orders = ref([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const pageSize = 10

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const res = await api.get('/orders', { params: { page: page.value, pageSize } })
    orders.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch { /* ignore */ }
  finally { loading.value = false }
}

function statusClass(status) {
  const map = { 1: 'bg-slate-100 text-slate-600', 2: 'bg-blue-50 text-blue-600', 3: 'bg-amber-50 text-amber-600', 4: 'bg-sky-50 text-sky-600', 5: 'bg-green-50 text-green-600', 6: 'bg-red-50 text-red-500' }
  return map[status] || 'bg-slate-100 text-slate-600'
}
</script>
