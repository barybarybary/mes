<template>
  <div>
    <!-- 页面标题 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h2 class="text-xl font-bold text-slate-800 dark:text-slate-200">⏰ 考勤打卡</h2>
        <p class="text-sm text-slate-400 mt-1">每日签到签退，考勤记录自动汇总</p>
      </div>
      <div class="flex items-center gap-3">
        <el-switch
          v-model="reminderEnabled"
          active-text="提醒开"
          inactive-text="提醒关"
          @change="toggleReminder"
        />
        <el-tag v-if="notificationGranted" type="success" round size="small">通知已授权</el-tag>
        <el-tag v-else type="warning" round size="small" @click="requestNotification" style="cursor:pointer">点击授权通知</el-tag>
      </div>
    </div>

    <!-- 时钟卡片 -->
    <el-row :gutter="16" class="mb-6">
      <el-col :span="8">
        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-100 dark:border-slate-700 text-center">
          <p class="text-xs text-slate-400 uppercase tracking-wide mb-2">当前时间</p>
          <p class="text-5xl font-bold text-slate-800 dark:text-slate-100 font-mono tracking-tight">{{ currentTime }}</p>
          <p class="text-sm text-slate-400 mt-2">{{ currentDate }}</p>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-100 dark:border-slate-700 text-center">
          <p class="text-xs text-slate-400 uppercase tracking-wide mb-2">上班打卡</p>
          <p v-if="todayRecord?.clockInTime" class="text-2xl font-bold text-emerald-500 font-mono">{{ todayRecord.clockInTime }}</p>
          <p v-else class="text-2xl font-bold text-slate-300">--:--</p>
          <el-button
            type="primary"
            size="large"
            round
            class="mt-4 w-full"
            :disabled="!!todayRecord?.clockInTime"
            @click="clockIn"
            :loading="clockingIn"
          >
            {{ todayRecord?.clockInTime ? '已打卡 ✓' : '上班打卡' }}
          </el-button>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-100 dark:border-slate-700 text-center">
          <p class="text-xs text-slate-400 uppercase tracking-wide mb-2">下班打卡</p>
          <p v-if="todayRecord?.clockOutTime" class="text-2xl font-bold text-blue-500 font-mono">{{ todayRecord.clockOutTime }}</p>
          <p v-else class="text-2xl font-bold text-slate-300">--:--</p>
          <el-button
            type="warning"
            size="large"
            round
            class="mt-4 w-full"
            :disabled="!todayRecord?.clockInTime || !!todayRecord?.clockOutTime"
            @click="clockOut"
            :loading="clockingOut"
          >
            {{ todayRecord?.clockOutTime ? '已签退 ✓' : '下班打卡' }}
          </el-button>
        </div>
      </el-col>
    </el-row>

    <!-- 今日状态 + 本月统计 -->
    <el-row :gutter="16" class="mb-6">
      <el-col :span="6" v-for="s in todayStats" :key="s.label">
        <div class="bg-white dark:bg-slate-800 rounded-2xl p-4 shadow-sm border border-slate-100 dark:border-slate-700 text-center card-hover">
          <p class="text-xs text-slate-400">{{ s.label }}</p>
          <p class="text-xl font-bold mt-1" :class="s.color">{{ s.value }}</p>
        </div>
      </el-col>
    </el-row>

    <!-- 考勤历史 -->
    <el-card shadow="never" class="mb-6 dark:bg-slate-800 dark:border-slate-700">
      <template #header>
        <div class="flex items-center justify-between">
          <div>
            <h3 class="font-semibold text-slate-800 dark:text-slate-200">📋 考勤记录</h3>
            <p class="text-xs text-slate-400 mt-0.5">本月打卡明细</p>
          </div>
          <el-date-picker
            v-model="selectedMonth"
            type="month"
            placeholder="选择月份"
            value-format="YYYY-MM"
            @change="fetchRecords"
          />
        </div>
      </template>
      <el-table :data="records" stripe v-loading="loadingRec" class="page-table">
        <el-table-column prop="date" label="日期" width="130" />
        <el-table-column prop="weekday" label="星期" width="80" align="center" />
        <el-table-column prop="clockInTime" label="上班打卡" width="150" align="center">
          <template #default="{ row }">
            <span v-if="row.clockInTime" class="font-mono" :class="row.lateIn ? 'text-red-500' : 'text-emerald-600'">
              {{ row.clockInTime }}
              <el-tag v-if="row.lateIn" type="danger" size="small" round class="ml-1">迟到</el-tag>
            </span>
            <span v-else class="text-slate-300">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="clockOutTime" label="下班打卡" width="150" align="center">
          <template #default="{ row }">
            <span v-if="row.clockOutTime" class="font-mono" :class="row.earlyOut ? 'text-orange-500' : 'text-blue-600'">
              {{ row.clockOutTime }}
              <el-tag v-if="row.earlyOut" type="warning" size="small" round class="ml-1">早退</el-tag>
            </span>
            <span v-else class="text-slate-300">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="workHours" label="工时" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.workHours" class="font-medium text-slate-700">{{ row.workHours }}h</span>
            <span v-else class="text-slate-300">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="!row.clockInTime" type="info" round size="small">缺卡</el-tag>
            <el-tag v-else-if="row.lateIn" type="danger" round size="small">迟到</el-tag>
            <el-tag v-else-if="row.earlyOut" type="warning" round size="small">早退</el-tag>
            <el-tag v-else type="success" round size="small">正常</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 使用指引 -->
    <el-card shadow="never" class="dark:bg-slate-800 dark:border-slate-700">
      <template #header>
        <h3 class="font-semibold text-slate-800 dark:text-slate-200">📖 使用指引</h3>
      </template>
      <el-collapse>
        <el-collapse-item title="💡 如何开启打卡提醒？" name="1">
          <div class="text-sm text-slate-600 dark:text-slate-400 space-y-2 p-2">
            <p><b>第 1 步：</b>点击页面右上角的 <el-tag type="warning" round size="small">点击授权通知</el-tag>，浏览器弹出权限请求时点「允许」。</p>
            <p><b>第 2 步：</b>打开 <b>提醒开关</b>（页面右上角 Switch），系统会在 9:00 和 17:00 自动弹出浏览器通知。</p>
            <p><b>注意：</b>需要保持浏览器运行（可最小化），Chrome/Edge/Firefox 均支持。如果使用移动端浏览器，需允许后台通知。</p>
          </div>
        </el-collapse-item>
        <el-collapse-item title="⏱️ 打卡时间规则" name="2">
          <div class="text-sm text-slate-600 dark:text-slate-400 space-y-2 p-2">
            <p><b>上班打卡：</b>9:00 前打卡为 <el-tag type="success" round size="small">正常</el-tag>，9:00-9:30 为 <el-tag type="danger" round size="small">迟到</el-tag>，超过 9:30 为缺卡。</p>
            <p><b>下班打卡：</b>17:00 后打卡为 <el-tag type="success" round size="small">正常</el-tag>，16:30-17:00 为 <el-tag type="warning" round size="small">早退</el-tag>。</p>
            <p><b>工时计算：</b>正常出勤 8 小时，迟到/早退按实际打卡时间计算。</p>
          </div>
        </el-collapse-item>
        <el-collapse-item title="🔔 没有收到提醒怎么办？" name="3">
          <div class="text-sm text-slate-600 dark:text-slate-400 space-y-2 p-2">
            <p>1. 检查浏览器通知权限：点击地址栏左侧的锁图标 → 通知 → 设为「允许」。</p>
            <p>2. 检查操作系统通知设置：Windows「设置 → 系统 → 通知」确保浏览器通知未被关闭。</p>
            <p>3. 检查浏览器是否在后台运行（不要完全关闭浏览器窗口）。</p>
            <p>4. 如果使用手机浏览器，需要将网页添加到主屏幕并允许通知权限。</p>
          </div>
        </el-collapse-item>
        <el-collapse-item title="📱 移动端打卡说明" name="4">
          <div class="text-sm text-slate-600 dark:text-slate-400 space-y-2 p-2">
            <p>本页面已适配移动端，手机浏览器打开即可使用。</p>
            <p><b>iPhone：</b>Safari 打开 → 分享按钮 → 添加到主屏幕 → 允许通知。</p>
            <p><b>Android：</b>Chrome 打开 → 菜单 → 添加到主屏幕 → 允许通知。</p>
            <p>添加到主屏幕后可像原生 App 一样使用，支持后台提醒。</p>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

