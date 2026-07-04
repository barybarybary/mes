import { useUserStore } from '@/stores/user'

/**
 * 检查当前用户是否拥有指定权限
 * @param {string} permission 权限标识，如 "system:user:add"
 * @returns {boolean}
 */
export function hasPermission(permission) {
  if (!permission) return true
  const store = useUserStore()
  // 管理员拥有所有权限
  if (store.roles?.some(r => r.code === 'admin')) return true
  return store.permissions?.includes(permission) ?? false
}

/**
 * 检查是否拥有任意一个权限
 * @param  {...string} permissions
 * @returns {boolean}
 */
export function hasAnyPermission(...permissions) {
  const store = useUserStore()
  if (store.roles?.some(r => r.code === 'admin')) return true
  return permissions.some(p => store.permissions?.includes(p))
}

/**
 * 检查是否拥有全部权限
 * @param  {...string} permissions
 * @returns {boolean}
 */
export function hasAllPermissions(...permissions) {
  const store = useUserStore()
  if (store.roles?.some(r => r.code === 'admin')) return true
  return permissions.every(p => store.permissions?.includes(p))
}
