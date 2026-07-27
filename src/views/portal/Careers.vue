<template>
  <div class="min-h-screen flex flex-col bg-slate-50">
    <PortalHeader />

    <main class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 py-8 w-full">
      <div class="mb-8">
        <div class="flex items-center gap-2 text-sm text-slate-400 mb-3">
          <router-link to="/portal" class="hover:text-sky-500 transition-colors no-underline">首页</router-link>
          <el-icon :size="12"><ArrowRight /></el-icon>
          <span class="text-slate-600">加入我们</span>
        </div>
        <h1 class="text-3xl font-bold text-slate-800">加入我们</h1>
        <p class="text-slate-500 mt-2">与优秀的人一起，做有意义的事</p>
      </div>

      <div class="relative rounded-3xl overflow-hidden mb-12 p-10 lg:p-16" style="background: linear-gradient(135deg, #0c1929 0%, #134e7a 50%, #0a1628 100%);">
        <div class="absolute inset-0 opacity-10" style="background-image: radial-gradient(circle at 30% 50%, rgba(14, 165, 233, 0.4) 0%, transparent 50%), radial-gradient(circle at 70% 50%, rgba(16, 185, 129, 0.3) 0%, transparent 50%);"></div>
        <div class="relative z-10 max-w-2xl">
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 text-sky-200 text-sm mb-6 border border-white/10">
            <span class="w-2 h-2 rounded-full bg-emerald-400"></span>
            人才招聘
          </div>
          <h2 class="text-3xl lg:text-4xl font-bold text-white mb-4">寻找改变制造业的同行者</h2>
          <p class="text-sky-100/80 text-lg leading-relaxed">
            我们正在寻找有激情、有创造力的人才，一起用技术推动制造业的数字化变革。
          </p>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
        <div v-for="(val, idx) in values" :key="idx" class="portal-card p-8 text-center group hover:shadow-card-hover hover:-translate-y-1 transition-all duration-300">
          <div class="w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-5 transition-transform duration-300 group-hover:scale-110 group-hover:-rotate-3" :class="val.bgClass">
            <el-icon :size="30" :class="val.iconClass"><component :is="val.icon" /></el-icon>
          </div>
          <h3 class="text-lg font-bold text-slate-800 mb-2">{{ val.title }}</h3>
          <p class="text-sm text-slate-500 leading-relaxed">{{ val.desc }}</p>
        </div>
      </div>

      <div class="mb-12">
        <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
          <el-icon color="#0ea5e9" :size="22"><Briefcase /></el-icon>
          热招职位
        </h2>
        <div class="space-y-4">
          <div v-for="(job, idx) in jobs" :key="idx" class="portal-card p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4 group hover:shadow-card-hover transition-all duration-300 cursor-pointer">
            <div class="flex-1">
              <h4 class="text-lg font-bold text-slate-800 group-hover:text-sky-600 transition-colors">{{ job.title }}</h4>
              <div class="flex flex-wrap items-center gap-2 mt-2">
                <span class="flex items-center gap-1 text-xs text-slate-400 bg-slate-50 px-2 py-1 rounded-md">
                  <el-icon :size="14"><Location /></el-icon> {{ job.location }}
                </span>
                <span class="flex items-center gap-1 text-xs text-slate-400 bg-slate-50 px-2 py-1 rounded-md">
                  <el-icon :size="14"><Clock /></el-icon> {{ job.type }}
                </span>
                <span class="text-xs text-slate-400 bg-slate-50 px-2 py-1 rounded-md">{{ job.exp }}</span>
              </div>
            </div>
            <div class="flex items-center gap-3">
              <span class="text-sky-500 font-semibold text-sm">{{ job.salary }}</span>
              <span class="flex items-center gap-1 text-sky-500 text-sm font-medium opacity-0 group-hover:opacity-100 transition-opacity">
                查看详情 <el-icon :size="14"><ArrowRight /></el-icon>
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="portal-card p-8 mb-12">
        <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
          <el-icon color="#f59e0b" :size="22"><Star /></el-icon>
          薪酬福利
        </h2>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-6">
          <div v-for="(benefit, idx) in benefits" :key="idx" class="text-center p-4 rounded-2xl bg-slate-50 hover:bg-sky-50 transition-colors group">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center mx-auto mb-3 transition-transform duration-300 group-hover:scale-110" :class="benefit.bgClass">
              <el-icon :size="22" :class="benefit.iconClass"><component :is="benefit.icon" /></el-icon>
            </div>
            <div class="font-semibold text-slate-800 text-sm">{{ benefit.title }}</div>
            <div class="text-xs text-slate-400 mt-1">{{ benefit.desc }}</div>
          </div>
        </div>
      </div>

      <div class="text-center py-6">
        <p class="text-slate-400 mb-2">简历投递邮箱</p>
        <a href="mailto:hr@zaoyi.com" class="text-sky-500 text-lg font-bold hover:text-sky-600 transition-colors no-underline">hr@zaoyi.com</a>
        <p class="text-xs text-slate-400 mt-1">邮件标题格式：姓名-应聘职位-工作地点</p>
      </div>
    </main>

    <PortalFooter />
  </div>
