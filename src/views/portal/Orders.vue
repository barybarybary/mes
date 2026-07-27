<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <main class="flex-1 max-w-5xl mx-auto px-4 sm:px-6 py-8 w-full">
      <div class="flex items-center gap-2 text-sm text-slate-400 mb-6">
        <router-link to="/portal" class="hover:text-sky-500 transition-colors no-underline">首页</router-link>
        <el-icon :size="12"><ArrowRight /></el-icon>
        <span class="text-slate-600">我的订单</span>
      </div>

      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-2xl md:text-3xl font-bold text-slate-800">我的订单</h1>
          <p class="text-slate-500 mt-1">查看和追踪您的所有订单</p>
        </div>
      </div>

      <div class="portal-card p-2 mb-6">
        <div class="flex gap-1 overflow-x-auto">
          <button
            v-for="tab in statusTabs"
            :key="tab.value"
            class="shrink-0 px-5 py-2.5 rounded-xl text-sm font-medium transition-all duration-200"
            :class="activeStatus === tab.value
              ? 'bg-gradient-to-r from-sky-500 to-blue-600 text-white shadow-md shadow-sky-500/25'
              : 'text-slate-600 hover:bg-slate-50 hover:text-slate-800'"
            @click="activeStatus = tab.value; page = 1; fetchOrders()"
          >
            {{ tab.label }}
          </button>
        </div>
      </div>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="flex flex-col items-center gap-4">
          <el-icon class="animate-spin text-sky-500" :size="40"><Loading /></el-icon>
          <span class="text-slate-400 text-sm">加载中...</span>
        </div>
      </div>

      <template v-else-if="orders.length > 0">
        <div class="space-y-5">
          <div
            v-for="order in orders"
            :key="order.id"
            class="portal-card overflow-hidden cursor-pointer hover:shadow-card transition-all duration-300 hover:-translate-y-0.5"
            @click="$router.push(`/portal/orders/${order.id}`)"
          >
            <div class="px-6 py-4 bg-slate-50/50 border-b border-slate-100 flex items-center justify-between">
              <div class="flex items-center gap-4">
                <span class="text-sm text-slate-400">订单号:</span>
                <span class="font-semibold text-slate-800">{{ order.orderNo }}</span>
                <span class="text-xs text-slate-400 hidden sm:inline">{{ order.createTime }}</span>
              </div>
              <OrderStatusTag :status="order.status" />
            </div>

            <div class="p-6">
              <div class="flex flex-col sm:flex-row gap-4">
                <div class="flex-1 min-w-0">
                  <div class="flex flex-wrap gap-2 mb-3">
                    <div
                      v-for="(item, index) in (order.items || []).slice(0, 3)"
                      :key="index"
                      class="w-14 h-14 rounded-lg overflow-hidden shrink-0"
                    >
                      <img
                        v-if="item.imageUrl"
                        :src="item.imageUrl"
                        class="w-full h-full object-cover"
                        :alt="item.productName"
                      />
                      <div
                        v-else
                        class="w-full h-full flex items-center justify-center relative overflow-hidden"
                        :style="{ background: itemGradient(item) }"
                      >
                        <div class="absolute inset-0 opacity-10">
                          <div class="absolute -top-2 -right-2 w-8 h-8 rounded-full bg-white"></div>
                          <div class="absolute -bottom-3 -left-3 w-10 h-10 rounded-full bg-white"></div>
                        </div>
                      </div>
                    </div>
                    <div
                      v-if="(order.items || []).length > 3"
                      class="w-14 h-14 rounded-lg bg-slate-100 flex items-center justify-center text-sm text-slate-500 font-medium shrink-0"
                    >
                      +{{ (order.items || []).length - 3 }}
                    </div>
                  </div>
                  <p class="text-sm text-slate-500 line-clamp-1">
                    {{ itemsSummary(order) }}
                  </p>
                </div>

                <div class="flex sm:flex-col items-end sm:items-end justify-between sm:justify-center gap-2 sm:gap-1 sm:min-w-[120px]">
                  <span class="text-sm text-slate-400 sm:text-right">共 {{ (order.items || []).reduce((s, i) => s + i.quantity, 0) }} 件商品</span>
                  <div class="flex items-baseline gap-1">
                    <span class="text-rose-500 text-sm">¥</span>
                    <span class="text-xl font-bold text-rose-500">{{ order.totalAmount?.toFixed(2) }}</span>
                  </div>
                </div>
              </div>

              <div class="flex items-center justify-between mt-5 pt-4 border-t border-slate-100">
                <span class="text-sm text-sky-500 hover:text-sky-600 font-medium flex items-center gap-1">
                  查看订单详情
                  <el-icon :size="14"><ArrowRight /></el-icon>
                </span>
                <div class="flex items-center gap-2">
                  <button
                    v-if="order.status === 1 || order.status === 2"
                    class="portal-btn-secondary !py-2 !px-4 !text-sm"
                    @click.stop="handleCancel(order)"
                  >
                    取消订单
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="total > pageSize" class="flex justify-center mt-10">
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            background
            @current-change="fetchOrders"
          />
        </div>
      </template>

      <div v-else class="portal-card py-20">
        <div class="empty-state">
          <div class="empty-state-icon w-24 h-24">
            <el-icon color="#cbd5e1" :size="40"><Document /></el-icon>
          </div>
          <h3 class="text-xl font-bold text-slate-700 mt-2">暂无订单</h3>
          <p class="text-slate-400 mt-2">快去挑选心仪的产品吧</p>
          <router-link to="/portal/products" class="portal-btn-primary mt-6">
            去选购
            <el-icon :size="16"><ArrowRight /></el-icon>
          </router-link>
        </div>
      </div>
    </main>

    <PortalFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Document, ArrowRight } from '@element-plus/icons-vue'
import api from '@/api/portal'
import { placeholderGradient } from '@/utils/placeholder'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'
import OrderStatusTag from '@/components/OrderStatusTag.vue'

const orders = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeStatus = ref('')

const statusTabs = [
  { value: '', label: '全部订单' },
  { value: '1', label: '待审核' },
  { value: '2', label: '已审核' },
  { value: '3', label: '生产中' },
  { value: '4', label: '部分发货' },
  { value: '5', label: '已完成' },
  { value: '6', label: '已取消' }
]

function itemGradient(item) {
  return placeholderGradient(item.productId)
}

function itemsSummary(order) {
  if (!order.items || order.items.length === 0) return ''
  return order.items.slice(0, 3).map(i => `${i.productName} ×${i.quantity}`).join('、') + (order.items.length > 3 ? '...' : '')
}

async function fetchOrders() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (activeStatus.value) {
      params.status = activeStatus.value
    }
    const res = await api.get('/orders', { params })
    orders.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch { /* ignore */
    orders.value = []
  } finally {
    loading.value = false
  }
}

async function handleCancel(order) {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '提示', { confirmButtonText: '确定取消', cancelButtonText: '再想想', type: 'warning' })
    await api.put(`/orders/${order.id}/cancel`)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (err) {
    if (err !== 'cancel' && err?.action !== 'cancel') {
      ElMessage.error('取消失败，请重试')
    }
  }
}

onMounted(() => { fetchOrders() })
</script>