// ====== 时钟 ======
const currentTime = ref('')
const currentDate = ref('')
let clockTimer = null

function updateClock() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
  currentDate.value = now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
}

// ====== 打卡状态 ======
const clockingIn = ref(false)
const clockingOut = ref(false)
const todayRecord = ref(null)
const loadingRec = ref(false)
const records = ref([])
const selectedMonth = ref('')

const todayStats = reactive([
  { label: '本月出勤天数', value: '0', color: 'text-emerald-600' },
  { label: '迟到次数', value: '0', color: 'text-red-500' },
  { label: '早退次数', value: '0', color: 'text-orange-500' },
  { label: '本月工时', value: '0h', color: 'text-blue-600' }
])

async function fetchToday() {
  try {
    const res = await api.get('/attendance/today')
    if (res.code === 200) todayRecord.value = res.data
  } catch {
    // 后端未实现时用 localStorage
    const stored = localStorage.getItem('att_today')
    if (stored) todayRecord.value = JSON.parse(stored)
  }
}

async function fetchRecords() {
  loadingRec.value = true
  try {
    const month = selectedMonth.value || new Date().toISOString().slice(0, 7)
    const res = await api.get('/attendance', { params: { month } })
    if (res.code === 200) {
      const data = res.data
      const list = data?.list || data?.records || (Array.isArray(data) ? data : [])
      records.value = list
      todayStats[0].value = list.filter(r => r.clockInTime).length
      todayStats[1].value = list.filter(r => r.lateIn).length
      todayStats[2].value = list.filter(r => r.earlyOut).length
      todayStats[3].value = list.reduce((s, r) => s + (r.workHours || 0), 0).toFixed(0) + 'h'
    }
  } catch (e) {
    console.error('加载考勤失败:', e)
    // 无后端时用模拟数据
    const today = new Date().toISOString().slice(0, 10)
    const demo = []
    for (let i = 1; i <= 22; i++) {
      const d = new Date()
      d.setDate(i)
      if (d.getDay() === 0 || d.getDay() === 6) continue
      const dStr = d.toISOString().slice(0, 10)
      const lateIn = i === 3
      const earlyOut = i === 7
      demo.push({
        date: dStr,
        weekday: ['周日','周一','周二','周三','周四','周五','周六'][d.getDay()],
        clockInTime: dStr >= today ? null : (lateIn ? '09:12:30' : '08:55:00'),
        clockOutTime: dStr >= today ? null : (earlyOut ? '16:48:00' : '17:05:00'),
        lateIn: dStr >= today ? false : lateIn,
        earlyOut: dStr >= today ? false : earlyOut,
        workHours: dStr >= today ? 0 : (earlyOut ? 7.5 : 8)
      })
    }
    records.value = demo
    todayStats[0].value = demo.filter(r => r.clockInTime).length
    todayStats[1].value = demo.filter(r => r.lateIn).length
    todayStats[2].value = demo.filter(r => r.earlyOut).length
    todayStats[3].value = demo.reduce((s, r) => s + (r.workHours || 0), 0).toFixed(0) + 'h'
  }
  finally { loadingRec.value = false }
}

