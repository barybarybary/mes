<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <main class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 py-8 w-full">
      <div class="mb-8">
        <div class="flex items-center gap-2 text-sm text-slate-400 mb-3">
          <router-link to="/portal" class="hover:text-sky-500 transition-colors no-underline">首页</router-link>
          <el-icon :size="12"><ArrowRight /></el-icon>
          <span class="text-slate-600">配件商城</span>
        </div>
        <h1 class="text-3xl font-bold text-slate-800">配件商城</h1>
        <p class="text-slate-500 mt-2">海量工业配件在线选购，覆盖各类设备配套需求</p>
      </div>

      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6 mb-12">
        <div v-for="(item, idx) in parts" :key="idx" class="portal-card p-6 text-center group hover:shadow-card-hover hover:-translate-y-1 transition-all duration-300 cursor-pointer">
          <div class="w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-4 transition-transform duration-300 group-hover:scale-110" :class="item.bgClass">
            <el-icon :size="30" :class="item.iconClass"><component :is="item.icon" /></el-icon>
          </div>
          <h3 class="font-bold text-slate-800 mb-1 group-hover:text-sky-600 transition-colors">{{ item.name }}</h3>
          <p class="text-xs text-slate-400">{{ item.count }}+ SKU</p>
        </div>
      </div>

      <div class="portal-card p-8 mb-12">
        <div class="flex flex-col lg:flex-row items-center gap-8">
          <div class="flex-1">
            <h2 class="text-xl font-bold text-slate-800 mb-4">找不到需要的配件？</h2>
            <p class="text-slate-500 leading-relaxed mb-4">我们拥有庞大的供应商网络，可以帮您快速匹配所需配件。提交需求后，专属采购顾问将在2小时内与您联系。</p>
            <a href="#" class="portal-btn-primary inline-flex items-center gap-2 no-underline">
              <el-icon :size="18"><Search /></el-icon>
              提交配件需求
            </a>
          </div>
          <div class="flex items-center gap-6 text-center">
            <div class="px-6">
              <div class="text-2xl font-extrabold text-sky-500">50万+</div>
              <div class="text-xs text-slate-400 mt-1">配件数据库</div>
            </div>
            <div class="w-px h-16 bg-slate-200"></div>
            <div class="px-6">
              <div class="text-2xl font-extrabold text-emerald-500">24h</div>
              <div class="text-xs text-slate-400 mt-1">快速响应</div>
            </div>
            <div class="w-px h-16 bg-slate-200"></div>
            <div class="px-6">
              <div class="text-2xl font-extrabold text-amber-500">98%</div>
              <div class="text-xs text-slate-400 mt-1">匹配成功率</div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <PortalFooter />
  </div>
</template>

<script setup>
import { markRaw } from 'vue'
import { ArrowRight, Search, Setting, Tools, Connection, MagicStick, Switch, Tickets, ScaleToOriginal } from '@element-plus/icons-vue'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'

const parts = [
  { name: '机械配件', icon: markRaw(Setting), bgClass: 'bg-sky-50', iconClass: 'text-sky-500', count: '15,000' },
  { name: '电气配件', icon: markRaw(Connection), bgClass: 'bg-amber-50', iconClass: 'text-amber-500', count: '12,000' },
  { name: '气动元件', icon: markRaw(Switch), bgClass: 'bg-emerald-50', iconClass: 'text-emerald-500', count: '8,000' },
  { name: '液压元件', icon: markRaw(Tools), bgClass: 'bg-rose-50', iconClass: 'text-rose-500', count: '6,000' },
  { name: '密封件', icon: markRaw(Tickets), bgClass: 'bg-violet-50', iconClass: 'text-violet-500', count: '10,000' },
  { name: '传动件', icon: markRaw(MagicStick), bgClass: 'bg-orange-50', iconClass: 'text-orange-500', count: '7,000' },
  { name: '模具配件', icon: markRaw(ScaleToOriginal), bgClass: 'bg-cyan-50', iconClass: 'text-cyan-500', count: '5,000' },
  { name: '管件阀门', icon: markRaw(Setting), bgClass: 'bg-teal-50', iconClass: 'text-teal-500', count: '9,000' },
]
</script>
