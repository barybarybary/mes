import { hasPermission } from '@/utils/permission'

/**
 * v-permission 指令：无权限时移除元素
 * 用法: <el-button v-permission="'system:user:add'">新增</el-button>
 */
export default {
  mounted(el, binding) {
    const perm = binding.value
    if (perm && !hasPermission(perm)) {
      el.parentNode?.removeChild(el)
    }
  }
}
