<template>
  <div class="page">
    <div class="summary-grid animate-in">
      <div v-for="(item, i) in summaryCards" :key="item.label" class="summary-card hover-card animate-in" :class="`delay-${i+1}`">
        <div class="summary-top">
          <span class="summary-label">{{ item.label }}</span>
          <el-tag :type="item.tagType" size="small">{{ item.tag }}</el-tag>
        </div>
        <div class="summary-value">{{ item.value }}</div>
        <div class="summary-bar"><div class="bar-fill" :style="{width: item.percent + '%', background: item.color}"></div></div>
        <div class="summary-sub">{{ item.sub }}</div>
      </div>
    </div>

    <div class="charts-row-2 animate-in delay-2">
      <div class="chart-card large">
        <div class="chart-header">
          <div><h3 class="chart-title">预约趋势分析</h3><p class="chart-sub">近30天每日预约数量</p></div>
          <el-radio-group v-model="trendType" size="small">
            <el-radio-button label="day">按天</el-radio-button>
            <el-radio-button label="week">按周</el-radio-button>
          </el-radio-group>
        </div>
        <div ref="trendRef" class="chart-body"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header"><div><h3 class="chart-title">会议室使用率</h3><p class="chart-sub">本月各房间占用率</p></div></div>
        <div ref="usageRef" class="chart-body"></div>
      </div>
    </div>

    <div class="charts-row-3 animate-in delay-3">
      <div class="chart-card">
        <div class="chart-header"><div><h3 class="chart-title">热门会议室</h3><p class="chart-sub">预约次数 TOP 5</p></div></div>
        <div ref="hotRef" class="chart-body"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header"><div><h3 class="chart-title">时段分布</h3><p class="chart-sub">各时段预约热度</p></div></div>
        <div ref="heatRef" class="chart-body"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header"><div><h3 class="chart-title">审批状态</h3><p class="chart-sub">本月审批情况</p></div></div>
        <div ref="statusRef" class="chart-body"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { statisticsApi } from '@/api/statistics'

const trendRef = ref(null), usageRef = ref(null), hotRef = ref(null), heatRef = ref(null), statusRef = ref(null)
const trendType = ref('day')
let charts = []
let trend30Data = null

const summaryCards = ref([
  { label: '总预约次数', value: '-', tag: '全部', tagType: '', percent: 0, color: '#4F6EF7', sub: '' },
  { label: '今日预约', value: '-', tag: '今天', tagType: 'success', percent: 0, color: '#10B981', sub: '' },
  { label: '待审批', value: '-', tag: '待处理', tagType: 'warning', percent: 0, color: '#F97316', sub: '' },
  { label: '审批通过率', value: '-', tag: '全部', tagType: 'success', percent: 0, color: '#F59E0B', sub: '' },
])

const palette = ['#4F6EF7','#10B981','#F97316','#F59E0B','#8B5CF6']

let c1, c2, c3, c4, c5

