<template>
  <footer class="bg-slate-900 text-slate-300 mt-auto">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 py-16">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-10 mb-12">
        <div class="lg:col-span-1">
          <div class="flex items-center gap-3 mb-5">
            <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-sky-400 to-blue-600 flex items-center justify-center shadow-lg shadow-sky-500/25">
              <span class="text-white text-sm font-extrabold tracking-tight">ZY</span>
            </div>
            <div class="flex flex-col">
              <span class="text-lg font-bold text-white leading-tight">造易</span>
              <span class="text-[10px] text-slate-400 font-medium tracking-wider">ZAOYI MES</span>
            </div>
          </div>
          <p class="text-sm text-slate-400 leading-relaxed mb-5">
            专注于制造业数字化转型，提供一站式智能制造解决方案，让制造更简单、更高效。
          </p>
          <div class="flex items-center gap-3">
            <a href="#" class="w-9 h-9 rounded-lg bg-slate-800 hover:bg-sky-500/20 flex items-center justify-center text-slate-400 hover:text-sky-400 transition-all duration-200">
              <el-icon :size="18"><ChatDotRound /></el-icon>
            </a>
            <a href="#" class="w-9 h-9 rounded-lg bg-slate-800 hover:bg-sky-500/20 flex items-center justify-center text-slate-400 hover:text-sky-400 transition-all duration-200">
              <el-icon :size="18"><Phone /></el-icon>
            </a>
            <a href="#" class="w-9 h-9 rounded-lg bg-slate-800 hover:bg-sky-500/20 flex items-center justify-center text-slate-400 hover:text-sky-400 transition-all duration-200">
              <el-icon :size="18"><Message /></el-icon>
            </a>
          </div>
        </div>

        <div>
          <h4 class="text-white font-semibold mb-5">产品服务</h4>
          <ul class="space-y-3">
            <li><router-link to="/portal/materials" class="text-sm text-slate-400 hover:text-sky-400 transition-colors no-underline">原材料采购</router-link></li>
            <li><router-link to="/portal/semi-finished" class="text-sm text-slate-400 hover:text-sky-400 transition-colors no-underline">半成品定制</router-link></li>
            <li><router-link to="/portal/finished-products" class="text-sm text-slate-400 hover:text-sky-400 transition-colors no-underline">成品供应</router-link></li>
            <li><router-link to="/portal/parts-mall" class="text-sm text-slate-400 hover:text-sky-400 transition-colors no-underline">配件商城</router-link></li>
            <li><router-link to="/portal/mes-system" class="text-sm text-slate-400 hover:text-sky-400 transition-colors no-underline">MES系统</router-link></li>
          </ul>
        </div>

        <div>
          <h4 class="text-white font-semibold mb-5">关于我们</h4>
          <ul class="space-y-3">
            <li><router-link to="/portal/about" class="text-sm text-slate-400 hover:text-sky-400 transition-colors no-underline">公司介绍</router-link></li>
            <li><router-link to="/portal/history" class="text-sm text-slate-400 hover:text-sky-400 transition-colors no-underline">发展历程</router-link></li>
            <li><router-link to="/portal/partners" class="text-sm text-slate-400 hover:text-sky-400 transition-colors no-underline">合作伙伴</router-link></li>
            <li><router-link to="/portal/news" class="text-sm text-slate-400 hover:text-sky-400 transition-colors no-underline">新闻动态</router-link></li>
            <li><router-link to="/portal/careers" class="text-sm text-slate-400 hover:text-sky-400 transition-colors no-underline">加入我们</router-link></li>
          </ul>
        </div>

        <div>
          <h4 class="text-white font-semibold mb-5">联系我们</h4>
          <ul class="space-y-4">
            <li class="flex items-start gap-3">
              <el-icon :size="18" class="text-sky-400 mt-0.5 shrink-0"><Location /></el-icon>
              <span class="text-sm text-slate-400">上海市浦东新区张江高科技园区</span>
            </li>
            <li class="flex items-center gap-3">
              <el-icon :size="18" class="text-sky-400 shrink-0"><Phone /></el-icon>
              <span class="text-sm text-slate-400">400-888-8888</span>
            </li>
            <li class="flex items-center gap-3">
              <el-icon :size="18" class="text-sky-400 shrink-0"><Message /></el-icon>
              <span class="text-sm text-slate-400">contact@zaoyi.com</span>
            </li>
            <li class="flex items-center gap-3">
              <el-icon :size="18" class="text-sky-400 shrink-0"><Clock /></el-icon>
              <span class="text-sm text-slate-400">周一至周五 9:00-18:00</span>
            </li>
          </ul>
        </div>
      </div>

      <div class="border-t border-slate-800 pt-8 flex flex-col md:flex-row items-center justify-between gap-4">
        <div class="flex items-center gap-6 text-xs text-slate-500">
          <span>© 2026 造易科技 版权所有</span>
          <span>沪ICP备xxxxxxxx号</span>
        </div>
        <div class="flex items-center gap-6">
          <a href="#" class="text-xs text-slate-500 hover:text-sky-400 transition-colors">隐私政策</a>
          <a href="#" class="text-xs text-slate-500 hover:text-sky-400 transition-colors">服务条款</a>
          <a href="#" class="text-xs text-slate-500 hover:text-sky-400 transition-colors">网站地图</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
</script>
