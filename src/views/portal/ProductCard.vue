<template>
  <router-link :to="`/portal/products/${product.id}`" class="bg-white rounded-xl shadow-sm border border-slate-100 overflow-hidden block hover:shadow-md transition no-underline">
    <div class="h-40 bg-gradient-to-br from-slate-100 to-slate-200 flex items-center justify-center text-slate-400 text-4xl">
      📦
    </div>
    <div class="p-3">
      <h3 class="text-sm font-medium text-slate-800 truncate">{{ product.name }}</h3>
      <p class="text-xs text-slate-400 mt-0.5 truncate">{{ product.spec || product.code }}</p>
      <div class="flex items-center justify-between mt-2">
        <span class="text-sky-600 font-bold text-base">¥{{ product.price }}</span>
        <span class="text-xs text-slate-400">{{ product.unit }}</span>
      </div>
    </div>
  </router-link>
</template>

<script setup>
defineProps({ product: Object })
</script>
