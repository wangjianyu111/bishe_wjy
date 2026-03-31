<template>
  <div class="dashboard" v-loading="loading">
    <div class="dash-header">
      <div>
        <h1 class="dash-title">工作台</h1>
        <p class="dash-sub">当前角色：{{ roleText }}</p>
      </div>
    </div>

    <el-row :gutter="20" class="stat-row">
      <el-col v-for="c in statCards" :key="c.key" :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card" :class="c.tone">
          <div class="stat-inner">
            <div class="stat-icon" :class="c.tone">
              <el-icon :size="22"><component :is="c.icon" /></el-icon>
            </div>
            <div class="stat-meta">
              <div class="stat-label">{{ c.label }}</div>
              <div class="stat-value">{{ formatNum(c.value) }}</div>
            </div>
          </div>
          <div class="stat-bar" :class="c.tone" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-head">
              <span>课题新增趋势</span>
              <span class="card-hint">近 12 个月</span>
            </div>
          </template>
          <div ref="barRef" class="chart-box" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-head">
              <span>课题状态分布</span>
            </div>
          </template>
          <div ref="pieRef" class="chart-box chart-box--pie" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-head">
              <span>选题申请趋势</span>
              <span class="card-hint">近 12 个月</span>
            </div>
          </template>
          <div ref="lineRef" class="chart-box" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="chart-card list-card">
          <template #header>
            <div class="card-head">
              <span>最新毕设题目</span>
            </div>
          </template>
          <div v-if="!overview?.recentTopics?.length" class="empty-hint">暂无课题数据</div>
          <ul v-else class="topic-list">
            <li v-for="t in overview.recentTopics" :key="t.topicId" class="topic-item">
              <div class="topic-avatar">{{ (t.topicName || '课').slice(0, 1) }}</div>
              <div class="topic-body">
                <div class="topic-name">{{ t.topicName }}</div>
                <div class="topic-sub">{{ t.teacherName || '—' }} · {{ t.academicYear || '—' }}</div>
              </div>
              <el-tag size="small" type="info" effect="plain">{{ t.status || '—' }}</el-tag>
            </li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  UserFilled,
  Document,
  EditPen,
  BellFilled,
} from '@element-plus/icons-vue'
import { fetchDashboardOverview } from '@/api/dashboard'
import { useUserStore } from '@/stores/user'

const store = useUserStore()
const loading = ref(false)
const overview = ref(null)

const barRef = ref(null)
const pieRef = ref(null)
const lineRef = ref(null)
/** @type {import('echarts').ECharts[]} */
let chartList = []

const roleText = computed(() => (store.user?.roles || []).join('、') || '—')

const statCards = computed(() => {
  const o = overview.value
  if (!o) {
    return [
      { key: 'u', label: '系统用户', value: 0, tone: 'tone-a', icon: UserFilled },
      { key: 't', label: '毕设题目', value: 0, tone: 'tone-b', icon: Document },
      { key: 's', label: '选题申请', value: 0, tone: 'tone-c', icon: EditPen },
      { key: 'n', label: '未读消息', value: 0, tone: 'tone-d', icon: BellFilled },
    ]
  }
  return [
    { key: 'u', label: '系统用户', value: o.totalUsers, tone: 'tone-a', icon: UserFilled },
    { key: 't', label: '毕设题目', value: o.totalTopics, tone: 'tone-b', icon: Document },
    { key: 's', label: '选题申请', value: o.totalSelections, tone: 'tone-c', icon: EditPen },
    { key: 'n', label: '未读消息', value: o.unreadNotifications, tone: 'tone-d', icon: BellFilled },
  ]
})

function formatNum(n) {
  if (n == null) return '—'
  return String(n)
}

function monthLabel(ym) {
  if (!ym || ym.length < 7) return ym
  const m = parseInt(ym.slice(5, 7), 10)
  return `${m}月`
}

function disposeCharts() {
  chartList.forEach((c) => {
    try {
      c.dispose()
    } catch {
      /* ignore */
    }
  })
  chartList = []
}

function resizeCharts() {
  chartList.forEach((c) => c.resize())
}

function buildBar() {
  const el = barRef.value
  if (!el || !overview.value?.topicMonthlyNew?.length) return
  const chart = echarts.init(el)
  const pts = overview.value.topicMonthlyNew
  const x = pts.map((p) => monthLabel(p.month))
  const y = pts.map((p) => p.total ?? 0)
  chart.setOption({
    color: ['#6366f1'],
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 16, bottom: 32, top: 28 },
    xAxis: {
      type: 'category',
      data: x,
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { color: '#6b7280', fontSize: 11 },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f3f4f6' } },
      axisLabel: { color: '#6b7280' },
    },
    series: [
      {
        name: '新增毕设',
        type: 'bar',
        barMaxWidth: 28,
        data: y,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#818cf8' },
            { offset: 1, color: '#6366f1' },
          ]),
        },
      },
    ],
  })
  chartList.push(chart)
}

