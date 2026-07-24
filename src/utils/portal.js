/**
 * 门户前台共享工具函数
 */

/** 将分类树展平为带缩进深度的列表（用于分类标签展示） */
export function flattenTree(tree, depth = 0) {
  const result = []
  for (const node of tree) {
    result.push({ ...node, _depth: depth })
    if (node.children && node.children.length > 0) {
      result.push(...flattenTree(node.children, depth + 1))
    }
  }
  return result
}

/** 订单状态 → 样式类名映射 */
export function statusClass(status) {
  const map = {
    1: 'bg-amber-50 text-amber-600',
    2: 'bg-blue-50 text-blue-600',
    3: 'bg-purple-50 text-purple-600',
    4: 'bg-sky-50 text-sky-600',
    5: 'bg-green-50 text-green-600',
    6: 'bg-red-50 text-red-500'
  }
  return map[status] || 'bg-slate-100 text-slate-600'
}