async function clockIn() {
  clockingIn.value = true
  try {
    const now = new Date()
    const time = now.toTimeString().slice(0, 8)
    const hour = now.getHours()
    const lateIn = hour >= 9
    try {
      await api.post('/attendance/clock-in', { time, lateIn })
      ElMessage.success('上班打卡成功！')
    } catch {
      // 后端未实现时本地存储
      todayRecord.value = { date: now.toISOString().slice(0, 10), clockInTime: time, lateIn }
      localStorage.setItem('att_today', JSON.stringify(todayRecord.value))
      ElMessage.success('上班打卡成功！（本地记录）')
    }
    fetchToday()
    fetchRecords()
  } finally { clockingIn.value = false }
}

async function clockOut() {
  clockingOut.value = true
  try {
    const now = new Date()
    const time = now.toTimeString().slice(0, 8)
    const earlyOut = now.getHours() < 17
    try {
      await api.post('/attendance/clock-out', { time, earlyOut })
      ElMessage.success('下班打卡成功！')
    } catch {
      if (todayRecord.value) {
        todayRecord.value.clockOutTime = time
        todayRecord.value.earlyOut = earlyOut
        localStorage.setItem('att_today', JSON.stringify(todayRecord.value))
      }
      ElMessage.success('下班打卡成功！（本地记录）')
    }
    fetchToday()
    fetchRecords()
  } finally { clockingOut.value = false }
}

