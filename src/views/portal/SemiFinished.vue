<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <main class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 py-8 w-full">
      <div class="mb-8">
        <div class="flex items-center gap-2 text-sm text-slate-400 mb-3">
          <router-link to="/portal" class="hover:text-sky-500 transition-colors no-underline">首页</router-link>
          <el-icon :size="12"><ArrowRight /></el-icon>
          <span class="text-slate-600">半成品定制</span>
        </div>
        <h1 class="text-3xl font-bold text-slate-800">半成品定制</h1>
        <p class="text-slate-500 mt-2">按需定制各类半成品零部件，精准匹配您的生产工艺需求</p>
      </div>

      <div class="portal-card p-8 mb-8">
        <h2 class="text-xl font-bold text-slate-800 mb-6">定制流程</h2>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <div v-for="(step, idx) in steps" :key="idx" class="relative text-center group">
            <div class="w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-4 transition-transform duration-300 group-hover:scale-110" :class="step.bgClass">
              <span class="text-2xl font-extrabold" :class="step.numClass">{{ idx + 1 }}</span>
            </div>
            <h4 class="font-bold text-slate-800 mb-2">{{ step.title }}</h4>
            <p class="text-sm text-slate-500 leading-relaxed">{{ step.desc }}</p>
            <div v-if="idx < steps.length - 1" class="hidden lg:block absolute top-8 -right-4 text-slate-300">
              <el-icon :size="24"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-12">
        <div class="portal-card p-8">
          <h3 class="text-lg font-bold text-slate-800 mb-5 flex items-center gap-2">
            <el-icon color="#0ea5e9" :size="22"><Box /></el-icon>
            可定制半成品类型
          </h3>
          <div class="space-y-4">
            <div v-for="(item, idx) in types" :key="idx" class="flex items-center gap-4 p-4 rounded-xl bg-slate-50 hover:bg-sky-50 transition-colors group cursor-pointer">
              <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0" :class="item.bgClass">
                <el-icon :size="22" :class="item.iconClass"><component :is="item.icon" /></el-icon>
              </div>
              <div class="flex-1">
                <div class="font-semibold text-slate-800 group-hover:text-sky-600 transition-colors">{{ item.name }}</div>
                <div class="text-xs text-slate-400 mt-0.5">{{ item.desc }}</div>
              </div>
              <el-icon :size="16" class="text-slate-300 group-hover:text-sky-500 group-hover:translate-x-1 transition-all"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>

        <div class="portal-card p-8">
          <h3 class="text-lg font-bold text-slate-800 mb-5 flex items-center gap-2">
            <el-icon color="#10b981" :size="22"><Medal /></el-icon>
            我们的优势
          </h3>
          <ul class="space-y-4">
            <li v-for="(item, idx) in advantages" :key="idx" class="flex items-start gap-3 p-3 rounded-xl hover:bg-slate-50 transition-colors">
              <el-icon :size="20" color="#10b981" class="shrink-0 mt-0.5"><CircleCheckFilled /></el-icon>
              <div>
                <div class="font-semibold text-slate-800">{{ item.title }}</div>
                <div class="text-sm text-slate-500 mt-0.5">{{ item.desc }}</div>
              </div>
            </li>
          </ul>
        </div>
      </div>

      <div class="text-center py-6">
        <router-link to="/portal/products" class="portal-btn-primary inline-flex items-center gap-2 no-underline">
          浏览更多产品 <el-icon :size="18"><ArrowRight /></el-icon>
        </router-link>
      </div>
    </main>

    <PortalFooter />
  </div>
</template>

<script setup>
import { markRaw } from 'vue'
import { ArrowRight, CircleCheckFilled, Box, Medal, Setting, Tools, Grid } from '@element-plus/icons-vue'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'

const steps = [
  { title: '提交需求', desc: '提供图纸、规格参数及工艺要求', bgClass: 'bg-sky-50', numClass: 'text-sky-500' },
  { title: '方案评估', desc: '工程师评审可行性并给出报价方案', bgClass: 'bg-amber-50', numClass: 'text-amber-500' },
  { title: '生产制造', desc: '确认方案后进入精密制造环节', bgClass: 'bg-emerald-50', numClass: 'text-emerald-500' },
  { title: '质检交付', desc: '严格质检后安全包装交付', bgClass: 'bg-violet-50', numClass: 'text-violet-500' },
]

const types = [
  { name: '精密机械零件', desc: '轴类、齿轮、法兰等精密加工件', bgClass: 'bg-sky-50', iconClass: 'text-sky-500', icon: markRaw(Setting) },
  { name: '钣金结构件', desc: '机箱、机柜、支架等钣金制品', bgClass: 'bg-amber-50', iconClass: 'text-amber-500', icon: markRaw(Tools) },
  { name: '注塑成型件', desc: '各类塑料外壳、结构件注塑加工', bgClass: 'bg-emerald-50', iconClass: 'text-emerald-500', icon: markRaw(Grid) },
  { name: '表面处理件', desc: '电镀、喷涂、阳极氧化等表面处理', bgClass: 'bg-violet-50', iconClass: 'text-violet-500', icon: markRaw(Box) },
]

const advantages = [
  { title: '按图定制', desc: '严格按客户图纸和规格要求进行定制生产' },
  { title: '快速打样', desc: '3-5个工作日内完成样品制作，加速产品开发周期' },
  { title: '小批量柔性生产', desc: '支持小批量订单，降低库存压力和资金占用' },
  { title: '全程可追溯', desc: '每批次产品附带完整生产工艺和质量检测记录' },
]
</script>
