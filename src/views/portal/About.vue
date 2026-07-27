<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <main class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 py-8 w-full">
      <div class="mb-8">
        <div class="flex items-center gap-2 text-sm text-slate-400 mb-3">
          <router-link to="/portal" class="hover:text-sky-500 transition-colors no-underline">首页</router-link>
          <el-icon :size="12"><ArrowRight /></el-icon>
          <span class="text-slate-600">公司介绍</span>
        </div>
        <h1 class="text-3xl font-bold text-slate-800">关于造易</h1>
        <p class="text-slate-500 mt-2">让制造变容易 — 智能制造领域的创新引领者</p>
      </div>

      <div class="relative rounded-3xl overflow-hidden mb-12 p-10 lg:p-16" style="background: linear-gradient(135deg, #0c1929 0%, #134e7a 50%, #0a1628 100%);">
        <div class="absolute inset-0 opacity-10" style="background-image: radial-gradient(circle at 30% 50%, rgba(14, 165, 233, 0.4) 0%, transparent 50%), radial-gradient(circle at 70% 50%, rgba(16, 185, 129, 0.3) 0%, transparent 50%);"></div>
        <div class="relative z-10 max-w-3xl">
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 text-sky-200 text-sm mb-6 border border-white/10">
            <span class="w-2 h-2 rounded-full bg-emerald-400"></span>
            造易科技
          </div>
          <h2 class="text-3xl lg:text-4xl font-bold text-white mb-6">以技术驱动制造升级</h2>
          <p class="text-sky-100/80 text-lg leading-relaxed">
            造易科技成立于2018年，总部位于上海，是一家专注于制造业数字化转型的高新技术企业。我们致力于通过先进的软件技术和深厚的行业经验，帮助制造企业实现从传统制造向智能制造的跨越。
          </p>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-12">
        <div class="portal-card p-8">
          <h3 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
            <el-icon color="#0ea5e9" :size="22"><Aim /></el-icon>
            使命与愿景
          </h3>
          <div class="space-y-5">
            <div class="p-5 rounded-2xl bg-sky-50 border border-sky-100">
              <div class="text-sm text-sky-500 font-semibold mb-1">我们的使命</div>
              <p class="text-slate-700">用数字化技术赋能每一家制造企业，让制造业变得更高效、更智能、更可持续。</p>
            </div>
            <div class="p-5 rounded-2xl bg-emerald-50 border border-emerald-100">
              <div class="text-sm text-emerald-500 font-semibold mb-1">我们的愿景</div>
              <p class="text-slate-700">成为全球领先的智能制造解决方案提供商，推动中国制造走向世界。</p>
            </div>
            <div class="p-5 rounded-2xl bg-amber-50 border border-amber-100">
              <div class="text-sm text-amber-500 font-semibold mb-1">核心价值观</div>
              <p class="text-slate-700">客户至上、创新驱动、品质为先、合作共赢。</p>
            </div>
          </div>
        </div>
        <div class="portal-card p-8">
          <h3 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
            <el-icon color="#10b981" :size="22"><TrendCharts /></el-icon>
            公司实力
          </h3>
          <div class="grid grid-cols-2 gap-4">
            <div v-for="(stat, idx) in stats" :key="idx" class="text-center p-5 rounded-2xl bg-slate-50">
              <div class="text-3xl font-extrabold mb-1" :class="stat.colorClass">{{ stat.value }}</div>
              <div class="text-sm text-slate-500">{{ stat.label }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="portal-card p-8 mb-12">
        <h3 class="text-xl font-bold text-slate-800 mb-6">资质认证</h3>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div v-for="(cert, idx) in certifications" :key="idx" class="flex items-center gap-3 p-4 rounded-xl bg-slate-50">
            <el-icon :size="28" color="#10b981"><Medal /></el-icon>
            <div>
              <div class="font-semibold text-slate-800 text-sm">{{ cert }}</div>
              <div class="text-xs text-slate-400">已认证</div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <PortalFooter />
  </div>
</template>

<script setup>
import { ArrowRight, Aim, TrendCharts, Medal } from '@element-plus/icons-vue'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'

const stats = [
  { value: '8+', label: '年行业经验', colorClass: 'text-sky-500' },
  { value: '500+', label: '技术团队', colorClass: 'text-emerald-500' },
  { value: '5,000+', label: '服务客户', colorClass: 'text-amber-500' },
  { value: '50+', label: '软件著作权', colorClass: 'text-violet-500' },
]

const certifications = [
  'ISO9001质量管理体系',
  '国家高新技术企业',
  'ISO27001信息安全',
  '双软企业认证',
  'CMMI 3级认证',
  'AAA信用等级',
  '专精特新企业',
  'ISO14001环境管理',
]
</script>