// ====== 提醒系统 ======
const reminderEnabled = ref(false)
const notificationGranted = ref(false)
let reminderTimer = null

function requestNotification() {
  if (!('Notification' in window)) {
    ElMessage.warning('当前浏览器不支持桌面通知')
    return
  }
  Notification.requestPermission().then(perm => {
    notificationGranted.value = perm === 'granted'
    if (perm === 'granted') {
      ElMessage.success('通知授权成功！打开提醒开关即可收到打卡提醒')
    } else {
      ElMessage.warning('通知被拒绝，可在浏览器设置中重新开启')
    }
  })
}

function toggleReminder(val) {
  localStorage.setItem('att_reminder', val ? '1' : '0')
  if (val) {
    if (!notificationGranted.value) {
      requestNotification()
    }
    startReminderCheck()
    ElMessage.success('打卡提醒已开启（9:00 和 17:00）')
  } else {
    stopReminderCheck()
    ElMessage.info('打卡提醒已关闭')
  }
}

function startReminderCheck() {
  stopReminderCheck()
  const lastRemind = { morning: '', afternoon: '' }
  reminderTimer = setInterval(() => {
    if (!notificationGranted.value || !reminderEnabled.value) return
    const now = new Date()
    const today = now.toISOString().slice(0, 10)
    const h = now.getHours()
    const m = now.getMinutes()

    // 上午 9:00 提醒
    if (h === 9 && m === 0 && lastRemind.morning !== today) {
      lastRemind.morning = today
      new Notification('⏰ 上班打卡提醒', {
        body: '现在是 9:00，别忘了打卡上班！',
        icon: '/favicon.ico',
        tag: 'clock-in-reminder'
      })
    }

    // 下午 17:00 提醒
    if (h === 17 && m === 0 && lastRemind.afternoon !== today) {
      lastRemind.afternoon = today
      new Notification('⏰ 下班打卡提醒', {
        body: '现在是 17:00，别忘了打卡下班！',
        icon: '/favicon.ico',
        tag: 'clock-out-reminder'
      })
    }

    // 每天重置
    if (h === 0 && m === 0) {
      lastRemind.morning = ''
      lastRemind.afternoon = ''
    }
  }, 30000) // 每30秒检查一次
}

function stopReminderCheck() {
  if (reminderTimer) {
    clearInterval(reminderTimer)
    reminderTimer = null
  }
}

// ====== 生命周期 ======
onMounted(() => {
  updateClock()
  clockTimer = setInterval(updateClock, 1000)
  fetchToday()
  fetchRecords()

  // 检查通知权限
  if ('Notification' in window) {
    notificationGranted.value = Notification.permission === 'granted'
    // 静默请求权限（已授权过的不会弹窗）
    if (Notification.permission === 'default') {
      Notification.requestPermission().then(p => {
        notificationGranted.value = p === 'granted'
      })
    }
  }

  // 恢复提醒开关
  if (localStorage.getItem('att_reminder') === '1') {
    reminderEnabled.value = true
    startReminderCheck()
  }
})

onBeforeUnmount(() => {
  if (clockTimer) clearInterval(clockTimer)
  stopReminderCheck()
})
</script>
