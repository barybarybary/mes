<template>
  <div class="min-h-screen bg-slate-50">
    <PortalNavbar />
    <div class="max-w-3xl mx-auto px-4 py-8">
      <router-link to="/portal/orders" class="text-sky-500 text-sm mb-4 inline-block">← 返回订单列表</router-link>

      <div v-if="loading" class="text-center py-10 text-slate-400">加载中...</div>
      <div v-else-if="!order" class="text-center py-10 text-slate-400">订单不存在</div>
      <div v-else class="bg-white rounded-xl shadow-sm border p-6">
        <div class="flex justify-between items-start mb-4">
          <div>
            <h2 class="text-lg font-bold text-slate-800">{{ order.orderNo }}</h2>
            <p class="text-sm text-slate-400">下单时间：{{ order.createTime }}</p>
          </div>
          <span class="text-sm px-3 py-1 rounded-full" :class="statusClass(order.status)">{{ order.statusText }}</span>
        </div>

        <!-- 进度条 -->
        <div class="mb-6">
          <el-steps :active="stepIndex" align-center>
            <el-step title="已下单" />
            <el-step title="生产中" />
            <el-step title="已发货" />
            <el-step title="已签收" />
          </el-steps>
        </div>

        <!-- 明细 -->
        <div class="border-t pt-4">
          <p class="text-sm font-medium text-slate-700 mb-3">订单明细</p>
          <div v-for="item in order.items" :key="item.productId" class="flex justify-between py-2 text-sm">
            <span class="text-slate-600">{{ item.productName }} <span class="text-slate-400">×{{ item.quantity }}</span></span>
            <span class="text-slate-800">¥{{ item.amount }}</span>
          </div>
          <div class="flex justify-between py-3 border-t mt-2 font-bold">
            <span>合计</span>
            <span class="text-sky-600 text-lg">¥{{ order.totalAmount }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api/portal'
import PortalNavbar from './PortalNavbar.vue'

const route = useRoute()
const order = ref(null)
const loading = ref(true)

const stepIndex = computed(() => {
  const s = order.value?.status || 0
  if (s <= 1) return 0
  if (s <= 3) return 1
  if (s <= 4) return 2
  return 3
})

onMounted(async () => {
  try {
    const res = await api.get(`/orders/${route.params.id}`)
    order.value = res.data
  } catch { /* ignore */ }
  finally { loading.value = false }
})

function statusClass(status) {
  const map = { 1: 'bg-slate-100 text-slate-600', 2: 'bg-blue-50 text-blue-600', 3: 'bg-amber-50 text-amber-600', 4: 'bg-sky-50 text-sky-600', 5: 'bg-green-50 text-green-600', 6: 'bg-red-50 text-red-500' }
  return map[status] || 'bg-slate-100 text-slate-600'
}
</script>