function buildPie() {
  const el = pieRef.value
  if (!el) return
  const raw = overview.value?.topicStatusDistribution || []
  const chart = echarts.init(el)
  const data = raw.map((d) => ({ name: d.name, value: d.value ?? 0 }))
  if (!data.length) {
    chart.setOption({
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: { color: '#9ca3af', fontSize: 14 },
      },
    })
    chartList.push(chart)
    return
  }
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, type: 'scroll', textStyle: { fontSize: 11 } },
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { formatter: '{b}\n{d}%' },
        data,
        color: ['#6366f1', '#a5b4fc', '#34d399', '#fbbf24', '#f472b6', '#38bdf8'],
      },
    ],
  })
  chartList.push(chart)
}

function buildLine() {
  const el = lineRef.value
  if (!el || !overview.value?.selectionMonthly?.length) return
  const chart = echarts.init(el)
  const pts = overview.value.selectionMonthly
  const x = pts.map((p) => monthLabel(p.month))
  const y = pts.map((p) => p.total ?? 0)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 16, bottom: 32, top: 28 },
    xAxis: {
      type: 'category',
      data: x,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { color: '#6b7280', fontSize: 11 },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f3f4f6' } },
      axisLabel: { color: '#6b7280' },
    },
    series: [
      {
        name: '申请数',
        type: 'line',
        smooth: true,
        symbolSize: 6,
        data: y,
        lineStyle: { width: 3, color: '#0ea5e9' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(14, 165, 233, 0.35)' },
            { offset: 1, color: 'rgba(14, 165, 233, 0.02)' },
          ]),
        },
      },
    ],
  })
  chartList.push(chart)
}

async function load() {
  loading.value = true
  disposeCharts()
  try {
    overview.value = await fetchDashboardOverview()
    await nextTick()
    buildBar()
    buildPie()
    buildLine()
  } catch {
    overview.value = null
  } finally {
    loading.value = false
  }
}

let onResize
onMounted(() => {
  load()
  onResize = () => resizeCharts()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  if (onResize) window.removeEventListener('resize', onResize)
  disposeCharts()
})
</script>

<style scoped>
.dashboard {
  padding: 4px 0 24px;
}

.dash-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 20px;
}

.dash-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  letter-spacing: -0.02em;
}

.dash-sub {
  margin: 6px 0 0;
  font-size: 13px;
  color: #6b7280;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
  border-radius: 14px;
  border: 1px solid #eef2f7;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
}

.stat-inner {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-icon.tone-a {
  background: linear-gradient(135deg, #fb923c, #f97316);
}
.stat-icon.tone-b {
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
}
.stat-icon.tone-c {
  background: linear-gradient(135deg, #2dd4bf, #14b8a6);
}
.stat-icon.tone-d {
  background: linear-gradient(135deg, #a78bfa, #8b5cf6);
}

.stat-meta {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  letter-spacing: -0.02em;
}

.stat-bar {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 3px;
  opacity: 0.85;
}

.stat-bar.tone-a {
  background: linear-gradient(90deg, #fb923c, #fdba74);
}
.stat-bar.tone-b {
  background: linear-gradient(90deg, #60a5fa, #93c5fd);
}
.stat-bar.tone-c {
  background: linear-gradient(90deg, #2dd4bf, #5eead4);
}
.stat-bar.tone-d {
  background: linear-gradient(90deg, #a78bfa, #c4b5fd);
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 14px;
  border: 1px solid #eef2f7;
}

.chart-card :deep(.el-card__header) {
  padding: 14px 18px;
  border-bottom: 1px solid #f3f4f6;
}

.chart-card :deep(.el-card__body) {
  padding: 12px 16px 18px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.card-hint {
  font-size: 12px;
  font-weight: 400;
  color: #9ca3af;
}

.chart-box {
  height: 300px;
  width: 100%;
}

.chart-box--pie {
  height: 320px;
}

.list-card :deep(.el-card__body) {
  padding-top: 8px;
}

.empty-hint {
  padding: 48px 16px;
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
}

.topic-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.topic-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.topic-item:last-child {
  border-bottom: none;
}

.topic-avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1, #a5b4fc);
  color: #fff;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 15px;
}

.topic-body {
  flex: 1;
  min-width: 0;
}

.topic-name {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.topic-sub {
  margin-top: 2px;
  font-size: 12px;
  color: #9ca3af;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
