<template>
  <el-card shadow="never">
    <template #header>
      <div class="header-row">
        <span>操作日志管理</span>
        <div class="header-actions">
          <span v-if="pagination.total > 0" class="record-count">共 {{ pagination.total }} 条</span>
          <el-button
            :loading="exporting"
            :disabled="pagination.total === 0"
            @click="handleExport"
          >
            <el-icon><Download /></el-icon>导出
          </el-button>
          <el-button
            type="danger"
            :disabled="selectedRows.length === 0"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>批量删除
          </el-button>
        </div>
      </div>
    </template>

    <!-- 搜索栏 -->
    <el-form :inline="true" class="filter-bar">
      <el-form-item label="操作类型">
        <el-select v-model="query.operationType" placeholder="全部" clearable style="width: 140px">
          <el-option value="LOGIN" label="登录" />
          <el-option value="LOGOUT" label="退出登录" />
          <el-option value="QUERY" label="查询数据" />
          <el-option value="CREATE" label="新增数据" />
          <el-option value="UPDATE" label="更新数据" />
          <el-option value="DELETE" label="删除数据" />
          <el-option value="EXPORT" label="导出数据" />
          <el-option value="APPROVE" label="审批操作" />
          <el-option value="OTHER" label="其他操作" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作名称">
        <el-input v-model="query.operationName" placeholder="操作名称" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 260px"
          @change="onDateRangeChange"
        />
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="操作人/URL/IP" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table
      ref="tableRef"
      :data="tableData"
      v-loading="loading"
      border
      stripe
      size="small"
      @selection-change="onSelectionChange"
    >
      <el-table-column type="selection" width="40" />
      <el-table-column prop="operateTime" label="操作时间" width="170" />
      <el-table-column prop="userName" label="操作人" width="110" />
      <el-table-column prop="userType" label="身份" width="80" align="center">
        <template #default="{ row }">{{ userTypeLabel(row.userType) }}</template>
      </el-table-column>
      <el-table-column prop="campusName" label="学校" width="140" show-overflow-tooltip />
      <el-table-column prop="operationName" label="操作名称" width="130" />
      <el-table-column prop="operationType" label="类型" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="typeTagType(row.operationType)" size="small">{{ typeLabel(row.operationType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="module" label="模块" width="100" />
      <el-table-column prop="requestMethod" label="方法" width="70" align="center">
        <template #default="{ row }">
          <el-tag :type="methodTagType(row.requestMethod)" size="small">{{ row.requestMethod }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="requestUrl" label="请求路径" min-width="180" show-overflow-tooltip />
      <el-table-column prop="ipAddress" label="IP地址" width="130" />
      <el-table-column prop="os" label="操作系统" width="100" show-overflow-tooltip />
      <el-table-column prop="browser" label="浏览器" width="100" show-overflow-tooltip />
      <el-table-column prop="duration" label="耗时" width="80" align="center">
        <template #default="{ row }">
          <span :class="{ 'slow-tag': row.duration > 1000 }">{{ row.duration }}ms</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
            {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="pagination.total"
        :current-page="pagination.current"
        :page-size="pagination.size"
        @current-change="loadData"
      />
    </div>
  </el-card>

  <!-- 详情弹窗 -->
  <el-dialog v-model="detailVisible" title="操作日志详情" width="680px" destroy-on-close>
    <el-descriptions :column="2" border size="small">
      <el-descriptions-item label="操作时间" :span="2">{{ currentLog?.operateTime }}</el-descriptions-item>
      <el-descriptions-item label="操作人">{{ currentLog?.userName }}</el-descriptions-item>
      <el-descriptions-item label="身份">{{ userTypeLabel(currentLog?.userType) }}</el-descriptions-item>
      <el-descriptions-item label="学校">{{ currentLog?.campusName }}</el-descriptions-item>
      <el-descriptions-item label="IP地址">{{ currentLog?.ipAddress }}</el-descriptions-item>
      <el-descriptions-item label="操作系统">{{ currentLog?.os }}</el-descriptions-item>
      <el-descriptions-item label="浏览器">{{ currentLog?.browser }}</el-descriptions-item>
      <el-descriptions-item label="执行时长">{{ currentLog?.duration }}ms</el-descriptions-item>
      <el-descriptions-item label="操作名称">{{ currentLog?.operationName }}</el-descriptions-item>
      <el-descriptions-item label="操作类型" :span="2">
        <el-tag :type="typeTagType(currentLog?.operationType)" size="small">{{ typeLabel(currentLog?.operationType) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="业务模块">{{ currentLog?.module }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="currentLog?.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
          {{ currentLog?.status === 'SUCCESS' ? '成功' : '失败' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="请求方法">{{ currentLog?.requestMethod }}</el-descriptions-item>
      <el-descriptions-item label="请求路径" :span="2">
        <span style="word-break: break-all">{{ currentLog?.requestUrl }}</span>
      </el-descriptions-item>
    </el-descriptions>

    <div v-if="currentLog?.requestParams" class="detail-section">
      <div class="section-title">请求参数</div>
      <pre class="code-block">{{ formatJson(currentLog.requestParams) }}</pre>
    </div>

    <div v-if="currentLog?.responseResult" class="detail-section">
      <div class="section-title">响应结果</div>
      <pre class="code-block">{{ formatJson(currentLog.responseResult) }}</pre>
    </div>

    <div v-if="currentLog?.errorMsg" class="detail-section">
      <div class="section-title">错误信息</div>
      <pre class="code-block error">{{ currentLog.errorMsg }}</pre>
    </div>

    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Delete } from '@element-plus/icons-vue'
import { fetchOperationLogPage, deleteOperationLogBatch } from '@/api/system'

const loading = ref(false)
const exporting = ref(false)
const tableRef = ref(null)
const tableData = ref([])
const selectedRows = ref([])
const pagination = reactive({ total: 0, current: 1, size: 20 })
const query = reactive({
  operationType: '',
  operationName: '',
  keyword: '',
  startDate: '',
  endDate: '',
})
const dateRange = ref([])

const detailVisible = ref(false)
const currentLog = ref(null)

async function loadData(page) {
  loading.value = true
  try {
    const res = await fetchOperationLogPage({
      current: page || pagination.current,
      size: pagination.size,
      ...query,
    })
    if (res) {
      tableData.value = res.records || []
      pagination.total = res.total || 0
      pagination.current = res.current || 1
    }
  } catch {
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadData(1)
}

function handleReset() {
  query.operationType = ''
  query.operationName = ''
  query.keyword = ''
  query.startDate = ''
  query.endDate = ''
  dateRange.value = []
  pagination.current = 1
  loadData(1)
}

function onDateRangeChange(val) {
  if (val && val.length === 2) {
    query.startDate = val[0]
    query.endDate = val[1]
  } else {
    query.startDate = ''
    query.endDate = ''
  }
}

function onSelectionChange(selection) {
  selectedRows.value = selection
}

async function handleBatchDelete() {
  const ids = selectedRows.value.map(r => r.logId)
  await ElMessageBox.confirm(`确定要删除选中的 ${ids.length} 条日志吗？`, '批量删除', { type: 'warning' })
  try {
    await deleteOperationLogBatch(ids)
    ElMessage.success('删除成功')
    selectedRows.value = []
    loadData(1)
  } catch (e) {
    if (e.message) ElMessage.error(e.message)
  }
}

function openDetail(row) {
  currentLog.value = row
  detailVisible.value = true
}

async function handleExport() {
  exporting.value = true
  try {
    const { fetchOperationLogPage: _unused, deleteOperationLogBatch: _unused2, ...api } = await import('@/api/system')
    const resp = await api.exportOperationLog({ ...query, size: pagination.total })
    const blob = resp
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `操作日志_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    if (e.message) ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

function formatJson(str) {
  try {
    const obj = JSON.parse(str)
    return JSON.stringify(obj, null, 2)
  } catch {
    return str
  }
}

function userTypeLabel(type) {
  if (type === 1) return '学生'
  if (type === 2) return '教师'
  if (type === 3) return '管理员'
  return '未知'
}

function typeLabel(type) {
  const map = {
    LOGIN: '登录',
    LOGOUT: '退出登录',
    QUERY: '查询',
    CREATE: '新增',
    UPDATE: '更新',
    DELETE: '删除',
    EXPORT: '导出',
    APPROVE: '审批',
    OTHER: '其他',
  }
  return map[type] || type || '其他'
}

function typeTagType(type) {
  const map = {
    LOGIN: 'warning',
    LOGOUT: 'info',
    QUERY: '',
    CREATE: 'success',
    UPDATE: '',
    DELETE: 'danger',
    EXPORT: '',
    APPROVE: 'success',
    OTHER: 'info',
  }
  return map[type] || 'info'
}

function methodTagType(method) {
  const map = { GET: '', POST: 'success', PUT: 'warning', DELETE: 'danger', PATCH: 'warning' }
  return map[method] || 'info'
}

onMounted(() => {
  loadData(1)
})
</script>

<style scoped>
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.record-count {
  color: #909399;
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
.detail-section {
  margin-top: 16px;
}
.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
}
.code-block {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  font-size: 12px;
  color: #303133;
  max-height: 260px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}
.code-block.error {
  color: #f56c6c;
  background: #fef0f0;
  border-color: #fde2e2;
}
.slow-tag {
  color: #e6a23c;
  font-weight: 600;
}
</style>
