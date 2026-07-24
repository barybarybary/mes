<template>
  <div class="min-h-screen bg-slate-50 dark:bg-slate-900">
    <PortalNavbar />
    <div class="max-w-3xl mx-auto px-4 py-8">
      <h2 class="text-xl font-bold text-slate-800 dark:text-slate-200 mb-6">购物车</h2>

      <div v-if="loading" class="text-center py-10 text-slate-400 dark:text-slate-500">加载中...</div>
      <div v-else-if="items.length === 0" class="text-center py-16">
        <p class="text-5xl mb-4">🛒</p>
        <p class="text-slate-400 dark:text-slate-500 mb-4">购物车是空的</p>
        <router-link to="/portal/products" class="text-sky-500 hover:text-sky-600">去逛逛 →</router-link>
      </div>
      <div v-else>
        <!-- 商品列表（可多选） -->
        <div class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border dark:border-slate-700 overflow-hidden">
          <!-- 全选行 -->
          <div class="flex items-center gap-4 p-4 bg-slate-50 dark:bg-slate-900 border-b dark:border-slate-700 text-sm text-slate-500 dark:text-slate-400">
            <el-checkbox v-model="allChecked" :indeterminate="indeterminate" @change="toggleAll" />
            <span class="flex-1">商品信息</span>
            <span class="w-24 text-center">数量</span>
            <span class="w-20 text-right">小计</span>
            <span class="w-10"></span>
          </div>
          <div v-for="item in items" :key="item.productId" class="flex items-center gap-4 p-4 border-b dark:border-slate-700 last:border-0">
            <el-checkbox v-model="item.checked" @change="calcChecked" />
            <div class="flex-1 min-w-0">
              <p class="font-medium text-slate-800 dark:text-slate-200 truncate">{{ item.productName }}</p>
              <p class="text-xs text-slate-400 dark:text-slate-500">{{ item.spec }} | ¥{{ item.price }}/{{ item.unit }}</p>
            </div>
            <div class="w-24 flex justify-center">
              <el-input-number v-model="item.quantity" :min="1" size="small" @change="updateQty(item)" />
            </div>
            <span class="text-sky-600 font-bold w-20 text-right">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
            <el-button text type="danger" :icon="Delete" circle size="small" @click="removeItem(item)" />
          </div>
        </div>

        <!-- 收货地址 -->
        <div class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border dark:border-slate-700 p-4 mt-4">
          <p class="text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">收货地址</p>
          <el-input v-model="address" placeholder="请输入收货地址" size="large" clearable />
        </div>

        <!-- 底部结算 -->
        <div class="flex justify-between items-center mt-6">
          <span class="text-sm text-slate-500 dark:text-slate-400">
            已选 <span class="text-sky-600 font-bold">{{ checkedCount }}</span> 件
          </span>
          <span class="text-lg font-bold text-slate-800 dark:text-slate-200">
            合计：<span class="text-sky-600">¥{{ total.toFixed(2) }}</span>
          </span>
          <el-button type="primary" size="large" class="!rounded-xl !px-10" :disabled="checkedCount === 0" @click="placeOrder">
            提交订单
          </el-button>
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
import { usePortalStore } from '@/stores/portal'
import PortalNavbar from './PortalNavbar.vue'

const router = useRouter()
const portalStore = usePortalStore()
const items = ref([])
const loading = ref(true)
const address = ref('')
const allChecked = ref(false)
const indeterminate = ref(false)

const checkedItems = computed(() => items.value.filter(i => i.checked))
const checkedCount = computed(() => checkedItems.value.reduce((s, i) => s + i.quantity, 0))
const total = computed(() => checkedItems.value.reduce((s, i) => s + i.price * i.quantity, 0))

function calcChecked() {
  const checked = items.value.filter(i => i.checked).length
  allChecked.value = checked === items.value.length && items.value.length > 0
  indeterminate.value = checked > 0 && checked < items.value.length
}

function toggleAll(v) {
  items.value.forEach(i => (i.checked = v))
  calcChecked()
}

onMounted(fetchCart)

async function fetchCart() {
  loading.value = true
  try {
    const res = await api.get('/cart')
    items.value = (res.data || []).map(i => ({
      ...i,
      quantity: parseInt(i.quantity) || 1,
      checked: true
    }))
    calcChecked()
  } catch { /* ignore */ }
  finally { loading.value = false }
}

async function updateQty(item) {
  await api.put(`/cart/${item.productId}`, null, { params: { quantity: item.quantity } })
}

async function removeItem(item) {
  try {
    await api.put(`/cart/${item.productId}`, null, { params: { quantity: 0 } })
    items.value = items.value.filter(i => i.productId !== item.productId)
    calcChecked()
    portalStore.fetchCartCount()
  } catch { /* ignore */ }
}

async function placeOrder() {
  if (!address.value.trim()) {
    ElMessage.warning('请填写收货地址')
    return
  }
  try {
    await ElMessageBox.confirm('确认提交订单？', '确认', { type: 'info' })
    const orderItems = checkedItems.value.map(i => ({
      productId: i.productId,
      quantity: i.quantity
    }))
    const res = await api.post('/orders', {
      items: orderItems,
      address: address.value
    })
    if (res.code === 200) {
      // 从购物车移除已下单商品
      const orderedIds = new Set(orderItems.map(i => i.productId))
      items.value = items.value.filter(i => !orderedIds.has(i.productId))
      calcChecked()
      portalStore.resetCart()
      portalStore.fetchCartCount()
      ElMessage.success('下单成功！')
      router.push(`/portal/orders/${res.data.id}`)
    }
  } catch { /* 取消或失败 */ }
}
</script>
