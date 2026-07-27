<template>
  <div
    class="group relative bg-white rounded-2xl border border-slate-100 overflow-hidden shadow-soft transition-all duration-500 hover:shadow-card-hover hover:-translate-y-2 cursor-pointer"
    @click="$emit('click', product)"
  >
    <div class="relative aspect-square bg-gradient-to-br from-slate-50 to-sky-50 overflow-hidden">
      <img
        v-if="product.imageUrl"
        :src="product.imageUrl"
        :alt="product.name"
        class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
      />
      <div
        v-else
        class="w-full h-full flex flex-col items-center justify-center relative overflow-hidden"
        :style="{ background: placeholderBg }"
      >
        <div class="absolute inset-0 opacity-10">
          <div class="absolute -top-6 -right-6 w-24 h-24 rounded-full bg-white"></div>
          <div class="absolute -bottom-8 -left-8 w-32 h-32 rounded-full bg-white"></div>
        </div>
        <span class="text-xs text-white/60 font-medium">{{ product.categoryName || '工业品' }}</span>
      </div>

      <div class="absolute top-3 left-3 flex flex-col gap-2">
        <span class="portal-badge portal-badge-primary backdrop-blur-sm bg-white/90">
          {{ product.categoryName || '未分类' }}
        </span>
      </div>

      <div class="absolute top-3 right-3">
        <span
          v-if="product.stockQty > 0"
          class="portal-badge portal-badge-success backdrop-blur-sm bg-white/90"
        >
          <el-icon :size="11"><CircleCheck /></el-icon>
          有货
        </span>
        <span v-else class="portal-badge portal-badge-slate backdrop-blur-sm bg-white/90">
          缺货
        </span>
      </div>

      <div class="absolute inset-0 bg-gradient-to-t from-black/20 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-end justify-center pb-6">
        <button
          class="portal-btn-primary !py-2.5 !px-6 !text-sm translate-y-4 group-hover:translate-y-0 transition-transform duration-300"
          :disabled="!product.stockQty || product.stockQty <= 0"
          @click.stop="$emit('add-to-cart', product)"
        >
          <el-icon :size="16"><ShoppingCart /></el-icon>
          {{ product.stockQty > 0 ? '加入购物车' : '暂时缺货' }}
        </button>
      </div>
    </div>

    <div class="p-5">
      <h3 class="font-semibold text-slate-800 text-sm mb-2 line-clamp-2 group-hover:text-sky-600 transition-colors duration-200 leading-snug min-h-[40px]">
        {{ product.name }}
      </h3>
      <p class="text-xs text-slate-400 mb-4" v-if="product.spec">规格: {{ product.spec }}</p>

      <div class="flex items-end justify-between">
        <div class="flex items-baseline gap-1">
          <span class="text-xs text-rose-500 font-medium">¥</span>
          <span class="text-2xl font-bold text-rose-500 leading-none">{{ product.price }}</span>
          <span class="text-xs text-slate-400 ml-1">/ {{ product.unit || 'pcs' }}</span>
        </div>
        <div class="text-xs text-slate-400">
          库存 {{ product.stockQty || 0 }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { placeholderGradient } from '@/utils/placeholder'

const props = defineProps({
  product: { type: Object, required: true }
})

defineEmits(['click', 'add-to-cart'])

const placeholderBg = computed(() => placeholderGradient(props.product.id))
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
