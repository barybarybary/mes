<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <main class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 py-8 w-full">
      <div class="mb-8">
        <div class="flex items-center gap-2 text-sm text-slate-400 mb-3">
          <router-link to="/portal" class="hover:text-sky-500 transition-colors no-underline">首页</router-link>
          <el-icon :size="12"><ArrowRight /></el-icon>
          <span class="text-slate-600">新闻动态</span>
        </div>
        <h1 class="text-3xl font-bold text-slate-800">新闻动态</h1>
        <p class="text-slate-500 mt-2">了解造易科技最新资讯与行业动态</p>
      </div>

      <div class="flex gap-3 mb-8 flex-wrap">
        <button
          v-for="cat in categories"
          :key="cat"
          class="px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200"
          :class="activeCategory === cat ? 'bg-sky-500 text-white shadow-md shadow-sky-500/25' : 'bg-white text-slate-600 hover:bg-sky-50 hover:text-sky-500 border border-slate-200'"
          @click="activeCategory = cat"
        >{{ cat }}</button>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-12">
        <article v-for="(news, idx) in newsList" :key="idx" class="portal-card overflow-hidden group cursor-pointer hover:shadow-card-hover hover:-translate-y-1 transition-all duration-300">
          <div class="h-48 flex items-center justify-center relative overflow-hidden" :class="news.bgClass">
            <el-icon :size="56" :class="news.iconClass"><component :is="news.icon" /></el-icon>
            <span class="absolute top-4 left-4 px-3 py-1 rounded-full text-xs font-medium bg-white/20 backdrop-blur-sm text-white">{{ news.tag }}</span>
          </div>
          <div class="p-6">
            <div class="flex items-center gap-3 text-xs text-slate-400 mb-3">
              <span class="flex items-center gap-1"><el-icon :size="14"><Clock /></el-icon> {{ news.date }}</span>
              <span class="flex items-center gap-1"><el-icon :size="14"><View /></el-icon> {{ news.views }}</span>
            </div>
            <h3 class="text-lg font-bold text-slate-800 mb-2 group-hover:text-sky-600 transition-colors line-clamp-2">{{ news.title }}</h3>
            <p class="text-sm text-slate-500 leading-relaxed line-clamp-2 mb-4">{{ news.summary }}</p>
            <span class="text-sky-500 text-sm font-medium flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
              阅读全文 <el-icon :size="14"><ArrowRight /></el-icon>
            </span>
          </div>
        </article>
      </div>

      <div class="flex justify-center">
        <div class="flex items-center gap-2">
          <button class="w-10 h-10 rounded-xl border border-slate-200 flex items-center justify-center text-slate-400 hover:border-sky-300 hover:text-sky-500 transition-colors disabled:opacity-30" disabled>
            <el-icon :size="16"><ArrowLeft /></el-icon>
          </button>
          <button class="w-10 h-10 rounded-xl bg-sky-500 text-white font-medium shadow-md shadow-sky-500/25">1</button>
          <button class="w-10 h-10 rounded-xl border border-slate-200 flex items-center justify-center text-slate-600 hover:border-sky-300 hover:text-sky-500 transition-colors">2</button>
          <button class="w-10 h-10 rounded-xl border border-slate-200 flex items-center justify-center text-slate-600 hover:border-sky-300 hover:text-sky-500 transition-colors">3</button>
          <span class="text-slate-400 px-2">...</span>
          <button class="w-10 h-10 rounded-xl border border-slate-200 flex items-center justify-center text-slate-600 hover:border-sky-300 hover:text-sky-500 transition-colors">8</button>
          <button class="w-10 h-10 rounded-xl border border-slate-200 flex items-center justify-center text-slate-600 hover:border-sky-300 hover:text-sky-500 transition-colors">
            <el-icon :size="16"><ArrowRight /></el-icon>
          </button>
        </div>
      </div>
    </main>

    <PortalFooter />
  </div>
</template>

<script setup>
import { ref, markRaw } from 'vue'
import { ArrowRight, ArrowLeft, Clock, View, Trophy, TrendCharts, Connection, DataAnalysis, Setting } from '@element-plus/icons-vue'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'

const activeCategory = ref('全部')

const categories = ['全部', '公司新闻', '产品动态', '行业资讯', '活动展会']

const newsList = [
  {
    title: '造易科技完成C轮融资，加速智能制造全球化布局',
    summary: '本轮融资将用于加大AI技术研发投入，拓展海外市场，打造国际领先的智能制造平台。',
    date: '2026-07-15', views: '2.3k', tag: '公司新闻',
    bgClass: 'bg-gradient-to-br from-sky-400 to-blue-500', iconClass: 'text-white',
    icon: markRaw(Trophy)
  },
  {
    title: '造易MES V4.0正式发布：AI大模型驱动的下一代智能工厂',
    summary: '新版本深度融合AI大模型能力，实现智能排产、预测性维护、AI质检等突破性功能。',
    date: '2026-06-28', views: '5.1k', tag: '产品动态',
    bgClass: 'bg-gradient-to-br from-emerald-400 to-teal-500', iconClass: 'text-white',
    icon: markRaw(TrendCharts)
  },
  {
    title: '携手华为云，造易科技推出制造业云原生解决方案',
    summary: '双方将在云计算、大数据、AI等领域深度合作，为制造企业提供更稳定可靠的云端服务。',
    date: '2026-06-10', views: '1.8k', tag: '公司新闻',
    bgClass: 'bg-gradient-to-br from-violet-400 to-purple-500', iconClass: 'text-white',
    icon: markRaw(Connection)
  },
  {
    title: '2026年中国智能制造发展白皮书发布：MES应用趋势解读',
    summary: '报告显示，中小制造企业MES渗透率持续提升，云端部署成为主流选择。',
    date: '2026-05-22', views: '3.6k', tag: '行业资讯',
    bgClass: 'bg-gradient-to-br from-amber-400 to-orange-500', iconClass: 'text-white',
    icon: markRaw(DataAnalysis)
  },
  {
    title: '造易科技荣获"2026年度最佳智能制造解决方案商"大奖',
    summary: '在由中国工业经济联合会主办的评选中，造易科技从数百家企业中脱颖而出。',
    date: '2026-05-08', views: '4.2k', tag: '公司新闻',
    bgClass: 'bg-gradient-to-br from-rose-400 to-pink-500', iconClass: 'text-white',
    icon: markRaw(Trophy)
  },
  {
    title: '造易科技亮相2026上海国际工业博览会，展示AI+制造创新成果',
    summary: '展会期间，造易科技展示了AI质检、智能排产等最新技术应用，吸引众多参观者驻足。',
    date: '2026-04-18', views: '2.9k', tag: '活动展会',
    bgClass: 'bg-gradient-to-br from-cyan-400 to-blue-500', iconClass: 'text-white',
    icon: markRaw(Setting)
  },
]
</script>
