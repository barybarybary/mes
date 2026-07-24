<template>
  <router-link :to="`/portal/products/${product.id}`" class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-slate-100 dark:border-slate-700 overflow-hidden block hover:shadow-md transition no-underline">
    <div class="h-40 bg-gradient-to-br from-slate-100 to-slate-200 dark:from-slate-700 dark:to-slate-600 flex items-center justify-center text-slate-400 dark:text-slate-500 text-4xl overflow-hidden">
      <img v-if="product.imageUrl && !imageError" :src="product.imageUrl" :alt="product.name" class="w-full h-full object-cover" @error="imageError = true" />
      <span v-if="!product.imageUrl || imageError" class="text-4xl">📦</span>
    </div>
    <div class="p-3">
      <h3 class="text-sm font-medium text-slate-800 dark:text-slate-200 truncate">{{ product.name }}</h3>
      <p class="text-xs text-slate-400 dark:text-slate-500 mt-0.5 truncate">{{ product.spec || product.code }}</p>
      <div class="flex items-center justify-between mt-2">
        <span class="text-sky-600 font-bold text-base">¥{{ product.price }}</span>
        <span class="text-xs text-slate-400 dark:text-slate-500">{{ product.unit }}</span>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { ref } from 'vue'

defineProps({ product: Object })

const imageError = ref(false)
</script>