</template>

<script setup>
import { markRaw } from 'vue'
import { ArrowRight, Briefcase, Star, Location, Clock, TrendCharts, Connection, Medal, CoffeeCup, Promotion, School, HomeFilled, Tickets } from '@element-plus/icons-vue'
import PortalHeader from '@/components/PortalHeader.vue'
import PortalFooter from '@/components/PortalFooter.vue'

const values = [
  { title: '创新驱动', desc: '鼓励技术创新和思维突破，为每个想法提供舞台', bgClass: 'bg-sky-50', iconClass: 'text-sky-500', icon: markRaw(TrendCharts) },
  { title: '开放协作', desc: '扁平化管理，开放透明的沟通文化', bgClass: 'bg-emerald-50', iconClass: 'text-emerald-500', icon: markRaw(Connection) },
  { title: '追求卓越', desc: '不断挑战自我，追求更高的标准和更好的结果', bgClass: 'bg-amber-50', iconClass: 'text-amber-500', icon: markRaw(Medal) },
]

const jobs = [
  { title: '高级Java开发工程师', location: '上海', type: '全职', exp: '5-10年', salary: '25-45K' },
  { title: '前端开发工程师（Vue/React）', location: '上海', type: '全职', exp: '3-5年', salary: '20-35K' },
  { title: 'AI算法工程师', location: '上海/北京', type: '全职', exp: '3-8年', salary: '30-60K' },
  { title: '产品经理（MES方向）', location: '上海', type: '全职', exp: '5-10年', salary: '25-45K' },
  { title: '实施顾问（制造业）', location: '全国', type: '全职', exp: '3-5年', salary: '15-30K' },
  { title: '测试开发工程师', location: '上海', type: '全职', exp: '3-5年', salary: '18-30K' },
  { title: 'UI/UX设计师', location: '上海', type: '全职', exp: '3-5年', salary: '18-30K' },
  { title: '前端开发实习生', location: '上海', type: '实习', exp: '应届', salary: '200-300元/天' },
]

const benefits = [
  { title: '竞争力薪酬', desc: '13薪+年终奖', bgClass: 'bg-sky-50', iconClass: 'text-sky-500', icon: markRaw(Tickets) },
  { title: '五险一金', desc: '足额缴纳+补充公积金', bgClass: 'bg-emerald-50', iconClass: 'text-emerald-500', icon: markRaw(HomeFilled) },
  { title: '弹性工作', desc: '弹性上下班时间', bgClass: 'bg-amber-50', iconClass: 'text-amber-500', icon: markRaw(Clock) },
  { title: '培训成长', desc: '技术课程+行业交流', bgClass: 'bg-violet-50', iconClass: 'text-violet-500', icon: markRaw(School) },
  { title: '免费餐饮', desc: '午餐补贴+零食饮料', bgClass: 'bg-rose-50', iconClass: 'text-rose-500', icon: markRaw(CoffeeCup) },
  { title: '晋升空间', desc: '双通道晋升机制', bgClass: 'bg-cyan-50', iconClass: 'text-cyan-500', icon: markRaw(Promotion) },
  { title: '团建活动', desc: '季度团建+年度旅游', bgClass: 'bg-orange-50', iconClass: 'text-orange-500', icon: markRaw(Tickets) },
  { title: '股权激励', desc: '核心员工股权计划', bgClass: 'bg-indigo-50', iconClass: 'text-indigo-500', icon: markRaw(Star) },
]
</script>
