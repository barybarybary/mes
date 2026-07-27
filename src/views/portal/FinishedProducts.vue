<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <main class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 py-8 w-full">
      <div class="mb-8">
        <div class="flex items-center gap-2 text-sm text-slate-400 mb-3">
          <router-link to="/portal" class="hover:text-sky-500 transition-colors no-underline">首页</router-link>
          <el-icon :size="12"><ArrowRight /></el-icon>
          <span class="text-slate-600">成品供应</span>
        </div>
        <h1 class="text-3xl font-bold text-slate-800">成品供应</h1>
        <p class="text-slate-500 mt-2">高品质成品现货供应，即买即用，快速交付</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-12">
        <div v-for="(cat, idx) in categories" :key="idx" class="portal-card overflow-hidden group cursor-pointer hover:shadow-card-hover hover:-translate-y-1 transition-all duration-300">
          <div class="h-40 flex items-center justify-center" :class="cat.bgClass">
            <el-icon :size="56" :class="cat.iconClass"><component :is="cat.icon" /></el-icon>
          </div>
          <div class="p-6">
            <h3 class="text-lg font-bold text-slate-800 mb-2 group-hover:text-sky-600 transition-colors">{{ cat.name }}</h3>
            <p class="text-sm text-slate-500 leading-relaxed mb-4">{{ cat.desc }}</p>
            <div class="flex items-center justify-between">
              <span class="text-xs text-slate-400">{{ cat.specs }}</span>
              <span class="text-sky-500 text-sm font-medium flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                了解更多 <el-icon :size="14"><ArrowRight /></el-icon>
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="portal-card p-8 mb-12">
        <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
          <el-icon color="#0ea5e9" :size="22"><TrendCharts /></el-icon>
          供应能力
        </h2>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-6 text-center">
          <div v-for="(stat, idx) in stats" :key="idx" class="p-4">
            <div class="text-3xl font-extrabold mb-1" :class="stat.colorClass">{{ stat.value }}</div>
            <div class="text-sm text-slate-500">{{ stat.label }}</div>
          </div>
        </div>
      </div>

      <div class="text-center py-6">
        <router-link to="/portal/products" class="portal-btn-primary inline-flex items-center gap-2 no-underline">
          浏览成品目录 <el-icon :size="18"><ArrowRight /></el-icon>
        </router-link>
      </div>
    </main>

    <PortalFooter />
  </div>
</template>

<script setup>
import { markRaw } from 'vue'
import { ArrowRight, Box, TrendCharts, Cpu, Connection, VideoCamera, Tickets } from '@element-plus/icons-vue'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'

const categories = [
  { name: '工业设备', desc: '自动化设备、检测仪器、生产线配套设备', bgClass: 'bg-gradient-to-br from-sky-50 to-blue-50', iconClass: 'text-sky-500', icon: markRaw(Cpu), specs: '200+ 款型号' },
  { name: '智能硬件', desc: 'IoT设备、传感器、控制器等智能终端产品', bgClass: 'bg-gradient-to-br from-violet-50 to-purple-50', iconClass: 'text-violet-500', icon: markRaw(Connection), specs: '150+ 款型号' },
  { name: '安防监控', desc: '工业摄像头、门禁系统、环境监测设备', bgClass: 'bg-gradient-to-br from-amber-50 to-orange-50', iconClass: 'text-amber-500', icon: markRaw(VideoCamera), specs: '100+ 款型号' },
  { name: '工具量具', desc: '精密量具、电动工具、气动工具等', bgClass: 'bg-gradient-to-br from-emerald-50 to-teal-50', iconClass: 'text-emerald-500', icon: markRaw(Tickets), specs: '500+ 款型号' },
  { name: '电气设备', desc: '配电柜、变频器、电机驱动等电气产品', bgClass: 'bg-gradient-to-br from-rose-50 to-pink-50', iconClass: 'text-rose-500', icon: markRaw(Box), specs: '300+ 款型号' },
  { name: '传动部件', desc: '减速机、联轴器、丝杆模组等传动系统', bgClass: 'bg-gradient-to-br from-cyan-50 to-teal-50', iconClass: 'text-cyan-500', icon: markRaw(Box), specs: '250+ 款型号' },
]

const stats = [
  { value: '10,000+', label: '成品品类', colorClass: 'text-sky-500' },
  { value: '48h', label: '平均发货时效', colorClass: 'text-emerald-500' },
  { value: '99.8%', label: '准时交付率', colorClass: 'text-amber-500' },
  { value: '5,000+', label: '合作客户', colorClass: 'text-violet-500' },
]
</script>
