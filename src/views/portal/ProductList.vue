<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <main class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 py-8 w-full">
      <div class="mb-8">
        <div class="flex items-center gap-2 text-sm text-slate-400 mb-3">
          <router-link to="/portal" class="hover:text-sky-500 transition-colors no-underline">首页</router-link>
          <el-icon :size="12"><ArrowRight /></el-icon>
          <span class="text-slate-600">产品中心</span>
        </div>
        <h1 class="text-3xl font-bold text-slate-800">全部产品</h1>
        <p class="text-slate-500 mt-2">浏览我们精心挑选的优质产品，满足您的采购需求</p>
      </div>

      <div class="flex gap-8">
        <aside class="hidden lg:block w-64 shrink-0">
          <div class="sticky top-24 space-y-6">
            <div class="portal-card p-6">
              <h4 class="font-bold text-slate-800 mb-4 flex items-center gap-2">
                <el-icon color="#0ea5e9" :size="18"><Search /></el-icon>
                搜索产品
              </h4>
              <el-input
                v-model="keyword"
                placeholder="输入关键词..."
                :prefix-icon="Search"
                clearable
                size="large"
                @input="onSearch"
                @clear="onSearch"
              />
            </div>

            <div class="portal-card p-6">
              <h4 class="font-bold text-slate-800 mb-4 flex items-center gap-2">
                <el-icon color="#0ea5e9" :size="18"><Menu /></el-icon>
                产品分类
              </h4>
              <div class="space-y-1">
                <button
                  v-for="cat in categories"
                  :key="cat.id"
                  class="w-full text-left px-4 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 flex items-center justify-between group"
                  :class="activeCategory === cat.id
                    ? 'bg-gradient-to-r from-sky-50 to-blue-50 text-sky-600'
                    : 'text-slate-600 hover:bg-slate-50 hover:text-slate-800'"
                  @click="activeCategory = cat.id; page = 1; fetchProducts()"
                >
                  <span>{{ cat.name }}</span>
                  <el-icon
                    :size="14"
                    :class="activeCategory === cat.id ? 'text-sky-500' : 'text-slate-300 group-hover:text-slate-400'"
                  ><ArrowRight /></el-icon>
                </button>
              </div>
            </div>

            <div class="portal-card p-6 bg-gradient-to-br from-sky-500 to-blue-600 text-white border-0">
              <h4 class="font-bold mb-2">需要帮助？</h4>
              <p class="text-sm text-sky-100 mb-4">专属客服为您提供一对一采购咨询服务</p>
              <div class="flex items-center gap-2 text-sm">
                <el-icon :size="18"><Phone /></el-icon>
                <span class="font-semibold">400-888-8888</span>
              </div>
            </div>
          </div>
        </aside>

        <div class="flex-1 min-w-0">
          <div class="lg:hidden mb-6 space-y-4">
            <el-input
              v-model="keyword"
              placeholder="搜索产品..."
              :prefix-icon="Search"
              clearable
              size="large"
              @input="onSearch"
              @clear="onSearch"
            />
            <div class="flex gap-2 overflow-x-auto pb-2 -mx-4 px-4">
              <button
                v-for="cat in categories"
                :key="cat.id"
                class="shrink-0 category-chip"
                :class="{ 'category-chip-active': activeCategory === cat.id }"
                @click="activeCategory = cat.id; page = 1; fetchProducts()"
              >{{ cat.name }}</button>
            </div>
          </div>

          <div class="flex items-center justify-between mb-6">
            <div class="text-sm text-slate-500">
              共 <span class="font-semibold text-slate-700">{{ total }}</span> 件产品
            </div>
            <div class="flex items-center gap-2">
              <span class="text-sm text-slate-400">排序:</span>
              <el-select v-model="sortBy" size="small" style="width: 120px;" @change="fetchProducts">
                <el-option label="默认排序" value="" />
                <el-option label="价格从低到高" value="price_asc" />
                <el-option label="价格从高到低" value="price_desc" />
              </el-select>
            </div>
          </div>

          <div v-if="loading" class="flex justify-center py-20">
            <div class="flex flex-col items-center gap-4">
              <el-icon class="animate-spin text-sky-500" :size="40"><Loading /></el-icon>
              <span class="text-slate-400 text-sm">加载中...</span>
            </div>
          </div>

          <div v-else-if="products.length > 0" class="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-3 gap-6">
            <ProductCard
              v-for="product in products"
              :key="product.id"
              :product="product"
              @click="goDetail(product.id)"
              @add-to-cart="handleAddToCart(product)"
            />
          </div>

          <div v-else class="portal-card py-20">
            <div class="empty-state">
              <div class="empty-state-icon">
                <el-icon color="#cbd5e1" :size="32"><Search /></el-icon>
              </div>
              <h3 class="text-lg font-semibold text-slate-700">未找到匹配的产品</h3>
              <p class="text-slate-400 mt-2">试试其他关键词或分类吧</p>
              <button
                class="portal-btn-primary mt-6 !py-2.5 !px-6 !text-sm"
                @click="keyword = ''; activeCategory = ''; fetchProducts()"
              >
                清除筛选
              </button>
            </div>
          </div>

          <div v-if="total > pageSize" class="flex justify-center mt-10">
            <el-pagination
              v-model:current-page="page"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              background
              @current-change="fetchProducts"
            />
          </div>
        </div>
      </div>
    </main>

    <PortalFooter />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { usePortalStore } from '@/stores/portal'
import { ElMessage } from 'element-plus'
import { Search, Loading, ArrowRight, Menu } from '@element-plus/icons-vue'
import api from '@/api/portal'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'
import ProductCard from '@/components/ProductCard.vue'

const router = useRouter()
const route = useRoute()
const portalStore = usePortalStore()

const products = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)
const keyword = ref(route.query.keyword || '')
const activeCategory = ref('')
const sortBy = ref('')
const categories = ref([{ id: '', name: '全部' }])

let searchTimer = null

function flattenTree(tree, result = []) {
  tree.forEach(node => {
    result.push({ id: node.id, name: node.name })
    if (node.children && node.children.length > 0) {
      flattenTree(node.children, result)
    }
  })
  return result
}

async function fetchCategories() {
  try {
    const res = await api.get('/categories')
    const tree = res.data || []
    categories.value = [{ id: '', name: '全部' }, ...flattenTree(tree)]
  } catch (err) { console.error(err) }
}

async function fetchProducts() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (activeCategory.value) {
      params.categoryId = activeCategory.value
    }
    if (keyword.value) {
      params.keyword = keyword.value
    }
    if (sortBy.value) {
      params.sortBy = sortBy.value
    }
    const res = await api.get('/products', { params })
    products.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch { /* ignore */
    products.value = []
  } finally {
    loading.value = false
  }
}

function onSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    router.replace({ query: { ...route.query, keyword: keyword.value || undefined } })
    fetchProducts()
  }, 400)
}

function goDetail(id) {
  router.push(`/portal/products/${id}`)
}

async function handleAddToCart(product) {
  if (!portalStore.token) {
    ElMessage.warning('请先登录')
    router.push('/portal/login')
    return
  }
  try {
    await portalStore.addToCart(product.id, 1)
    ElMessage.success(`"${product.name}" 已加入购物车`)
  } catch (err) { console.error(err) }
}

watch(() => route.query.keyword, (val) => {
  keyword.value = val || ''
  page.value = 1
  fetchProducts()
})

onMounted(() => {
  fetchCategories()
  fetchProducts()
})
</script>
