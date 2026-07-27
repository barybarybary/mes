<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <main class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 py-8 w-full">
      <div class="mb-8">
        <div class="flex items-center gap-2 text-sm text-slate-400 mb-3">
          <router-link to="/portal" class="hover:text-sky-500 transition-colors no-underline">首页</router-link>
          <el-icon :size="12"><ArrowRight /></el-icon>
          <span class="text-slate-600">发展历程</span>
        </div>
        <h1 class="text-3xl font-bold text-slate-800">发展历程</h1>
        <p class="text-slate-500 mt-2">从初创到行业领先，每一步都坚实有力</p>
      </div>

      <div class="relative">
        <div class="absolute left-8 top-0 bottom-0 w-0.5 bg-gradient-to-b from-sky-200 via-sky-300 to-emerald-200 hidden md:block"></div>

        <div class="space-y-8 mb-12">
          <div v-for="(milestone, idx) in milestones" :key="idx" class="flex gap-8 group">
            <div class="hidden md:flex flex-col items-center shrink-0 relative z-10">
              <div class="w-16 h-16 rounded-2xl flex items-center justify-center transition-transform duration-300 group-hover:scale-110" :class="milestone.bgClass">
                <span class="text-2xl font-extrabold" :class="milestone.yearColor">{{ milestone.year }}</span>
              </div>
            </div>
            <div class="flex-1 portal-card p-6 hover:shadow-card-hover transition-all duration-300">
              <div class="md:hidden inline-flex items-center px-3 py-1 rounded-lg text-sm font-bold mb-3" :class="milestone.bgClass + ' ' + milestone.yearColor">
                {{ milestone.year }}
              </div>
              <h3 class="text-lg font-bold text-slate-800 mb-2 group-hover:text-sky-600 transition-colors">{{ milestone.title }}</h3>
              <p class="text-sm text-slate-500 leading-relaxed">{{ milestone.desc }}</p>
              <div v-if="milestone.highlights" class="flex flex-wrap gap-2 mt-4">
                <span v-for="(h, hi) in milestone.highlights" :key="hi" class="px-3 py-1 rounded-full text-xs font-medium bg-sky-50 text-sky-600">{{ h }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="text-center py-8 portal-card p-8">
        <h2 class="text-xl font-bold text-slate-800 mb-2">携手共创未来</h2>
        <p class="text-slate-500 mb-6">造易科技将继续深耕智能制造领域，与合作伙伴一起推动行业数字化转型</p>
        <router-link to="/portal/partners" class="portal-btn-secondary inline-flex items-center gap-2 no-underline">
          了解合作伙伴 <el-icon :size="18"><ArrowRight /></el-icon>
        </router-link>
      </div>
    </main>

    <PortalFooter />
  </div>
</template>

<script setup>
import { ArrowRight } from '@element-plus/icons-vue'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'

const milestones = [
  {
    year: '2018', title: '公司成立', desc: '造易科技在上海张江正式成立，核心团队来自知名制造企业和互联网公司，怀揣"让制造变容易"的初心开启创业征程。',
    bgClass: 'bg-sky-50', yearColor: 'text-sky-500',
    highlights: ['天使轮融资', '首款MES产品上线']
  },
  {
    year: '2019', title: '产品打磨', desc: '深入服务首批客户，持续迭代MES系统核心功能。产品在机械制造、电子电器行业积累了大量实践经验，获得客户一致好评。',
    bgClass: 'bg-sky-50', yearColor: 'text-sky-500',
    highlights: ['签约100+客户', '通过ISO9001认证']
  },
  {
    year: '2020', title: '快速成长', desc: '完成A轮融资，团队规模突破100人。推出云端部署方案，帮助中小企业以更低成本实现数字化转型。获得国家高新技术企业认定。',
    bgClass: 'bg-emerald-50', yearColor: 'text-emerald-500',
    highlights: ['A轮融资', '高新技术企业', '云端方案上线']
  },
  {
    year: '2021', title: '行业深耕', desc: '深入汽车零部件、医疗器械等垂直行业，推出行业专属解决方案。客户数量突破1,000家，覆盖全国主要制造业聚集区。',
    bgClass: 'bg-emerald-50', yearColor: 'text-emerald-500',
    highlights: ['行业解决方案', '1,000+客户', '区域服务中心']
  },
  {
    year: '2022', title: '平台升级', desc: '完成B轮融资，推出造易MES V3.0版本。新增AI质检、智能排产等创新功能，打造新一代智能制造平台。',
    bgClass: 'bg-amber-50', yearColor: 'text-amber-500',
    highlights: ['B轮融资', 'V3.0发布', 'AI功能上线']
  },
  {
    year: '2023', title: '生态构建', desc: '开放API平台，构建合作伙伴生态。推出客户门户、供应商协同等产品线，形成完整的制造业数字化产品矩阵。',
    bgClass: 'bg-amber-50', yearColor: 'text-amber-500',
    highlights: ['开放平台', '生态伙伴', '产品矩阵']
  },
  {
    year: '2024', title: '市场领先', desc: '服务客户突破5,000家，成为国内制造业MES领域的领军企业。荣获多项行业大奖，持续引领智能制造创新发展。',
    bgClass: 'bg-violet-50', yearColor: 'text-violet-500',
    highlights: ['5,000+客户', '行业领军', '多项大奖']
  },
  {
    year: '2025', title: '走向国际', desc: '启动国际化战略，产品进入东南亚、南美等海外市场。持续加大研发投入，探索AI大模型在制造业的应用场景。',
    bgClass: 'bg-violet-50', yearColor: 'text-violet-500',
    highlights: ['国际化', 'AI大模型', '海外市场']
  },
]
</script>
