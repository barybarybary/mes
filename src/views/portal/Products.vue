<template>
  <div class="min-h-screen bg-slate-50">
    <PortalNavbar />
    <div class="max-w-6xl mx-auto px-4 py-8">
      <!-- 搜索 + 分类 -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
        <h2 class="text-xl font-bold text-slate-800">产品中心</h2>
        <el-input v-model="keyword" placeholder="搜索产品..." :prefix-icon="Search" clearable class="max-w-xs" @input="fetchData" />
      </div>
      <!-- 分类标签 -->
      <div class="flex flex-wrap gap-2 mb-6">
        <button
          @click="categoryId = null; fetchData()"
          :class="!categoryId ? 'bg-sky-500 text-white' : 'bg-white text-slate-600 border-slate-200 hover:border-sky-300'"
          class="px-4 py-1.5 rounded-full text-sm border transition"
        >全部</button>
        <button
          v-for="cat in categories" :key="cat.id"
          @click="categoryId = cat.id; fetchData()"
          :class="categoryId === cat.id ? 'bg-sky-500 text-white' : 'bg-white text-slate-600 border-slate-200 hover:border-sky-300'"
          class="px-4 py-1.5 rounded-full text-sm border transition"
        >{{ cat.name }}</button>
      </div>
      <!-- 列表 -->
      <div v-if="loading" class="text-center py-10 text-slate-400">加载中...</div>
      <div v-else-if="products.length === 0" class="text-center py-10 text-slate-400">暂无产品</div>
      <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        <ProductCard v-for="p in products" :key="p.id" :product="p" />
      </div>
      <!-- 分页 -->
      <div class="flex justify-center mt-8">
        <el-pagination v-if="total > pageSize" background layout="prev, pager, next" :total="total" :page-size="pageSize" v-model:current-page="page" @current-change="fetchData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import api from '@/api/portal'
import PortalNavbar from './PortalNavbar.vue'
import ProductCard from './ProductCard.vue'

const products = ref([])
const categories = ref([])
const loading = ref(true)
const keyword = ref('')
const categoryId = ref(null)
const page = ref(1)
const total = ref(0)
const pageSize = 12

let timer = null
function fetchData() {
  clearTimeout(timer)
  timer = setTimeout(async () => {
    loading.value = true
    try {
      const params = { page: page.value, pageSize }
      if (keyword.value) params.keyword = keyword.value
      if (categoryId.value) params.categoryId = categoryId.value
      const res = await api.get('/products', { params })
      products.value = res.data?.list || []
      total.value = res.data?.total || 0
    } catch { /* ignore */ }
    finally { loading.value = false }
  }, 300)
}

onMounted(async () => {
  try {
    const res = await api.get('/categories')
    categories.value = res.data || []
  } catch { /* ignore */ }
  fetchData()
})
</script>
