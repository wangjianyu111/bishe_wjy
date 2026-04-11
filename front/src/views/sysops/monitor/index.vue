<template>
  <div class="monitor-page">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <span>系统监控管理</span>
          <div class="header-actions">
            <el-button type="danger" @click="handleClearHistory" :loading="clearLoading">清理历史数据</el-button>
            <el-button type="primary" @click="loadDashboard">
              <el-icon><Refresh /></el-icon>刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 监控概览 -->
      <div class="dashboard-grid">
        <div class="metric-card" :class="getMetricClass(dashboard.cpuUsage)" @click="switchTab('metrics')">
          <div class="metric-icon"><el-icon size="28"><Cpu /></el-icon></div>
          <div class="metric-info">
            <div class="metric-label">CPU 使用率</div>
            <div class="metric-value">{{ dashboard.cpuUsage || 0 }}%</div>
            <el-progress :percentage="Number(dashboard.cpuUsage) || 0" :stroke-width="6" :show-text="false"
              :color="getProgressColor(dashboard.cpuUsage, 80)" />
          </div>
        </div>

        <div class="metric-card" :class="getMetricClass(dashboard.memoryUsage)" @click="switchTab('metrics')">
          <div class="metric-icon"><el-icon size="28"><Monitor /></el-icon></div>
          <div class="metric-info">
            <div class="metric-label">内存使用率</div>
            <div class="metric-value">{{ dashboard.memoryUsage || 0 }}%</div>
            <el-progress :percentage="Number(dashboard.memoryUsage) || 0" :stroke-width="6" :show-text="false"
              :color="getProgressColor(dashboard.memoryUsage, 85)" />
          </div>
        </div>

        <div class="metric-card" :class="getMetricClass(dashboard.diskUsage)" @click="switchTab('metrics')">
          <div class="metric-icon"><el-icon size="28"><Folder /></el-icon></div>
          <div class="metric-info">
            <div class="metric-label">磁盘使用率</div>
            <div class="metric-value">{{ dashboard.diskUsage || 0 }}%</div>
            <el-progress :percentage="Number(dashboard.diskUsage) || 0" :stroke-width="6" :show-text="false"
              :color="getProgressColor(dashboard.diskUsage, 90)" />
          </div>
        </div>

        <div class="metric-card" :class="getMetricClass(dashboard.dbConnectionCount / dashboard.dbMaxConnections * 100)">
          <div class="metric-icon"><el-icon size="28"><Connection /></el-icon></div>
          <div class="metric-info">
            <div class="metric-label">数据库连接</div>
            <div class="metric-value">{{ dashboard.dbConnectionCount || 0 }} / {{ dashboard.dbMaxConnections || 0 }}</div>
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-icon"><el-icon size="28"><User /></el-icon></div>
          <div class="metric-info">
            <div class="metric-label">在线用户</div>
            <div class="metric-value">{{ dashboard.onlineUsers || 0 }} 人</div>
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-icon"><el-icon size="28"><DataAnalysis /></el-icon></div>
          <div class="metric-info">
            <div class="metric-label">今日 API 请求</div>
            <div class="metric-value">{{ dashboard.todayApiRequests || 0 }} 次</div>
          </div>
        </div>
      </div>

      <!-- 活跃告警提示 -->
      <div v-if="dashboard.activeAlertCount > 0" class="alert-banner">
        <el-alert :title="`当前有 ${dashboard.activeAlertCount} 条活跃告警`" type="warning" :closable="false" show-icon>
          <template #default>
            <div v-for="alert in dashboard.activeAlerts" :key="alert.alertId" class="alert-item">
              <span class="alert-badge" :class="'alert-' + alert.alertLevel.toLowerCase()">{{ alert.alertLevelLabel }}</span>
              <span class="alert-text">{{ alert.alertTitle }}</span>
              <el-button type="warning" link size="small" @click="handleResolve(alert)">处理</el-button>
            </div>
          </template>
        </el-alert>
      </div>
    </el-card>

    <!-- 标签页 -->
    <el-card shadow="never" class="mt-12">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="指标历史" name="metrics">
          <el-form :inline="true" class="filter-bar">
            <el-form-item label="指标类型">
              <el-select v-model="metricsQuery.metricType" placeholder="全部" clearable style="width:140px" @change="loadMetrics">
                <el-option value="CPU" label="CPU 使用率" />
                <el-option value="MEMORY" label="内存使用率" />
                <el-option value="DISK" label="磁盘使用率" />
                <el-option value="DB_CONNECTION" label="数据库连接" />
                <el-option value="NETWORK" label="网络流量" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="metricsQuery.status" placeholder="全部" clearable style="width:140px" @change="loadMetrics">
                <el-option value="NORMAL" label="正常" />
                <el-option value="WARNING" label="警告" />
                <el-option value="CRITICAL" label="严重" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadMetrics">查询</el-button>
              <el-button @click="metricsQuery = { metricType: '', status: '' }; loadMetrics()">重置</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="metricsData" v-loading="metricsLoading" border stripe size="small">
            <el-table-column prop="metricTypeLabel" label="指标类型" width="120" />
            <el-table-column prop="metricName" label="指标名称" width="140" />
            <el-table-column prop="metricValue" label="当前值" width="100" align="center">
              <template #default="{ row }">{{ row.metricValue }}{{ row.metricUnit }}</template>
            </el-table-column>
            <el-table-column prop="threshold" label="阈值" width="100" align="center">
              <template #default="{ row }">{{ row.threshold }}{{ row.metricUnit }}</template>
            </el-table-column>
            <el-table-column prop="statusLabel" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="statusTagType(row.status)">{{ row.statusLabel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="recordTime" label="记录时间" width="170">
              <template #default="{ row }">{{ formatTime(row.recordTime) }}</template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination background layout="total, prev, pager, next"
              :total="metricsTotal"
              :current-page="metricsPage"
              :page-size="metricsSize"
              @current-change="loadMetrics" />
          </div>
        </el-tab-pane>

        <el-tab-pane :label="`告警记录${dashboard.activeAlertCount > 0 ? ` (${dashboard.activeAlertCount})` : ''}`" name="alerts">
          <el-form :inline="true" class="filter-bar">
            <el-form-item label="告警级别">
              <el-select v-model="alertsQuery.alertLevel" placeholder="全部" clearable style="width:140px" @change="loadAlerts">
                <el-option value="INFO" label="通知" />
                <el-option value="WARNING" label="警告" />
                <el-option value="ERROR" label="错误" />
                <el-option value="CRITICAL" label="严重" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="alertsQuery.status" placeholder="全部" clearable style="width:140px" @change="loadAlerts">
                <el-option value="ACTIVE" label="未处理" />
                <el-option value="RESOLVED" label="已解决" />
                <el-option value="IGNORED" label="已忽略" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadAlerts">查询</el-button>
              <el-button @click="alertsQuery = { alertLevel: '', status: '' }; loadAlerts()">重置</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="alertsData" v-loading="alertsLoading" border stripe size="small">
            <el-table-column prop="alertLevelLabel" label="级别" width="90" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="alertLevelTagType(row.alertLevel)">{{ row.alertLevelLabel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="alertTypeLabel" label="类型" width="120" />
            <el-table-column prop="alertTitle" label="告警标题" min-width="180" />
            <el-table-column prop="alertContent" label="告警内容" min-width="200" show-overflow-tooltip />
            <el-table-column prop="statusLabel" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === 'ACTIVE' ? 'danger' : 'success'">{{ row.statusLabel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="triggerTime" label="触发时间" width="170">
              <template #default="{ row }">{{ formatTime(row.triggerTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 'ACTIVE'" type="primary" link size="small" @click="handleResolve(row)">处理</el-button>
                <span v-else style="color:#909399">已处理</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination background layout="total, prev, pager, next"
              :total="alertsTotal"
              :current-page="alertsPage"
              :page-size="alertsSize"
              @current-change="loadAlerts" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="接口统计" name="apiStats">
          <el-table :data="apiStatsData" v-loading="apiStatsLoading" border stripe size="small">
            <el-table-column prop="apiPath" label="接口路径" min-width="200" show-overflow-tooltip />
            <el-table-column prop="apiName" label="接口名称" width="150" />
            <el-table-column prop="requestMethod" label="方法" width="80" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="methodTagType(row.requestMethod)">{{ row.requestMethod }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalRequests" label="请求总数" width="100" align="center" sortable />
            <el-table-column prop="successCount" label="成功" width="80" align="center">
              <template #default="{ row }"><span style="color:#67c23a">{{ row.successCount }}</span></template>
            </el-table-column>
            <el-table-column prop="failCount" label="失败" width="80" align="center">
              <template #default="{ row }"><span style="color:#f56c6c">{{ row.failCount }}</span></template>
            </el-table-column>
            <el-table-column prop="avgResponseTime" label="平均响应" width="110" align="center">
              <template #default="{ row }">
                <span :class="{ 'slow-tag': Number(row.avgResponseTime) > 1000 }">{{ row.avgResponseTime }}ms</span>
              </template>
            </el-table-column>
            <el-table-column prop="maxResponseTime" label="最大响应" width="110" align="center">
              <template #default="{ row }">{{ row.maxResponseTime }}ms</template>
            </el-table-column>
            <el-table-column prop="tps" label="TPS" width="80" align="center" />
            <el-table-column prop="statTime" label="统计时间" width="170">
              <template #default="{ row }">{{ formatTime(row.statTime) }}</template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination background layout="total, prev, pager, next"
              :total="apiStatsTotal"
              :current-page="apiStatsPage"
              :page-size="apiStatsSize"
              @current-change="loadApiStats" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 处理告警对话框 -->
    <el-dialog v-model="resolveDialogVisible" title="处理告警" width="480px" destroy-on-close>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="告警标题">{{ currentAlert?.alertTitle }}</el-descriptions-item>
        <el-descriptions-item label="告警内容">{{ currentAlert?.alertContent }}</el-descriptions-item>
        <el-descriptions-item label="触发时间">{{ currentAlert?.triggerTime }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="resolveForm" style="margin-top:16px">
        <el-form-item label="处理备注">
          <el-input v-model="resolveForm.remark" type="textarea" :rows="3" placeholder="请输入处理说明..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="confirmResolve">确认处理</el-button>
      </template>
    </el-dialog>

    <!-- 清理历史对话框 -->
    <el-dialog v-model="clearDialogVisible" title="清理历史数据" width="400px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="保留天数">
          <el-input-number v-model="clearDays" :min="7" :max="365" />
          <span style="margin-left:8px;color:#909399">天</span>
        </el-form-item>
        <el-alert type="warning" :closable="false" show-icon>
          将删除 {{ clearDays }} 天之前的指标历史和接口统计数据，此操作不可恢复。
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="clearDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="clearLoading" @click="confirmClearHistory">确认清理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { formatDateTime as formatTime } from '@/utils/timeFormat'
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh, Cpu, Monitor, Folder, Connection, User, DataAnalysis
} from '@element-plus/icons-vue'
import {
  fetchMonitorDashboard,
  fetchMonitorMetricsPage,
  fetchMonitorAlertsPage,
  fetchMonitorApiStatsPage,
  resolveMonitorAlert,
  clearMonitorHistory,
} from '@/api/system'

const REFRESH_INTERVAL = 30000 // 30s 自动刷新
let timer = null

const activeTab = ref('metrics')
const dashboard = ref({})
const loading = ref(false)

// 指标历史
const metricsQuery = reactive({ metricType: '', status: '' })
const metricsData = ref([])
const metricsLoading = ref(false)
const metricsPage = ref(1)
const metricsSize = ref(20)
const metricsTotal = ref(0)

// 告警记录
const alertsQuery = reactive({ alertLevel: '', status: '' })
const alertsData = ref([])
const alertsLoading = ref(false)
const alertsPage = ref(1)
const alertsSize = ref(20)
const alertsTotal = ref(0)

// 接口统计
const apiStatsData = ref([])
const apiStatsLoading = ref(false)
const apiStatsPage = ref(1)
const apiStatsSize = ref(20)
const apiStatsTotal = ref(0)

const resolveDialogVisible = ref(false)
const clearDialogVisible = ref(false)
const clearDays = ref(30)
const submitLoading = ref(false)
const clearLoading = ref(false)
const currentAlert = ref(null)
const resolveForm = ref({ remark: '' })

async function loadDashboard() {
  try {
    const data = await fetchMonitorDashboard()
    dashboard.value = data || {}
  } catch (e) {
    ElMessage.error('加载监控数据失败')
  }
}

async function loadMetrics(page) {
  metricsLoading.value = true
  try {
    const res = await fetchMonitorMetricsPage({
      current: page || metricsPage.value,
      size: metricsSize.value,
      ...metricsQuery,
    })
    if (res) {
      metricsData.value = res.records || []
      metricsTotal.value = res.total || 0
      metricsPage.value = res.current || 1
    }
  } catch {} finally {
    metricsLoading.value = false
  }
}

async function loadAlerts(page) {
  alertsLoading.value = true
  try {
    const res = await fetchMonitorAlertsPage({
      current: page || alertsPage.value,
      size: alertsSize.value,
      ...alertsQuery,
    })
    if (res) {
      alertsData.value = res.records || []
      alertsTotal.value = res.total || 0
      alertsPage.value = res.current || 1
    }
  } catch {} finally {
    alertsLoading.value = false
  }
}

async function loadApiStats(page) {
  apiStatsLoading.value = true
  try {
    const res = await fetchMonitorApiStatsPage({
      current: page || apiStatsPage.value,
      size: apiStatsSize.value,
    })
    if (res) {
      apiStatsData.value = res.records || []
      apiStatsTotal.value = res.total || 0
      apiStatsPage.value = res.current || 1
    }
  } catch {} finally {
    apiStatsLoading.value = false
  }
}

function handleResolve(row) {
  currentAlert.value = row
  resolveForm.value.remark = ''
  resolveDialogVisible.value = true
}

async function confirmResolve() {
  submitLoading.value = true
  try {
    await resolveMonitorAlert({
      alertId: currentAlert.value.alertId,
      resolveRemark: resolveForm.value.remark,
    })
    ElMessage.success('处理成功')
    resolveDialogVisible.value = false
    loadAlerts()
    loadDashboard()
  } catch (e) {
    ElMessage.error(e.message || '处理失败')
  } finally {
    submitLoading.value = false
  }
}

function handleClearHistory() {
  clearDays.value = 30
  clearDialogVisible.value = true
}

async function confirmClearHistory() {
  clearLoading.value = true
  try {
    await clearMonitorHistory(clearDays.value)
    ElMessage.success('清理成功')
    clearDialogVisible.value = false
  } catch (e) {
    ElMessage.error(e.message || '清理失败')
  } finally {
    clearLoading.value = false
  }
}

function switchTab(tab) {
  activeTab.value = tab
  if (tab === 'metrics') loadMetrics()
  else if (tab === 'alerts') loadAlerts()
  else if (tab === 'apiStats') loadApiStats()
}

function getMetricClass(value) {
  if (!value) return ''
  if (value >= 90) return 'metric-critical'
  if (value >= 70) return 'metric-warning'
  return ''
}

function getProgressColor(value, threshold) {
  if (!value) return '#67c23a'
  if (value >= threshold) return '#f56c6c'
  if (value >= threshold * 0.7) return '#e6a23c'
  return '#67c23a'
}

function statusTagType(status) {
  const map = { NORMAL: 'success', WARNING: 'warning', CRITICAL: 'danger' }
  return map[status] || 'info'
}

function alertLevelTagType(level) {
  const map = { INFO: 'info', WARNING: 'warning', ERROR: 'danger', CRITICAL: 'danger' }
  return map[level] || 'info'
}

function methodTagType(method) {
  const map = { GET: '', POST: 'success', PUT: 'warning', DELETE: 'danger', PATCH: 'warning' }
  return map[method] || 'info'
}

onMounted(() => {
  loadDashboard()
  loadMetrics()
  timer = setInterval(loadDashboard, REFRESH_INTERVAL)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.mt-12 {
  margin-top: 12px;
}
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-actions {
  display: flex;
  gap: 10px;
}
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.metric-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  cursor: pointer;
  transition: all 0.2s;
}
.metric-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.metric-card.metric-warning {
  border-color: #e6a23c;
  background: #fdf6ec;
}
.metric-card.metric-critical {
  border-color: #f56c6c;
  background: #fef0f0;
}
.metric-icon {
  color: #409eff;
  flex-shrink: 0;
}
.metric-info {
  flex: 1;
  min-width: 0;
}
.metric-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}
.metric-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}
.alert-banner {
  margin-top: 12px;
}
.alert-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
}
.alert-badge {
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}
.alert-badge.alert-info { background: #ecf5ff; color: #409eff; }
.alert-badge.alert-warning { background: #fdf6ec; color: #e6a23c; }
.alert-badge.alert-error, .alert-badge.alert-critical { background: #fef0f0; color: #f56c6c; }
.alert-text {
  flex: 1;
  font-size: 13px;
}
.filter-bar {
  margin-bottom: 12px;
}
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.slow-tag {
  color: #e6a23c;
  font-weight: 600;
}
</style>
