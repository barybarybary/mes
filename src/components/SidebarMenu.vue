<template>
  <template v-for="menu in menuList" :key="menu.id">
    <!-- 目录 -->
    <el-sub-menu v-if="menu.type === 1 && menu.children?.length" :index="menu.path || String(menu.id)">
      <template #title>
        <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
        <span>{{ menu.name }}</span>
      </template>
      <SidebarMenu :menu-list="menu.children" />
    </el-sub-menu>

    <!-- 菜单项 -->
    <el-menu-item v-else-if="menu.type === 2 && menu.visible !== 0" :index="menu.path">
      <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
      <template #title>{{ menu.name }}</template>
    </el-menu-item>
  </template>
</template>

<script setup>
/* eslint-disable no-undef */
defineProps({
  menuList: { type: Array, default: () => [] }
})
</script>
