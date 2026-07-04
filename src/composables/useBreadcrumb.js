import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 层级路径（从菜单树解析出的完整路径）
const hierarchicalPath = ref([])

export function useBreadcrumb() {
  const route = useRoute()
  const userStore = useUserStore()

  function resolveMenuPath(targetPath, menus) {
    if (!menus || !Array.isArray(menus)) return []

    for (const menu of menus) {
      // 叶子菜单：path 匹配
      if (menu.type === 2 && menu.path === targetPath) {
        return [{ name: menu.name, path: menu.path }]
      }
      // 目录：递归搜索子节点
      if (menu.children && menu.children.length) {
        const found = resolveMenuPath(targetPath, menu.children)
        if (found.length) {
          // 目录不参与跳转，所以 path 为空
          return [{ name: menu.name, path: '' }, ...found]
        }
      }
    }
    // 顶层路径匹配（dashboard 等可能在根级）
    for (const menu of menus) {
      if (menu.path === targetPath) {
        return [{ name: menu.name, path: menu.path }]
      }
    }
    return []
  }

  function updatePath() {
    const currentPath = route.path
    const menus = userStore.menus

    if (!menus || !menus.length) {
      // 菜单尚未加载，稍后重试
      hierarchicalPath.value = []
      return
    }

    // 首页特殊处理
    if (currentPath === '/dashboard') {
      hierarchicalPath.value = [{ name: '首页', path: '/dashboard' }]
      return
    }

    const resolved = resolveMenuPath(currentPath, menus)
    hierarchicalPath.value = resolved.length ? resolved : [{ name: route.meta?.title || currentPath, path: currentPath }]
  }

  // 监听路由变化
  watch(() => route.path, updatePath, { immediate: true })

  // 监听菜单加载（登录后菜单才从后端拉取）
  watch(() => userStore.menus, updatePath)

  return { hierarchicalPath }
}
