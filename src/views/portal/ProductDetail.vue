<template>
  <div class="min-h-screen bg-slate-50 dark:bg-slate-900">
    <PortalNavbar />
    <div class="max-w-4xl mx-auto px-4 py-8">
      <div v-if="loading" class="text-center py-10 text-slate-400 dark:text-slate-500">加载中...</div>
      <div v-else-if="!product" class="text-center py-10 text-slate-400 dark:text-slate-500">产品不存在</div>
      <div v-else class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border dark:border-slate-700 p-6 md:p-8">
        <div class="grid md:grid-cols-2 gap-8">
          <!-- 产品图 -->
          <div class="h-60 md:h-80 bg-gradient-to-br from-slate-100 to-slate-200 dark:from-slate-700 dark:to-slate-600 rounded-xl flex items-center justify-center text-6xl text-slate-400 dark:text-slate-500 overflow-hidden">
            <img v-if="product.imageUrl && !imageError" :src="product.imageUrl" :alt="product.name" class="w-full h-full object-cover" @error="imageError = true" />
            <span v-if="!product.imageUrl || imageError" class="text-6xl">📦</span>
          </div>
          <!-- 信息 -->
          <div>
            <p class="text-xs text-slate-400 dark:text-slate-500 mb-1">{{ product.code }}</p>
            <h1 class="text-2xl font-bold text-slate-800 dark:text-slate-200 mb-2">{{ product.name }}</h1>
            <p class="text-slate-500 dark:text-slate-400 mb-1">规格：{{ product.spec || '-' }}</p>
            <p class="text-slate-500 dark:text-slate-400 mb-1">单位：{{ product.unit }}</p>
            <p class="text-slate-500 dark:text-slate-400 mb-4">库存：{{ product.stockQuantity > 0 ? `${product.stockQuantity} ${product.unit || 'pcs'}` : '暂时缺货' }}</p>
            <div class="text-3xl font-bold text-sky-600 mb-6">¥{{ product.price }}</div>

            <div class="flex items-center gap-3 mb-6">
              <span class="text-sm text-slate-500 dark:text-slate-400">数量</span>
              <el-input-number v-model="qty" :min="1" :max="Math.max(product.stockQuantity, 1)" size="large" />
            </div>

            <el-button type="primary" size="large" class="w-full !rounded-xl" :disabled="product.stockQuantity <= 0" @click="addToCart">
              {{ product.stockQuantity > 0 ? '加入购物车' : '暂时缺货' }}
            </el-button>

            <p v-if="product.remark" class="mt-6 text-sm text-slate-500 dark:text-slate-400 leading-relaxed border-t dark:border-slate-700 pt-4">{{ product.remark }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api/portal'
import { usePortalStore } from '@/stores/portal'
import PortalNavbar from './PortalNavbar.vue'

const route = useRoute()
const router = useRouter()
const portalStore = usePortalStore()
const product = ref(null)
const qty = ref(1)
const loading = ref(true)
const imageError = ref(false)

onMounted(async () => {
  try {
    const res = await api.get(`/products/${route.params.id}`)
    product.value = res.data
  } catch { /* ignore */ }
  finally { loading.value = false }
})

async function addToCart() {
  if (!portalStore.isLoggedIn) { router.push('/portal/login'); return }
  try {
    await api.post('/cart/add', null, { params: { productId: product.value.id, quantity: qty.value } })
    portalStore.incrementCart(qty.value)
    ElMessage.success('已加入购物车')
    router.push('/portal/cart')
  } catch { /* ignore */ }
}
</script>
