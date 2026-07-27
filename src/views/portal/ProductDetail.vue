<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <main class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 py-8 w-full">
      <div class="flex items-center gap-2 text-sm text-slate-400 mb-6">
        <router-link to="/portal" class="hover:text-sky-500 transition-colors no-underline">首页</router-link>
        <el-icon :size="12"><ArrowRight /></el-icon>
        <router-link to="/portal/products" class="hover:text-sky-500 transition-colors no-underline">产品中心</router-link>
        <el-icon :size="12"><ArrowRight /></el-icon>
        <span class="text-slate-600 truncate max-w-[200px]">{{ product?.name || '产品详情' }}</span>
      </div>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="flex flex-col items-center gap-4">
          <el-icon class="animate-spin text-sky-500" :size="40"><Loading /></el-icon>
          <span class="text-slate-400 text-sm">加载中...</span>
        </div>
      </div>

      <template v-else-if="product">
        <div class="portal-card overflow-hidden mb-8">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-0">
            <div class="relative aspect-square md:aspect-auto bg-gradient-to-br from-slate-50 via-sky-50 to-blue-50 flex items-center justify-center p-10">
              <div class="absolute top-5 left-5 flex flex-col gap-2">
                <span class="portal-badge portal-badge-primary">
                  {{ product.categoryName || '未分类' }}
                </span>
              </div>
              <img
                v-if="product.imageUrl"
                :src="product.imageUrl"
                :alt="product.name"
                class="max-w-full max-h-full object-contain rounded-2xl shadow-xl"
              />
              <div v-else class="flex flex-col items-center gap-4">
                <el-icon color="#cbd5e1" :size="120"><Box /></el-icon>
                <span class="text-slate-400">暂无产品图片</span>
              </div>
            </div>

            <div class="p-8 md:p-10 flex flex-col">
              <div class="mb-4">
                <span class="portal-badge portal-badge-success">
                  <el-icon :size="11"><CircleCheck /></el-icon>
                  正品保障
                </span>
                <span class="portal-badge portal-badge-primary ml-2">
                  <el-icon :size="11"><Van /></el-icon>
                  快速发货
                </span>
              </div>

              <h1 class="text-2xl md:text-3xl font-bold text-slate-800 mb-3 leading-tight">
                {{ product.name }}
              </h1>

              <div class="flex flex-wrap gap-x-6 gap-y-2 mb-6 text-sm text-slate-500">
                <span class="flex items-center gap-1.5">
                  <span class="text-slate-400">产品编码:</span>
                  <span class="text-slate-700 font-medium">{{ product.code || '-' }}</span>
                </span>
                <span v-if="product.spec" class="flex items-center gap-1.5">
                  <span class="text-slate-400">规格:</span>
                  <span class="text-slate-700 font-medium">{{ product.spec }}</span>
                </span>
                <span class="flex items-center gap-1.5">
                  <span class="text-slate-400">单位:</span>
                  <span class="text-slate-700 font-medium">{{ product.unit || 'pcs' }}</span>
                </span>
              </div>

              <div class="bg-gradient-to-r from-rose-50 to-pink-50 rounded-2xl p-6 mb-6 -mx-2">
                <div class="flex items-baseline gap-2 mb-2">
                  <span class="text-rose-500 text-lg font-semibold">¥</span>
                  <span class="text-4xl md:text-5xl font-bold text-rose-500 leading-none">{{ product.price }}</span>
                  <span class="text-slate-400 text-sm ml-2">/ {{ product.unit || 'pcs' }}</span>
                </div>
                <div class="flex items-center gap-4 text-sm">
                  <span class="text-slate-400 line-through">¥{{ (product.price * 1.2).toFixed(2) }}</span>
                  <span class="portal-badge portal-badge-danger">限时优惠</span>
                </div>
              </div>

              <div class="mb-6">
                <span v-if="product.stockQty > 0" class="inline-flex items-center gap-2 text-sm text-emerald-600 bg-emerald-50 px-4 py-2 rounded-xl font-medium">
                  <el-icon :size="18"><CircleCheckFilled /></el-icon>
                  库存充足 ({{ product.stockQty }} {{ product.unit || 'pcs' }})
                </span>
                <span v-else class="inline-flex items-center gap-2 text-sm text-slate-400 bg-slate-50 px-4 py-2 rounded-xl">
                  <el-icon :size="18"><Warning /></el-icon>
                  暂时缺货
                </span>
              </div>

              <div class="flex items-center gap-4 mb-8">
                <span class="text-sm font-medium text-slate-600">数量:</span>
                <div class="qty-selector">
                  <button
                    class="qty-btn"
                    :class="{ 'cursor-not-allowed opacity-40': quantity <= 1 }"
                    :disabled="quantity <= 1"
                    @click="quantity--"
                  >-</button>
                  <input
                    v-model.number="quantity"
                    class="qty-input"
                    min="1"
                    type="number"
                  />
                  <button class="qty-btn" @click="quantity++">+</button>
                </div>
                <span class="text-sm text-slate-400 ml-auto">
                  小计: <span class="text-xl font-bold text-rose-500">¥{{ (product.price * quantity).toFixed(2) }}</span>
                </span>
              </div>

              <div class="flex flex-wrap gap-4 mt-auto">
                <button
                  class="portal-btn-primary flex-1 min-w-[160px]"
                  :disabled="!product.stockQty || addingToCart"
                  @click="handleAddToCart"
                >
                  <el-icon v-if="addingToCart" class="animate-spin"><Loading /></el-icon>
                  <el-icon v-else :size="18"><ShoppingCart /></el-icon>
                  <span>{{ addingToCart ? '添加中...' : '加入购物车' }}</span>
                </button>
                <button
                  class="portal-btn-secondary flex-1 min-w-[160px]"
                  :disabled="!product.stockQty"
                  @click="handleBuyNow"
                >
                  <el-icon :size="18"><CreditCard /></el-icon>
                  立即购买
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="product.remark" class="portal-card p-8 mb-8">
          <h3 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
            <span class="w-1 h-5 bg-gradient-to-b from-sky-500 to-blue-600 rounded-full"></span>
            产品说明
          </h3>
          <div class="text-slate-600 leading-relaxed whitespace-pre-line">
            {{ product.remark }}
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div class="portal-card p-6 flex items-center gap-4">
            <div class="w-12 h-12 rounded-xl bg-sky-50 flex items-center justify-center shrink-0">
              <el-icon color="#0ea5e9" :size="24"><CircleCheckFilled /></el-icon>
            </div>
            <div>
              <h4 class="font-semibold text-slate-800">品质保证</h4>
              <p class="text-sm text-slate-500">严格质检，正品保障</p>
            </div>
          </div>
          <div class="portal-card p-6 flex items-center gap-4">
            <div class="w-12 h-12 rounded-xl bg-emerald-50 flex items-center justify-center shrink-0">
              <el-icon color="#10b981" :size="24"><Van /></el-icon>
            </div>
            <div>
              <h4 class="font-semibold text-slate-800">快速发货</h4>
              <p class="text-sm text-slate-500">48小时内发货</p>
            </div>
          </div>
          <div class="portal-card p-6 flex items-center gap-4">
            <div class="w-12 h-12 rounded-xl bg-amber-50 flex items-center justify-center shrink-0">
              <el-icon color="#f59e0b" :size="24"><Service /></el-icon>
            </div>
            <div>
              <h4 class="font-semibold text-slate-800">售后无忧</h4>
              <p class="text-sm text-slate-500">7天无理由退换</p>
            </div>
          </div>
        </div>
      </template>

      <div v-else class="portal-card py-20">
        <div class="empty-state">
          <div class="empty-state-icon">
            <el-icon color="#cbd5e1" :size="32"><Warning /></el-icon>
          </div>
          <h3 class="text-lg font-semibold text-slate-700">产品不存在</h3>
          <p class="text-slate-400 mt-2">您访问的产品可能已下架或不存在</p>
          <router-link to="/portal/products" class="portal-btn-primary mt-6">
            返回产品列表
          </router-link>
        </div>
      </div>
    </main>

    <PortalFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePortalStore } from '@/stores/portal'
