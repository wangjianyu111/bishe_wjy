<template>
  <div class="warning-page">

    <!-- ====== 管理员视图 ====== -->
    <template v-if="isAdmin">
      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stat-row">
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.totalCount }}</div>
            <div class="stat-label">预警总数</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card level1">
            <div class="stat-value">{{ stats.levelOneCount }}</div>
            <div class="stat-label">提醒</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card level2">
            <div class="stat-value">{{ stats.levelTwoCount }}</div>
            <div class="stat-label">警告</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card level3">
            <div class="stat-value">{{ stats.levelThreeCount }}</div>
            <div class="stat-label">严重</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 分类统计 -->
      <el-row :gutter="16" class="stat-row type-row">
        <el-col :xs="12" :sm="4">
          <el-card shadow="hover" class="stat-card type-card">
            <div class="stat-value">{{ stats.openCount }}</div>
            <div class="stat-label">待处理</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="4">
          <el-card shadow="hover" class="stat-card type-card">
            <div class="stat-value">{{ stats.noGuidanceCount }}</div>
            <div class="stat-label">长期未指导</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="4">
          <el-card shadow="hover" class="stat-card type-card">
            <div class="stat-value">{{ stats.docDelayCount }}</div>
            <div class="stat-label">文档提交滞后</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="4">
          <el-card shadow="hover" class="stat-card type-card">
            <div class="stat-value">{{ stats.progressDelayCount }}</div>
            <div class="stat-label">进度滞后</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="4">
          <el-card shadow="hover" class="stat-card type-card">
            <div class="stat-value">{{ stats.lowFrequencyCount }}</div>
            <div class="stat-label">指导频率不足</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 预警列表 -->
      <el-card shadow="never" class="list-card">
        <template #header>
          <div class="card-header">
            <span>预警记录</span>
            <span v-if="pagination.total > 0" class="record-count">共 {{ pagination.total }} 条</span>
          </div>
        </template>

        <el-form :inline="true" class="filter-bar">
          <el-form-item label="学校">
            <el-input v-model="query.campusName" placeholder="学校名称" clearable style="width:140px" />
          </el-form-item>
          <el-form-item label="学年">
            <el-input v-model="query.academicYear" placeholder="如 2024" clearable style="width:110px" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="query.warnType" placeholder="全部" clearable style="width:140px">
              <el-option label="长期未指导" value="NO_GUIDANCE" />
              <el-option label="文档提交滞后" value="DOC_DELAY" />
              <el-option label="进度滞后" value="PROGRESS_DELAY" />
              <el-option label="指导频率不足" value="LOW_FREQUENCY" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item label="级别">
            <el-select v-model="query.warnLevel" placeholder="全部" clearable style="width:100px">
              <el-option label="提醒" :value="1" />
              <el-option label="警告" :value="2" />
              <el-option label="严重" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部" clearable style="width:110px">
              <el-option label="已开启" value="OPEN" />
              <el-option label="已关闭" value="CLOSED" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="query.keyword" placeholder="姓名/学号/教师" clearable style="width:150px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="tableData" border stripe v-loading="loading" size="small">
          <el-table-column prop="teacherName" label="指导教师" width="100" show-overflow-tooltip />
          <el-table-column prop="studentName" label="学生" width="90" />
          <el-table-column prop="studentNo" label="学号" width="110" />
          <el-table-column label="学校" width="130" show-overflow-tooltip>
            <template #default="{ row }">{{ row.campusName || '—' }}</template>
          </el-table-column>
          <el-table-column label="课题" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">{{ row.topicName || '—' }}</template>
          </el-table-column>
          <el-table-column label="预警级别" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="levelTagType(row.warnLevel)">
                {{ row.warnLevelName || '提醒' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="预警类型" width="130">
            <template #default="{ row }">
              <el-tag size="small" type="warning">{{ row.warnTypeName || row.warnType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="预警原因" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="statusTagType(row.status)">
                {{ row.statusName || row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="handlerName" label="处理人" width="90" show-overflow-tooltip>
            <template #default="{ row }">{{ row.handlerName || '—' }}</template>
          </el-table-column>
          <el-table-column prop="handleComment" label="处理意见" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">{{ row.handleComment || '—' }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openHandleDialog(row)">处理</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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
    </template>

    <!-- ====== 教师视图 ====== -->
    <template v-else>
      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stat-row">
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.totalCount }}</div>
            <div class="stat-label">我的预警总数</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card level1">
            <div class="stat-value">{{ stats.levelOneCount }}</div>
            <div class="stat-label">提醒</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card level3">
            <div class="stat-value">{{ stats.levelThreeCount }}</div>
            <div class="stat-label">严重</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.openCount }}</div>
            <div class="stat-label">待处理</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>我的预警</span>
            <el-button type="primary" @click="openAddDialog">
              <el-icon><Plus /></el-icon>添加预警
            </el-button>
          </div>
        </template>

        <el-form :inline="true" class="filter-bar">
          <el-form-item label="学年">
            <el-input v-model="query.academicYear" placeholder="如 2024" clearable style="width:110px" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="query.warnType" placeholder="全部" clearable style="width:140px">
              <el-option label="长期未指导" value="NO_GUIDANCE" />
              <el-option label="文档提交滞后" value="DOC_DELAY" />
              <el-option label="进度滞后" value="PROGRESS_DELAY" />
              <el-option label="指导频率不足" value="LOW_FREQUENCY" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item label="级别">
            <el-select v-model="query.warnLevel" placeholder="全部" clearable style="width:100px">
              <el-option label="提醒" :value="1" />
              <el-option label="警告" :value="2" />
              <el-option label="严重" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部" clearable style="width:110px">
              <el-option label="已开启" value="OPEN" />
              <el-option label="已关闭" value="CLOSED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="tableData" border stripe v-loading="loading" size="small">
          <el-table-column prop="studentName" label="学生" width="90" />
          <el-table-column prop="studentNo" label="学号" width="110" />
          <el-table-column label="课题" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">{{ row.topicName || '—' }}</template>
          </el-table-column>
          <el-table-column label="预警级别" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="levelTagType(row.warnLevel)">
                {{ row.warnLevelName || '提醒' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="预警类型" width="130">
            <template #default="{ row }">
              <el-tag size="small" type="warning">{{ row.warnTypeName || row.warnType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="预警原因" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="statusTagType(row.status)">
                {{ row.statusName || row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="handlerName" label="处理人" width="90" show-overflow-tooltip>
            <template #default="{ row }">{{ row.handlerName || '—' }}</template>
          </el-table-column>
          <el-table-column prop="handleComment" label="处理意见" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">{{ row.handleComment || '—' }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
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
    </template>

    <!-- 添加预警弹窗（教师） -->
    <el-dialog v-model="addDialogVisible" title="添加预警" width="560px" destroy-on-close>
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="110px">
        <el-form-item label="选择选题" prop="selectionId">
          <el-select v-model="addForm.selectionId" placeholder="请选择要预警的选题" style="width:100%" @change="onSelectionChange">
            <el-option
              v-for="item in selectionOptions"
              :key="item.selectionId"
              :label="`${item.studentName} - ${item.topicName || item.customTopicName}`"
              :value="item.selectionId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学生">
          <el-input :value="selectedSelection?.studentName || '—'" disabled />
        </el-form-item>
        <el-form-item label="预警级别" prop="warnLevel">
          <el-radio-group v-model="addForm.warnLevel">
            <el-radio :value="1">提醒</el-radio>
            <el-radio :value="2">警告</el-radio>
            <el-radio :value="3">严重</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="预警类型" prop="warnType">
          <el-select v-model="addForm.warnType" placeholder="请选择预警类型" style="width:100%">
            <el-option label="长期未指导" value="NO_GUIDANCE" />
            <el-option label="文档提交滞后" value="DOC_DELAY" />
            <el-option label="进度滞后" value="PROGRESS_DELAY" />
            <el-option label="指导频率不足" value="LOW_FREQUENCY" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="预警原因" prop="reason">
          <el-input
            v-model="addForm.reason"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请详细描述预警原因，如：学生已超过30天未提交任何材料..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addLoading" @click="handleAdd">提交</el-button>
      </template>
    </el-dialog>

    <!-- 处理预警弹窗（管理员） -->
    <el-dialog v-model="handleDialogVisible" title="处理预警" width="520px" destroy-on-close>
      <el-form ref="handleFormRef" :model="handleForm" label-width="90px">
        <el-form-item label="处理意见">
          <el-input
            v-model="handleForm.handleComment"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请填写处理意见..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="handleLoading" @click="handleWarning">确认关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  fetchTeacherWarningPage,
  fetchAdminWarningPage,
  fetchAdminWarningStats,
  fetchTeacherWarningStats,
  addQualityWarning,
  handleQualityWarning,
  deleteQualityWarning,
  fetchSelectionsForGuidance
} from '@/api/guidance'

const store = useUserStore()
const isAdmin = computed(() => store.user?.userType === 3)

function hasPermission(code) {
  return store.hasPerm(code)
}

// ---- 数据加载 ----
const tableData = ref([])
const loading = ref(false)
const pagination = reactive({ total: 0, current: 1, size: 10 })
const query = reactive({
  campusName: '',
  academicYear: '',
  warnType: '',
  warnLevel: null,
  status: '',
  keyword: '',
})
const stats = reactive({
  totalCount: 0,
  levelOneCount: 0,
  levelTwoCount: 0,
  levelThreeCount: 0,
  openCount: 0,
  closedCount: 0,
  noGuidanceCount: 0,
  docDelayCount: 0,
  progressDelayCount: 0,
  lowFrequencyCount: 0,
  otherCount: 0,
})

async function loadStats() {
  try {
    let res
    if (isAdmin.value) {
      res = await fetchAdminWarningStats({
        campusName: query.campusName || undefined,
        academicYear: query.academicYear || undefined,
      })
    } else {
      res = await fetchTeacherWarningStats({ academicYear: query.academicYear || undefined })
    }
    if (res) Object.assign(stats, res)
  } catch (e) {
    console.error(e)
  }
}

async function loadData(page = 1) {
  loading.value = true
  try {
    const params = {
      current: page || pagination.current,
      size: pagination.size,
      academicYear: query.academicYear || undefined,
      warnType: query.warnType || undefined,
      warnLevel: query.warnLevel || undefined,
      status: query.status || undefined,
    }
    let res
    if (isAdmin.value) {
      Object.assign(params, {
        campusName: query.campusName || undefined,
        keyword: query.keyword || undefined,
      })
      res = await fetchAdminWarningPage(params)
    } else {
      res = await fetchTeacherWarningPage(params)
    }
    if (res) {
      tableData.value = res.records || []
      pagination.total = res.total || 0
      pagination.current = res.current || 1
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadData(1)
  loadStats()
}

function handleReset() {
  Object.assign(query, { campusName: '', academicYear: '', warnType: '', warnLevel: null, status: '', keyword: '' })
  handleSearch()
}

// ---- 添加预警 ----
const addDialogVisible = ref(false)
const addLoading = ref(false)
const addFormRef = ref()
const addForm = reactive({ selectionId: null, warnLevel: 1, warnType: '', reason: '' })
const selectionOptions = ref([])
const selectedSelection = ref(null)

const addRules = {
  selectionId: [{ required: true, message: '请选择选题', trigger: 'change' }],
  warnLevel: [{ required: true, message: '请选择预警级别', trigger: 'change' }],
  warnType: [{ required: true, message: '请选择预警类型', trigger: 'change' }],
  reason: [{ required: true, message: '请填写预警原因', trigger: 'blur' }],
}

async function openAddDialog() {
  addDialogVisible.value = true
  Object.assign(addForm, { selectionId: null, warnLevel: 1, warnType: '', reason: '' })
  selectedSelection.value = null
  try {
    const res = await fetchSelectionsForGuidance()
    selectionOptions.value = res || []
  } catch (e) {
    console.error(e)
  }
}

function onSelectionChange(selectionId) {
  selectedSelection.value = selectionOptions.value.find(s => s.selectionId === selectionId)
}

async function handleAdd() {
  const valid = await addFormRef.value?.validate().catch(() => false)
  if (!valid) return
  addLoading.value = true
  try {
    await addQualityWarning(addForm)
    ElMessage.success('预警添加成功')
    addDialogVisible.value = false
    loadData()
    loadStats()
  } catch (e) {
    ElMessage.error(e.message || '添加失败')
  } finally {
    addLoading.value = false
  }
}

// ---- 处理预警 ----
const handleDialogVisible = ref(false)
const handleLoading = ref(false)
const handleFormRef = ref()
const handleForm = reactive({ warnId: null, handleComment: '' })

function openHandleDialog(row) {
  handleDialogVisible.value = true
  Object.assign(handleForm, { warnId: row.warnId, handleComment: '' })
}

async function handleWarning() {
  handleLoading.value = true
  try {
    await handleQualityWarning(handleForm)
    ElMessage.success('预警已关闭')
    handleDialogVisible.value = false
    loadData()
    loadStats()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    handleLoading.value = false
  }
}

// ---- 删除 ----
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除该预警记录吗？`, '删除确认', { type: 'warning' })
  try {
    await deleteQualityWarning(row.warnId)
    ElMessage.success('删除成功')
    loadData()
    loadStats()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

// ---- 工具 ----
function levelTagType(level) {
  const map = { 1: 'info', 2: 'warning', 3: 'danger' }
  return map[level] || ''
}

function statusTagType(status) {
  const map = { OPEN: 'warning', CLOSED: 'info' }
  return map[status] || ''
}

onMounted(() => {
  loadData()
  loadStats()
})
</script>

<style scoped>
.warning-page {
  padding: 0;
}
.stat-row {
  margin-bottom: 16px;
}
.type-row {
  margin-top: 0;
}
.stat-card {
  text-align: center;
  cursor: default;
}
.stat-card .stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
  margin-bottom: 6px;
}
.stat-card.level1 .stat-value { color: #909399; }
.stat-card.level2 .stat-value { color: #e6a23c; }
.stat-card.level3 .stat-value { color: #f56c6c; }
.stat-card .stat-label {
  font-size: 13px;
  color: #909399;
}
.list-card {
  margin-top: 0;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.record-count {
  font-size: 13px;
  color: #909399;
  font-weight: 400;
}
.filter-bar {
  margin-bottom: 12px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
