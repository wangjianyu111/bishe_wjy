<template>
  <div class="feedback-page">

    <!-- ====== 管理员视图 ====== -->
    <template v-if="isAdmin">
      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stat-row">
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.totalCount }}</div>
            <div class="stat-label">反馈总数</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card pending">
            <div class="stat-value">{{ stats.pendingCount }}</div>
            <div class="stat-label">待处理</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card handling">
            <div class="stat-value">{{ stats.handlingCount }}</div>
            <div class="stat-label">处理中</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card resolved">
            <div class="stat-value">{{ stats.resolvedCount }}</div>
            <div class="stat-label">已解决</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 分类统计 -->
      <el-row :gutter="16" class="stat-row type-row">
        <el-col :xs="12" :sm="4">
          <el-card shadow="hover" class="stat-card type-card">
            <div class="stat-value">{{ stats.teachingQualityCount }}</div>
            <div class="stat-label">教学质量问题</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="4">
          <el-card shadow="hover" class="stat-card type-card">
            <div class="stat-value">{{ stats.studentIssueCount }}</div>
            <div class="stat-label">学生问题</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="4">
          <el-card shadow="hover" class="stat-card type-card">
            <div class="stat-value">{{ stats.systemImproveCount }}</div>
            <div class="stat-label">系统改进建议</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="4">
          <el-card shadow="hover" class="stat-card type-card">
            <div class="stat-value">{{ stats.resourceLackCount }}</div>
            <div class="stat-label">资源不足</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="4">
          <el-card shadow="hover" class="stat-card type-card">
            <div class="stat-value">{{ stats.otherCount }}</div>
            <div class="stat-label">其他</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 反馈列表 -->
      <el-card shadow="never" class="list-card">
        <template #header>
          <div class="card-header">
            <span>反馈记录</span>
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
            <el-select v-model="query.feedbackType" placeholder="全部" clearable style="width:140px">
              <el-option label="教学质量问题" value="TEACHING_QUALITY" />
              <el-option label="学生问题" value="STUDENT_ISSUE" />
              <el-option label="系统改进建议" value="SYSTEM_IMPROVE" />
              <el-option label="资源不足" value="RESOURCE_LACK" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
              <el-option label="待处理" value="PENDING" />
              <el-option label="处理中" value="HANDLING" />
              <el-option label="已解决" value="RESOLVED" />
              <el-option label="已驳回" value="REJECTED" />
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
          <el-table-column prop="teacherName" label="反馈教师" width="100" show-overflow-tooltip />
          <el-table-column prop="studentName" label="学生" width="90" />
          <el-table-column prop="studentNo" label="学号" width="110" />
          <el-table-column label="学校" width="130" show-overflow-tooltip>
            <template #default="{ row }">{{ row.campusName || '—' }}</template>
          </el-table-column>
          <el-table-column label="课题" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">{{ row.topicName || '—' }}</template>
          </el-table-column>
          <el-table-column label="反馈类型" width="130">
            <template #default="{ row }">
              <el-tag size="small" :type="typeTagType(row.feedbackType)">
                {{ row.feedbackTypeName || row.feedbackType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="反馈内容" min-width="200" show-overflow-tooltip />
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
          <el-table-column prop="createTime" label="提交时间" width="170" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openHandleDialog(row)">处理</el-button>
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
            <div class="stat-label">我的反馈总数</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card pending">
            <div class="stat-value">{{ stats.pendingCount }}</div>
            <div class="stat-label">待处理</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card resolved">
            <div class="stat-value">{{ stats.resolvedCount }}</div>
            <div class="stat-label">已解决</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.systemImproveCount }}</div>
            <div class="stat-label">系统改进建议</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>我的反馈</span>
            <el-button type="primary" @click="openSubmitDialog">
              <el-icon><Plus /></el-icon>提交反馈
            </el-button>
          </div>
        </template>

        <el-form :inline="true" class="filter-bar">
          <el-form-item label="学年">
            <el-input v-model="query.academicYear" placeholder="如 2024" clearable style="width:110px" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="query.feedbackType" placeholder="全部" clearable style="width:140px">
              <el-option label="教学质量问题" value="TEACHING_QUALITY" />
              <el-option label="学生问题" value="STUDENT_ISSUE" />
              <el-option label="系统改进建议" value="SYSTEM_IMPROVE" />
              <el-option label="资源不足" value="RESOURCE_LACK" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
              <el-option label="待处理" value="PENDING" />
              <el-option label="处理中" value="HANDLING" />
              <el-option label="已解决" value="RESOLVED" />
              <el-option label="已驳回" value="REJECTED" />
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
          <el-table-column label="反馈类型" width="130">
            <template #default="{ row }">
              <el-tag size="small" :type="typeTagType(row.feedbackType)">
                {{ row.feedbackTypeName || row.feedbackType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="反馈内容" min-width="200" show-overflow-tooltip />
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
          <el-table-column prop="createTime" label="提交时间" width="170" />
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

    <!-- 提交反馈弹窗（教师） -->
    <el-dialog v-model="submitDialogVisible" title="提交反馈" width="560px" destroy-on-close>
      <el-form ref="submitFormRef" :model="submitForm" :rules="submitRules" label-width="110px">
        <el-form-item label="选择选题" prop="selectionId">
          <el-select v-model="submitForm.selectionId" placeholder="请选择要反馈的选题" style="width:100%">
            <el-option
              v-for="item in selectionOptions"
              :key="item.selectionId"
              :label="`${item.studentName} - ${item.topicName || item.customTopicName}`"
              :value="item.selectionId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="问题类型" prop="feedbackType">
          <el-select v-model="submitForm.feedbackType" placeholder="请选择问题类型" style="width:100%">
            <el-option label="教学质量问题" value="TEACHING_QUALITY" />
            <el-option label="学生问题" value="STUDENT_ISSUE" />
            <el-option label="系统改进建议" value="SYSTEM_IMPROVE" />
            <el-option label="资源不足" value="RESOURCE_LACK" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="反馈内容" prop="content">
          <el-input
            v-model="submitForm.content"
            type="textarea"
            :rows="5"
            maxlength="1000"
            show-word-limit
            placeholder="请详细描述问题或改进建议..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- 处理反馈弹窗（管理员） -->
    <el-dialog v-model="handleDialogVisible" title="处理反馈" width="520px" destroy-on-close>
      <el-form ref="handleFormRef" :model="handleForm" :rules="handleRules" label-width="90px">
        <el-form-item label="处理状态">
          <el-radio-group v-model="handleForm.status">
            <el-radio value="HANDLING">处理中</el-radio>
            <el-radio value="RESOLVED">已解决</el-radio>
            <el-radio value="REJECTED">已驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理意见" prop="handleComment">
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
        <el-button type="primary" :loading="handleLoading" @click="handleFeedback">确认处理</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  fetchTeacherFeedbackPage,
  fetchAdminFeedbackPage,
  fetchAdminFeedbackStats,
  fetchTeacherFeedbackStats,
  submitTeacherFeedback,
  handleTeacherFeedback,
  fetchSelectionsForGuidance
} from '@/api/guidance'

const store = useUserStore()
const isAdmin = computed(() => store.user?.userType === 3)
const isTeacher = computed(() => store.user?.userType === 2)
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
  feedbackType: '',
  status: '',
  keyword: '',
})
const stats = reactive({
  totalCount: 0,
  pendingCount: 0,
  handlingCount: 0,
  resolvedCount: 0,
  rejectedCount: 0,
  teachingQualityCount: 0,
  studentIssueCount: 0,
  systemImproveCount: 0,
  resourceLackCount: 0,
  otherCount: 0,
})

