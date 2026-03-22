<template>
  <div class="dashboard">
    <!-- Stats cards -->
    <div class="stats-grid">
      <div v-for="(stat, i) in stats" :key="stat.key" class="stat-card hover-card animate-in" :class="`delay-${i+1}`">
        <div class="stat-icon" :style="{background: stat.bg}">
          <el-icon :size="22" :style="{color: stat.color}"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-change" :class="stat.changeType">
            <el-icon><component :is="stat.changeType === 'up' ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
            {{ stat.change }}
          </div>
        </div>
        <div class="stat-decoration" :style="{background: stat.bg}"></div>
      </div>
    </div>

    <!-- Charts row -->
    <div class="charts-grid animate-in delay-3">
      <div class="chart-card">
        <div class="chart-header">
          <div>
            <h3 class="chart-title">预约趋势</h3>
            <p class="chart-sub">近7天预约数据</p>
          </div>
          <el-select v-model="trendRange" size="small" style="width:100px">
            <el-option label="近7天" value="7" />
            <el-option label="近30天" value="30" />
          </el-select>
        </div>
        <div ref="trendChartRef" class="chart-canvas"></div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <div>
            <h3 class="chart-title">会议室使用率</h3>
            <p class="chart-sub">各会议室占用情况</p>
          </div>
        </div>
        <div ref="usageChartRef" class="chart-canvas"></div>
      </div>
    </div>

    <!-- Recent reservations -->
    <div class="recent-card animate-in delay-4">
      <div class="chart-header">
        <div>
          <h3 class="chart-title">最近预约</h3>
          <p class="chart-sub">今日预约记录</p>
        </div>
        <router-link to="/reservation" class="view-all">查看全部 →</router-link>
      </div>
      <el-table :data="recentReservations" class="recent-table" :show-header="true">
        <el-table-column prop="roomName" label="会议室" width="120" />
        <el-table-column prop="applicant" label="预约人" width="90" />
        <el-table-column label="预约时间" width="210">
          <template #default="{row}">
            <span style="white-space:nowrap">
              {{ row.startTime.slice(0,10) }}
              {{ row.startTime.slice(11,16) }} ~ {{ row.endTime.slice(11,16) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="purpose" label="会议用途" min-width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type" size="small">{{ statusMap[row.status]?.label }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { statisticsApi } from '@/api/statistics'
import { reservationApi } from '@/api/reservation'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
const isUser = computed(() => authStore.user?.role === 'USER')

const trendChartRef = ref(null)
const usageChartRef = ref(null)
const trendRange = ref('7')
let trendChart, usageChart

const stats = ref([
  { key: 'total', label: '会议室总数', value: 0, icon: 'OfficeBuilding', bg: 'rgba(79,110,247,0.1)', color: '#4F6EF7', change: '较上月 +2', changeType: 'up' },
  { key: 'today', label: '今日预约', value: 0, icon: 'Calendar', bg: 'rgba(16,185,129,0.1)', color: '#10B981', change: '较昨日 +5', changeType: 'up' },
  { key: 'available', label: '当前空闲', value: 0, icon: 'CircleCheck', bg: 'rgba(249,115,22,0.1)', color: '#F97316', change: '可立即预约', changeType: 'up' },
  { key: 'pending', label: '待审批', value: 0, icon: 'Stamp', bg: 'rgba(245,158,11,0.1)', color: '#F59E0B', change: '需要处理', changeType: 'down' },
])

const recentReservations = ref([])
const statusMap = {
  0: { label: '待审批', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'danger' },
  3: { label: '已取消', type: 'info' },
}


const initTrendChart = (days, values) => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: '#fff', borderColor: '#E2E8F0', textStyle: { color: '#0F172A', fontFamily: 'DM Sans' } },
    grid: { left: 0, right: 20, top: 20, bottom: 0, containLabel: true },
    xAxis: { type: 'category', data: days || ['周一','周二','周三','周四','周五','周六','周日'], axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: '#94A3B8', fontFamily: 'DM Sans' } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#F1F5F9' } }, axisLabel: { color: '#94A3B8', fontFamily: 'DM Sans' } },
    series: [{
      data: values || [0,0,0,0,0,0,0],
      type: 'line', smooth: true,
      areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(79,110,247,0.2)' }, { offset: 1, color: 'rgba(79,110,247,0.02)' }] } },
      lineStyle: { color: '#4F6EF7', width: 3 },
      itemStyle: { color: '#4F6EF7', borderWidth: 3, borderColor: '#fff' },
      symbol: 'circle', symbolSize: 8,
    }]
  })
}

const initUsageChart = (rooms, usage) => {
  if (!usageChartRef.value) return
  usageChart = echarts.init(usageChartRef.value)
  const colors = ['#4F6EF7','#10B981','#F97316','#F59E0B','#8B5CF6']
  const data = rooms
    ? rooms.map((name, i) => ({ value: usage[i], name, itemStyle: { color: colors[i % colors.length] } }))
    : []
  usageChart.setOption({
    tooltip: { trigger: 'item', backgroundColor: '#fff', borderColor: '#E2E8F0', textStyle: { color: '#0F172A' } },
    legend: { bottom: 0, left: 'center', textStyle: { color: '#64748B', fontFamily: 'DM Sans' } },
    series: [{
      type: 'pie', radius: ['48%', '72%'], center: ['50%', '44%'],
      data,
      label: { show: false },
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.1)' } }
    }]
  })
}

