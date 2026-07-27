<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <main class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 py-8 w-full">
      <div class="mb-8">
        <div class="flex items-center gap-2 text-sm text-slate-400 mb-3">
          <router-link to="/portal" class="hover:text-sky-500 transition-colors no-underline">首页</router-link>
          <el-icon :size="12"><ArrowRight /></el-icon>
          <span class="text-slate-600">合作伙伴</span>
        </div>
        <h1 class="text-3xl font-bold text-slate-800">合作伙伴</h1>
        <p class="text-slate-500 mt-2">与行业顶尖伙伴携手，共建智能制造生态</p>
      </div>

      <div class="mb-12">
        <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
          <el-icon color="#0ea5e9" :size="22"><Connection /></el-icon>
          技术合作伙伴
        </h2>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-6">
          <div v-for="(p, idx) in techPartners" :key="idx" class="portal-card p-8 text-center group hover:shadow-card-hover hover:-translate-y-1 transition-all duration-300">
            <div class="w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-4 transition-transform duration-300 group-hover:scale-110" :class="p.bgClass">
              <el-icon :size="30" :class="p.iconClass"><component :is="p.icon" /></el-icon>
            </div>
            <h4 class="font-bold text-slate-800 mb-1">{{ p.name }}</h4>
            <p class="text-xs text-slate-400">{{ p.desc }}</p>
          </div>
        </div>
      </div>

      <div class="mb-12">
        <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
          <el-icon color="#10b981" :size="22"><OfficeBuilding /></el-icon>
          行业客户
        </h2>
        <div class="grid grid-cols-2 md:grid-cols-5 gap-4">
          <div v-for="(c, idx) in clients" :key="idx" class="portal-card p-6 text-center group hover:shadow-card-hover transition-all duration-300 cursor-default">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center mx-auto mb-3 transition-transform duration-300 group-hover:scale-110" :class="c.bgClass">
              <el-icon :size="22" :class="c.iconClass"><component :is="c.icon" /></el-icon>
            </div>
            <div class="font-semibold text-slate-800 text-sm">{{ c.name }}</div>
            <div class="text-xs text-slate-400 mt-1">{{ c.industry }}</div>
          </div>
        </div>
      </div>

      <div class="portal-card p-8 mb-12">
        <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
          <el-icon color="#f59e0b" :size="22"><Service /></el-icon>
          合作模式
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div v-for="(mode, idx) in modes" :key="idx" class="text-center p-6 rounded-2xl border border-slate-100 hover:border-sky-200 hover:bg-sky-50/30 transition-all duration-300">
            <div class="w-14 h-14 rounded-2xl flex items-center justify-center mx-auto mb-4" :class="mode.bgClass">
              <el-icon :size="28" :class="mode.iconClass"><component :is="mode.icon" /></el-icon>
            </div>
            <h4 class="font-bold text-slate-800 mb-2">{{ mode.title }}</h4>
            <p class="text-sm text-slate-500 leading-relaxed">{{ mode.desc }}</p>
          </div>
        </div>
      </div>

      <div class="text-center py-6">
        <p class="text-slate-400 mb-4">期待与您携手合作，共创智能制造美好未来</p>
        <a href="#" class="portal-btn-primary inline-flex items-center gap-2 no-underline">
          <el-icon :size="18"><Message /></el-icon>
          成为合作伙伴
        </a>
      </div>
    </main>

    <PortalFooter />
  </div>
</template>

<script setup>
import { markRaw } from 'vue'
import { ArrowRight, Connection, OfficeBuilding, Service, Message, Cpu, Monitor, Cloudy, Setting, Box, Grid, Tickets, MagicStick, TrendCharts, DataAnalysis } from '@element-plus/icons-vue'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'

const techPartners = [
  { name: '华为云', desc: '云计算基础设施', bgClass: 'bg-red-50', iconClass: 'text-red-500', icon: markRaw(Cloudy) },
  { name: '阿里云', desc: '云服务与大数据', bgClass: 'bg-orange-50', iconClass: 'text-orange-500', icon: markRaw(Cloudy) },
  { name: '西门子', desc: '工业自动化', bgClass: 'bg-sky-50', iconClass: 'text-sky-500', icon: markRaw(Setting) },
  { name: '研华科技', desc: '工业物联网', bgClass: 'bg-emerald-50', iconClass: 'text-emerald-500', icon: markRaw(Monitor) },
  { name: '用友网络', desc: '企业管理系统', bgClass: 'bg-violet-50', iconClass: 'text-violet-500', icon: markRaw(Grid) },
  { name: '海康威视', desc: '智能视觉方案', bgClass: 'bg-cyan-50', iconClass: 'text-cyan-500', icon: markRaw(Cpu) },
  { name: 'ABB', desc: '机器人及自动化', bgClass: 'bg-blue-50', iconClass: 'text-blue-500', icon: markRaw(Setting) },
  { name: '树根互联', desc: '工业互联网平台', bgClass: 'bg-teal-50', iconClass: 'text-teal-500', icon: markRaw(Connection) },
]

const clients = [
  { name: '三一重工', industry: '工程机械', bgClass: 'bg-sky-50', iconClass: 'text-sky-500', icon: markRaw(Box) },
  { name: '比亚迪', industry: '新能源汽车', bgClass: 'bg-emerald-50', iconClass: 'text-emerald-500', icon: markRaw(Tickets) },
  { name: '美的集团', industry: '家电制造', bgClass: 'bg-amber-50', iconClass: 'text-amber-500', icon: markRaw(Setting) },
  { name: '海尔智家', industry: '智能家居', bgClass: 'bg-sky-50', iconClass: 'text-sky-500', icon: markRaw(Monitor) },
  { name: '立讯精密', industry: '电子制造', bgClass: 'bg-violet-50', iconClass: 'text-violet-500', icon: markRaw(Cpu) },
  { name: '迈瑞医疗', industry: '医疗器械', bgClass: 'bg-rose-50', iconClass: 'text-rose-500', icon: markRaw(DataAnalysis) },
  { name: '汇川技术', industry: '工业自动化', bgClass: 'bg-cyan-50', iconClass: 'text-cyan-500', icon: markRaw(TrendCharts) },
  { name: '宁德时代', industry: '新能源电池', bgClass: 'bg-emerald-50', iconClass: 'text-emerald-500', icon: markRaw(MagicStick) },
  { name: '舜宇光学', industry: '光学仪器', bgClass: 'bg-amber-50', iconClass: 'text-amber-500', icon: markRaw(Box) },
  { name: '大族激光', industry: '激光设备', bgClass: 'bg-violet-50', iconClass: 'text-violet-500', icon: markRaw(Grid) },
]

const modes = [
  { title: '技术集成', desc: '开放API接口，支持与ERP、PLM等系统深度集成', bgClass: 'bg-sky-50', iconClass: 'text-sky-500', icon: markRaw(Connection) },
  { title: '渠道代理', desc: '面向全国招募区域代理商，共同开拓制造企业市场', bgClass: 'bg-emerald-50', iconClass: 'text-emerald-500', icon: markRaw(Service) },
  { title: '联合创新', desc: '与行业伙伴联合研发，打造垂直行业解决方案', bgClass: 'bg-violet-50', iconClass: 'text-violet-500', icon: markRaw(TrendCharts) },
]
</script>
