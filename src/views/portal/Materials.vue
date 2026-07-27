<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <main class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 py-8 w-full">
      <div class="mb-8">
        <div class="flex items-center gap-2 text-sm text-slate-400 mb-3">
          <router-link to="/portal" class="hover:text-sky-500 transition-colors no-underline">首页</router-link>
          <el-icon :size="12"><ArrowRight /></el-icon>
          <span class="text-slate-600">原材料采购</span>
        </div>
        <h1 class="text-3xl font-bold text-slate-800">原材料采购</h1>
        <p class="text-slate-500 mt-2">优质原材料一站式采购平台，为制造企业提供各类高品质工业原材料</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-12">
        <div v-for="(item, idx) in categories" :key="idx" class="portal-card p-6 hover:shadow-card-hover hover:-translate-y-1 transition-all duration-300 group cursor-pointer">
          <div class="w-14 h-14 rounded-2xl flex items-center justify-center mb-5 transition-transform duration-300 group-hover:scale-110" :class="item.bgClass">
            <el-icon :size="28" :class="item.iconClass"><component :is="item.icon" /></el-icon>
          </div>
          <h3 class="text-lg font-bold text-slate-800 mb-2">{{ item.name }}</h3>
          <p class="text-sm text-slate-500 leading-relaxed">{{ item.desc }}</p>
          <div class="mt-4 pt-4 border-t border-slate-100 flex items-center justify-between">
            <span class="text-xs text-slate-400">{{ item.count }}+ 种规格</span>
            <span class="text-sky-500 text-sm font-medium flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
              查看详情 <el-icon :size="14"><ArrowRight /></el-icon>
            </span>
          </div>
        </div>
      </div>

      <div class="portal-card p-8 mb-12">
        <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
          <el-icon color="#0ea5e9" :size="22"><CircleCheckFilled /></el-icon>
          采购优势
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div v-for="(adv, idx) in advantages" :key="idx" class="flex gap-4">
            <div class="w-10 h-10 rounded-xl flex items-center justify-center shrink-0" :class="adv.bgClass">
              <el-icon :size="20" :class="adv.iconClass"><component :is="adv.icon" /></el-icon>
            </div>
            <div>
              <h4 class="font-semibold text-slate-800 mb-1">{{ adv.title }}</h4>
              <p class="text-sm text-slate-500">{{ adv.desc }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="text-center py-8">
        <p class="text-slate-400 mb-4">如需大批量采购或定制原材料，请联系我们获取专属报价</p>
        <a href="#" class="portal-btn-primary inline-flex items-center gap-2 no-underline">
          <el-icon :size="18"><Phone /></el-icon>
          联系采购顾问：400-888-8888
        </a>
      </div>
    </main>

    <PortalFooter />
  </div>
</template>

<script setup>
import { markRaw } from 'vue'
import { ArrowRight, CircleCheckFilled, Phone, Box, Coin, Setting, Magnet, Grid } from '@element-plus/icons-vue'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'

const categories = [
  { name: '金属材料', desc: '各类钢材、铝材、铜材及合金材料，满足不同加工需求', bgClass: 'bg-slate-100', iconClass: 'text-slate-600', icon: markRaw(Box), count: '2,000' },
  { name: '塑料与橡胶', desc: '工程塑料、通用塑料、特种橡胶等高性能材料', bgClass: 'bg-sky-50', iconClass: 'text-sky-500', icon: markRaw(Magnet), count: '1,500' },
  { name: '电子元器件', desc: 'IC芯片、电阻电容、连接器等各类电子零部件', bgClass: 'bg-amber-50', iconClass: 'text-amber-500', icon: markRaw(Setting), count: '5,000' },
  { name: '化工原料', desc: '工业化学品、涂料、胶粘剂等辅助生产材料', bgClass: 'bg-emerald-50', iconClass: 'text-emerald-500', icon: markRaw(Grid), count: '800' },
  { name: '包装材料', desc: '纸箱、木箱、塑料包装等成品包装解决方案', bgClass: 'bg-rose-50', iconClass: 'text-rose-500', icon: markRaw(Box), count: '600' },
  { name: '标准件/紧固件', desc: '螺栓、螺母、垫圈、轴承等各类标准零部件', bgClass: 'bg-violet-50', iconClass: 'text-violet-500', icon: markRaw(Coin), count: '3,000' },
]

const advantages = [
  { title: '源头直供', desc: '与国内外优质原材料厂商直接合作，减少中间环节', bgClass: 'bg-emerald-50', iconClass: 'text-emerald-500', icon: markRaw(CircleCheckFilled) },
  { title: '品质保障', desc: '每批次材料附带质检报告，符合行业标准', bgClass: 'bg-sky-50', iconClass: 'text-sky-500', icon: markRaw(CircleCheckFilled) },
  { title: '灵活采购', desc: '支持小批量试购和大批量订货，按需灵活配置', bgClass: 'bg-amber-50', iconClass: 'text-amber-500', icon: markRaw(CircleCheckFilled) },
]
</script>
