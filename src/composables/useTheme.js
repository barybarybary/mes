import { ref } from 'vue'

// 预设主题配置
export const THEME_PRESETS = [
  { name: 'sky', label: '天空蓝', color: '#0ea5e9', gradient: 'from-sky-400 to-blue-600' },
  { name: 'emerald', label: '翡翠绿', color: '#10b981', gradient: 'from-emerald-400 to-teal-600' },
  { name: 'violet', label: '紫罗兰', color: '#8b5cf6', gradient: 'from-violet-400 to-purple-600' },
  { name: 'amber', label: '琥珀橙', color: '#f59e0b', gradient: 'from-amber-400 to-orange-600' },
  { name: 'rose', label: '玫红', color: '#f43f5e', gradient: 'from-rose-400 to-pink-600' },
]

const THEME_KEY = 'app-theme'
const DARK_KEY = 'app-dark-mode'
const BREADCRUMB_MODE_KEY = 'app-breadcrumb-mode'

function loadFromStorage(key, fallback) {
  try {
    const val = localStorage.getItem(key)
    if (val === null || val === undefined) return fallback
    if (typeof fallback === 'boolean') return val === 'true'
    return val
  } catch { return fallback }
}

// 模块级单例 ref（跨组件共享）
const currentTheme = ref(loadFromStorage(THEME_KEY, 'sky'))
const isDark = ref(loadFromStorage(DARK_KEY, false))
const breadcrumbMode = ref(loadFromStorage(BREADCRUMB_MODE_KEY, 'simple'))

export function useTheme() {
  function applyTheme(themeName) {
    currentTheme.value = themeName
    document.documentElement.setAttribute('data-theme', themeName)
    localStorage.setItem(THEME_KEY, themeName)
  }

  function toggleDark(enabled) {
    isDark.value = enabled
    document.documentElement.classList.toggle('dark', enabled)
    localStorage.setItem(DARK_KEY, String(enabled))
  }

  function setBreadcrumbMode(mode) {
    breadcrumbMode.value = mode
    localStorage.setItem(BREADCRUMB_MODE_KEY, mode)
  }

  function init() {
    document.documentElement.setAttribute('data-theme', currentTheme.value)
    document.documentElement.classList.toggle('dark', isDark.value)
  }

  return {
    currentTheme,
    isDark,
    breadcrumbMode,
    applyTheme,
    toggleDark,
    setBreadcrumbMode,
    init,
  }
}
