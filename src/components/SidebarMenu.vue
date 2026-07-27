<template>
  <template v-for="item in visibleMenus" :key="item.id">
    <!-- 目录（有 children） -->
    <el-sub-menu v-if="item.children?.length" :index="item.path || item.id">
      <template #title>
        <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
        <span>{{ item.name }}</span>
      </template>
      <!-- 递归子菜单 -->
      <SidebarMenu v-if="item.children?.length" :menu-list="visibleChildren(item)" />
    </el-sub-menu>

    <!-- 叶子菜单项 -->
    <el-menu-item v-else :index="item.path">
      <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
      <template #title>{{ item.name }}</template>
    </el-menu-item>
  </template>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  menuList: { type: Array, default: () => [] }
})

const userStore = useUserStore()

const isAdmin = computed(() => userStore.roles.some(r => r.code === 'admin'))

function canSee(perm) {
  if (!perm) return true           // 无权限要求，所有人可见
  if (isAdmin.value) return true   // 管理员全可见
  return userStore.permissions.includes(perm)
}

const visibleMenus = computed(() =>
  props.menuList.filter(item => {
    // 叶子节点：检查自身权限
    if (!item.children) return canSee(item.permission)
    // 目录节点：有至少一个可见子节点才显示
    return visibleChildren(item).length > 0
  })
)

function visibleChildren(item) {
  if (!item.children) return []
  return item.children.filter(child => canSee(child.permission))
}
</script>