function initCharts() {
  c1 = echarts.init(trendRef.value)
  c1.setOption({ tooltip:{trigger:'axis',backgroundColor:'#fff',borderColor:'#E2E8F0',textStyle:{color:'#0F172A'}}, grid:{left:10,right:20,top:20,bottom:20,containLabel:true}, xAxis:{type:'category',data:[],axisLine:{lineStyle:{color:'#E2E8F0'}},axisTick:{show:false},axisLabel:{color:'#94A3B8',fontSize:10}}, yAxis:{type:'value',splitLine:{lineStyle:{color:'#F1F5F9',type:'dashed'}},axisLabel:{color:'#94A3B8'}}, series:[{data:[],type:'bar',barWidth:'60%',itemStyle:{color:{type:'linear',x:0,y:0,x2:0,y2:1,colorStops:[{offset:0,color:'#4F6EF7'},{offset:1,color:'rgba(79,110,247,0.3)'}]},borderRadius:[6,6,0,0]}},{data:[],type:'line',smooth:true,lineStyle:{color:'#7B93FF',width:2},symbol:'none'}] })
  charts.push(c1)

  c2 = echarts.init(usageRef.value)
  c2.setOption({ tooltip:{trigger:'axis',backgroundColor:'#fff',borderColor:'#E2E8F0'}, grid:{left:10,right:40,top:10,bottom:10,containLabel:true}, xAxis:{type:'value',axisLabel:{color:'#94A3B8'},splitLine:{lineStyle:{color:'#F1F5F9'}}}, yAxis:{type:'category',data:[],axisLabel:{color:'#64748B',fontWeight:500}}, series:[{type:'bar',data:[],barWidth:18,itemStyle:{borderRadius:[0,8,8,0],color:p=>palette[p.dataIndex%5]},label:{show:true,position:'right',color:'#64748B',fontSize:12}}] })
  charts.push(c2)

  c3 = echarts.init(hotRef.value)
  c3.setOption({ tooltip:{trigger:'item',backgroundColor:'#fff',borderColor:'#E2E8F0'}, legend:{bottom:5,textStyle:{color:'#64748B',fontSize:11}}, series:[{type:'pie',radius:['40%','68%'],center:['50%','42%'],data:[],label:{show:false},emphasis:{itemStyle:{shadowBlur:12}}}] })
  charts.push(c3)

  c4 = echarts.init(heatRef.value)
  c4.setOption({ tooltip:{trigger:'axis',backgroundColor:'#fff',borderColor:'#E2E8F0'}, grid:{left:10,right:10,top:10,bottom:20,containLabel:true}, xAxis:{type:'category',data:[],axisLabel:{color:'#94A3B8',fontSize:10},axisLine:{show:false},axisTick:{show:false}}, yAxis:{type:'value',splitLine:{lineStyle:{color:'#F1F5F9'}},axisLabel:{color:'#94A3B8'}}, series:[{data:[],type:'line',smooth:true,symbol:'none',areaStyle:{color:{type:'linear',x:0,y:0,x2:0,y2:1,colorStops:[{offset:0,color:'rgba(16,185,129,0.25)'},{offset:1,color:'rgba(16,185,129,0.02)'}]}},lineStyle:{color:'#10B981',width:3}}] })
  charts.push(c4)

  c5 = echarts.init(statusRef.value)
  c5.setOption({ tooltip:{trigger:'item',backgroundColor:'#fff',borderColor:'#E2E8F0'}, series:[{type:'pie',radius:'72%',center:['50%','50%'],data:[],label:{formatter:'{b}\n{d}%',color:'#64748B',fontSize:12},emphasis:{itemStyle:{shadowBlur:12}}}] })
  charts.push(c5)
}

async function loadData() {
  // 汇总卡片
  const ov = await statisticsApi.overview()
  const total = Number(ov.reservationCount || 0)
  const pending = Number(ov.pendingCount || 0)
  const approved = Number(ov.approvedCount || 0)
  const passRate = (approved + pending + Number(ov.rejectedCount || 0)) > 0
    ? Math.round(approved / (approved + pending + Number(ov.rejectedCount || 0)) * 100)
    : 0
  summaryCards.value[0].value = total.toLocaleString()
  summaryCards.value[0].percent = Math.min(100, total)
  summaryCards.value[1].value = String(ov.todayCount || 0)
  summaryCards.value[1].percent = Math.min(100, Number(ov.todayCount || 0) * 10)
  summaryCards.value[2].value = String(pending)
  summaryCards.value[2].percent = total > 0 ? Math.round(pending / total * 100) : 0
  summaryCards.value[3].value = passRate + '%'
  summaryCards.value[3].percent = passRate
  summaryCards.value[3].sub = '拒绝率 ' + (total > 0 ? Math.round(Number(ov.rejectedCount || 0) / total * 100) : 0) + '%'

  // 30天趋势
  const t30 = await statisticsApi.trend30()
  trend30Data = t30
  renderTrend(t30)

  // 会议室使用次数
  const ru = await statisticsApi.roomUsage()
  c2.setOption({ yAxis:{data: ru.rooms}, series:[{data: ru.usage}] })

  // 热门会议室（排行）
  const rr = await statisticsApi.roomRank()
  const pieData = rr.rooms.map((name, i) => ({ value: rr.counts[i], name, itemStyle:{color: palette[i%5]} }))
  c3.setOption({ series:[{data: pieData}] })

  // 时段分布
  const hd = await statisticsApi.hourDist()
  c4.setOption({ xAxis:{data: hd.hours}, series:[{data: hd.values}] })

  // 审批状态
  const as = await statisticsApi.approvalStatus()
  c5.setOption({ series:[{data:[
    {value: as.approved, name:'已通过', itemStyle:{color:'#10B981'}},
    {value: as.pending,  name:'待审批', itemStyle:{color:'#F59E0B'}},
    {value: as.rejected, name:'已拒绝', itemStyle:{color:'#EF4444'}}
  ]}] })
}

