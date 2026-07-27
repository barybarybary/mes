import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { menuConfig } from '@/config/menus'

// 层级路径（从菜单树解析出的完整路径）
const hierarchicalPath = ref([])

export function useBreadcrumb() {
  const route = useRoute()

  function resolveMenuPath(targetPath, menus) {
    if (!menus || !Array.isArray(menus)) return []

    for (const menu of menus) {
      // 叶子菜单：直接匹配 path
      if (!menu.children && menu.path === targetPath) {
        return [{ name: menu.name, path: menu.path }]
      }
      // 目录：递归搜索子节点
      if (menu.children?.length) {
        const found = resolveMenuPath(targetPath, menu.children)
        if (found.length) {
          // 目录不参与跳转，所以 path 为空
          return [{ name: menu.name, path: '' }, ...found]
        }
      }
    }
    return []
  }

  function updatePath() {
    const currentPath = route.path

    // 首页特殊处理
    if (currentPath === '/dashboard') {
      hierarchicalPath.value = [{ name: '首页', path: '/dashboard' }]
      return
    }

    const resolved = resolveMenuPath(currentPath, menuConfig)
    hierarchicalPath.value = resolved.length
      ? resolved
      : [{ name: route.meta?.title || currentPath, path: currentPath }]
  }

  // 监听路由变化
  watch(() => route.path, updatePath, { immediate: true })

  return { hierarchicalPath }
}
