<template>
  <div class="midterm-page">
    <!-- ===== 学生端视图 ===== -->
    <template v-if="isStudent">
      <el-card shadow="never" class="intro-card">
        <div class="intro-title">
          <el-icon color="#409eff" size="18"><Finished /></el-icon>
          <span>我的中期检查</span>
        </div>

        <div v-if="mySelection" class="selection-info">
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="选题名称">
              <span>{{ mySelection.isCustomTopic ? mySelection.customTopicName : mySelection.topicName }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="指导教师">
              <span>{{ mySelection.teacherName || '—' }}{{ mySelection.teacherNo ? `(${mySelection.teacherNo})` : '' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="学校">
              <span>{{ mySelection.campusName || '—' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="学年">
              <span>{{ mySelection.academicYear }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="选题状态">
              <el-tag size="small" :type="selectionTagType(mySelection.status)">{{ mySelection.statusLabel }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="中期检查状态">
              <el-tag size="small" :type="midtermTagType(myMidTerm?.status)">
                {{ midtermStatusText(myMidTerm?.status) }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <el-empty v-else description="您还没有审批通过的选题，无法提交中期检查" />

        <div v-if="mySelection?.status === 'APPROVED'" class="action-bar">
          <el-button
            v-if="!myMidTerm || myMidTerm.status === 'FAILED'"
            type="primary"
            @click="openSubmitDialog"
          >
            <el-icon><Upload /></el-icon>
            {{ myMidTerm?.status === 'FAILED' ? '重新提交中期检查' : '提交中期检查' }}
          </el-button>
          <el-button
            v-if="myMidTerm && myMidTerm.status === 'PENDING'"
            type="warning"
            @click="handleRecall"
          >
            <el-icon><RefreshLeft /></el-icon>
            撤回提交
          </el-button>
        </div>
      </el-card>

      <!-- 审核结果展示 -->
      <el-card v-if="myMidTerm && myMidTerm.status !== 'PENDING'" shadow="never" class="result-card">
        <template #header>
          <div class="card-header">
            <span>审核结果</span>
            <el-tag :type="midtermTagType(myMidTerm.status)">
              {{ midtermStatusText(myMidTerm.status) }}
            </el-tag>
          </div>
        </template>
        <div v-if="myMidTerm.status === 'FAILED'" class="result-content">
          <el-alert
            :title="`驳回原因：${myMidTerm.inspectComment || '未填写'}`"
            type="error"
            :closable="false"
            show-icon
          />
        </div>
        <div v-if="myMidTerm.status === 'PASSED'" class="result-content">
          <el-alert
            title="您的中期检查已通过审核"
            type="success"
            :closable="false"
            show-icon
          />
        </div>

        <el-descriptions v-if="myMidTerm.inspectComment" :column="2" border size="small" style="margin-top:16px">
          <el-descriptions-item label="审核人">{{ myMidTerm.inspectorName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ formatDate(myMidTerm.inspectTime) }}</el-descriptions-item>
          <el-descriptions-item label="审核意见" :span="2">{{ myMidTerm.inspectComment }}</el-descriptions-item>
        </el-descriptions>

        <!-- 已上传的文件 -->
        <div v-if="myMidTerm.fileName" class="file-info">
          <el-icon><Document /></el-icon>
          <span>已上传文件：{{ myMidTerm.fileName }}</span>
        </div>
      </el-card>
    </template>

    <!-- ===== 管理员/教师端视图 ===== -->
    <template v-else>
      <el-card shadow="never" class="search-card">
        <el-form :inline="true" :model="query">
          <el-form-item v-if="isAdmin" label="学校">
            <el-input v-model="query.campusName" placeholder="搜索学校名称" clearable style="width: 180px" />
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="query.keyword" placeholder="搜索课题/学生姓名/学号" clearable style="width: 180px" />
          </el-form-item>
          <el-form-item label="学年">
            <el-select v-model="query.academicYear" placeholder="全部学年" clearable style="width: 160px">
              <el-option v-for="y in academicYearOptions" :key="y" :label="y" :value="y" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
              <el-option label="全部" value="" />
              <el-option label="待审核" value="PENDING" />
              <el-option label="已通过" value="PASSED" />
              <el-option label="已驳回" value="FAILED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData(1)">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card shadow="never">
        <el-table :data="tableData" v-loading="loading" border stripe size="small">
          <el-table-column prop="studentName" label="学生姓名" width="100" fixed />
          <el-table-column prop="studentNo" label="学号" width="110" />
          <el-table-column v-if="isAdmin" prop="campusName" label="学校" width="130" />
          <el-table-column prop="teacherName" label="指导教师" width="110">
            <template #default="{ row }">
              {{ row.teacherName || '—' }}{{ row.teacherNo ? `(${row.teacherNo})` : '' }}
            </template>
          </el-table-column>
          <el-table-column label="课题" min-width="200">
            <template #default="{ row }">
              <span v-if="row.isCustomTopic" class="custom-tag">自拟</span>
              {{ row.isCustomTopic ? row.customTopicName : row.topicName }}
            </template>
          </el-table-column>
          <el-table-column prop="academicYear" label="学年" width="110" />
          <el-table-column prop="fileName" label="附件" width="160">
            <template #default="{ row }">
              <span v-if="row.fileName" class="file-link">
                <el-icon><Document /></el-icon>
                {{ row.fileName }}
              </span>
              <span v-else style="color:#c0c4cc">无</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="midtermTagType(row.status)">
                {{ midtermStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" width="170">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" plain @click="openDetail(row)">查看详情</el-button>
              <template v-if="row.status === 'PENDING'">
                <el-button type="success" size="small" @click="openReviewDialog(row, 'PASSED')">通过</el-button>
                <el-button type="danger" size="small" @click="openReviewDialog(row, 'FAILED')">驳回</el-button>
              </template>
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

    <!-- ===== 学生提交对话框 ===== -->
    <el-dialog v-model="submitDialogVisible" title="提交中期检查" width="600px" destroy-on-close>
      <el-form :model="submitForm" :rules="submitRules" ref="submitFormRef" label-width="110px">
        <el-form-item label="选题名称">
          <span>{{ mySelection?.isCustomTopic ? mySelection.customTopicName : mySelection?.topicName }}</span>
        </el-form-item>
        <el-form-item label="指导教师">
          <span>{{ mySelection?.teacherName || '—' }}</span>
        </el-form-item>
        <el-form-item label="报告内容" prop="reportContent">
          <el-input
            v-model="submitForm.reportContent"
            type="textarea"
            :rows="5"
            placeholder="请填写中期报告内容，说明当前完成情况、存在的问题及后续计划等"
          />
        </el-form-item>
        <el-form-item label="上传附件">
          <div class="upload-area">
            <el-upload
              ref="submitUploadRef"
              :auto-upload="false"
              :limit="1"
              :on-change="handleSubmitFileChange"
              :on-remove="handleSubmitFileRemove"
              :file-list="submitFileList"
              accept=".doc,.docx,.pdf,.zip,.rar"
              action="#"
            >
              <el-button type="primary" plain>
                <el-icon><Upload /></el-icon> 选择文件
              </el-button>
            </el-upload>
            <div class="upload-tip">支持 doc/docx/pdf/zip/rar 格式，单个文件不超过 50MB</div>
            <div v-if="submitFileList.length > 0" class="file-preview">
              <el-icon><Document /></el-icon>
              <span>{{ submitFileList[0].name }}</span>
              <el-button type="danger" size="small" link @click="handleSubmitFileRemove">移除</el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitMidterm">确认提交</el-button>
      </template>
    </el-dialog>

    <!-- ===== 详情对话框 ===== -->
    <el-dialog v-model="detailVisible" :title="`中期检查详情 - ${currentRow?.studentName || ''}`" width="700px" destroy-on-close>
      <div v-if="currentRow" class="detail-panel">
        <el-descriptions :column="2" border size="small" style="margin-bottom:20px">
          <el-descriptions-item label="学生姓名">{{ currentRow.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentRow.studentNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="学校">{{ currentRow.campusName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="指导教师">{{ currentRow.teacherName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="课题" :span="2">
            <span v-if="currentRow.isCustomTopic" class="custom-tag">自拟</span>
            {{ currentRow.isCustomTopic ? currentRow.customTopicName : currentRow.topicName }}
          </el-descriptions-item>
          <el-descriptions-item label="学年">{{ currentRow.academicYear }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDate(currentRow.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag size="small" :type="midtermTagType(currentRow.status)">
              {{ midtermStatusText(currentRow.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核人">{{ currentRow.inspectorName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ formatDate(currentRow.inspectTime) }}</el-descriptions-item>
          <el-descriptions-item label="审核意见" :span="2">
            {{ currentRow.inspectComment || '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="报告内容" :span="2">
            <div class="report-content">{{ currentRow.reportContent || '—' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="附件" :span="2">
            <span v-if="currentRow.fileName" class="file-link">
              <el-icon><Document /></el-icon>
              {{ currentRow.fileName }}
            </span>
            <span v-else style="color:#c0c4cc">无</span>
          </el-descriptions-item>
        </el-descriptions>

        <template v-if="currentRow.status === 'PENDING' && !isStudent">
          <el-divider />
          <div class="review-action">
            <el-button type="success" :loading="reviewLoading" @click="handleReview('PASSED')">通过</el-button>
            <el-button type="danger" :loading="reviewLoading" @click="handleReview('FAILED')">驳回</el-button>
          </div>
        </template>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 驳回原因对话框 ===== -->
    <el-dialog v-model="rejectDialogVisible" title="驳回中期检查" width="500px" destroy-on-close>
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef" label-width="100px">
        <el-form-item label="驳回原因" prop="inspectComment">
          <el-input
            v-model="rejectForm.inspectComment"
            type="textarea"
            :rows="4"
            placeholder="请填写驳回原因，以便学生了解改进方向"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="reviewLoading" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Finished, Document, RefreshLeft } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { fetchMySelection, submitMidTerm, recallMidTerm, fetchAdminMidTermPage, fetchTeacherMidTermPage, reviewMidTerm } from '@/api/project'

const store = useUserStore()

const isStudent = computed(() => store.user?.userType === 1)
const isAdmin = computed(() => store.user?.userType === 3)

// ======== 学生端 ========
const mySelection = ref(null)
const myMidTerm = ref(null)

async function loadMyData() {
  try {
    mySelection.value = await fetchMySelection()
    if (!mySelection.value) return
    try {
      myMidTerm.value = await fetchMyMidTerm()
    } catch {
      myMidTerm.value = null
    }
  } catch { /* handled by interceptor */ }
}

function midtermTagType(status) {
  if (status === 'PASSED') return 'success'
  if (status === 'FAILED') return 'danger'
  if (status === 'PENDING') return 'warning'
  if (status === 'SUBMITTED') return 'primary'
  return 'info'
}

function midtermStatusText(status) {
  if (!status) return '未提交'
  const map = {
    SUBMITTED: '已提交',
    PENDING: '待审核',
    PASSED: '已通过',
    FAILED: '已驳回',
  }
  return map[status.toUpperCase()] || status
}

function selectionTagType(status) {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'WITHDRAWN') return 'warning'
  return 'info'
}

// 提交对话框
const submitDialogVisible = ref(false)
const submitLoading = ref(false)
const submitFormRef = ref(null)
const submitUploadRef = ref(null)
const submitFileList = ref([])
const pendingReviewRow = ref(null)

const submitForm = reactive({
  reportContent: '',
  fileId: null,
})

const submitRules = {
  reportContent: [
    { required: true, message: '请填写中期报告内容', trigger: 'blur' },
    { min: 10, message: '报告内容至少填写10个字符', trigger: 'blur' },
  ],
}

function openSubmitDialog() {
  submitForm.reportContent = ''
  submitForm.fileId = null
  submitFileList.value = []
  submitDialogVisible.value = true
}

function handleSubmitFileChange(file, files) {
  submitFileList.value = files.slice(-1)
}

function handleSubmitFileRemove() {
  submitFileList.value = []
  submitForm.fileId = null
}

async function handleSubmitMidterm() {
  const valid = await submitFormRef.value.validate().catch(() => false)
  if (!valid) return

  if (submitFileList.value.length === 0) {
    ElMessage.warning('请选择要上传的附件')
    return
  }

  submitLoading.value = true
  try {
    const file = submitFileList.value[0]
    const formData = new FormData()
    formData.append('reportContent', submitForm.reportContent)
    formData.append('selectionId', mySelection.value.selectionId)
    formData.append('file', file.raw)

    await submitMidTerm(formData)
    ElMessage.success('中期检查提交成功，已通知指导教师')
    submitDialogVisible.value = false
    await loadMyData()
  } catch { /* handled */ } finally {
    submitLoading.value = false
  }
}

async function handleRecall() {
  if (!myMidTerm.value?.midId) return
  try {
    await ElMessageBox.confirm('确定要撤回中期检查吗？撤回后可重新提交。', '确认撤回', {
      confirmButtonText: '撤回',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await recallMidTerm(myMidTerm.value.midId)
    ElMessage.success('已成功撤回')
    await loadMyData()
  } catch { /* cancel */ }
}

// ======== 管理员/教师端 ========
const loading = ref(false)
const tableData = ref([])
const query = reactive({
  campusName: '',
  keyword: '',
  academicYear: '',
  status: '',
})
const pagination = reactive({
  total: 0,
  current: 1,
  size: 10,
})

const academicYearOptions = computed(() => {
  const y = new Date().getFullYear()
  return [`${y - 1}-${y}`, `${y}-${y + 1}`, `${y + 1}-${y + 2}`]
})

async function loadData(page = 1) {
  pagination.current = page
  loading.value = true
  try {
    const params = {
      current: page,
      size: pagination.size,
      campusName: isAdmin.value ? (query.campusName || undefined) : undefined,
      keyword: query.keyword || undefined,
      academicYear: query.academicYear || undefined,
      status: query.status || undefined,
    }
    let res
    if (isAdmin.value) {
      res = await fetchAdminMidTermPage(params)
    } else {
      res = await fetchTeacherMidTermPage(params)
    }
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch { /* handled by interceptor */ } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.campusName = ''
  query.keyword = ''
  query.academicYear = ''
  query.status = ''
  loadData(1)
}

// 详情
const detailVisible = ref(false)
const currentRow = ref(null)

function openDetail(row) {
  currentRow.value = { ...row }
  detailVisible.value = true
}

// 审核
const reviewLoading = ref(false)
const rejectDialogVisible = ref(false)
const rejectFormRef = ref(null)
const rejectForm = reactive({ inspectComment: '' })
const rejectRules = {
  inspectComment: [
    { required: true, message: '请填写驳回原因', trigger: 'blur' },
    { min: 5, message: '驳回原因至少5个字符', trigger: 'blur' },
  ],
}

function openReviewDialog(row, status) {
  pendingReviewRow.value = row
  if (status === 'PASSED') {
    handleReview('PASSED')
  } else {
    rejectForm.inspectComment = ''
    rejectDialogVisible.value = true
  }
}

async function confirmReject() {
  const valid = await rejectFormRef.value.validate().catch(() => false)
  if (!valid) return
  reviewLoading.value = true
  try {
    await reviewMidTerm({
      midId: pendingReviewRow.value.midId,
      status: 'FAILED',
      inspectComment: rejectForm.inspectComment,
    })
    ElMessage.success('已驳回中期检查，已通知学生')
    rejectDialogVisible.value = false
    detailVisible.value = false
    await loadData()
  } catch { /* handled */ } finally {
    reviewLoading.value = false
  }
}

async function handleReview(status) {
  reviewLoading.value = true
  try {
    await reviewMidTerm({
      midId: currentRow.value.midId,
      status: status,
      inspectComment: status === 'FAILED' ? rejectForm.inspectComment : null,
    })
    ElMessage.success(status === 'PASSED' ? '已通过审核，已通知学生' : '已驳回，已通知学生')
    detailVisible.value = false
    await loadData()
  } catch { /* handled */ } finally {
    reviewLoading.value = false
  }
}

// ======== 公共函数 ========
function formatDate(value) {
  if (!value) return '—'
  const d = new Date(value)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// ======== 初始化 ========
onMounted(async () => {
  if (isStudent.value) {
    await loadMyData()
  } else {
    await loadData()
  }
})
</script>

<style scoped>
.midterm-page {
  padding: 16px;
}

.intro-card {
  margin-bottom: 16px;
}

.intro-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.selection-info {
  margin-top: 8px;
}

.action-bar {
  margin-top: 16px;
  display: flex;
  gap: 8px;
}

.result-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-content {
  margin-bottom: 0;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #606266;
  font-size: 14px;
}

.search-card {
  margin-bottom: 16px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.custom-tag {
  background: #fdf6ec;
  color: #e6a23c;
  border: 1px solid #f5dab1;
  border-radius: 4px;
  padding: 1px 5px;
  font-size: 12px;
  margin-right: 4px;
}

.file-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #409eff;
  cursor: pointer;
  font-size: 13px;
}

.file-link:hover {
  color: #66b1ff;
}

.detail-panel {
  max-height: 60vh;
  overflow-y: auto;
}

.report-content {
  white-space: pre-wrap;
  color: #606266;
  line-height: 1.6;
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
}

.review-action {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.upload-area {
  width: 100%;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 6px;
}

.file-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  padding: 8px 12px;
  background: #f0f9eb;
  border: 1px solid #e1f3d8;
  border-radius: 4px;
  color: #67c23a;
  font-size: 13px;
}
</style>
