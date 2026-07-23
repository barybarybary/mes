<template>
  <div class="min-h-screen bg-slate-50">
    <PortalNavbar />
    <div class="max-w-3xl mx-auto px-4 py-8">
      <h2 class="text-xl font-bold text-slate-800 mb-6">购物车</h2>

      <div v-if="loading" class="text-center py-10 text-slate-400">加载中...</div>
      <div v-else-if="items.length === 0" class="text-center py-16">
        <p class="text-5xl mb-4">🛒</p>
        <p class="text-slate-400 mb-4">购物车是空的</p>
        <router-link to="/portal/products" class="text-sky-500 hover:text-sky-600">去逛逛 →</router-link>
      </div>
      <div v-else>
        <div class="bg-white rounded-xl shadow-sm border overflow-hidden">
          <div v-for="item in items" :key="item.productId" class="flex items-center gap-4 p-4 border-b last:border-0">
            <div class="flex-1 min-w-0">
              <p class="font-medium text-slate-800 truncate">{{ item.productName }}</p>
              <p class="text-xs text-slate-400">{{ item.spec }} | ¥{{ item.price }}/{{ item.unit }}</p>
            </div>
            <el-input-number v-model="item.quantity" :min="1" size="small" @change="updateQty(item)" />
            <span class="text-sky-600 font-bold w-20 text-right">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
            <el-button text type="danger" :icon="Delete" circle size="small" @click="removeItem(item)" />
          </div>
        </div>
        <div class="flex justify-between items-center mt-6">
          <span class="text-lg font-bold text-slate-800">合计：<span class="text-sky-600">¥{{ total.toFixed(2) }}</span></span>
          <el-button type="primary" size="large" class="!rounded-xl !px-10" @click="placeOrder">提交订单</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import api from '@/api/portal'
import PortalNavbar from './PortalNavbar.vue'

const router = useRouter()
const items = ref([])
const loading = ref(true)

const total = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0))

onMounted(fetchCart)

async function fetchCart() {
  loading.value = true
  try {
    const res = await api.get('/cart')
    items.value = (res.data || []).map(i => ({ ...i, quantity: parseInt(i.quantity) || 1 }))
  } catch { /* ignore */ }
  finally { loading.value = false }
}

async function updateQty(item) {
  await api.put(`/cart/${item.productId}`, null, { params: { quantity: item.quantity } })
}

async function removeItem(item) {
  await api.put(`/cart/${item.productId}`, null, { params: { quantity: 0 } })
  items.value = items.value.filter(i => i.productId !== item.productId)
}

async function placeOrder() {
  try {
    await ElMessageBox.confirm('确认提交订单？', '确认', { type: 'info' })
    const orderItems = items.value.map(i => ({ productId: i.productId, quantity: i.quantity }))
    const res = await api.post('/orders', { items: orderItems })
    if (res.code === 200) {
      ElMessage.success('下单成功！')
      router.push(`/portal/orders/${res.data.id}`)
    }
  } catch { /* 取消或失败 */ }
}
</script>