const handleResize = () => {
  trendChart?.resize()
  usageChart?.resize()
}

onMounted(async () => {
  // 统计卡片
  try {
    const res = await statisticsApi.overview()
    const d = res.data || res
    const roomCount = d.roomCount ?? 0
    const todayCount = d.todayCount ?? 0
    const yesterdayCount = d.yesterdayCount ?? 0
    const pendingCount = d.pendingCount ?? 0

    const todayDiff = todayCount - yesterdayCount
    const todayDiffStr = todayDiff >= 0 ? `较昨日 +${todayDiff}` : `较昨日 ${todayDiff}`

    stats.value[0].value = roomCount
    stats.value[0].change = `共 ${roomCount} 间`
    stats.value[0].changeType = 'up'

    stats.value[1].value = todayCount
    stats.value[1].change = todayDiffStr
    stats.value[1].changeType = todayDiff >= 0 ? 'up' : 'down'

    stats.value[2].value = d.reservationCount ?? 0
    stats.value[2].change = '总预约数'
    stats.value[2].changeType = 'up'

    stats.value[3].value = pendingCount
    stats.value[3].label = isUser.value ? '我的预约' : '待审批'
    stats.value[3].change = isUser.value ? '我发起的预约' : (pendingCount > 0 ? '需要处理' : '暂无待审批')
    stats.value[3].changeType = pendingCount > 0 ? 'down' : 'up'
  } catch {}

  // 最近预约（取今日）
  try {
    const res = await reservationApi.list()
    const list = res.data || []
    const statusNum = (s) => ({ PENDING: 0, APPROVED: 1, REJECTED: 2, CANCELLED: 3 }[s] ?? 0)
    recentReservations.value = list.slice(0, 5).map(item => ({
      roomName: item.roomName || ('会议室#' + item.roomId),
      applicant: item.applicant || '',
      startTime: (item.reservationDate || '') + ' ' + (item.startTime || ''),
      endTime: (item.reservationDate || '') + ' ' + (item.endTime || ''),
      purpose: item.purpose || '',
      status: statusNum(item.status)
    }))
    // USER 的第4个卡片显示自己的预约总数
    if (isUser.value) {
      stats.value[3].value = list.length
    }
    // 当前空闲 = 总会议室 - 今日已预约会议室数
    const todayRoomIds = new Set(list.filter(r => r.reservationDate === new Date().toISOString().slice(0,10)).map(r => r.roomId))
    stats.value[2].value = Math.max(0, stats.value[0].value - todayRoomIds.size)
  } catch {}

  // 趋势图
  try {
    const res = await statisticsApi.trend()
    const d = res.data || res
    if (d.days && d.values) {
      initTrendChart(d.days, d.values)
    } else {
      initTrendChart()
    }
  } catch { initTrendChart() }

  // 使用率图
  try {
    const res = await statisticsApi.roomUsage()
    const d = res.data || res
    if (d.rooms && d.usage) {
      initUsageChart(d.rooms, d.usage)
    } else {
      initUsageChart()
    }
  } catch { initUsageChart() }

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  trendChart?.dispose()
  usageChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard { display: flex; flex-direction: column; gap: 24px; }

/* Stats grid */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }

.stat-card {
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 22px 20px;
  display: flex; align-items: flex-start; gap: 14px;
  box-shadow: var(--shadow-card);
  position: relative; overflow: hidden;
  cursor: default;
  transition: all var(--transition);
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
  border-color: #C7C5FF;
}

.stat-icon {
  width: 44px; height: 44px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.stat-content { flex: 1; min-width: 0; }
.stat-label {
  font-size: 11px; color: var(--text-muted);
  font-weight: 600; text-transform: uppercase; letter-spacing: 0.07em;
}
.stat-value {
  font-family: var(--font-display); font-size: 30px; font-weight: 800;
  color: var(--text-primary); margin: 5px 0 4px;
  line-height: 1; letter-spacing: -0.03em;
}
.stat-change { font-size: 12px; display: flex; align-items: center; gap: 3px; font-weight: 500; }
.stat-change.up   { color: var(--success); }
.stat-change.down { color: var(--warning); }

.stat-decoration {
  position: absolute; width: 90px; height: 90px; border-radius: 50%;
  right: -24px; top: -24px; opacity: 0.08; filter: blur(4px);
}

/* Charts */
.charts-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }

.chart-card, .recent-card {
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 22px 22px 20px;
  box-shadow: var(--shadow-card);
  transition: box-shadow var(--transition), border-color var(--transition);
}
.chart-card:hover, .recent-card:hover {
  box-shadow: var(--shadow-md);
  border-color: #C7C5FF;
}

.chart-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 18px; }
.chart-title {
  font-size: 15px; font-weight: 700;
  color: var(--text-primary); letter-spacing: -0.01em;
}
.chart-sub { font-size: 12px; color: var(--text-muted); margin-top: 2px; }
.chart-canvas { height: 230px; }

.view-all {
  font-size: 12px; color: var(--primary); text-decoration: none;
  font-weight: 600; transition: all var(--transition);
  padding: 4px 12px; border-radius: 6px;
  background: var(--primary-bg);
  white-space: nowrap;
}
.view-all:hover { background: #E0DFFF; }

.recent-table { margin-top: 4px; }

@media (max-width: 1200px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-grid { grid-template-columns: 1fr; }
}
</style>
