<template>
  <!-- 4.1 数据分析 /back/dashboard -->
  <div v-loading="loading" class="dashboard">
    <!-- ① 统计卡片区 FR-D01 -->
    <el-row :gutter="16">
      <el-col v-for="card in cards" :key="card.label" :span="6">
        <div class="stat-card">
          <div class="stat-icon" :style="{ background: card.color }">
            <el-icon :size="20" color="#fff"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">{{ card.label }}</div>
            <template v-if="!loading">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-sub">{{ card.sub }}</div>
            </template>
            <el-skeleton v-else :rows="2" animated style="height: 44px" />
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- ② 双栏图表区 FR-D02 / FR-D03 -->
    <el-row :gutter="16" class="mt16">
      <el-col :span="14">
        <div class="panel">
          <div class="panel-title">情绪趋势分析</div>
          <div ref="trendRef" class="chart chart-trend"></div>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="panel">
          <div class="panel-title">咨询会话统计</div>
          <div class="consult-summary">
            <div class="cs-item">
              <div class="cs-label">总会话数</div>
              <div class="cs-value">{{ stats.totalConsults }}</div>
            </div>
            <div class="cs-item">
              <div class="cs-label">平均时长</div>
              <div class="cs-value cs-value-sm">{{ avgDuration }}</div>
            </div>
            <div class="cs-item">
              <div class="cs-label">活跃用户</div>
              <div class="cs-value">{{ stats.activeUsers }}</div>
            </div>
          </div>
          <div ref="barRef" class="chart chart-bar"></div>
        </div>
      </el-col>
    </el-row>

    <!-- ③ 全宽图表区 FR-D04（Q4 占位：活跃用户数折线图） -->
    <div class="panel mt16">
      <div class="panel-title">用户活跃度趋势</div>
      <div ref="activityRef" class="chart chart-activity"></div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { User, ChatLineSquare, ChatDotRound, Sunny } from '@element-plus/icons-vue'
import { getDashboardStats, getDashboardTrends } from '@/api'

const stats = ref({})
const loading = ref(true)

// FR-D01 四张统计卡片
const cards = computed(() => [
  { label: '总用户数', value: stats.value.totalUsers, sub: `活跃用户: ${stats.value.activeUsers ?? '-'}`, color: '#7C5CFC', icon: User },
  { label: '情绪日志', value: stats.value.totalMoods, sub: `今日新增: ${stats.value.moodsToday ?? '-'}`, color: '#F472B6', icon: ChatLineSquare },
  { label: '咨询会话', value: stats.value.totalConsults, sub: `今日新增: ${stats.value.consultsToday ?? '-'}`, color: '#409EFF', icon: ChatDotRound },
  { label: '平均情绪', value: stats.value.avgScore != null ? `${stats.value.avgScore}/10` : '-', sub: '情绪健康指数', color: '#67C23A', icon: Sunny },
])

// R2：平均时长 = 已结束会话时长均值（分钟，1 位小数），后端直接返回
const avgDuration = computed(() =>
  stats.value.avgDurationMin != null ? `${stats.value.avgDurationMin}分钟` : '-',
)

// ---------- 图表 ----------
function useChart(elRef, buildOption) {
  let chart = null
  const render = () => {
    if (!elRef.value) return
    if (!chart) chart = echarts.init(elRef.value)
    chart.setOption(buildOption())
  }
  const resize = () => chart?.resize()
  onMounted(() => window.addEventListener('resize', resize))
  onBeforeUnmount(() => {
    window.removeEventListener('resize', resize)
    chart?.dispose()
    chart = null
  })
  return render
}

const trendRef = ref(null)
const barRef = ref(null)
const activityRef = ref(null)

// 趋势数据（后端返回等长升序序列，date=MM-dd）
const emotionTrend = ref([])
const consultTrend = ref([])
const activityTrend = ref([])

// FR-D02 双 Y 轴折线图：左轴平均情绪评分(0-10) / 右轴记录数量
const renderTrend = useChart(trendRef, () => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['平均情绪评分', '记录数量'], top: 0 },
  grid: { left: 44, right: 40, top: 36, bottom: 28 },
  xAxis: { type: 'category', data: emotionTrend.value.map((d) => d.date) },
  yAxis: [
    { type: 'value', name: '评分', min: 0, max: 10 },
    { type: 'value', name: '条数', min: 0, interval: 1 },
  ],
  series: [
    {
      name: '平均情绪评分',
      type: 'line',
      smooth: true,
      connectNulls: true,
      data: emotionTrend.value.map((d) => d.avgScore),
      itemStyle: { color: '#409EFF' },
    },
    {
      name: '记录数量',
      type: 'line',
      smooth: true,
      yAxisIndex: 1,
      data: emotionTrend.value.map((d) => d.count),
      itemStyle: { color: '#E6A23C' },
    },
  ],
}))

// FR-D03 咨询活动统计双系列柱状图
const renderBar = useChart(barRef, () => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['会话数量', '参与用户数'], top: 0 },
  grid: { left: 36, right: 12, top: 36, bottom: 28 },
  xAxis: { type: 'category', data: consultTrend.value.map((d) => d.date) },
  yAxis: { type: 'value', minInterval: 1 },
  series: [
    { name: '会话数量', type: 'bar', barGap: 0, data: consultTrend.value.map((d) => d.count), itemStyle: { color: '#409EFF' } },
    { name: '参与用户数', type: 'bar', data: consultTrend.value.map((d) => d.userCount), itemStyle: { color: '#E6A23C' } },
  ],
}))

// FR-D04 用户活跃度趋势（Q4 占位口径）
const renderActivity = useChart(activityRef, () => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['活跃用户数'], top: 0 },
  grid: { left: 36, right: 20, top: 36, bottom: 28 },
  xAxis: { type: 'category', data: activityTrend.value.map((d) => d.date) },
  yAxis: { type: 'value', minInterval: 1 },
  series: [
    {
      name: '活跃用户数',
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.15 },
      data: activityTrend.value.map((d) => d.activeUsers),
      itemStyle: { color: '#67C23A' },
    },
  ],
}))

onMounted(async () => {
  try {
    const [statsData, trends] = await Promise.all([getDashboardStats(), getDashboardTrends(30)])
    stats.value = statsData
    emotionTrend.value = trends.emotionTrend || []
    consultTrend.value = trends.consultTrend || []
    activityTrend.value = trends.activityTrend || []
  } catch (e) {
    ElMessage.error(e.message || '数据加载失败')
  } finally {
    loading.value = false
  }
  renderTrend()
  renderBar()
  renderActivity()
})
</script>

<style scoped>
.mt16 {
  margin-top: 16px;
}
/* 统计卡片 */
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}
.stat-icon {
  flex: none;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-label {
  font-size: 13px;
  color: #909399;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  line-height: 1.3;
}
.stat-sub {
  font-size: 12px;
  color: #909399;
}
/* 图表面板 */
.panel {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
}
.panel-title {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}
.chart {
  width: 100%;
}
.chart-trend,
.chart-bar {
  height: 300px;
}
.chart-activity {
  height: 260px;
}
/* 咨询会话统计汇总指标 */
.consult-summary {
  display: flex;
  padding: 8px 0 4px;
}
.cs-item {
  flex: 1;
  text-align: center;
}
.cs-label {
  font-size: 12px;
  color: #909399;
}
.cs-value {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin-top: 2px;
}
.cs-value-sm {
  font-size: 16px;
  line-height: 1.7;
}
</style>
