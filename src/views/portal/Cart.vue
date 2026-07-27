<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <main class="flex-1 max-w-5xl mx-auto px-4 sm:px-6 py-8 w-full">
      <div class="flex items-center gap-2 text-sm text-slate-400 mb-6">
        <router-link to="/portal" class="hover:text-sky-500 transition-colors no-underline">首页</router-link>
        <el-icon :size="12"><ArrowRight /></el-icon>
        <span class="text-slate-600">购物车</span>
      </div>

      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-2xl md:text-3xl font-bold text-slate-800">我的购物车</h1>
          <p class="text-slate-500 mt-1">共 {{ totalCount }} 件商品</p>
        </div>
        <button
          v-if="items.length > 0"
          class="text-sm text-slate-400 hover:text-rose-500 transition-colors flex items-center gap-1"
          @click="handleClear"
        >
          <el-icon :size="16"><Delete /></el-icon>
          清空购物车
        </button>
      </div>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="flex flex-col items-center gap-4">
          <el-icon class="animate-spin text-sky-500" :size="40"><Loading /></el-icon>
          <span class="text-slate-400 text-sm">加载中...</span>
        </div>
      </div>

      <template v-else-if="items.length > 0">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div class="lg:col-span-2 space-y-4">
            <div
              v-for="item in items"
              :key="item.productId"
              class="portal-card p-5 flex items-center gap-5 hover:shadow-card transition-shadow"
            >
              <div
                class="w-24 h-24 rounded-xl bg-gradient-to-br from-slate-50 to-sky-50 flex items-center justify-center shrink-0 cursor-pointer overflow-hidden"
                @click="goDetail(item.productId)"
              >
                <el-icon color="#cbd5e1" :size="36"><Box /></el-icon>
              </div>

              <div class="flex-1 min-w-0">
                <h3
                  class="font-semibold text-slate-800 mb-1 truncate hover:text-sky-600 cursor-pointer transition-colors"
                  @click="goDetail(item.productId)"
                >{{ item.name }}</h3>
                <p class="text-xs text-slate-400 mb-2" v-if="item.spec">规格: {{ item.spec }}</p>
                <p class="text-lg font-bold text-rose-500">¥{{ item.price }} <span class="text-xs text-slate-400 font-normal">/ {{ item.unit || 'pcs' }}</span></p>
              </div>

              <div class="flex flex-col items-end gap-3">
                <div class="qty-selector">
                  <button
                    class="qty-btn"
                    :disabled="item.quantity <= 1"
                    @click="updateQty(item, item.quantity - 1)"
                  >-</button>
                  <span class="qty-input flex items-center justify-center">{{ item.quantity }}</span>
                  <button class="qty-btn" @click="updateQty(item, item.quantity + 1)">+</button>
                </div>
                <div class="text-right">
                  <div class="text-lg font-bold text-slate-800">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
                </div>
              </div>

              <button
                class="p-2 text-slate-300 hover:text-rose-500 hover:bg-rose-50 rounded-lg transition-all shrink-0"
                @click="removeItem(item)"
              >
                <el-icon :size="18"><Delete /></el-icon>
              </button>
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
                  <div class="flex justify-between items-baseline">
                    <span class="text-slate-600 font-medium">应付总额</span>
                    <div class="flex items-baseline gap-1">
                      <span class="text-rose-500 text-lg">¥</span>
                      <span class="text-3xl font-bold text-rose-500">{{ totalPrice.toFixed(2) }}</span>
                    </div>
                  </div>
                </div>

                <router-link
                  to="/portal/checkout"
                  class="portal-btn-primary w-full !justify-center"
                >
                  去结算
                  <el-icon :size="18"><ArrowRight /></el-icon>
                </router-link>

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

              <div class="mt-4 portal-card p-5 bg-gradient-to-br from-sky-50 to-blue-50 border-0">
                <div class="flex items-start gap-3">
                  <div class="w-10 h-10 rounded-xl bg-white flex items-center justify-center shrink-0">
                    <el-icon color="#0ea5e9" :size="20"><Present /></el-icon>
                  </div>
                  <div>
                    <h4 class="font-semibold text-slate-800 text-sm">新用户优惠</h4>
                    <p class="text-xs text-slate-500 mt-0.5">首单立减20元，满500可用</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <div v-else class="portal-card py-20">
        <div class="empty-state">
          <div class="empty-state-icon w-24 h-24">
            <el-icon color="#cbd5e1" :size="40"><ShoppingCart /></el-icon>
          </div>
          <h3 class="text-xl font-bold text-slate-700 mt-2">购物车是空的</h3>
          <p class="text-slate-400 mt-2">快去挑选心仪的产品吧</p>
          <router-link
            to="/portal/products" class="portal-btn-primary mt-6">
            去逛逛
            <el-icon :size="16"><ArrowRight /></el-icon>
          </router-link>
        </div>
      </div>
    </main>

    <PortalFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { usePortalStore } from '@/stores/portal'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Loading, ShoppingCart, Delete, ArrowRight, Wallet, CircleCheckFilled, Service, Present, Box
} from '@element-plus/icons-vue'
import api from '@/api/portal'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'

const router = useRouter()
const portalStore = usePortalStore()
const items = ref([])
const loading = ref(false)

const totalCount = computed(() => items.value.reduce((s, i) => s + i.quantity, 0))
const totalPrice = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0))

async function fetchCart() {
  loading.value = true
  try {
    const res = await api.get('/cart')
    items.value = res.data || []
    portalStore.cartCount = items.value.reduce((s, i) => s + i.quantity, 0)
  } catch { /* ignore */
    items.value = []
  } finally {
    loading.value = false
  }
}

async function updateQty(item, newQty) {
  if (newQty <= 0) {
    await removeItem(item)
    return
  }
  try {
    await api.put(`/cart/${item.productId}`, null, { params: { quantity: newQty } })
    item.quantity = newQty
    portalStore.fetchCartCount()
  } catch (err) { console.error(err) }
}

async function removeItem(item) {
  try {
    await api.put(`/cart/${item.productId}`, null, { params: { quantity: 0 } })
    items.value = items.value.filter(i => i.productId !== item.productId)
    portalStore.fetchCartCount()
    ElMessage.success('已删除')
  } catch (err) { console.error(err) }
}

async function handleClear() {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    for (const item of items.value) {
      await api.put(`/cart/${item.productId}`, null, { params: { quantity: 0 } })
    }
    items.value = []
    portalStore.cartCount = 0
    ElMessage.success('购物车已清空')
  } catch (err) { console.error(err) }
}

function goDetail(id) {
  router.push(`/portal/products/${id}`)
}

onMounted(() => { fetchCart() })
</script>