async function loadStats() {
  try {
    let res
    if (isAdmin.value) {
      res = await fetchAdminFeedbackStats({
        campusName: query.campusName,
        academicYear: query.academicYear,
      })
    } else {
      res = await fetchTeacherFeedbackStats({ academicYear: query.academicYear })
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
      feedbackType: query.feedbackType || undefined,
      status: query.status || undefined,
    }
    let res
    if (isAdmin.value) {
      Object.assign(params, {
        campusName: query.campusName || undefined,
        keyword: query.keyword || undefined,
      })
      res = await fetchAdminFeedbackPage(params)
    } else {
      res = await fetchTeacherFeedbackPage(params)
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
  Object.assign(query, { campusName: '', academicYear: '', feedbackType: '', status: '', keyword: '' })
  handleSearch()
}

// ---- 提交反馈 ----
const submitDialogVisible = ref(false)
const submitLoading = ref(false)
const submitFormRef = ref()
const submitForm = reactive({ selectionId: null, feedbackType: '', content: '' })
const selectionOptions = ref([])

const submitRules = {
  selectionId: [{ required: true, message: '请选择选题', trigger: 'change' }],
  feedbackType: [{ required: true, message: '请选择问题类型', trigger: 'change' }],
  content: [{ required: true, message: '请填写反馈内容', trigger: 'blur' }],
}

async function openSubmitDialog() {
  submitDialogVisible.value = true
  Object.assign(submitForm, { selectionId: null, feedbackType: '', content: '' })
  try {
    const res = await fetchSelectionsForGuidance()
    if (res) selectionOptions.value = res
  } catch (e) {
    console.error(e)
  }
}

async function handleSubmit() {
  const valid = await submitFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await submitTeacherFeedback(submitForm)
    ElMessage.success('提交成功')
    submitDialogVisible.value = false
    loadData()
    loadStats()
  } catch (e) {
    console.error(e)
  } finally {
    submitLoading.value = false
  }
}

// ---- 处理反馈 ----
const handleDialogVisible = ref(false)
const handleLoading = ref(false)
const handleFormRef = ref()
const handleForm = reactive({ fbId: null, status: 'HANDLING', handleComment: '' })

const handleRules = {
  handleComment: [{ required: true, message: '请填写处理意见', trigger: 'blur' }],
}

function openHandleDialog(row) {
  handleDialogVisible.value = true
  Object.assign(handleForm, { fbId: row.fbId, status: 'HANDLING', handleComment: '' })
}

async function handleFeedback() {
  const valid = await handleFormRef.value.validate().catch(() => false)
  if (!valid) return
  handleLoading.value = true
  try {
    await handleTeacherFeedback(handleForm)
    ElMessage.success('处理成功')
    handleDialogVisible.value = false
    loadData()
    loadStats()
  } catch (e) {
    console.error(e)
  } finally {
    handleLoading.value = false
  }
}

// ---- 工具 ----
function typeTagType(type) {
  const map = {
    TEACHING_QUALITY: 'danger',
    STUDENT_ISSUE: 'warning',
    SYSTEM_IMPROVE: 'primary',
    RESOURCE_LACK: 'info',
    OTHER: '',
  }
  return map[type] || ''
}

function statusTagType(status) {
  const map = {
    PENDING: 'warning',
    HANDLING: 'primary',
    RESOLVED: 'success',
    REJECTED: 'info',
  }
  return map[status] || ''
}

onMounted(() => {
  loadData()
  loadStats()
})
</script>

<style scoped>
.feedback-page {
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
.stat-card.pending .stat-value { color: #e6a23c; }
.stat-card.handling .stat-value { color: #409eff; }
.stat-card.resolved .stat-value { color: #67c23a; }
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
