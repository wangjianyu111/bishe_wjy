<template>
  <div class="thesis-page">
    <!-- ===== 学生端视图 ===== -->
    <template v-if="isStudent">
      <el-card shadow="never" class="intro-card">
        <div class="intro-title">
          <el-icon color="#409eff" size="18"><Finished /></el-icon>
          <span>我的论文文档</span>
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
          </el-descriptions>
        </div>
        <el-empty v-else description="您还没有审批通过的选题，无法提交论文文档" />

        <div class="action-bar">
          <el-button type="primary" @click="handleStudentSubmitClick">
            <el-icon><Upload /></el-icon>
            {{ studentSubmitButtonLabel }}
          </el-button>
          <el-button
            v-if="mySelection?.status === 'APPROVED' && latestThesis && latestThesis.status === 'PENDING'"
            type="warning"
            @click="handleRecall"
          >
            <el-icon><RefreshLeft /></el-icon>
            撤回提交
          </el-button>
        </div>
      </el-card>

      <!-- 历史记录表格 -->
      <el-card shadow="never" class="history-card">
        <template #header>
          <div class="card-header">
            <span>提交记录</span>
            <span v-if="myThesisList.length" class="record-count">共 {{ myThesisList.length }} 条</span>
          </div>
        </template>
        <el-table :data="myThesisList" border stripe size="small" v-loading="loadingMyData">
          <el-table-column label="序号" type="index" width="60" align="center" />
          <el-table-column prop="createTime" label="提交时间" width="170">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="版本" width="70" align="center">
            <template #default="{ row }">
              <el-tag size="small" type="info">v{{ row.versionNo || 1 }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="thesisTagType(row.status)">
                {{ thesisStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="论文说明" min-width="200">
            <template #default="{ row }">
              <span class="report-summary">{{ row.reportContent || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="附件" width="140">
            <template #default="{ row }">
              <el-tooltip v-if="row.fileName" :content="`点击下载 ${row.fileName}`" placement="top" :show-after="300">
                <span class="file-link" @click.stop="previewFile(row.fileId, row.fileName)">
                  <el-icon><Document /></el-icon>
                  <span class="file-name">{{ row.fileName }}</span>
                </span>
              </el-tooltip>
              <span v-else style="color:#c0c4cc">无</span>
            </template>
          </el-table-column>
          <el-table-column label="审核时间" width="170">
            <template #default="{ row }">
              {{ formatDate(row.inspectTime) }}
            </template>
          </el-table-column>
          <el-table-column label="审核人" width="100">
            <template #default="{ row }">
              {{ row.inspectorName || '—' }}
            </template>
          </el-table-column>
          <el-table-column label="审核意见" min-width="160">
            <template #default="{ row }">
              <span class="inspect-comment">{{ row.inspectComment || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openHistoryDetail(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loadingMyData && !myThesisList.length" description="暂无提交记录" style="padding: 40px 0" />
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
          <el-table-column label="版本" width="70" align="center">
            <template #default="{ row }">
              <el-tag size="small" type="info">v{{ row.versionNo || 1 }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="附件" width="180">
            <template #default="{ row }">
              <el-tooltip v-if="row.fileName" :content="`点击预览 ${row.fileName}`" placement="top" :show-after="300">
                <span class="file-link" @click.stop="previewFile(row.fileId, row.fileName)">
                  <el-icon><Document /></el-icon>
                  <span class="file-name">{{ row.fileName }}</span>
                </span>
              </el-tooltip>
              <span v-else style="color:#c0c4cc">无</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="thesisTagType(row.status)">
                {{ thesisStatusText(row.status) }}
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
              <div class="action-btns">
                <el-button type="primary" link size="small" @click="openDetail(row)">查看详情</el-button>
                <template v-if="row.status === 'PENDING'">
                  <el-button type="success" link size="small" @click="handlePass(row)">通过</el-button>
                  <el-button type="danger" link size="small" @click="openRejectDialog(row)">驳回</el-button>
                </template>
              </div>
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
    <el-dialog v-model="submitDialogVisible" title="提交论文文档" width="600px" destroy-on-close>
      <el-form :model="submitForm" :rules="submitRules" ref="submitFormRef" label-width="110px">
        <el-form-item label="选题名称">
          <span>{{ mySelection?.isCustomTopic ? mySelection.customTopicName : mySelection?.topicName }}</span>
        </el-form-item>
        <el-form-item label="指导教师">
          <span>{{ mySelection?.teacherName || '—' }}</span>
        </el-form-item>
        <el-form-item label="版本号" v-if="latestThesis?.versionNo">
          <el-tag type="info">将提交第 {{ latestThesis.versionNo + 1 }} 版</el-tag>
        </el-form-item>
        <el-form-item label="论文说明" prop="reportContent">
          <el-input
            v-model="submitForm.reportContent"
            type="textarea"
            :rows="5"
            placeholder="请填写论文说明，如研究内容摘要、创新点等"
          />
        </el-form-item>
        <el-form-item label="上传附件" prop="file">
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
              drag
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">将文件拖到此处，或 <em>点击选择</em></div>
            </el-upload>
            <div class="upload-tip">支持 .doc / .docx / .pdf / .zip / .rar，单个文件不超过 50MB</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看详情对话框 ===== -->
    <el-dialog v-model="detailVisible" title="论文文档详情" width="640px" destroy-on-close>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="学生姓名">{{ currentRow?.studentName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentRow?.studentNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="指导教师">{{ currentRow?.teacherName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="学校">{{ currentRow?.campusName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="课题" :span="2">
          <span v-if="currentRow?.isCustomTopic" class="custom-tag">自拟</span>
          {{ currentRow?.isCustomTopic ? currentRow?.customTopicName : currentRow?.topicName }}
        </el-descriptions-item>
        <el-descriptions-item label="学年">{{ currentRow?.academicYear || '—' }}</el-descriptions-item>
        <el-descriptions-item label="版本">
          <el-tag size="small" type="info">v{{ currentRow?.versionNo || 1 }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="thesisTagType(currentRow?.status)">
            {{ thesisStatusText(currentRow?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间" :span="2">
          {{ formatDate(currentRow?.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="论文说明" :span="2">
          <div class="report-content-wrap">{{ currentRow?.reportContent || '—' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="附件" :span="2">
          <template v-if="currentRow?.fileName">
            <span class="file-link" @click="previewFile(currentRow.fileId, currentRow.fileName)">
              <el-icon><Document /></el-icon>
              {{ currentRow.fileName }}
            </span>
          </template>
          <span v-else style="color:#909399">无</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentRow?.status !== 'PENDING'" label="审核人">
          {{ currentRow?.inspectorName || '—' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentRow?.status !== 'PENDING'" label="审核时间">
          {{ formatDate(currentRow?.inspectTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentRow?.status !== 'PENDING'" label="审核意见" :span="2">
          {{ currentRow?.inspectComment || '—' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer v-if="!isStudent && currentRow?.status === 'PENDING'">
        <el-button type="success" :loading="reviewLoading" @click="handlePass(currentRow)">通过</el-button>
        <el-button type="danger" :loading="reviewLoading" @click="openRejectDialog(currentRow)">驳回</el-button>
      </template>
    </el-dialog>

    <!-- ===== 驳回原因对话框 ===== -->
    <el-dialog v-model="rejectDialogVisible" title="驳回论文文档" width="500px" destroy-on-close>
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef" label-width="100px">
        <el-form-item label="驳回原因" prop="inspectComment">
          <el-input
            v-model="rejectForm.inspectComment"
            type="textarea"
            :rows="4"
            placeholder="请填写驳回原因，以便学生修改后重新提交"
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
import { formatDateTime as formatDate } from '@/utils/timeFormat'
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, UploadFilled, Finished, Document, RefreshLeft } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  fetchMySelection,
  submitThesis,
  recallThesis,
  fetchAdminThesisPage,
  fetchTeacherThesisPage,
  reviewThesis,
  downloadDocFile,
  fetchMyThesisList,
} from '@/api/project'

const store = useUserStore()

const isStudent = computed(() => store.user?.userType === 1)
const isAdmin = computed(() => store.user?.userType === 3)

// ======== 学生端 ========
const loadingMyData = ref(false)
const mySelection = ref(null)
const myThesisList = ref([])

async function loadMyData() {
  loadingMyData.value = true
  try {
    mySelection.value = await fetchMySelection()
    if (!mySelection.value) {
      myThesisList.value = []
      return
    }
    try {
      const res = await fetchMyThesisList()
      myThesisList.value = Array.isArray(res) ? res : []
    } catch {
      myThesisList.value = []
    }
  } catch { /* handled by interceptor */ } finally {
    loadingMyData.value = false
  }
}

const latestThesis = computed(() => myThesisList.value[0] ?? null)

const studentSubmitButtonLabel = computed(() => {
  if (!mySelection.value) return '提交论文文档'
  if (mySelection.value.status !== 'APPROVED') return '提交论文文档（选题审批中）'
  const latest = latestThesis.value
  if (!latest) return '提交论文文档'
  if (String(latest.status || '').toUpperCase() === 'FAILED') return '重新提交论文文档'
  if (String(latest.status || '').toUpperCase() === 'PASSED') return '再次提交论文文档'
  return '提交论文文档（审核中）'
})

function handleStudentSubmitClick() {
  if (!mySelection.value) {
    ElMessage.warning('您还没有选题，无法提交论文文档')
    return
  }
  if (mySelection.value.status !== 'APPROVED') {
    ElMessage.warning('选题尚未审批通过，请等待审批完成后再提交论文文档')
    return
  }
  const latest = latestThesis.value
  const st = latest?.status ? String(latest.status).toUpperCase() : ''
  if (st === 'PENDING') {
    ElMessage.warning('当前有一份论文正在审核中，请先点击「撤回提交」后再重新上传')
    return
  }
  openSubmitDialog()
}

function thesisTagType(status) {
  if (status === 'PASSED') return 'success'
  if (status === 'FAILED') return 'danger'
  if (status === 'PENDING') return 'warning'
  if (status === 'SUBMITTED') return 'primary'
  return 'info'
}

function thesisStatusText(status) {
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
  if (status === 'PENDING') return 'warning'
  return 'info'
}

// 文件预览
async function previewFile(fileId, fileName) {
  if (!fileId) {
    ElMessage.warning('文件不存在')
    return
  }
  try {
    const res = await downloadDocFile(fileId)
    if (!res) {
      ElMessage.error('文件下载失败')
      return
    }
    const blob = res instanceof Blob ? res : new Blob([res])
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  } catch (e) {
    // 错误已在拦截器处理
  }
}

const submitForm = reactive({
  reportContent: '',
  fileId: null,
})

const submitRules = {
  reportContent: [
    { required: true, message: '请填写论文说明', trigger: 'blur' },
    { min: 10, message: '论文说明至少填写10个字符', trigger: 'blur' },
  ],
}

const submitDialogVisible = ref(false)
const submitFormRef = ref(null)
const submitUploadRef = ref(null)
const submitFileList = ref([])
const submitLoading = ref(false)

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

async function handleSubmit() {
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

    await submitThesis(formData)
    ElMessage.success('论文文档提交成功，已通知指导教师')
    submitDialogVisible.value = false
    await loadMyData()
  } catch { /* handled */ } finally {
    submitLoading.value = false
  }
}

async function handleRecall() {
  const latest = myThesisList.value[0]
  if (!latest?.thesisId) return
  try {
    await ElMessageBox.confirm('确定要撤回论文文档吗？撤回后可重新提交。', '确认撤回', {
      confirmButtonText: '撤回',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await recallThesis(latest.thesisId)
    ElMessage.success('已成功撤回')
    await loadMyData()
  } catch { /* cancel */ }
}

function openHistoryDetail(row) {
  currentRow.value = { ...row }
  detailVisible.value = true
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
      res = await fetchAdminThesisPage(params)
    } else {
      res = await fetchTeacherThesisPage(params)
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
const pendingRejectRow = ref(null)

function openRejectDialog(row) {
  pendingRejectRow.value = row
  rejectForm.inspectComment = ''
  rejectDialogVisible.value = true
}

async function confirmReject() {
  const valid = await rejectFormRef.value.validate().catch(() => false)
  if (!valid) return
  reviewLoading.value = true
  try {
    await reviewThesis({
      thesisId: pendingRejectRow.value.thesisId,
      status: 'FAILED',
      inspectComment: rejectForm.inspectComment,
    })
    ElMessage.success('已驳回论文文档，已通知学生')
    rejectDialogVisible.value = false
    detailVisible.value = false
    await loadData()
  } catch { /* handled */ } finally {
    reviewLoading.value = false
  }
}

async function handlePass(row) {
  reviewLoading.value = true
  try {
    await reviewThesis({
      thesisId: row.thesisId,
      status: 'PASSED',
      inspectComment: null,
    })
    ElMessage.success('已通过审核，已通知学生')
    detailVisible.value = false
    await loadData()
  } catch { /* handled */ } finally {
    reviewLoading.value = false
  }
}

// ======== 公共函数 ========
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
.thesis-page {
  padding: 16px;
}

.intro-card {
  margin-bottom: 16px;
}

.history-card {
  margin-bottom: 16px;
}

.record-count {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.report-summary {
  color: #606266;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.inspect-comment {
  color: #e6a23c;
  font-size: 13px;
}

.file-link {
  color: #409eff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.file-link:hover {
  text-decoration: underline;
}

.file-name {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.custom-tag {
  display: inline-block;
  background: #fdf6ec;
  color: #e6a23c;
  border: 1px solid #faecd8;
  border-radius: 4px;
  padding: 0 4px;
  font-size: 12px;
  line-height: 18px;
  margin-right: 4px;
}

.report-content-wrap {
  white-space: pre-wrap;
  word-break: break-word;
  color: #606266;
  font-size: 14px;
  line-height: 1.8;
  max-height: 200px;
  overflow-y: auto;
}

.action-btns {
  display: flex;
  align-items: center;
  gap: 4px;
}

.search-card {
  margin-bottom: 16px;
}

.intro-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.selection-info {
  margin-bottom: 12px;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 4px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.upload-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.upload-area :deep(.el-upload) {
  width: 100%;
}

.upload-area :deep(.el-upload-dragger) {
  width: 100%;
  padding: 24px 16px;
}

.upload-area :deep(.el-icon--upload) {
  font-size: 40px;
  color: #c0c4cc;
  margin-bottom: 8px;
}

.upload-area :deep(.el-upload__text) {
  font-size: 14px;
  color: #606266;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
}
</style>
