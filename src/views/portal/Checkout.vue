<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <main class="flex-1 max-w-5xl mx-auto px-4 sm:px-6 py-8 w-full">
      <div class="flex items-center gap-2 text-sm text-slate-400 mb-6">
        <router-link to="/portal" class="hover:text-sky-500 transition-colors no-underline">首页</router-link>
        <el-icon :size="12"><ArrowRight /></el-icon>
        <router-link to="/portal/cart" class="hover:text-sky-500 transition-colors no-underline">购物车</router-link>
        <el-icon :size="12"><ArrowRight /></el-icon>
        <span class="text-slate-600">确认订单</span>
      </div>

      <h1 class="text-2xl md:text-3xl font-bold text-slate-800 mb-8">确认订单</h1>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="flex flex-col items-center gap-4">
          <el-icon class="animate-spin text-sky-500" :size="40"><Loading /></el-icon>
          <span class="text-slate-400 text-sm">加载中...</span>
        </div>
      </div>

      <template v-else>
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div class="lg:col-span-2 space-y-6">
            <div class="portal-card p-6">
              <div class="flex items-center justify-between mb-5">
                <h3 class="font-bold text-slate-800 flex items-center gap-2">
                  <span class="w-7 h-7 rounded-lg bg-sky-100 text-sky-600 flex items-center justify-center text-sm font-bold">1</span>
                  收货信息
                </h3>
                <router-link to="/portal/profile" class="text-sm text-sky-500 hover:text-sky-600 no-underline flex items-center gap-1">
                  <el-icon :size="14"><Edit /></el-icon>
                  修改
                </router-link>
              </div>
              <div class="bg-slate-50 rounded-xl p-5">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                  <div class="flex items-start gap-2">
                    <el-icon color="#94a3b8" :size="18" class="mt-0.5 shrink-0"><OfficeBuilding /></el-icon>
                    <div>
                      <span class="text-slate-400">公司名称</span>
                      <p class="text-slate-700 font-medium mt-0.5">{{ customer.companyName || '-' }}</p>
                    </div>
                  </div>
                  <div class="flex items-start gap-2">
                    <el-icon color="#94a3b8" :size="18" class="mt-0.5 shrink-0"><User /></el-icon>
                    <div>
                      <span class="text-slate-400">联系人</span>
                      <p class="text-slate-700 font-medium mt-0.5">{{ customer.contactName || '-' }}</p>
                    </div>
                  </div>
                  <div class="flex items-start gap-2">
                    <el-icon color="#94a3b8" :size="18" class="mt-0.5 shrink-0"><Phone /></el-icon>
                    <div>
                      <span class="text-slate-400">联系电话</span>
                      <p class="text-slate-700 font-medium mt-0.5">{{ customer.phone || '-' }}</p>
                    </div>
                  </div>
                  <div class="flex items-start gap-2">
                    <el-icon color="#94a3b8" :size="18" class="mt-0.5 shrink-0"><Location /></el-icon>
                    <div>
                      <span class="text-slate-400">收货地址</span>
                      <p class="text-slate-700 font-medium mt-0.5">{{ customer.address || '-' }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="portal-card p-6">
              <h3 class="font-bold text-slate-800 flex items-center gap-2 mb-5">
                <span class="w-7 h-7 rounded-lg bg-sky-100 text-sky-600 flex items-center justify-center text-sm font-bold">2</span>
                订单明细
              </h3>
              <div class="space-y-3">
                <div
                  v-for="item in cartItems"
                  :key="item.productId"
                  class="flex items-center gap-4 p-4 bg-slate-50 rounded-xl hover:bg-slate-100/50 transition-colors"
                >
                  <div class="w-16 h-16 rounded-lg bg-gradient-to-br from-slate-100 to-sky-50 flex items-center justify-center shrink-0">
                    <el-icon color="#cbd5e1" :size="24"><Box /></el-icon>
                  </div>
                  <div class="flex-1 min-w-0">
                    <h4 class="font-medium text-slate-700 text-sm truncate">{{ item.name }}</h4>
                    <p class="text-xs text-slate-400 mt-0.5" v-if="item.spec">{{ item.spec }}</p>
                  </div>
                  <div class="text-right shrink-0">
                    <div class="text-sm font-medium text-slate-700">¥{{ item.price }} × {{ item.quantity }}</div>
                    <div class="text-base font-bold text-slate-800 mt-1">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="portal-card p-6">
              <h3 class="font-bold text-slate-800 flex items-center gap-2 mb-5">
                <span class="w-7 h-7 rounded-lg bg-sky-100 text-sky-600 flex items-center justify-center text-sm font-bold">3</span>
                订单备注
              </h3>
              <el-input
                v-model="remark"
                type="textarea"
                :rows="3"
                placeholder="如有特殊要求请备注（选填）"
              />
            </div>
          </div>

          <div class="lg:col-span-1">
            <div class="sticky top-24">
              <div class="portal-card p-6">
                <h3 class="font-bold text-slate-800 mb-5 flex items-center gap-2">
                  <el-icon color="#0ea5e9" :size="20"><Wallet /></el-icon>
                  订单摘要
                </h3>

                <div class="space-y-3 mb-5">
                  <div class="flex justify-between text-sm">
                    <span class="text-slate-500">商品数量</span>
                    <span class="text-slate-700 font-medium">{{ totalCount }} 件</span>
                  </div>
                  <div class="flex justify-between text-sm">
                    <span class="text-slate-500">商品总额</span>
                    <span class="text-slate-700 font-medium">¥{{ totalPrice.toFixed(2) }}</span>
                  </div>
                  <div class="flex justify-between text-sm">
                    <span class="text-slate-500">运费</span>
                    <span class="text-emerald-600 font-medium">免运费</span>
                  </div>
                </div>

                <div class="border-t border-slate-100 pt-5 mb-5">
                  <div class="flex justify-between items-baseline mb-1">
                    <span class="text-slate-600 font-medium">应付总额</span>
                    <div class="flex items-baseline gap-1">
                      <span class="text-rose-500 text-lg">¥</span>
                      <span class="text-3xl font-bold text-rose-500">{{ totalPrice.toFixed(2) }}</span>
                    </div>
                  </div>
                  <p class="text-xs text-slate-400 text-right">支付安全由银联保障</p>
                </div>

                <button
                  class="portal-btn-primary w-full !justify-center !py-4 !text-lg"
                  :disabled="submitting || cartItems.length === 0"
                  @click="handleSubmit"
                >
                  <el-icon v-if="submitting" class="animate-spin"><Loading /></el-icon>
                  <span v-else>提交订单</span>
                </button>

                <div class="mt-4 flex items-center justify-center gap-4 text-xs text-slate-400">
                  <span class="flex items-center gap-1">
                    <el-icon :size="14"><CircleCheckFilled /></el-icon>
                    安全支付
                  </span>
                  <span class="flex items-center gap-1">
                    <el-icon :size="14"><Service /></el-icon>
                    售后保障
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </main>

    <PortalFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { usePortalStore } from '@/stores/portal'
import { ElMessage } from 'element-plus'
import {
  Loading, ArrowRight, Edit, OfficeBuilding, User, Phone, Location,
  Wallet, CircleCheckFilled, Service, Box
} from '@element-plus/icons-vue'
import api from '@/api/portal'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'

const router = useRouter()
const portalStore = usePortalStore()

const cartItems = ref([])
const loading = ref(false)
const submitting = ref(false)
const remark = ref('')

const customer = computed(() => portalStore.customer || {})
const totalCount = computed(() => cartItems.value.reduce((s, i) => s + i.quantity, 0))
const totalPrice = computed(() => cartItems.value.reduce((s, i) => s + i.price * i.quantity, 0))

async function fetchCart() {
  loading.value = true
  try {
    const res = await api.get('/cart')
    cartItems.value = res.data || []
    if (cartItems.value.length === 0) {
      ElMessage.warning('购物车为空')
      router.push('/portal/cart')
    }
  } catch { /* ignore */
    cartItems.value = []
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车为空')
    return
  }
  submitting.value = true
  try {
    const items = cartItems.value.map(i => ({ productId: i.productId, quantity: i.quantity, price: i.price }))
    const res = await api.post('/orders', {
      items,
      remark: remark.value,
      receiverName: customer.value.contactName,
      receiverPhone: customer.value.phone,
      receiverAddress: customer.value.address
    })
    const order = res.data
    portalStore.cartCount = 0
    ElMessage.success('订单提交成功！')
    router.push(`/portal/orders/${order.id}`)
  } catch (err) { console.error(err) }
  finally {
    submitting.value = false
  }
}

onMounted(() => { fetchCart() })
</script>
