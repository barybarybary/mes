<template>
  <el-tag
    :type="tagType"
    :effect="effect"
    size="small"
    round
  >{{ label }}</el-tag>
</template>

<script setup>
/* eslint-disable no-undef */
import { computed } from 'vue'

const props = defineProps({
  status: { type: [Number, String], required: true },
  effect: { type: String, default: 'light' }
})

const STATUS_MAP = {
  1: { label: '待审核', type: 'info' },
  2: { label: '已审核', type: '' },
  3: { label: '生产中', type: 'primary' },
  4: { label: '部分发货', type: 'warning' },
  5: { label: '已完成', type: 'success' },
  6: { label: '已取消', type: 'danger' }
}

const tagType = computed(() => STATUS_MAP[props.status]?.type || 'info')
const label = computed(() => STATUS_MAP[props.status]?.label || '未知')
</script>
