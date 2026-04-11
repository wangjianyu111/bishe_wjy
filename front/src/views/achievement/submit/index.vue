<template>
  <div class="achievement-submit-page">
    <!-- ===== 学生端视图 ===== -->
    <template v-if="isStudent">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <div class="intro-title">
              <el-icon color="#409eff" size="18"><Trophy /></el-icon>
              <span>我的成果提交</span>
            </div>
            <el-button type="primary" @click="openSubmitDialog" :disabled="!mySelection">
              <el-icon><Upload /></el-icon>
              上传成果压缩包
            </el-button>
          </div>
        </template>

        <div v-if="mySelection" class="selection-info">
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="选题名称">
              <span>{{ mySelection.isCustomTopic ? mySelection.customTopicName : mySelection.topicName }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="指导教师">
              <span>{{ mySelection.teacherName || '—' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="学校">
              <span>{{ mySelection.campusName || '—' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="学年">
              <span>{{ mySelection.academicYear }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="选题状态">
              <el-tag size="small" :type="selectionTagType(mySelection.status)">
                {{ mySelection.statusLabel }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <el-empty v-else description="您还没有选题，无法提交成果" />

        <div v-if="mySubmission" class="submission-card">
          <div class="submission-info">
            <div class="file-section">
              <el-icon size="20" color="#409eff"><Document /></el-icon>
              <div class="file-info">
                <div class="file-name">{{ mySubmission.fileName }}</div>
                <div class="file-size">{{ formatFileSize(mySubmission.fileSize) }}</div>
              </div>
            </div>
            <div class="file-meta">
              <span v-if="mySubmission.remark" class="remark-text">备注：{{ mySubmission.remark }}</span>
              <span class="upload-time">上传时间：{{ mySubmission.createTime }}</span>
            </div>
          </div>
          <div class="submission-actions">
            <el-button type="primary" link size="small" @click="downloadFile(mySubmission.fileId, mySubmission.fileName)">
              <el-icon><Download /></el-icon>下载
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </div>
        </div>
        <el-empty v-else-if="!loadingMyData && mySelection" description="尚未上传成果压缩包" style="margin-top:8px" />
      </el-card>
    </template>

    <!-- ===== 管理员/教师端视图 ===== -->
    <template v-else>
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>成果提交管理</span>
            <div class="header-right">
              <span v-if="pagination.total > 0" class="record-count">共 {{ pagination.total }} 条</span>
              <el-button
                v-if="selectedRows.length > 0"
                type="primary"
                size="small"
                @click="handleBatchDownload"
              >
                <el-icon><Download /></el-icon>
                批量下载 ({{ selectedRows.length }})
              </el-button>
            </div>
          </div>
        </template>

        <!-- 搜索栏 -->
        <el-form :inline="true" class="filter-bar">
          <el-form-item label="学校">
            <el-input v-model="query.campusName" placeholder="学校名称" clearable style="width:150px" />
          </el-form-item>
          <el-form-item label="学年">
            <el-input v-model="query.academicYear" placeholder="如 2024" clearable style="width:120px" />
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="query.keyword" placeholder="学生姓名/学号/课题" clearable style="width:160px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table
          ref="tableRef"
          :data="tableData"
          border
          stripe
          v-loading="loading"
          size="small"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="50" :selectable="checkSelectable" />
          <el-table-column prop="studentName" label="学生" width="100" />
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column label="学校" width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ row.campusName || '—' }}</template>
          </el-table-column>
          <el-table-column label="课题" min-width="180">
            <template #default="{ row }">
              <span v-if="row.isCustomTopic" class="custom-tag">自拟</span>
              {{ row.isCustomTopic ? row.customTopicName : row.topicName }}
            </template>
          </el-table-column>
          <el-table-column prop="teacherName" label="指导教师" width="100">
            <template #default="{ row }">{{ row.teacherName || '—' }}</template>
          </el-table-column>
          <el-table-column label="压缩包" min-width="200">
            <template #default="{ row }">
              <template v-if="row.fileName">
                <span class="file-link" @click="downloadFile(row.fileId, row.fileName)">
                  <el-icon><Document /></el-icon>
                  {{ row.fileName }}
                </span>
                <span v-if="row.fileSize" class="file-size">({{ formatFileSize(row.fileSize) }})</span>
              </template>
              <span v-else style="color:#c0c4cc">无</span>
            </template>
          </el-table-column>
          <el-table-column prop="academicYear" label="学年" width="110" />
          <el-table-column prop="createTime" label="上传时间" width="170" />
          <el-table-column label="操作" width="100" fixed="right">
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
    </template>

    <!-- ===== 学生上传对话框 ===== -->
    <el-dialog v-model="submitDialogVisible" title="上传成果压缩包" width="560px" destroy-on-close>
      <el-form :model="submitForm" :rules="submitRules" ref="submitFormRef" label-width="100px">
        <el-form-item label="选题名称">
          <span>{{ mySelection?.isCustomTopic ? mySelection.customTopicName : mySelection?.topicName }}</span>
        </el-form-item>
        <el-form-item label="指导教师">
          <span>{{ mySelection?.teacherName || '—' }}</span>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input
            v-model="submitForm.remark"
            type="textarea"
            :rows="3"
            placeholder="可选，填写对成果的说明"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="上传压缩包" prop="file">
          <div class="upload-area">
            <el-upload
              ref="submitUploadRef"
              :auto-upload="false"
              :limit="1"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              :file-list="fileList"
              accept=".zip,.rar,.7z,.tar,.gz"
              action="#"
              drag
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">将压缩包拖到此处，或 <em>点击选择</em></div>
            </el-upload>
            <div class="upload-tip">支持 .zip / .rar / .7z / .tar.gz 等压缩包格式，单个文件不超过 50MB</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- ===== 详情对话框 ===== -->
    <el-dialog v-model="detailVisible" title="成果提交详情" width="600px" destroy-on-close>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="学生姓名">{{ currentRow?.studentName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentRow?.studentNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="学校">{{ currentRow?.campusName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="指导教师">{{ currentRow?.teacherName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="课题" :span="2">
          <span v-if="currentRow?.isCustomTopic" class="custom-tag">自拟</span>
          {{ currentRow?.isCustomTopic ? currentRow?.customTopicName : currentRow?.topicName }}
        </el-descriptions-item>
        <el-descriptions-item label="学年">{{ currentRow?.academicYear || '—' }}</el-descriptions-item>
        <el-descriptions-item label="上传时间">{{ currentRow?.createTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="备注说明" :span="2">
          <div>{{ currentRow?.remark || '—' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="压缩包" :span="2">
          <template v-if="currentRow?.fileName">
            <span class="file-link" @click="downloadFile(currentRow.fileId, currentRow.fileName)">
              <el-icon><Document /></el-icon>
              {{ currentRow.fileName }}
            </span>
            <span v-if="currentRow?.fileSize" class="file-size">({{ formatFileSize(currentRow.fileSize) }})</span>
          </template>
          <span v-else style="color:#909399">无</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="currentRow?.fileId"
          type="primary"
          @click="downloadFile(currentRow.fileId, currentRow.fileName)"
        >
          <el-icon><Download /></el-icon>下载压缩包
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, UploadFilled, Trophy, Document, Delete, Download } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  fetchMySelection,
  fetchMyAchievement,
  submitAchievement,
  deleteAchievement,
  fetchAdminAchievementPage,
  fetchTeacherAchievementPage,
  downloadDocFile,
} from '@/api/project'

const store = useUserStore()
const isStudent = computed(() => store.user?.userType === 1)
const isAdmin = computed(() => store.user?.userType === 3)

// ======== 学生端 ========
const loadingMyData = ref(false)
const mySelection = ref(null)
const mySubmission = ref(null)
const submitDialogVisible = ref(false)
const submitLoading = ref(false)
const submitFormRef = ref(null)
const submitUploadRef = ref(null)
const fileList = ref([])
const submitForm = ref({ remark: '' })
const submitRules = {
  file: [{ required: true, message: '请选择压缩包', trigger: 'change' }],
}

async function loadMyData() {
  loadingMyData.value = true
  try {
    mySelection.value = await fetchMySelection()
    if (!mySelection.value) {
      mySubmission.value = null
      return
    }
    try {
      mySubmission.value = await fetchMyAchievement(mySelection.value.selectionId)
    } catch {
      mySubmission.value = null
    }
  } catch {
  } finally {
    loadingMyData.value = false
  }
}

function openSubmitDialog() {
  if (!mySelection.value) {
    ElMessage.warning('您还没有选题，无法上传成果')
    return
  }
  if (mySelection.value.status !== 'APPROVED') {
    ElMessage.warning('选题尚未审批通过，请等待审批完成后再上传')
    return
  }
  submitForm.value.remark = mySubmission.value?.remark || ''
  fileList.value = []
  submitDialogVisible.value = true
}

function handleFileChange(file) {
  fileList.value = [file.raw]
}

function handleFileRemove() {
  fileList.value = []
}

async function handleSubmit() {
  if (!fileList.value.length) {
    ElMessage.warning('请选择要上传的压缩包')
    return
  }
  submitLoading.value = true
  try {
    const fd = new FormData()
    fd.append('selectionId', mySelection.value.selectionId)
    fd.append('remark', submitForm.value.remark || '')
    fd.append('file', fileList.value[0])
    await submitAchievement(fd)
    ElMessage.success('上传成功')
    submitDialogVisible.value = false
    loadMyData()
  } catch (e) {
    ElMessage.error(e.message || '上传失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete() {
  await ElMessageBox.confirm('确定要删除已上传的成果压缩包吗？', '删除确认', { type: 'warning' })
  try {
    await deleteAchievement(mySubmission.value.submitId)
    ElMessage.success('删除成功')
    mySubmission.value = null
    loadMyData()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

// ======== 管理端 ========
const loading = ref(false)
const tableRef = ref(null)
const tableData = ref([])
const selectedRows = ref([])
const pagination = reactive({ total: 0, current: 1, size: 10 })
const query = reactive({
  campusName: '',
  academicYear: '',
  keyword: '',
})

async function loadData(page) {
  loading.value = true
  try {
    const params = { current: page || pagination.current, size: pagination.size, ...query }
    const res = isAdmin.value
      ? await fetchAdminAchievementPage(params)
      : await fetchTeacherAchievementPage(params)
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
  query.campusName = ''
  query.academicYear = ''
  query.keyword = ''
  pagination.current = 1
  loadData(1)
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function checkSelectable() {
  return true
}

async function handleBatchDownload() {
  if (!selectedRows.value.length) {
    ElMessage.warning('请选择要下载的记录')
    return
  }
  for (const row of selectedRows.value) {
    if (row.fileId) {
      try {
        await downloadFile(row.fileId, row.fileName)
      } catch {
      }
      await new Promise((r) => setTimeout(r, 500))
    }
  }
  ElMessage.success(`已发送 ${selectedRows.value.filter((r) => r.fileId).length} 个文件的下载请求`)
}

// ======== 文件下载 ========
async function downloadFile(fileId, fileName) {
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
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', fileName)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  } catch {
    ElMessage.error('文件下载失败')
  }
}

// ======== 详情 ========
const detailVisible = ref(false)
const currentRow = ref(null)

function openDetail(row) {
  currentRow.value = row
  detailVisible.value = true
}

// ======== 工具函数 ========
function selectionTagType(status) {
  const s = String(status || '').toUpperCase()
  if (s === 'APPROVED') return 'success'
  if (s === 'PENDING') return 'warning'
  if (s === 'REJECTED') return 'danger'
  return 'info'
}

function formatFileSize(bytes) {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

onMounted(() => {
  if (isStudent.value) {
    loadMyData()
  } else {
    loadData(1)
  }
})
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.intro-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 15px;
}
.selection-info {
  margin-bottom: 12px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
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
.file-link {
  color: #409eff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 13px;
}
.file-link:hover {
  text-decoration: underline;
}
.file-size {
  color: #909399;
  font-size: 12px;
  margin-left: 4px;
}
.custom-tag {
  background: #f0f9eb;
  color: #67c23a;
  border: 1px solid #e1f3d8;
  border-radius: 4px;
  font-size: 12px;
  padding: 0 4px;
  margin-right: 4px;
}
.submission-card {
  margin-top: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 16px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.submission-info {
  flex: 1;
  min-width: 0;
}
.file-section {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.file-info .file-name {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}
.file-info .file-size {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}
.file-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
  color: #909399;
}
.remark-text {
  color: #606266;
  font-size: 13px;
}
.submission-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}
.upload-area {
  width: 100%;
}
.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 6px;
}
</style>
