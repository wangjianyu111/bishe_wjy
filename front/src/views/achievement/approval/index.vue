<template>
  <div class="approval-page">
    <!-- ===== 学生端视图 ===== -->
    <template v-if="isStudent">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <div class="intro-title">
              <el-icon color="#409eff" size="18"><Tickets /></el-icon>
              <span>我的审批意见</span>
            </div>
          </div>
        </template>

        <el-form :inline="true" class="filter-bar">
          <el-form-item label="学年">
            <el-input v-model="studentQuery.academicYear" placeholder="如 2025" clearable style="width: 140px" />
          </el-form-item>
          <el-form-item label="文档类型">
            <el-select v-model="studentQuery.docType" placeholder="全部" clearable style="width: 140px">
              <el-option label="开题报告" value="PROPOSAL" />
              <el-option label="中期检查" value="MIDTERM" />
              <el-option label="论文文档" value="THESIS" />
              <el-option label="文档归档" value="ARCHIVE" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="studentQuery.status" placeholder="全部" clearable style="width: 120px">
              <el-option label="待处理" value="SUBMITTED" />
              <el-option label="已通过" value="PASSED" />
              <el-option label="已驳回" value="FAILED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadStudentData(1)">查询</el-button>
            <el-button @click="resetStudentQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="tableData" v-loading="loading" border stripe>
          <el-table-column prop="docTypeLabel" label="文档类型" width="110" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="docTypeTag(row.docType)">{{ row.docTypeLabel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="roundNo" label="评审轮次" width="90" align="center">
            <template #default="{ row }">第{{ row.roundNo }}轮</template>
          </el-table-column>
          <el-table-column prop="score" label="评分" width="80" align="center">
            <template #default="{ row }">
              <span v-if="row.score != null" :class="scoreClass(row.score)">{{ row.score }}分</span>
              <span v-else style="color:#999">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="reviewerName" label="评审教师" width="100" />
          <el-table-column prop="statusLabel" label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="statusTag(row.status)">{{ row.statusLabel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="comment" label="审批意见" min-width="220" show-overflow-tooltip />
          <el-table-column label="附件" width="90" align="center">
            <template #default="{ row }">
              <el-button v-if="row.fileId" type="primary" link size="small" @click="handleDownload(row)">
                <el-icon><Download /></el-icon>下载
              </el-button>
              <span v-else style="color:#999">无</span>
            </template>
          </el-table-column>
          <el-table-column label="时间" width="160">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="pagination-bar"
          background
          layout="total, prev, pager, next"
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          @current-change="loadStudentData"
        />
      </el-card>
    </template>

    <!-- ===== 教师/管理员端视图 ===== -->
    <template v-else>
      <el-card shadow="never" class="search-card">
        <el-form :inline="true" :model="query">
          <el-form-item v-if="isAdmin" label="学校">
            <el-input v-model="query.campusName" placeholder="学校名称" clearable style="width: 160px" />
          </el-form-item>
          <el-form-item label="学年">
            <el-input v-model="query.academicYear" placeholder="如 2024-2025" clearable style="width: 140px" />
          </el-form-item>
          <el-form-item label="文档类型">
            <el-select v-model="query.docType" placeholder="全部" clearable style="width: 140px">
              <el-option label="开题报告" value="PROPOSAL" />
              <el-option label="中期检查" value="MIDTERM" />
              <el-option label="论文文档" value="THESIS" />
              <el-option label="文档归档" value="ARCHIVE" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
              <el-option label="待处理" value="SUBMITTED" />
              <el-option label="已通过" value="PASSED" />
              <el-option label="已驳回" value="FAILED" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="query.keyword" placeholder="学生姓名/学号/课题名" clearable style="width: 180px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData(1)">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card shadow="never" class="table-card">
        <template #header>
          <div class="card-header">
            <span>{{ isAdmin ? '审批意见管理' : '我的评审意见' }}</span>
            <el-button type="primary" @click="openAddDialog">
              <el-icon><Plus /></el-icon>添加意见
            </el-button>
          </div>
        </template>

        <el-table :data="tableData" v-loading="loading" border stripe size="small">
          <el-table-column prop="studentName" label="学生姓名" width="100" />
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column v-if="isAdmin" prop="campusName" label="学校" width="130" />
          <el-table-column label="课题" min-width="200">
            <template #default="{ row }">
              <span v-if="row.isCustomTopic" class="custom-tag">自拟</span>
              {{ row.isCustomTopic ? row.customTopicName : row.topicName }}
            </template>
          </el-table-column>
          <el-table-column prop="docTypeLabel" label="文档类型" width="110" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="docTypeTag(row.docType)">{{ row.docTypeLabel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="roundNo" label="轮次" width="70" align="center">
            <template #default="{ row }">第{{ row.roundNo }}轮</template>
          </el-table-column>
          <el-table-column prop="score" label="评分" width="80" align="center">
            <template #default="{ row }">
              <span v-if="row.score != null" :class="scoreClass(row.score)">{{ row.score }}分</span>
              <span v-else style="color:#999">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="reviewerName" label="评审教师" width="100" />
          <el-table-column prop="statusLabel" label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="statusTag(row.status)">{{ row.statusLabel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="comment" label="审批意见" min-width="200" show-overflow-tooltip />
          <el-table-column label="附件" width="80" align="center">
            <template #default="{ row }">
              <el-button v-if="row.fileId" type="primary" link size="small" @click="handleDownload(row)">
                <el-icon><Download /></el-icon>下载
              </el-button>
              <span v-else style="color:#999">无</span>
            </template>
          </el-table-column>
          <el-table-column label="时间" width="160">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openEditDialog(row)">编辑</el-button>
              <el-button type="success" link size="small" @click="handlePass(row)">通过</el-button>
              <el-button type="danger" link size="small" @click="openReject(row)">驳回</el-button>
              <el-button type="info" link size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadData(1)"
            @current-change="loadData"
          />
        </div>
      </el-card>
    </template>

    <!-- ===== 添加/编辑意见弹窗 ===== -->
    <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑审批意见' : '添加审批意见'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="100px" :rules="formRules">
        <el-form-item label="选题" prop="selectionId">
          <el-select
            v-model="form.selectionId"
            filterable
            placeholder="请选择选题（输入学生姓名或课题名搜索）"
            style="width: 100%"
          >
            <el-option
              v-for="item in selectionOptions"
              :key="item.selectionId"
              :label="`${item.studentName} - ${item.isCustomTopic ? item.customTopicName : item.topicName}`"
              :value="item.selectionId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="文档类型" prop="docType">
          <el-select v-model="form.docType" placeholder="请选择文档类型" style="width: 100%">
            <el-option label="开题报告" value="PROPOSAL" />
            <el-option label="中期检查" value="MIDTERM" />
            <el-option label="论文文档" value="THESIS" />
            <el-option label="文档归档" value="ARCHIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="评审轮次">
          <el-input-number v-model="form.roundNo" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="评分（可选）">
          <el-input-number v-model="form.score" :min="0" :max="100" :precision="0" />
          <span style="margin-left: 8px; color: #999">0-100分</span>
        </el-form-item>
        <el-form-item label="文字批注">
          <el-input v-model="form.comment" type="textarea" :rows="5" placeholder="请输入审批意见或批注内容" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="附件（可选）">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".doc,.docx,.pdf,.zip,.rar,.xls,.xlsx"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
          >
            <el-button type="primary" plain>
              <el-icon><Upload /></el-icon>选择文件
            </el-button>
            <template #tip>
              <div class="upload-tip">支持 doc/docx/pdf/zip/rar/xls/xlsx，单文件不超过50MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- ===== 驳回弹窗 ===== -->
    <el-dialog v-model="rejectDialogVisible" title="驳回审批意见" width="500px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="学生姓名"><span>{{ rejectForm.studentName }}</span></el-form-item>
        <el-form-item label="课题"><span>{{ rejectForm.topicName }}</span></el-form-item>
        <el-form-item label="驳回原因" required>
          <el-input v-model="rejectForm.comment" type="textarea" :rows="4" placeholder="请填写驳回原因" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>

    <!-- ===== 详情弹窗 ===== -->
    <el-dialog v-model="detailDialogVisible" title="审批意见详情" width="620px" destroy-on-close>
      <el-descriptions v-if="currentOpinion" :column="1" border size="default">
        <el-descriptions-item label="学生姓名">{{ currentOpinion.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentOpinion.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="课题">{{ currentOpinion.isCustomTopic ? currentOpinion.customTopicName : currentOpinion.topicName }}</el-descriptions-item>
        <el-descriptions-item label="文档类型"><el-tag size="small">{{ currentOpinion.docTypeLabel }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="评审轮次">第{{ currentOpinion.roundNo }}轮</el-descriptions-item>
        <el-descriptions-item label="评分">{{ currentOpinion.score != null ? currentOpinion.score + '分' : '未评分' }}</el-descriptions-item>
        <el-descriptions-item label="评审教师">{{ currentOpinion.reviewerName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="statusTag(currentOpinion.status)">{{ currentOpinion.statusLabel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审批意见"><div style="white-space: pre-wrap;">{{ currentOpinion.comment || '无' }}</div></el-descriptions-item>
        <el-descriptions-item label="附件">
          <el-button v-if="currentOpinion.fileId" type="primary" link size="small" @click="handleDownload(currentOpinion)">
            <el-icon><Download /></el-icon>{{ currentOpinion.fileName || '下载附件' }}
          </el-button>
          <span v-else>无</span>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatTime(currentOpinion.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  fetchTeacherApprovalPage,
  fetchAdminApprovalPage,
  fetchStudentApprovalPage,
  submitApproval,
  updateApprovalStatus,
  deleteApproval,
  fetchApprovalDetail,
  downloadApprovalFile,
} from '@/api/approval'
import { fetchSelectionPage } from '@/api/project'

const userStore = useUserStore()

const isAdmin = computed(() => userStore.user?.userType === 3)
const isStudent = computed(() => userStore.user?.userType === 1)

const loading = ref(false)
const submitting = ref(false)
const actionLoading = ref(false)
const tableData = ref([])

const query = reactive({
  campusName: '',
  academicYear: '',
  docType: '',
  status: '',
  keyword: '',
})

const studentQuery = reactive({
  academicYear: '',
  docType: '',
  status: '',
})

const pagination = reactive({
  total: 0,
  current: 1,
  size: 10,
})

// 表单
const formDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const uploadRef = ref()
const currentOpinionId = ref(null)
const currentFile = ref(null)

const form = reactive({
  selectionId: null,
  docType: '',
  roundNo: 1,
  score: null,
  comment: '',
})

const formRules = {
  selectionId: [{ required: true, message: '请选择选题', trigger: 'change' }],
  docType: [{ required: true, message: '请选择文档类型', trigger: 'change' }],
}

const selectionOptions = ref([])

// 驳回
const rejectDialogVisible = ref(false)
const rejectForm = reactive({
  opinionId: null,
  studentName: '',
  topicName: '',
  comment: '',
})

// 详情
const detailDialogVisible = ref(false)
const currentOpinion = ref(null)

// ===== 加载数据 =====
async function loadData(page = 1) {
  pagination.current = page
  loading.value = true
  try {
    const params = {
      current: page,
      size: pagination.size,
      academicYear: query.academicYear || undefined,
      docType: query.docType || undefined,
      status: query.status || undefined,
      keyword: query.keyword || undefined,
      campusName: isAdmin.value ? (query.campusName || undefined) : undefined,
    }
    const res = isAdmin.value
      ? await fetchAdminApprovalPage(params)
      : await fetchTeacherApprovalPage(params)
    tableData.value = res?.records || []
    pagination.total = res?.total || 0
  } catch { /* interceptor */ } finally {
    loading.value = false
  }
}

async function loadStudentData(page = 1) {
  pagination.current = page
  loading.value = true
  try {
    const params = {
      current: page,
      size: pagination.size,
      academicYear: studentQuery.academicYear || undefined,
      docType: studentQuery.docType || undefined,
      status: studentQuery.status || undefined,
    }
    const res = await fetchStudentApprovalPage(params)
    tableData.value = res?.records || []
    pagination.total = res?.total || 0
  } catch { /* interceptor */ } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.academicYear = ''
  query.docType = ''
  query.status = ''
  query.keyword = ''
  if (isAdmin.value) query.campusName = ''
  loadData(1)
}

function resetStudentQuery() {
  studentQuery.academicYear = ''
  studentQuery.docType = ''
  studentQuery.status = ''
  loadStudentData(1)
}

// ===== 标签方法 =====
function statusTag(status) {
  if (status === 'SUBMITTED') return 'warning'
  if (status === 'PASSED') return 'success'
  if (status === 'FAILED') return 'danger'
  return 'info'
}

function docTypeTag(type) {
  const map = { PROPOSAL: '', MIDTERM: 'warning', THESIS: 'success', ARCHIVE: 'info' }
  return map[type] || 'info'
}

function scoreClass(score) {
  if (score >= 90) return 'score-high'
  if (score >= 60) return 'score-mid'
  return 'score-low'
}

function formatTime(value) {
  if (!value) return '—'
  const d = new Date(value)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// ===== 添加/编辑 =====
function loadSelectionOptions() {
  fetchSelectionPage({ current: 1, size: 1000, status: 'APPROVED' }).then(res => {
    selectionOptions.value = res?.records || []
  }).catch(() => {})
}

function openAddDialog() {
  isEdit.value = false
  currentOpinionId.value = null
  Object.assign(form, { selectionId: null, docType: '', roundNo: 1, score: null, comment: '' })
  currentFile.value = null
  if (uploadRef.value) uploadRef.value.clearFiles()
  loadSelectionOptions()
  formDialogVisible.value = true
}

async function openEditDialog(row) {
  isEdit.value = true
  currentOpinionId.value = row.opinionId
  Object.assign(form, {
    selectionId: row.selectionId,
    docType: row.docType,
    roundNo: row.roundNo || 1,
    score: row.score,
    comment: row.comment || '',
  })
  currentFile.value = null
  if (uploadRef.value) uploadRef.value.clearFiles()
  loadSelectionOptions()
  formDialogVisible.value = true
}

function handleFileChange(file) {
  currentFile.value = file.raw
}

function handleFileRemove() {
  currentFile.value = null
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const fd = new FormData()
    fd.append('selectionId', form.selectionId)
    fd.append('docType', form.docType)
    fd.append('roundNo', form.roundNo || 1)
    if (form.score != null) fd.append('score', form.score)
    fd.append('comment', form.comment || '')
    if (currentFile.value) fd.append('file', currentFile.value)

    await submitApproval(fd)
    ElMessage.success(isEdit.value ? '更新成功' : '提交成功')
    formDialogVisible.value = false
    loadData(pagination.current)
  } catch { /* interceptor */ } finally {
    submitting.value = false
  }
}

// ===== 通过/驳回/删除 =====
async function handlePass(row) {
  try {
    await ElMessageBox.confirm(`确定将学生「${row.studentName}」的审批意见标记为通过吗？`, '确认通过', { type: 'success', confirmButtonText: '确认' })
    actionLoading.value = true
    await updateApprovalStatus({ opinionId: row.opinionId, status: 'PASSED' })
    ElMessage.success('已标记为通过')
    loadData(pagination.current)
  } catch (err) {
    if (err !== 'cancel') ElMessage.error('操作失败')
  } finally {
    actionLoading.value = false
  }
}

function openReject(row) {
  Object.assign(rejectForm, {
    opinionId: row.opinionId,
    studentName: row.studentName,
    topicName: row.isCustomTopic ? row.customTopicName : row.topicName,
    comment: '',
  })
  rejectDialogVisible.value = true
}

async function confirmReject() {
  if (!rejectForm.comment?.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  try {
    actionLoading.value = true
    await updateApprovalStatus({ opinionId: rejectForm.opinionId, status: 'FAILED', comment: rejectForm.comment })
    ElMessage.success('已驳回')
    rejectDialogVisible.value = false
    loadData(pagination.current)
  } catch { /* interceptor */ } finally {
    actionLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该审批意见吗？删除后无法恢复。', '确认删除', { type: 'warning', confirmButtonText: '删除' })
    await deleteApproval(row.opinionId)
    ElMessage.success('删除成功')
    loadData(pagination.current)
  } catch (err) {
    if (err !== 'cancel') ElMessage.error('删除失败')
  }
}

// ===== 详情 =====
async function openDetail(row) {
  try {
    const res = await fetchApprovalDetail(row.opinionId)
    currentOpinion.value = res
    detailDialogVisible.value = true
  } catch { /* interceptor */ }
}

// ===== 下载附件 =====
async function handleDownload(row) {
  if (!row.fileId) return
  try {
    const res = await downloadApprovalFile(row.fileId)
    const blob = new Blob([res])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.fileName || '附件'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch {
    ElMessage.error('下载失败')
  }
}

onMounted(() => {
  if (isStudent.value) {
    loadStudentData()
  } else {
    loadData()
  }
})
</script>

<style scoped>
.approval-page { padding: 16px; }

.search-card { margin-bottom: 12px; }

.table-card :deep(.el-table) { font-size: 13px; }

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.intro-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
}

.filter-bar { margin-bottom: 16px; }

.custom-tag {
  background-color: #e6a23c;
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  margin-right: 4px;
}

.score-high { color: #67c23a; font-weight: 600; }
.score-mid  { color: #409eff; }
.score-low  { color: #f56c6c; }

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.pagination-wrap {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.pagination-bar {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
