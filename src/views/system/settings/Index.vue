<template>
  <div class="space-y-6">
    <!-- 主题颜色 -->
    <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
      <div class="px-5 sm:px-6 py-4 border-b border-slate-50 dark:border-slate-800">
        <h3 class="text-base font-semibold text-slate-800 dark:text-slate-200 flex items-center gap-2.5">
          <el-icon :size="18" color="#64748b"><Brush /></el-icon>主题颜色
        </h3>
        <p class="text-xs text-slate-400 dark:text-slate-300 mt-1">选择系统的主色调</p>
      </div>
      <div class="p-5 sm:p-6">
        <div class="flex flex-wrap gap-4">
          <button
            v-for="theme in THEME_PRESETS"
            :key="theme.name"
            @click="applyTheme(theme.name)"
            class="w-12 h-12 rounded-xl border-2 transition-all duration-200 flex items-center justify-center shadow-sm hover:shadow-md hover:-translate-y-0.5"
            :class="currentTheme === theme.name ? 'border-current shadow-md' : 'border-slate-200 dark:border-slate-700 hover:border-slate-300'"
            :style="{ color: theme.color, borderColor: currentTheme === theme.name ? theme.color : '' }"
            :title="theme.label"
          >
            <span
              class="w-6 h-6 rounded-full"
              :style="{ backgroundColor: theme.color }"
            ></span>
          </button>
        </div>
        <div class="mt-5 flex items-center gap-3 p-3 rounded-xl bg-gradient-to-r from-slate-50 to-slate-100 border border-slate-100 dark:border-slate-700">
          <div class="w-8 h-8 rounded-lg bg-gradient-to-br flex items-center justify-center text-white text-xs font-bold shadow"
               :class="currentGradient">
            ZY
          </div>
          <div>
            <p class="text-sm font-medium text-slate-700 dark:text-slate-600">{{ currentLabel }}</p>
            <p class="text-xs text-slate-400 dark:text-slate-300">预览效果</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 深色模式 -->
    <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
      <div class="px-5 sm:px-6 py-4 border-b border-slate-50 dark:border-slate-800">
        <h3 class="text-base font-semibold text-slate-800 dark:text-slate-200 flex items-center gap-2.5">
          <el-icon :size="18" color="#64748b"><Moon /></el-icon>深色模式
        </h3>
        <p class="text-xs text-slate-400 dark:text-slate-300 mt-1">切换亮色/暗色界面风格</p>
      </div>
      <div class="p-5 sm:p-6">
        <div class="flex items-center justify-between py-2">
          <div>
            <span class="text-sm font-medium text-slate-700 dark:text-slate-600">启用深色模式</span>
            <p class="text-xs text-slate-400 dark:text-slate-300 mt-1">降低屏幕亮度，适合夜间使用</p>
          </div>
          <el-switch
            :model-value="isDark"
            @change="toggleDark"
            size="large"
            active-color="#0f172a"
            inactive-color="#cbd5e1"
          />
        </div>
      </div>
    </div>

    <!-- 导航设置 -->
    <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden">
      <div class="px-5 sm:px-6 py-4 border-b border-slate-50 dark:border-slate-800">
        <h3 class="text-base font-semibold text-slate-800 dark:text-slate-200 flex items-center gap-2.5">
          <el-icon :size="18" color="#64748b"><Guide /></el-icon>导航设置
        </h3>
        <p class="text-xs text-slate-400 dark:text-slate-300 mt-1">设置面包屑导航的展示方式</p>
      </div>
      <div class="p-5 sm:p-6">
        <el-radio-group :model-value="breadcrumbMode" @change="setBreadcrumbMode" class="flex flex-col gap-4">
          <div
            class="flex items-start gap-3 p-4 rounded-xl border-2 transition-all duration-200 cursor-pointer"
            :class="breadcrumbMode === 'simple' ? 'border-sky-400 bg-sky-50/50' : 'border-slate-100 dark:border-slate-700 hover:border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800'"
            @click="setBreadcrumbMode('simple')"
          >
            <el-radio value="simple" class="!mr-0 mt-0.5" />
            <div>
              <p class="text-sm font-medium text-slate-700 dark:text-slate-600">简洁模式</p>
              <p class="text-xs text-slate-400 dark:text-slate-300 mt-0.5">仅显示「首页 / 当前页面」</p>
            </div>
          </div>
          <div
            class="flex items-start gap-3 p-4 rounded-xl border-2 transition-all duration-200 cursor-pointer"
            :class="breadcrumbMode === 'hierarchical' ? 'border-sky-400 bg-sky-50/50' : 'border-slate-100 dark:border-slate-700 hover:border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800'"
            @click="setBreadcrumbMode('hierarchical')"
          >
            <el-radio value="hierarchical" class="!mr-0 mt-0.5" />
            <div>
              <p class="text-sm font-medium text-slate-700 dark:text-slate-600">层级模式</p>
              <p class="text-xs text-slate-400 dark:text-slate-300 mt-0.5">显示完整菜单路径，如「首页 / 系统管理 / 用户管理」</p>
            </div>
          </div>
        </el-radio-group>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Brush, Moon, Guide } from '@element-plus/icons-vue'
import { useTheme, THEME_PRESETS } from '@/composables/useTheme'

const { currentTheme, isDark, breadcrumbMode, applyTheme, toggleDark, setBreadcrumbMode } = useTheme()

const currentGradient = computed(() => {
  const preset = THEME_PRESETS.find(t => t.name === currentTheme.value)
  return preset ? preset.gradient : 'from-sky-400 to-blue-600'
})

const currentLabel = computed(() => {
  const preset = THEME_PRESETS.find(t => t.name === currentTheme.value)
  return preset ? preset.label : '天空蓝'
})
</script>
