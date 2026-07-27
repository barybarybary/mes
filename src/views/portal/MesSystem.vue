<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <main class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 py-8 w-full">
      <div class="mb-8">
        <div class="flex items-center gap-2 text-sm text-slate-400 mb-3">
          <router-link to="/portal" class="hover:text-sky-500 transition-colors no-underline">首页</router-link>
          <el-icon :size="12"><ArrowRight /></el-icon>
          <span class="text-slate-600">MES系统</span>
        </div>
        <h1 class="text-3xl font-bold text-slate-800">造易 MES 系统</h1>
        <p class="text-slate-500 mt-2">新一代智能制造执行系统，助力工厂数字化转型</p>
      </div>

      <div class="relative rounded-3xl overflow-hidden mb-12 p-10 lg:p-16" style="background: linear-gradient(135deg, #0c1929 0%, #0f2847 40%, #0a1628 100%);">
        <div class="absolute inset-0 opacity-10" style="background-image: radial-gradient(circle at 20% 50%, rgba(14, 165, 233, 0.4) 0%, transparent 50%), radial-gradient(circle at 80% 50%, rgba(16, 185, 129, 0.3) 0%, transparent 50%);"></div>
        <div class="relative z-10">
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 text-sky-200 text-sm mb-6 border border-white/10">
            <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
            智能制造解决方案
          </div>
          <h2 class="text-3xl lg:text-4xl font-bold text-white mb-4">打造数字化智能工厂</h2>
          <p class="text-sky-100/80 text-lg mb-8 max-w-2xl leading-relaxed">
            造易MES系统覆盖从订单到交付的全流程管理，实现生产过程透明化、质量可追溯、设备可监控，帮助企业降本增效。
          </p>
          <div class="flex flex-wrap gap-4">
            <a href="#" class="inline-flex items-center gap-2 px-6 py-3 rounded-xl font-semibold bg-white text-slate-800 hover:bg-sky-50 transition-all duration-300 no-underline shadow-lg">
              预约演示 <el-icon :size="18"><ArrowRight /></el-icon>
            </a>
            <a href="#" class="inline-flex items-center gap-2 px-6 py-3 rounded-xl font-semibold border border-white/20 text-white hover:bg-white/10 transition-all duration-300 no-underline">
              下载产品手册
            </a>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-12">
        <div v-for="(mod, idx) in modules" :key="idx" class="portal-card p-6 group hover:shadow-card-hover hover:-translate-y-1 transition-all duration-300">
          <div class="w-12 h-12 rounded-xl flex items-center justify-center mb-4 transition-transform duration-300 group-hover:scale-110" :class="mod.bgClass">
            <el-icon :size="24" :class="mod.iconClass"><component :is="mod.icon" /></el-icon>
          </div>
          <h3 class="font-bold text-slate-800 mb-2">{{ mod.title }}</h3>
          <p class="text-sm text-slate-500 leading-relaxed">{{ mod.desc }}</p>
          <ul class="mt-4 space-y-1.5">
            <li v-for="(feat, fi) in mod.features" :key="fi" class="flex items-center gap-2 text-xs text-slate-500">
              <el-icon :size="14" color="#10b981"><CircleCheckFilled /></el-icon>
              {{ feat }}
            </li>
          </ul>
        </div>
      </div>

      <div class="text-center py-8 portal-card p-8">
        <h2 class="text-xl font-bold text-slate-800 mb-2">已有 5,000+ 制造企业使用造易MES</h2>
        <p class="text-slate-500 mb-6">覆盖机械制造、电子电器、汽车零部件、医疗器械等多个行业</p>
        <a href="#" class="portal-btn-primary inline-flex items-center gap-2 no-underline">
          <el-icon :size="18"><Phone /></el-icon>
          咨询热线：400-888-8888
        </a>
      </div>
    </main>

    <PortalFooter />
  </div>
</template>

<script setup>
import { markRaw } from 'vue'
import { ArrowRight, CircleCheckFilled, Phone, Monitor, DataAnalysis, Clock, Document, Setting, Box } from '@element-plus/icons-vue'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'

const modules = [
  {
    title: '生产管理', desc: '工单排程、生产派工、进度跟踪全流程数字化',
    bgClass: 'bg-sky-50', iconClass: 'text-sky-500', icon: markRaw(Monitor),
    features: ['智能排产', '工单追踪', '进度看板', '异常预警']
  },
  {
    title: '质量管理', desc: '来料检验、过程质检、成品检验全覆盖',
    bgClass: 'bg-emerald-50', iconClass: 'text-emerald-500', icon: markRaw(DataAnalysis),
    features: ['质检标准', '检验记录', '不合格品处理', '质量追溯']
  },
  {
    title: '设备管理', desc: '设备台账、维修保养、OEE分析一体化',
    bgClass: 'bg-amber-50', iconClass: 'text-amber-500', icon: markRaw(Setting),
    features: ['设备台账', '点检保养', '维修工单', 'OEE分析']
  },
  {
    title: '库存管理', desc: '原材料、半成品、成品库存实时掌控',
    bgClass: 'bg-violet-50', iconClass: 'text-violet-500', icon: markRaw(Box),
    features: ['库存查询', '出入库管理', '库存预警', '盘点管理']
  },
  {
    title: '报表分析', desc: '多维度数据看板，辅助管理决策',
    bgClass: 'bg-rose-50', iconClass: 'text-rose-500', icon: markRaw(Document),
    features: ['生产报表', '质量报表', '效率分析', '成本分析']
  },
  {
    title: '移动办公', desc: '支持手机APP、平板，随时随地掌握生产动态',
    bgClass: 'bg-cyan-50', iconClass: 'text-cyan-500', icon: markRaw(Clock),
    features: ['移动报工', '消息推送', '扫码操作', '实时查询']
  },
]
</script>