import { ElMessage } from 'element-plus'
import {
  Loading, Warning, ArrowRight, CircleCheck, CircleCheckFilled,
  ShoppingCart, Van, Service, CreditCard
} from '@element-plus/icons-vue'
import api from '@/api/portal'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'

const route = useRoute()
const router = useRouter()
const portalStore = usePortalStore()

const product = ref(null)
const loading = ref(false)
const quantity = ref(1)
const addingToCart = ref(false)

async function fetchProduct() {
  loading.value = true
  try {
    const res = await api.get(`/products/${route.params.id}`)
    product.value = res.data
  } catch { /* ignore */
    product.value = null
  } finally {
    loading.value = false
  }
}

async function handleAddToCart() {
  if (!portalStore.token) {
    ElMessage.warning('请先登录')
    router.push('/portal/login')
    return
  }
  addingToCart.value = true
  try {
    await portalStore.addToCart(product.value.id, quantity.value)
    ElMessage.success(`"${product.value.name}" ×${quantity.value} 已加入购物车`)
  } catch (err) { console.error(err) }
  finally {
    addingToCart.value = false
  }
}

async function handleBuyNow() {
  if (!portalStore.token) {
    ElMessage.warning('请先登录')
    router.push('/portal/login')
    return
  }
  addingToCart.value = true
  try {
    await portalStore.addToCart(product.value.id, quantity.value)
    router.push('/portal/checkout')
  } catch (err) { console.error(err) }
  finally {
    addingToCart.value = false
  }
}

onMounted(() => {
  fetchProduct()
})
</script>