onMounted(() => {
  setTimeout(() => {
    initCharts()
    loadData()
  }, 150)
  window.addEventListener('resize', () => charts.forEach(c => c.resize()))
})

watch(trendType, () => { if (trend30Data) renderTrend(trend30Data) })

function renderTrend(t30) {
  if (trendType.value === 'day') {
    const labels = t30.days.map(d => d.slice(5))
    c1.setOption({ xAxis:{data: labels}, series:[{data: t30.values},{data: t30.values}] })
  } else {
    // 按周聚合
    const weekMap = {}
    t30.days.forEach((day, i) => {
      const d = new Date(day)
      const startOfWeek = new Date(d)
      startOfWeek.setDate(d.getDate() - d.getDay() + 1)
      const key = startOfWeek.toISOString().slice(5, 10)
      weekMap[key] = (weekMap[key] || 0) + Number(t30.values[i])
    })
    const wLabels = Object.keys(weekMap)
    const wValues = Object.values(weekMap)
    c1.setOption({ xAxis:{data: wLabels}, series:[{data: wValues},{data: wValues}] })
  }
}

onUnmounted(() => { charts.forEach(c => c.dispose()) })
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 24px; }

.summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.summary-card {
  background: #fff; border-radius: var(--radius);
  border: 1px solid var(--border);
  padding: 20px; box-shadow: var(--shadow-card); cursor: default;
  transition: box-shadow var(--transition), border-color var(--transition), transform var(--transition);
}
.summary-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
  border-color: #C7C5FF;
}
.summary-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.summary-label { font-size: 12px; color: var(--text-muted); font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; }
.summary-value { font-family: var(--font-display); font-size: 28px; font-weight: 800; color: var(--text-primary); margin: 6px 0; letter-spacing: -0.03em; }
.summary-bar { height: 5px; background: var(--bg); border-radius: 99px; overflow: hidden; margin-bottom: 6px; }
.bar-fill { height: 100%; border-radius: 99px; transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1); }
.summary-sub { font-size: 12px; color: var(--text-muted); }

.charts-row-2 { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; }
.charts-row-3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }

.chart-card {
  background: #fff; border-radius: var(--radius);
  border: 1px solid var(--border);
  padding: 22px; box-shadow: var(--shadow-card);
  transition: box-shadow var(--transition), border-color var(--transition);
}
.chart-card:hover { box-shadow: var(--shadow-md); border-color: #C7C5FF; }
.chart-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 18px; }
.chart-title { font-size: 15px; font-weight: 700; color: var(--text-primary); letter-spacing: -0.01em; }
.chart-sub { font-size: 12px; color: var(--text-muted); margin-top: 2px; }
.chart-body { height: 240px; }

@media (max-width: 1200px) {
  .summary-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-row-2, .charts-row-3 { grid-template-columns: 1fr; }
}
</style>
