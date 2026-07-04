// 屏蔽 webpack-dev-server 的 ResizeObserver 无害报错
window.addEventListener('error', function (e) {
  if (e.message && e.message.includes('ResizeObserver')) {
    e.stopImmediatePropagation()
  }
})

import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { createPinia } from 'pinia'
import './styles/tailwind.css'
import App from './App.vue'
import router from './router'
import permissionDirective from './directives/permission'
import { useTheme } from '@/composables/useTheme'

// 在挂载前初始化主题，避免页面闪烁
const { init } = useTheme()
init()

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.directive('permission', permissionDirective)
app.use(ElementPlus, { locale: zhCn })
app.use(createPinia())
app.use(router)
app.mount('#app')
