<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <main class="flex-1 max-w-5xl mx-auto px-4 sm:px-6 py-8 w-full">
      <div class="flex items-center gap-2 text-sm text-slate-400 mb-6">
        <router-link to="/portal" class="hover:text-sky-500 transition-colors no-underline">首页</router-link>
        <el-icon :size="12"><ArrowRight /></el-icon>
        <router-link to="/portal/orders" class="hover:text-sky-500 transition-colors no-underline">我的订单</router-link>
        <el-icon :size="12"><ArrowRight /></el-icon>
        <span class="text-slate-600">订单详情</span>
      </div>

      <router-link
        to="/portal/orders"
        class="inline-flex items-center gap-1 text-sm text-slate-500 hover:text-sky-500 mb-6 no-underline transition-colors"
      >
        <el-icon :size="16"><ArrowLeft /></el-icon>
        返回订单列表
      </router-link>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="flex flex-col items-center gap-4">
          <el-icon class="animate-spin text-sky-500" :size="40"><Loading /></el-icon>
          <span class="text-slate-400 text-sm">加载中...</span>
        </div>
      </div>

      <template v-else-if="order">
        <div class="portal-card p-6 mb-6 bg-gradient-to-r from-slate-800 to-slate-900 text-white border-0 overflow-hidden relative">
          <div class="absolute top-0 right-0 w-64 h-64 bg-sky-500/10 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2"></div>
          <div class="relative z-10 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
              <div class="flex items-center gap-3 mb-2">
                <h1 class="text-xl md:text-2xl font-bold">{{ order.orderNo }}</h1>
                <OrderStatusTag :status="order.status" />
              </div>
              <div class="flex flex-wrap gap-x-6 gap-y-1 text-sm text-slate-400">
                <span>下单时间: {{ order.createTime }}</span>
                <span v-if="order.deliveryNo">发货单号: {{ order.deliveryNo }}</span>
              </div>
            </div>
            <div class="text-right">
              <div class="text-sm text-slate-400 mb-1">订单金额</div>
              <div class="flex items-baseline gap-1 justify-end">
                <span class="text-rose-400 text-lg">¥</span>
                <span class="text-3xl md:text-4xl font-bold text-rose-400">{{ order.totalAmount?.toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="portal-card p-6 mb-6">
          <h3 class="font-bold text-slate-800 mb-6 flex items-center gap-2">
            <span class="w-1 h-5 bg-gradient-to-b from-sky-500 to-blue-600 rounded-full"></span>
            订单进度
          </h3>
          <el-steps :active="activeStep" align-center finish-status="success">
            <el-step title="已下单" description="订单已提交" />
            <el-step title="生产中" description="正在生产" />
            <el-step title="已发货" description="运输途中" />
            <el-step title="已完成" description="订单完成" />
          </el-steps>
        </div>

        <div class="portal-card p-6 mb-6">
          <h3 class="font-bold text-slate-800 mb-5 flex items-center gap-2">
            <span class="w-1 h-5 bg-gradient-to-b from-sky-500 to-blue-600 rounded-full"></span>
            收货信息
          </h3>
          <div class="bg-slate-50 rounded-xl p-5 grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
            <div class="flex items-start gap-2" v-if="order.companyName">
              <el-icon color="#94a3b8" :size="18" class="mt-0.5 shrink-0"><OfficeBuilding /></el-icon>
              <div>
                <span class="text-slate-400">公司名称</span>
                <p class="text-slate-700 font-medium mt-0.5">{{ order.companyName }}</p>
              </div>
            </div>
            <div class="flex items-start gap-2" v-if="order.contactName">
              <el-icon color="#94a3b8" :size="18" class="mt-0.5 shrink-0"><User /></el-icon>
              <div>
                <span class="text-slate-400">联系人</span>
                <p class="text-slate-700 font-medium mt-0.5">{{ order.contactName }}</p>
              </div>
            </div>
            <div class="flex items-start gap-2" v-if="order.phone">
              <el-icon color="#94a3b8" :size="18" class="mt-0.5 shrink-0"><Phone /></el-icon>
              <div>
                <span class="text-slate-400">联系电话</span>
                <p class="text-slate-700 font-medium mt-0.5">{{ order.phone }}</p>
              </div>
            </div>
            <div class="flex items-start gap-2 md:col-span-2" v-if="order.address">
              <el-icon color="#94a3b8" :size="18" class="mt-0.5 shrink-0"><Location /></el-icon>
              <div>
                <span class="text-slate-400">收货地址</span>
                <p class="text-slate-700 font-medium mt-0.5">{{ order.address }}</p>
              </div>
            </div>
            <div v-if="!order.companyName && !order.contactName && !order.phone && !order.address"
                 class="md:col-span-2 text-slate-400 text-sm">暂无收货信息</div>
          </div>
        </div>

        <div class="portal-card p-6 mb-6">
          <h3 class="font-bold text-slate-800 mb-5 flex items-center gap-2">
            <span class="w-1 h-5 bg-gradient-to-b from-sky-500 to-blue-600 rounded-full"></span>
            订单明细
          </h3>
          <div class="overflow-x-auto">
            <el-table :data="order.items || []" stripe style="width: 100%" :border="false">
              <el-table-column prop="productName" label="产品名称" min-width="180">
                <template #default="{ row }">
                  <div class="flex items-center gap-3 py-2">
                    <div class="w-12 h-12 rounded-lg overflow-hidden shrink-0">
                      <img
                        v-if="row.imageUrl"
                        :src="row.imageUrl"
                        class="w-full h-full object-cover"
                        :alt="row.productName"
                      />
                      <div
                        v-else
                        class="w-full h-full flex items-center justify-center relative overflow-hidden"
                        :style="{ background: placeholderGradient(row.productId) }"
                      >
                        <div class="absolute inset-0 opacity-10">
                          <div class="absolute -top-2 -right-2 w-7 h-7 rounded-full bg-white"></div>
                          <div class="absolute -bottom-2 -left-2 w-9 h-9 rounded-full bg-white"></div>
                        </div>
                      </div>
                    </div>
                    <span class="font-medium text-slate-700">{{ row.productName }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="productCode" label="产品编码" width="130" />
              <el-table-column prop="quantity" label="数量" width="80" align="center" />
              <el-table-column prop="unit" label="单位" width="80" align="center" />
              <el-table-column prop="price" label="单价" width="110" align="right">
                <template #default="{ row }">
                  <span class="text-slate-700">¥{{ row.price?.toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="小计" width="130" align="right">
                <template #default="{ row }">
                  <span class="font-semibold text-slate-800">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="flex justify-end mt-6 pt-5 border-t border-slate-100">
            <div class="text-right">
              <div class="flex items-center justify-end gap-3 mb-2">
                <span class="text-slate-500">商品总额:</span>
                <span class="text-slate-700 font-medium">¥{{ order.totalAmount?.toFixed(2) }}</span>
              </div>
              <div class="flex items-center justify-end gap-3 mb-2">
                <span class="text-slate-500">运费:</span>
                <span class="text-emerald-600 font-medium">免运费</span>
              </div>
              <div class="flex items-center justify-end gap-3 mt-3 pt-3 border-t border-slate-100">
                <span class="text-slate-600 font-medium text-base">应付总额:</span>
                <div class="flex items-baseline gap-1">
                  <span class="text-rose-500 text-lg">¥</span>
                  <span class="text-2xl font-bold text-rose-500">{{ order.totalAmount?.toFixed(2) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="order.remark" class="portal-card p-6">
          <h3 class="font-bold text-slate-800 mb-3 flex items-center gap-2">
            <span class="w-1 h-5 bg-gradient-to-b from-sky-500 to-blue-600 rounded-full"></span>
            订单备注
          </h3>
          <p class="text-slate-600 leading-relaxed bg-slate-50 rounded-xl p-4">{{ order.remark }}</p>
        </div>
      </template>

      <div v-else class="portal-card py-20">
        <div class="empty-state">
          <div class="empty-state-icon">
            <el-icon color="#cbd5e1" :size="32"><Warning /></el-icon>
          </div>
          <h3 class="text-lg font-semibold text-slate-700">订单不存在</h3>
          <p class="text-slate-400 mt-2">您访问的订单可能不存在或已被删除</p>
          <router-link to="/portal/orders" class="portal-btn-primary mt-6">
            返回订单列表
          </router-link>
        </div>
      </div>
    </main>

    <PortalFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Loading, Warning, ArrowRight, ArrowLeft, OfficeBuilding, User, Phone, Location } from '@element-plus/icons-vue'
import api from '@/api/portal'
import { placeholderGradient } from '@/utils/placeholder'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'
import OrderStatusTag from '@/components/OrderStatusTag.vue'

const route = useRoute()
const order = ref(null)
const loading = ref(false)

const activeStep = computed(() => {
  if (!order.value) return 0
  const status = order.value.status
  if (status === 1 || status === 2) return 0
  if (status === 3) return 1
  if (status === 4) return 2
  if (status === 5) return 3
  return 0
})

async function fetchOrder() {
  loading.value = true
  try {
    const res = await api.get(`/orders/${route.params.id}`)
    order.value = res.data
  } catch { /* ignore */
    order.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => { fetchOrder() })
</script>
