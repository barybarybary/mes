<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <section class="relative gradient-hero text-white overflow-hidden">
      <div class="absolute inset-0 hero-grid-pattern opacity-30"></div>
      <div class="absolute top-20 -right-20 w-96 h-96 rounded-full bg-white/5 blur-3xl animate-float"></div>
      <div class="absolute -bottom-32 left-20 w-80 h-80 rounded-full bg-sky-300/10 blur-3xl animate-float float-card-delay-1"></div>

      <div class="max-w-7xl mx-auto px-4 sm:px-6 py-20 lg:py-28 relative z-10">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          <div class="animate-slide-up">
            <div class="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white/10 backdrop-blur-sm border border-white/20 mb-6">
              <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
              <span class="text-sm text-sky-100">智能制造 · 品质保障</span>
            </div>
            <h1 class="text-4xl lg:text-6xl font-bold leading-tight mb-6">
              造易 — 让制造
              <span class="relative inline-block">
                <span class="relative z-10 bg-gradient-to-r from-sky-200 to-white bg-clip-text text-transparent">变容易</span>
                <span class="absolute -bottom-2 left-0 right-0 h-3 bg-gradient-to-r from-sky-400/40 to-transparent rounded-full -skew-x-12"></span>
              </span>
            </h1>
            <p class="text-lg text-sky-100 mb-10 leading-relaxed max-w-xl">
              高品质制造一站式采购平台，为制造企业提供优质原材料、半成品与配件。
              实时追踪订单进度，让采购更简单、更高效、更透明。
            </p>
            <div class="flex flex-wrap gap-4">
              <router-link
                to="/portal/products"
                class="inline-flex items-center gap-2 bg-white text-sky-600 px-8 py-4 rounded-2xl font-bold text-lg hover:bg-sky-50 transition-all duration-300 shadow-2xl shadow-black/20 hover:shadow-3xl hover:-translate-y-1 transform no-underline group"
              >
                浏览产品
                <el-icon :size="20" class="group-hover:translate-x-1 transition-transform"><ArrowRight /></el-icon>
              </router-link>
              <a
                href="#"
                class="inline-flex items-center gap-2 px-8 py-4 rounded-2xl font-bold text-lg border-2 border-white/30 text-white hover:bg-white/10 hover:border-white/50 transition-all duration-300 no-underline backdrop-blur-sm"
              >
                <el-icon :size="20"><VideoPlay /></el-icon>
                了解更多
              </a>
            </div>

            <div class="flex items-center gap-8 mt-12 pt-8 border-t border-white/10">
              <div>
                <div class="text-3xl font-bold">10,000+</div>
                <div class="text-sm text-sky-200">产品种类</div>
              </div>
              <div class="w-px h-12 bg-white/20"></div>
              <div>
                <div class="text-3xl font-bold">5,000+</div>
                <div class="text-sm text-sky-200">合作企业</div>
              </div>
              <div class="w-px h-12 bg-white/20"></div>
              <div>
                <div class="text-3xl font-bold">99.8%</div>
                <div class="text-sm text-sky-200">准时交付率</div>
              </div>
            </div>
          </div>

          <div class="hidden lg:block relative">
            <div class="relative">
              <div class="absolute -top-4 -left-4 w-72 h-72 bg-sky-400/20 rounded-full blur-3xl"></div>
              <div class="absolute -bottom-4 -right-4 w-60 h-60 bg-blue-500/20 rounded-full blur-3xl"></div>

              <div class="relative bg-white/10 backdrop-blur-xl rounded-3xl border border-white/20 p-6 shadow-2xl float-card">
                <div class="flex items-center justify-between mb-5">
                  <span class="text-white/80 text-sm font-medium">实时订单追踪</span>
                  <span class="portal-badge portal-badge-success !bg-emerald-500/20 !text-emerald-200">
                    进行中
                  </span>
                </div>
                <div class="space-y-4">
                  <div class="flex items-center gap-4 p-3 rounded-xl bg-white/5">
                    <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-sky-400 to-blue-500 flex items-center justify-center">
                      <el-icon :size="24"><Box /></el-icon>
                    </div>
                    <div class="flex-1">
                      <div class="font-semibold text-white">精密齿轮组件</div>
                      <div class="text-sm text-sky-200">订单号: PO20260723001</div>
                    </div>
                    <div class="text-right">
                      <div class="font-bold text-white">¥12,580</div>
                      <div class="text-xs text-sky-200">生产中</div>
                    </div>
                  </div>
                  <div class="flex items-center gap-4 p-3 rounded-xl bg-white/5">
                    <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-emerald-400 to-teal-500 flex items-center justify-center">
                      <el-icon :size="24"><Tools /></el-icon>
                    </div>
                    <div class="flex-1">
                      <div class="font-semibold text-white">不锈钢板材</div>
                      <div class="text-sm text-sky-200">订单号: PO20260722008</div>
                    </div>
                    <div class="text-right">
                      <div class="font-bold text-white">¥8,960</div>
                      <div class="text-xs text-emerald-300">已发货</div>
                    </div>
                  </div>
                </div>
                <div class="mt-5 pt-5 border-t border-white/10">
                  <div class="flex justify-between text-sm mb-2">
                    <span class="text-sky-200">生产进度</span>
                    <span class="text-white font-semibold">68%</span>
                  </div>
                  <div class="h-2 bg-white/10 rounded-full overflow-hidden">
                    <div class="h-full w-2/3 bg-gradient-to-r from-sky-400 to-emerald-400 rounded-full"></div>
                  </div>
                </div>
              </div>

              <div class="absolute -bottom-6 -left-12 bg-white rounded-2xl shadow-2xl p-4 float-card float-card-delay-1">
                <div class="flex items-center gap-3">
                  <div class="w-12 h-12 rounded-full bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center text-white">
                    <el-icon :size="24"><Trophy /></el-icon>
                  </div>
                  <div>
                    <div class="font-bold text-slate-800">品质认证</div>
                    <div class="text-xs text-slate-500">ISO9001 / CE / RoHS</div>
                  </div>
                </div>
              </div>

              <div class="absolute -top-6 -right-6 bg-white rounded-2xl shadow-2xl p-4 float-card float-card-delay-2">
                <div class="flex items-center gap-3">
                  <div class="w-12 h-12 rounded-full bg-gradient-to-br from-rose-400 to-pink-500 flex items-center justify-center text-white">
                    <el-icon :size="24"><Service /></el-icon>
                  </div>
                  <div>
                    <div class="font-bold text-slate-800">专属客服</div>
                    <div class="text-xs text-slate-500">7x24小时在线</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="absolute bottom-0 left-0 right-0">
        <svg viewBox="0 0 1440 80" fill="none" xmlns="http://www.w3.org/2000/svg" class="w-full">
          <path d="M0 80L60 73.3C120 66.7 240 53.3 360 46.7C480 40 600 40 720 43.3C840 46.7 960 53.3 1080 56.7C1200 60 1320 60 1380 60L1440 60V80H1380C1320 80 1200 80 1080 80C960 80 840 80 720 80C600 80 480 80 360 80C240 80 120 80 60 80H0Z" fill="#f8fafc"/>
        </svg>
      </div>
    </section>

    <section class="bg-white py-16 -mt-1 relative z-10">
      <div class="max-w-7xl mx-auto px-4 sm:px-6">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-6">
          <div v-for="(feature, index) in coreFeatures" :key="index" class="text-center p-6 rounded-2xl hover:bg-slate-50 transition-all duration-300 group">
            <div class="w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-4 transition-transform duration-300 group-hover:scale-110 group-hover:-translate-y-1" :class="feature.bgClass">
              <el-icon :size="28" :class="feature.iconClass"><component :is="feature.icon" /></el-icon>
            </div>
            <h3 class="font-bold text-slate-800 mb-2">{{ feature.title }}</h3>
            <p class="text-sm text-slate-500 leading-relaxed">{{ feature.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <section class="bg-slate-50 py-4">
      <div class="max-w-7xl mx-auto px-4 sm:px-6">
        <div class="bg-white rounded-2xl p-6 shadow-soft">
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 rounded-xl bg-sky-50 flex items-center justify-center">
              <el-icon color="#0ea5e9" :size="20"><Grid /></el-icon>
            </div>
            <div>
              <h3 class="font-bold text-slate-800">产品分类</h3>
              <p class="text-xs text-slate-400">快速找到您需要的产品</p>
            </div>
          </div>
          <div class="flex flex-wrap gap-3">
            <button
              v-for="cat in categories"
              :key="cat.id"
              class="category-chip"
              :class="{ 'category-chip-active': activeCategory === cat.id }"
              @click="activeCategory = cat.id; fetchProducts()"
            >
              {{ cat.name }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <section class="flex-1 bg-slate-50 py-12">
      <div class="max-w-7xl mx-auto px-4 sm:px-6">
        <div class="flex items-end justify-between mb-10">
          <div>
            <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-sky-50 text-sky-600 text-sm font-medium mb-3">
              <span class="w-1.5 h-1.5 rounded-full bg-sky-500"></span>
              精选推荐
            </div>
            <h2 class="text-3xl font-bold text-slate-800">{{ activeCategoryName || '热门产品' }}</h2>
            <p class="text-slate-500 mt-2">严选优质产品，品质有保障</p>
          </div>
          <router-link
            to="/portal/products"
            class="hidden sm:inline-flex items-center gap-2 text-sky-500 hover:text-sky-600 font-semibold no-underline group"
          >
            查看全部
            <el-icon :size="16" class="group-hover:translate-x-1 transition-transform"><ArrowRight /></el-icon>
          </router-link>
        </div>

        <div v-if="loading" class="flex justify-center py-20">
          <div class="flex flex-col items-center gap-4">
            <el-icon class="animate-spin text-sky-500" :size="40"><Loading /></el-icon>
            <span class="text-slate-400 text-sm">加载中...</span>
          </div>
        </div>

        <div v-else-if="products.length > 0" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          <ProductCard
            v-for="(product, index) in products"
            :key="product.id"
            :product="product"
            :style="{ animationDelay: `${index * 0.1}s` }"
            class="animate-slide-up"
            @click="goDetail(product.id)"
            @add-to-cart="handleAddToCart(product)"
          />
        </div>

        <div v-else class="bg-white rounded-2xl py-20 text-center border border-slate-100">
          <div class="empty-state-icon mx-auto">
            <el-icon color="#cbd5e1" :size="32"><Box /></el-icon>
          </div>
          <p class="text-slate-400 mt-4">暂无产品数据</p>
        </div>

        <div class="mt-10 text-center sm:hidden">
          <router-link
            to="/portal/products"
            class="portal-btn-secondary"
          >
            查看全部产品
            <el-icon :size="16"><ArrowRight /></el-icon>
          </router-link>
        </div>
      </div>
    </section>

    <section class="bg-white py-20">
      <div class="max-w-7xl mx-auto px-4 sm:px-6">
        <div class="text-center mb-14">
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-50 text-emerald-600 text-sm font-medium mb-3">
            <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
            服务优势
          </div>
          <h2 class="text-3xl font-bold text-slate-800">为什么选择造易？</h2>
          <p class="text-slate-500 mt-3 max-w-2xl mx-auto">
            我们致力于为制造企业提供最优质的产品和服务，助力您的业务腾飞
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div v-for="(advantage, index) in advantages" :key="index" class="relative group">
            <div class="absolute inset-0 bg-gradient-to-br from-sky-50 to-transparent rounded-3xl opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
            <div class="relative p-8 rounded-3xl border border-slate-100 bg-white hover:border-sky-200 transition-all duration-500 hover:shadow-card-hover hover:-translate-y-2">
              <div class="w-16 h-16 rounded-2xl flex items-center justify-center mb-6 transition-transform duration-300 group-hover:scale-110 group-hover:-rotate-3" :class="advantage.bgClass">
                <el-icon :size="32" :class="advantage.iconClass"><component :is="advantage.icon" /></el-icon>
              </div>
              <h3 class="text-xl font-bold text-slate-800 mb-3">{{ advantage.title }}</h3>
              <p class="text-slate-500 leading-relaxed">{{ advantage.desc }}</p>
              <div class="mt-6 pt-6 border-t border-slate-100">
                <ul class="space-y-2">
                  <li v-for="(item, i) in advantage.points" :key="i" class="flex items-center gap-2 text-sm text-slate-600">
                    <el-icon color="#10b981" :size="16"><CircleCheckFilled /></el-icon>
                    {{ item }}
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="gradient-hero py-20 relative overflow-hidden">
      <div class="absolute inset-0 hero-grid-pattern opacity-20"></div>
      <div class="max-w-4xl mx-auto px-4 sm:px-6 text-center relative z-10">
        <h2 class="text-3xl lg:text-4xl font-bold text-white mb-4">准备好开启数字化采购之旅了吗？</h2>
        <p class="text-sky-100 text-lg mb-8">立即注册，享受专属会员价和优先发货服务</p>
        <div class="flex flex-wrap justify-center gap-4">
          <router-link
            to="/portal/register"
            class="inline-flex items-center gap-2 bg-white text-sky-600 px-8 py-4 rounded-2xl font-bold text-lg hover:bg-sky-50 transition-all duration-300 shadow-2xl shadow-black/20 hover:shadow-3xl hover:-translate-y-1 transform no-underline"
          >
            免费注册
            <el-icon :size="20"><ArrowRight /></el-icon>
          </router-link>
          <a
            href="#"
            class="inline-flex items-center gap-2 px-8 py-4 rounded-2xl font-bold text-lg border-2 border-white/30 text-white hover:bg-white/10 hover:border-white/50 transition-all duration-300 no-underline backdrop-blur-sm"
          >
            <el-icon :size="20"><Phone /></el-icon>
            400-888-8888
          </a>
        </div>
      </div>
    </section>

    <PortalFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { usePortalStore } from '@/stores/portal'
import { ElMessage } from 'element-plus'
import {
  Loading, Box, ArrowRight, VideoPlay, Tools, Trophy, Service, Grid,
  CircleCheckFilled, Phone, Clock, Headset, TrendCharts, Medal
} from '@element-plus/icons-vue'
import api from '@/api/portal'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'
import ProductCard from '@/components/ProductCard.vue'

const router = useRouter()
const portalStore = usePortalStore()

const products = ref([])
const loading = ref(false)
const activeCategory = ref('')
const categories = ref([{ id: '', name: '全部' }])

const activeCategoryName = computed(() => {
  return categories.value.find(c => c.id === activeCategory.value)?.name || '全部'
})

const coreFeatures = [
  { icon: markRaw(Box), title: '丰富产品', desc: '涵盖原材料、半成品、成品及配件', bgClass: 'bg-sky-50', iconClass: 'text-sky-500' },
  { icon: markRaw(CircleCheckFilled), title: '品质保证', desc: '严格质量管控，每批产品符合标准', bgClass: 'bg-emerald-50', iconClass: 'text-emerald-500' },
  { icon: markRaw(Clock), title: '实时追踪', desc: '在线下单，实时查看订单进度', bgClass: 'bg-amber-50', iconClass: 'text-amber-500' },
  { icon: markRaw(Headset), title: '专属服务', desc: '一对一客户经理，全程贴心服务', bgClass: 'bg-rose-50', iconClass: 'text-rose-500' },
]

const advantages = [
  {
    icon: markRaw(TrendCharts),
    title: '高效采购',
    desc: '一站式采购平台，简化采购流程，降低采购成本',
    bgClass: 'bg-gradient-to-br from-sky-100 to-blue-100',
    iconClass: 'text-sky-600',
    points: ['一站式采购', '价格透明', '快速报价', '批量优惠']
  },
  {
    icon: markRaw(Medal),
    title: '品质保障',
    desc: '严选优质供应商，每批产品经过严格质检',
    bgClass: 'bg-gradient-to-br from-emerald-100 to-teal-100',
    iconClass: 'text-emerald-600',
    points: ['优质供应商', '严格质检', '品质认证', '售后无忧']
  },
  {
    icon: markRaw(Service),
    title: '智能追踪',
    desc: '全流程数字化管理，实时掌握订单生产进度',
    bgClass: 'bg-gradient-to-br from-violet-100 to-purple-100',
    iconClass: 'text-violet-600',
    points: ['实时追踪', '生产可视化', '物流跟踪', '异常预警']
  },
]

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
    const params = { page: 1, pageSize: 8 }
    if (activeCategory.value) {
      params.categoryId = activeCategory.value
    }
    const res = await api.get('/products', { params })
    products.value = res.data?.list || []
  } catch { /* ignore */
    products.value = []
  } finally {
    loading.value = false
  }
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

onMounted(() => {
  fetchCategories()
  fetchProducts()
})
</script>
