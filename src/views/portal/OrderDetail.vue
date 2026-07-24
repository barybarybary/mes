<template>
  <div class="min-h-screen bg-slate-50 dark:bg-slate-900">
    <PortalNavbar />
    <div class="max-w-3xl mx-auto px-4 py-8">
      <router-link to="/portal/orders" class="text-sky-500 text-sm mb-4 inline-block">← 返回订单列表</router-link>

      <div v-if="loading" class="text-center py-10 text-slate-400 dark:text-slate-500">加载中...</div>
      <div v-else-if="!order" class="text-center py-10 text-slate-400 dark:text-slate-500">订单不存在</div>
      <div v-else class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border dark:border-slate-700 p-6">
        <div class="flex justify-between items-start mb-4">
          <div>
            <h2 class="text-lg font-bold text-slate-800 dark:text-slate-200">{{ order.orderNo }}</h2>
            <p class="text-sm text-slate-400 dark:text-slate-500">下单时间：{{ order.createTime }}</p>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-sm px-3 py-1 rounded-full" :class="statusClass(order.status)">{{ order.statusText }}</span>
            <el-button v-if="order.status === 1 && !order.paid" type="success" size="small" class="!rounded-lg" @click="payOrder">💳 去支付</el-button>
            <el-button v-if="order.status === 1" text type="danger" size="small" @click="cancelOrder">取消订单</el-button>
          </div>
        </div>

        <!-- 进度条 -->
        <div class="mb-6">
          <el-steps :active="stepIndex" align-center>
            <el-step title="已下单" :description="stepIndex >= 0 ? order.createTime?.slice(0, 10) : ''" />
            <el-step title="已审核" />
            <el-step title="生产中" />
            <el-step title="已发货" :description="order.deliveryDate || ''" />
            <el-step title="已完成" />
          </el-steps>
        </div>

        <!-- 发货信息 -->
        <div v-if="order.deliveryNo" class="bg-sky-50 dark:bg-sky-900/30 rounded-lg p-3 mb-4 text-sm">
          <span class="text-sky-600 dark:text-sky-400 font-medium">🚚 已发货</span>
          <span class="text-slate-500 dark:text-slate-400 ml-2">单号：{{ order.deliveryNo }}</span>
          <span v-if="order.deliveryDate" class="text-slate-400 dark:text-slate-500 ml-2">日期：{{ order.deliveryDate }}</span>
        </div>

        <!-- 明细 -->
        <div class="border-t dark:border-slate-700 pt-4">
          <p class="text-sm font-medium text-slate-700 dark:text-slate-300 mb-3">订单明细</p>
          <div v-for="item in order.items" :key="item.productId" class="flex justify-between py-2 text-sm">
            <span class="text-slate-600 dark:text-slate-300">{{ item.productName }} <span class="text-slate-400 dark:text-slate-500">×{{ item.quantity }}</span></span>
            <span class="text-slate-800 dark:text-slate-200">¥{{ item.amount }}</span>
          </div>
          <div class="flex justify-between py-3 border-t dark:border-slate-700 mt-2 font-bold">
            <span class="dark:text-slate-200">合计</span>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api/portal'
import PortalNavbar from './PortalNavbar.vue'
import { statusClass } from '@/utils/portal'

const route = useRoute()
const order = ref(null)
const loading = ref(true)

// 5步进度条：待付款(1) → 已支付(2) → 生产中(3) → 已发货(4) → 已完成(5)
const stepIndex = computed(() => {
  const s = order.value?.status || 0
  if (s === 6) return -1       // 已取消：无进度
  if (s <= 1) return 0         // 待付款
  if (s === 2) return 1        // 已支付
  if (s === 3) return 2        // 生产中
  if (s === 4) return 3        // 已发货
  if (s >= 5) return 4         // 已完成
  return 0
})

onMounted(async () => {
  try {
    const res = await api.get(`/orders/${route.params.id}`)
    order.value = res.data
  } catch { /* ignore */ }
  finally { loading.value = false }
})

async function payOrder() {
  try {
    await ElMessageBox.confirm(`确认支付 ¥${order.value.totalAmount}？\n（模拟支付，不会真正扣款）`, '确认支付', { type: 'info', confirmButtonText: '确认支付' })
    await api.put(`/orders/${route.params.id}/pay`)
    ElMessage.success('支付成功！')
    const res = await api.get(`/orders/${route.params.id}`)
    order.value = res.data
  } catch { /* ignore */ }
}

async function cancelOrder() {
  try {
    await ElMessageBox.confirm('确定取消此订单？取消后无法恢复。', '取消订单', { type: 'warning', confirmButtonText: '确定取消' })
    await api.put(`/orders/${route.params.id}/cancel`)
    ElMessage.success('订单已取消')
    // 刷新数据
    const res = await api.get(`/orders/${route.params.id}`)
    order.value = res.data
  } catch { /* ignore */ }
}
</script>
