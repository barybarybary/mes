<template>
  <div class="min-h-screen bg-slate-50">
    <!-- 顶部导航 -->
    <PortalNavbar />

    <!-- Hero -->
    <div class="bg-gradient-to-br from-sky-500 to-blue-700 text-white py-16 px-4">
      <div class="max-w-6xl mx-auto text-center">
        <h1 class="text-3xl md:text-4xl font-bold mb-3">造易 — 让制造变容易</h1>
        <p class="text-sky-100 text-lg mb-6">高品质制造，一站式采购</p>
        <router-link to="/portal/products" class="inline-block bg-white text-sky-600 px-8 py-3 rounded-xl font-semibold hover:bg-sky-50 transition">
          浏览产品 →
        </router-link>
      </div>
    </div>

    <!-- 产品列表 -->
    <div class="max-w-6xl mx-auto px-4 py-10">
      <h2 class="text-xl font-bold text-slate-800 mb-6">推荐产品</h2>
      <div v-if="loading" class="text-center py-10 text-slate-400">加载中...</div>
      <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        <ProductCard v-for="p in products" :key="p.id" :product="p" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/portal'
import PortalNavbar from './PortalNavbar.vue'
import ProductCard from './ProductCard.vue'

const products = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await api.get('/products', { params: { page: 1, pageSize: 8 } })
    products.value = res.data?.list || []
  } catch { /* ignore */ }
  finally { loading.value = false }
})
</script>
