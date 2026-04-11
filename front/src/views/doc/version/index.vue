<template>
  <div class="version-page">
    <!-- ===== 学生端视图 ===== -->
    <template v-if="isStudent">
      <el-card shadow="never" class="intro-card">
        <template #header>
          <div class="card-header">
            <div class="intro-title">
              <el-icon color="#409eff" size="18"><Finished /></el-icon>
              <span>我的文档版本</span>
            </div>
            <el-button type="primary" @click="handleStudentSubmitClick" :disabled="!mySelection">
              <el-icon><Upload /></el-icon>
              上传文档版本
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
              <el-tag size="small" :type="selectionTagType(mySelection.status)">{{ mySelection.statusLabel }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <el-empty v-else description="您还没有选题，无法上传文档" />

        <!-- 历史记录表格 -->
        <el-table :data="myVersionList" border stripe size="small" v-loading="loadingMyData" style="margin-top:16px">
          <el-table-column label="序号" type="index" width="60" align="center" />
          <el-table-column prop="stageName" label="阶段" width="140" />
          <el-table-column label="版本" width="70" align="center">
            <template #default="{ row }">
              <el-tag size="small" type="info">v{{ row.versionNo || 1 }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
          <el-table-column label="附件" width="160">
            <template #default="{ row }">
              <template v-if="row.fileName">
                <span class="file-link" @click="downloadFile(row.fileId, row.fileName)">
                  <el-icon><Document /></el-icon>
                  {{ row.fileName }}
                </span>
              </template>
              <span v-else style="color:#c0c4cc">无</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="上传时间" width="170" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loadingMyData && myVersionList.length === 0 && mySelection" description="暂无上传记录" style="margin-top:8px" />
      </el-card>
    </template>

    <!-- ===== 管理员/教师端视图 ===== -->
    <template v-else>
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>文档版本管理</span>
            <span v-if="pagination.total > 0" class="record-count">共 {{ pagination.total }} 条</span>
          </div>
        </template>

        <!-- 搜索栏 -->
        <el-form :inline="true" class="filter-bar">
          <el-form-item label="阶段">
            <el-input v-model="query.stageName" placeholder="阶段名称" clearable style="width:140px" />
          </el-form-item>
          <el-form-item label="学年">
            <el-input v-model="query.academicYear" placeholder="如 2024" clearable style="width:120px" />
          </el-form-item>
          <el-form-item label="学校">
            <el-input v-model="query.campusName" placeholder="学校名称" clearable style="width:150px" />
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="query.keyword" placeholder="学生姓名/学号" clearable style="width:150px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="tableData" border stripe v-loading="loading" size="small">
          <el-table-column prop="studentName" label="学生" width="100" />
          <el-table-column prop="studentNo" label="学号" width="110" />
          <el-table-column prop="teacherName" label="指导教师" width="100">
            <template #default="{ row }">
              {{ row.teacherName || '—' }}{{ row.teacherNo ? `(${row.teacherNo})` : '' }}
            </template>
          </el-table-column>
          <el-table-column label="课题" min-width="180">
            <template #default="{ row }">
              <span v-if="row.isCustomTopic" class="custom-tag">自拟</span>
              {{ row.isCustomTopic ? row.customTopicName : row.topicName }}
            </template>
          </el-table-column>
          <el-table-column prop="campusName" label="学校" width="140" show-overflow-tooltip />
          <el-table-column prop="stageName" label="阶段" width="130" />
          <el-table-column label="版本" width="70" align="center">
            <template #default="{ row }">
              <el-tag size="small" type="info">v{{ row.versionNo || 1 }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="附件" width="160">
            <template #default="{ row }">
              <template v-if="row.fileName">
                <span class="file-link" @click="downloadFile(row.fileId, row.fileName)">
                  <el-icon><Document /></el-icon>
                  {{ row.fileName }}
                </span>
              </template>
              <span v-else style="color:#c0c4cc">无</span>
            </template>
          </el-table-column>
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
    <el-dialog v-model="submitDialogVisible" title="上传文档版本" width="600px" destroy-on-close>
      <el-form :model="submitForm" :rules="submitRules" ref="submitFormRef" label-width="100px">
        <el-form-item label="选题名称">
          <span>{{ mySelection?.isCustomTopic ? mySelection.customTopicName : mySelection?.topicName }}</span>
        </el-form-item>
        <el-form-item label="指导教师">
          <span>{{ mySelection?.teacherName || '—' }}</span>
        </el-form-item>
        <el-form-item label="毕设阶段" prop="stageName">
          <el-input v-model="submitForm.stageName" placeholder="请输入毕设阶段，如：开题报告、中期检查、论文定稿" maxlength="100" clearable />
        </el-form-item>
        <el-form-item label="版本号" v-if="nextVersion > 1">
          <el-tag type="info">将提交第 {{ nextVersion }} 版（已在原版本上递增）</el-tag>
        </el-form-item>
        <el-form-item label="备注说明" prop="remark">
          <el-input
            v-model="submitForm.remark"
            type="textarea"
            :rows="3"
            placeholder="可选，填写对该版本的说明"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="上传文件" prop="file">
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
    <el-dialog v-model="detailVisible" title="文档版本详情" width="640px" destroy-on-close>
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
        <el-descriptions-item label="阶段">{{ currentRow?.stageName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="版本">
          <el-tag size="small" type="info">v{{ currentRow?.versionNo || 1 }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="上传时间" :span="2">
          {{ currentRow?.createTime || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注说明" :span="2">
          <div>{{ currentRow?.remark || '—' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="附件" :span="2">
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
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, UploadFilled, Finished, Document } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  fetchMySelection,
  submitVersion,
  deleteVersion,
  fetchAdminVersionPage,
  fetchTeacherVersionPage,
  downloadDocFile,
  fetchMyVersionList,
} from '@/api/project'

const store = useUserStore()

const isStudent = computed(() => store.user?.userType === 1)
const isAdmin = computed(() => store.user?.userType === 3)

// ======== 学生端 ========
const loadingMyData = ref(false)
const mySelection = ref(null)
const myVersionList = ref([])

async function loadMyData() {
  loadingMyData.value = true
  try {
    mySelection.value = await fetchMySelection()
    if (!mySelection.value) {
      myVersionList.value = []
      return
    }
    try {
      const res = await fetchMyVersionList(mySelection.value.selectionId)
      myVersionList.value = Array.isArray(res) ? res : []
    } catch {
      myVersionList.value = []
    }
  } catch {
  } finally {
    loadingMyData.value = false
  }
}

const nextVersion = computed(() => {
  if (!myVersionList.value.length) return 1
  const latest = myVersionList.value.find(v => v.stageName === submitForm.value.stageName)
  return latest ? latest.versionNo + 1 : 1
})

function handleStudentSubmitClick() {
  if (!mySelection.value) {
    ElMessage.warning('您还没有选题，无法上传文档')
    return
  }
  if (mySelection.value.status !== 'APPROVED') {
    ElMessage.warning('选题尚未审批通过，请等待审批完成后再上传文档')
    return
  }
  submitForm.stageName = ''
  submitForm.remark = ''
  submitFileList.value = []
  submitDialogVisible.value = true
}

const submitDialogVisible = ref(false)
const submitLoading = ref(false)
const submitFormRef = ref(null)
const submitUploadRef = ref(null)
const submitFileList = ref([])
const submitForm = ref({
  stageName: '',
  remark: '',
})

const submitRules = {
  stageName: [{ required: true, message: '请填写毕设阶段名称', trigger: 'blur' }],
}

function handleSubmitFileChange(file) {
  submitFileList.value = [file.raw]
}

function handleSubmitFileRemove() {
  submitFileList.value = []
}

async function handleSubmit() {
  const valid = await submitFormRef.value.validate().catch(() => false)
  if (!valid) return
  if (!submitFileList.value.length) {
    ElMessage.warning('请选择要上传的文件')
    return
  }
  submitLoading.value = true
  try {
    const fd = new FormData()
    fd.append('selectionId', mySelection.value.selectionId)
    fd.append('stageName', submitForm.value.stageName)
    fd.append('remark', submitForm.value.remark || '')
    fd.append('file', submitFileList.value[0])
    await submitVersion(fd)
    ElMessage.success('上传成功')
    submitDialogVisible.value = false
    loadMyData()
  } catch {
  } finally {
    submitLoading.value = false
  }
}

// ======== 管理端 ========
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ total: 0, current: 1, size: 10 })
const query = reactive({
  stageName: '',
  academicYear: '',
  campusName: '',
  keyword: '',
})

async function loadData(page) {
  loading.value = true
  try {
    const params = { current: page || pagination.current, size: pagination.size, ...query }
    const res = isStudent.value ? null : isAdmin.value
      ? await fetchAdminVersionPage(params)
      : await fetchTeacherVersionPage(params)
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
  query.stageName = ''
  query.academicYear = ''
  query.campusName = ''
  query.keyword = ''
  pagination.current = 1
  loadData(1)
}

// ======== 详情 ========
const detailVisible = ref(false)
const currentRow = ref(null)

function openDetail(row) {
  currentRow.value = row
  detailVisible.value = true
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

// ======== 工具函数 ========
function selectionTagType(status) {
  const s = String(status || '').toUpperCase()
  if (s === 'APPROVED') return 'success'
  if (s === 'PENDING') return 'warning'
  if (s === 'REJECTED' || s === 'FAILED') return 'danger'
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
.upload-area {
  width: 100%;
}
.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 6px;
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
</style>
