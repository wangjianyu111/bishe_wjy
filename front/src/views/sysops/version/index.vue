<template>
  <div class="version-manage-page">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <span>版本更新管理</span>
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon>新增版本
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" class="filter-bar">
        <el-form-item label="应用类型">
          <el-select v-model="query.appType" placeholder="全部" clearable style="width: 140px" @change="handleSearch">
            <el-option value="FRONTEND" label="后台前端" />
            <el-option value="BACKEND" label="后端服务" />
            <el-option value="APP" label="移动端" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px" @change="handleSearch">
            <el-option value="DEVELOPING" label="开发中" />
            <el-option value="RELEASED" label="已发布" />
            <el-option value="ROLLBACKED" label="已回滚" />
            <el-option value="DEPRECATED" label="已废弃" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="版本号/名称" clearable style="width: 160px" @keydown.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column prop="versionCode" label="版本号" width="120" />
        <el-table-column prop="versionName" label="版本名称" min-width="160" />
        <el-table-column prop="appTypeLabel" label="应用类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.appTypeLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="releaseTypeLabel" label="发布类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="releaseTagType(row.releaseType)">{{ row.releaseTypeLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="statusLabel" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType(row.status)">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="灰度比例" width="100" align="center">
          <template #default="{ row }">
            <el-progress :percentage="row.rolloutPercent || 0" :stroke-width="8" :show-text="true" />
          </template>
        </el-table-column>
        <el-table-column prop="deployedByName" label="部署人" width="100" />
        <el-table-column prop="deployedTime" label="部署时间" width="170">
          <template #default="{ row }">{{ formatTime(row.deployedTime) }}</template>
        </el-table-column>
        <el-table-column label="强制更新" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.forceUpdate === 1" type="danger" size="small">是</el-tag>
            <span v-else style="color:#909399">否</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'DEVELOPING'" type="success" link size="small" @click="openRolloutDialog(row, 'GRAY')">灰度发布</el-button>
            <el-button v-if="row.status === 'RELEASED'" type="warning" link size="small" @click="openRolloutDialog(row, 'FULL')">全量发布</el-button>
            <el-button v-if="row.status === 'RELEASED'" type="danger" link size="small" @click="handleRollback(row)">回滚</el-button>
            <el-button v-if="row.status === 'DEVELOPING'" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增版本对话框 -->
    <el-dialog v-model="addDialogVisible" title="新增版本" width="580px" destroy-on-close>
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="100px">
        <el-form-item label="版本号" prop="versionCode">
          <el-input v-model="addForm.versionCode" placeholder="如 1.0.0" />
        </el-form-item>
        <el-form-item label="版本名称" prop="versionName">
          <el-input v-model="addForm.versionName" placeholder="如 V1.0.0 正式版" />
        </el-form-item>
        <el-form-item label="应用类型" prop="appType">
          <el-select v-model="addForm.appType" placeholder="请选择应用类型" style="width:100%">
            <el-option value="FRONTEND" label="后台前端" />
            <el-option value="BACKEND" label="后端服务" />
            <el-option value="APP" label="移动端" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布类型" prop="releaseType">
          <el-radio-group v-model="addForm.releaseType">
            <el-radio value="STABLE">正式版</el-radio>
            <el-radio value="BETA">测试版</el-radio>
            <el-radio value="CANARY">灰度版</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="更新日志">
          <el-input v-model="addForm.changelog" type="textarea" :rows="4" placeholder="描述本次更新的内容..." maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="新功能">
          <el-input v-model="addForm.featureList" type="textarea" :rows="3" placeholder="每行一个功能点，如：新增用户管理模块" />
        </el-form-item>
        <el-form-item label="下载地址">
          <el-input v-model="addForm.downloadUrl" placeholder="如 https://example.com/download/v1.0.0.zip" />
        </el-form-item>
        <el-form-item label="最低兼容版本">
          <el-input v-model="addForm.minCompatibleVersion" placeholder="如 0.9.0" />
        </el-form-item>
        <el-form-item label="强制更新">
          <el-switch v-model="addForm.forceUpdate" :active-value="1" :inactive-value="0" />
          <span style="margin-left:8px;color:#909399;font-size:12px">开启后用户必须更新才能继续使用</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleAdd">确认创建</el-button>
      </template>
    </el-dialog>

    <!-- 发布对话框 -->
    <el-dialog v-model="rolloutDialogVisible" :title="rolloutDialogTitle" width="480px" destroy-on-close>
      <el-form :model="rolloutForm" label-width="100px">
        <el-form-item label="目标版本">
          <el-input :model-value="currentVersion?.versionName + ' (' + currentVersion?.versionCode + ')'" disabled />
        </el-form-item>
        <el-form-item v-if="rolloutForm.rolloutType === 'GRAY'" label="灰度比例">
          <el-slider v-model="rolloutForm.rolloutPercent" :min="1" :max="100" :step="5" show-input />
        </el-form-item>
        <el-form-item v-if="rolloutForm.rolloutType === 'FULL'" label="发布说明">
          <el-alert type="info" :closable="false" show-icon>
            全量发布将替代当前运行版本，请确认已完成充分测试。已发布的旧版本将自动标记为废弃。
          </el-alert>
        </el-form-item>
        <el-form-item v-if="rolloutForm.rolloutType === 'ROLLBACK'" label="回滚说明">
          <el-alert type="warning" :closable="false" show-icon>
            回滚将恢复至上一次稳定版本，当前版本将被标记为已回滚。是否继续？
          </el-alert>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="rolloutForm.remark" type="textarea" :rows="2" placeholder="可选：记录发布备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rolloutDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleRollout">确认发布</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="版本详情" width="640px" destroy-on-close>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="版本号">{{ currentVersion?.versionCode }}</el-descriptions-item>
        <el-descriptions-item label="版本名称">{{ currentVersion?.versionName }}</el-descriptions-item>
        <el-descriptions-item label="应用类型">
          <el-tag size="small">{{ currentVersion?.appTypeLabel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发布类型">
          <el-tag size="small" :type="releaseTagType(currentVersion?.releaseType)">{{ currentVersion?.releaseTypeLabel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="statusTagType(currentVersion?.status)">{{ currentVersion?.statusLabel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="强制更新">{{ currentVersion?.forceUpdate === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="灰度比例">{{ currentVersion?.rolloutPercent || 0 }}%</el-descriptions-item>
        <el-descriptions-item label="最低兼容版本">{{ currentVersion?.minCompatibleVersion || '无限制' }}</el-descriptions-item>
        <el-descriptions-item label="下载地址" :span="2">
          <a v-if="currentVersion?.downloadUrl" :href="currentVersion?.downloadUrl" target="_blank" style="color:#409eff">{{ currentVersion?.downloadUrl }}</a>
          <span v-else style="color:#909399">无</span>
        </el-descriptions-item>
        <el-descriptions-item label="部署人">{{ currentVersion?.deployedByName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="部署时间">{{ currentVersion?.deployedTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentVersion?.createTime }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="currentVersion?.changelog" class="detail-section">
        <div class="section-title">更新日志</div>
        <div class="changelog-content">{{ currentVersion?.changelog }}</div>
      </div>

      <div v-if="currentVersion?.featureList" class="detail-section">
        <div class="section-title">新功能</div>
        <div class="feature-list">
          <div v-for="(feature, idx) in parseFeatures(currentVersion?.featureList)" :key="idx" class="feature-item">
            <el-icon><CircleCheckFilled style="color:#67c23a" /></el-icon>
            <span>{{ feature }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { formatDateTime as formatTime } from '@/utils/timeFormat'
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, CircleCheckFilled } from '@element-plus/icons-vue'
import {
  fetchVersionPage,
  fetchVersionDetail,
  addVersion,
  performVersionRollout,
  deleteVersion,
} from '@/api/system'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const pagination = reactive({ total: 0, current: 1, size: 20 })
const query = reactive({ appType: '', status: '', keyword: '' })

const addDialogVisible = ref(false)
const addFormRef = ref(null)
const addForm = ref({
  versionCode: '',
  versionName: '',
  appType: '',
  releaseType: 'STABLE',
  changelog: '',
  featureList: '',
  downloadUrl: '',
  minCompatibleVersion: '',
  forceUpdate: 0,
})
const addRules = {
  versionCode: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
  versionName: [{ required: true, message: '请输入版本名称', trigger: 'blur' }],
  appType: [{ required: true, message: '请选择应用类型', trigger: 'change' }],
  releaseType: [{ required: true, message: '请选择发布类型', trigger: 'change' }],
}

const rolloutDialogVisible = ref(false)
const rolloutDialogTitle = ref('')
const rolloutForm = ref({ rolloutType: '', rolloutPercent: 50, remark: '' })

const detailDialogVisible = ref(false)
const currentVersion = ref(null)

async function loadData(page) {
  loading.value = true
  try {
    const res = await fetchVersionPage({ current: page || pagination.current, size: pagination.size, ...query })
    if (res) {
      tableData.value = res.records || []
      pagination.total = res.total || 0
      pagination.current = res.current || 1
    }
  } catch {} finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadData(1)
}

function handleReset() {
  query.appType = ''
  query.status = ''
  query.keyword = ''
  pagination.current = 1
  loadData(1)
}

function openAddDialog() {
  addForm.value = {
    versionCode: '',
    versionName: '',
    appType: '',
    releaseType: 'STABLE',
    changelog: '',
    featureList: '',
    downloadUrl: '',
    minCompatibleVersion: '',
    forceUpdate: 0,
  }
  addDialogVisible.value = true
}

async function handleAdd() {
  const valid = await addFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const payload = { ...addForm.value }
    if (payload.featureList) {
      payload.featureList = payload.featureList.split('\n').filter(s => s.trim()).join('\n')
    }
    await addVersion(payload)
    ElMessage.success('版本创建成功')
    addDialogVisible.value = false
    loadData(1)
  } catch (e) {
    ElMessage.error(e.message || '创建失败')
  } finally {
    submitLoading.value = false
  }
}

function openRolloutDialog(row, type) {
  currentVersion.value = row
  rolloutForm.value = { rolloutType: type, rolloutPercent: row.rolloutPercent || 50, remark: '' }
  rolloutDialogTitle.value = type === 'GRAY' ? '灰度发布' : '全量发布'
  rolloutDialogVisible.value = true
}

async function handleRollout() {
  submitLoading.value = true
  try {
    await performVersionRollout({
      versionId: currentVersion.value.versionId,
      rolloutType: rolloutForm.value.rolloutType,
      rolloutPercent: rolloutForm.value.rolloutPercent,
      remark: rolloutForm.value.remark,
    })
    ElMessage.success('发布操作成功')
    rolloutDialogVisible.value = false
    loadData(1)
  } catch (e) {
    ElMessage.error(e.message || '发布失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleRollback(row) {
  await ElMessageBox.confirm('确定要回滚到上一个稳定版本吗？', '版本回滚', { type: 'warning' })
  submitLoading.value = true
  try {
    await performVersionRollout({
      versionId: row.versionId,
      rolloutType: 'ROLLBACK',
      remark: '手动回滚',
    })
    ElMessage.success('回滚成功')
    loadData(1)
  } catch (e) {
    ElMessage.error(e.message || '回滚失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除版本「${row.versionName}」吗？`, '删除确认', { type: 'warning' })
  try {
    await deleteVersion(row.versionId)
    ElMessage.success('删除成功')
    loadData(1)
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

async function openDetail(row) {
  try {
    const data = await fetchVersionDetail(row.versionId)
    currentVersion.value = data
    detailDialogVisible.value = true
  } catch (e) {
    ElMessage.error(e.message || '获取详情失败')
  }
}

function parseFeatures(str) {
  if (!str) return []
  return str.split('\n').filter(s => s.trim())
}

function releaseTagType(type) {
  const map = { STABLE: 'success', BETA: 'warning', CANARY: 'info' }
  return map[type] || 'info'
}

function statusTagType(status) {
  const map = { DEVELOPING: 'info', RELEASED: 'success', ROLLBACKED: 'danger', DEPRECATED: 'warning' }
  return map[status] || 'info'
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
.changelog-content {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  font-size: 13px;
  color: #303133;
  white-space: pre-wrap;
  line-height: 1.6;
  max-height: 200px;
  overflow-y: auto;
}
.feature-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  color: #303133;
  line-height: 1.4;
}
</style>
