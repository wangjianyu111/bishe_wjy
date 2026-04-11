<template>
  <div class="defense-page">
    <!-- ===== 学生端视图 ===== -->
    <template v-if="isStudent">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <div class="intro-title">
              <el-icon color="#409eff" size="18"><Microphone /></el-icon>
              <span>答辩安排</span>
            </div>
          </div>
        </template>

        <!-- 筛选栏 -->
        <el-form :inline="true" class="filter-bar">
          <el-form-item label="学年">
            <el-input v-model="studentQuery.academicYear" placeholder="如 2025" clearable style="width:120px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadStudentDefense"><el-icon><Search /></el-icon>查询</el-button>
            <el-button @click="studentQuery.academicYear = ''"><el-icon><Refresh /></el-icon>重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 答辩安排列表 -->
        <el-table :data="pagination.records" v-loading="loading" stripe>
          <el-table-column prop="sessionName" label="场次名称" min-width="140" />
          <el-table-column prop="defenseDate" label="答辩日期" width="120" />
          <el-table-column label="答辩时间" width="130">
            <template #default="{ row }">
              {{ row.startTime }} ~ {{ row.endTime }}
            </template>
          </el-table-column>
          <el-table-column prop="defenseForm" label="答辩形式" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="row.defenseForm === 'ONLINE' ? 'success' : 'primary'">
                {{ row.defenseForm === 'ONLINE' ? '线上' : '线下' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="答辩地点" min-width="140">
            <template #default="{ row }">
              <span>{{ row.location || '线上答辩' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="academicYear" label="学年" width="100" />
          <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
          <el-table-column label="附件" width="100" align="center">
            <template #default="{ row }">
              <el-button
                v-if="row.fileId"
                type="primary"
                link
                size="small"
                @click="handleDownload(row.fileId, row.fileName)"
              >
                <el-icon><Download /></el-icon>{{ row.fileName || '下载' }}
              </el-button>
              <span v-else style="color:#999">无附件</span>
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
          @current-change="loadStudentDefense"
        />
      </el-card>
    </template>

    <!-- ===== 管理员/教师端视图 ===== -->
    <template v-else>
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>{{ isAdmin ? '答辩安排管理' : '我的答辩安排' }}</span>
            <div class="header-right">
              <span v-if="pagination.total > 0" class="record-count">共 {{ pagination.total }} 条</span>
              <el-button type="primary" @click="openPublishDialog">
                <el-icon><Plus /></el-icon>发布安排
              </el-button>
            </div>
          </div>
        </template>

        <!-- 筛选栏 -->
        <el-form :inline="true" class="filter-bar">
          <el-form-item v-if="isAdmin" label="学校">
            <el-input v-model="query.campusName" placeholder="学校名称" clearable style="width:160px" />
          </el-form-item>
          <el-form-item label="学年">
            <el-input v-model="query.academicYear" placeholder="如 2025" clearable style="width:120px" />
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="query.keyword" placeholder="场次名称/地点" clearable style="width:160px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadDefense"><el-icon><Search /></el-icon>查询</el-button>
            <el-button @click="resetQuery"><el-icon><Refresh /></el-icon>重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 答辩安排列表 -->
        <el-table :data="pagination.records" v-loading="loading" stripe>
          <el-table-column prop="sessionName" label="场次名称" min-width="140" />
          <el-table-column prop="defenseDate" label="答辩日期" width="120" />
          <el-table-column label="答辩时间" width="130">
            <template #default="{ row }">
              {{ row.startTime }} ~ {{ row.endTime }}
            </template>
          </el-table-column>
          <el-table-column prop="defenseForm" label="答辩形式" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="row.defenseForm === 'ONLINE' ? 'success' : 'primary'">
                {{ row.defenseForm === 'ONLINE' ? '线上' : '线下' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="答辩地点" min-width="140">
            <template #default="{ row }">
              <span>{{ row.location || '线上答辩' }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="isAdmin" prop="campusName" label="所属学校" min-width="140" />
          <el-table-column prop="academicYear" label="学年" width="100" />
          <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
          <el-table-column label="附件" width="100" align="center">
            <template #default="{ row }">
              <el-button
                v-if="row.fileId"
                type="primary"
                link
                size="small"
                @click="handleDownload(row.fileId, row.fileName)"
              >
                <el-icon><Download /></el-icon>{{ row.fileName || '下载' }}
              </el-button>
              <span v-else style="color:#999">无附件</span>
            </template>
          </el-table-column>
          <el-table-column v-if="isAdmin" label="操作" width="180" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openArrangeDialog(row)">
                <el-icon><List /></el-icon>查看明细
              </el-button>
              <el-button type="info" link size="small" @click="openDetailDialog(row)">
                <el-icon><InfoFilled /></el-icon>详情
              </el-button>
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
          @current-change="loadDefense"
        />
      </el-card>
    </template>

    <!-- ===== 发布答辩安排对话框 ===== -->
    <el-dialog v-model="publishDialogVisible" title="发布答辩安排" width="560px" destroy-on-close>
      <el-form :model="publishForm" :rules="publishRules" ref="publishFormRef" label-width="100px">
        <el-form-item label="场次名称" prop="sessionName">
          <el-input v-model="publishForm.sessionName" placeholder="如：第一场、第二场" />
        </el-form-item>
        <el-form-item label="答辩日期" prop="defenseDate">
          <el-date-picker
            v-model="publishForm.defenseDate"
            type="date"
            placeholder="选择答辩日期"
            value-format="YYYY-MM-DD"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-select
            v-model="publishForm.startTime"
            start="08:00"
            step="00:30"
            end="22:00"
            placeholder="选择开始时间"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-select
            v-model="publishForm.endTime"
            start="08:00"
            step="00:30"
            end="22:00"
            placeholder="选择结束时间"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="答辩形式" prop="defenseForm">
          <el-radio-group v-model="publishForm.defenseForm">
            <el-radio value="OFFLINE">线下答辩</el-radio>
            <el-radio value="ONLINE">线上答辩</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="答辩地点" prop="location">
          <el-input v-model="publishForm.location" placeholder="线下填写教室/会议室，线上可不填" />
        </el-form-item>
        <el-form-item label="学年" prop="academicYear">
          <el-input v-model="publishForm.academicYear" placeholder="如：2025" />
        </el-form-item>
        <el-form-item label="上传附件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".doc,.docx,.xls,.xlsx,.pdf,.zip,.rar,.7z,.tar.gz"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
          >
            <template #trigger>
              <el-button><el-icon><Upload /></el-icon>选择文件</el-button>
            </template>
            <template #tip>
              <div class="upload-tip">支持 Word、Excel、PDF、压缩包等格式</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input v-model="publishForm.remark" type="textarea" :rows="3" placeholder="可选填备注说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handlePublish">确认发布</el-button>
      </template>
    </el-dialog>

    <!-- ===== 答辩安排明细对话框 ===== -->
    <el-dialog v-model="arrangeDialogVisible" title="答辩安排明细" width="800px" destroy-on-close>
      <div v-if="currentSession" class="session-info-bar">
        <el-descriptions :column="4" border size="small">
          <el-descriptions-item label="场次名称">{{ currentSession.sessionName }}</el-descriptions-item>
          <el-descriptions-item label="答辩日期">{{ currentSession.defenseDate }}</el-descriptions-item>
          <el-descriptions-item label="答辩时间">{{ currentSession.startTime }} ~ {{ currentSession.endTime }}</el-descriptions-item>
          <el-descriptions-item label="答辩形式">
            <el-tag size="small" :type="currentSession.defenseForm === 'ONLINE' ? 'success' : 'primary'">
              {{ currentSession.defenseForm === 'ONLINE' ? '线上' : '线下' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="答辩地点">{{ currentSession.location || '线上答辩' }}</el-descriptions-item>
          <el-descriptions-item label="附件">
            <el-button v-if="currentSession.fileId" type="primary" link size="small" @click="handleDownload(currentSession.fileId, currentSession.fileName)">
              <el-icon><Download /></el-icon>{{ currentSession.fileName || '下载' }}
            </el-button>
            <span v-else>无</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <el-table :data="arrangeTable.records" v-loading="arrangeLoading" stripe style="margin-top:16px">
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column label="课题名称" min-width="160">
          <template #default="{ row }">
            {{ row.isCustomTopic ? row.customTopicName : row.topicName }}
          </template>
        </el-table-column>
        <el-table-column prop="teacherName" label="指导教师" width="100" />
        <el-table-column prop="sortOrder" label="答辩顺序" width="80" align="center" />
      </el-table>
      <el-pagination
        class="pagination-bar"
        background
        small
        layout="total, prev, pager, next"
        v-model:current-page="arrangeTable.current"
        v-model:page-size="arrangeTable.size"
        :total="arrangeTable.total"
        @current-change="loadArrangeDetail"
      />
    </el-dialog>

    <!-- ===== 详情对话框 ===== -->
    <el-dialog v-model="detailDialogVisible" title="答辩安排详情" width="560px" destroy-on-close>
      <el-descriptions v-if="currentSession" :column="1" border size="default">
        <el-descriptions-item label="场次名称">{{ currentSession.sessionName }}</el-descriptions-item>
        <el-descriptions-item label="答辩日期">{{ currentSession.defenseDate }}</el-descriptions-item>
        <el-descriptions-item label="答辩时间">{{ currentSession.startTime }} ~ {{ currentSession.endTime }}</el-descriptions-item>
        <el-descriptions-item label="答辩形式">
          <el-tag size="small" :type="currentSession.defenseForm === 'ONLINE' ? 'success' : 'primary'">
            {{ currentSession.defenseForm === 'ONLINE' ? '线上' : '线下' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="答辩地点">{{ currentSession.location || '线上答辩' }}</el-descriptions-item>
        <el-descriptions-item label="所属学校">{{ currentSession.campusName }}</el-descriptions-item>
        <el-descriptions-item label="学年">{{ currentSession.academicYear }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentSession.remark || '无' }}</el-descriptions-item>
        <el-descriptions-item label="附件">
          <el-button v-if="currentSession.fileId" type="primary" link size="small" @click="handleDownload(currentSession.fileId, currentSession.fileName)">
            <el-icon><Download /></el-icon>{{ currentSession.fileName || '下载附件' }}
          </el-button>
          <span v-else>无</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  fetchStudentDefensePage,
  fetchDefensePage,
  fetchTeacherDefensePage,
  fetchDefenseArrangePage,
  fetchDefenseDetail,
  publishDefense,
  downloadDefenseFile,
} from '@/api/defense'

const userStore = useUserStore()

const isAdmin = computed(() => userStore.user?.userType === 3)
const isStudent = computed(() => userStore.user?.userType === 1)
const isTeacher = computed(() => userStore.user?.userType === 2)

const loading = ref(false)
const submitting = ref(false)
const arrangeLoading = ref(false)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
  records: [],
})

const query = reactive({
  campusName: '',
  academicYear: '',
  keyword: '',
})

const studentQuery = reactive({
  academicYear: '',
})

const publishDialogVisible = ref(false)
const publishFormRef = ref()
const uploadRef = ref()
const publishFile = ref(null)

const publishForm = reactive({
  sessionName: '',
  defenseDate: '',
  startTime: '',
  endTime: '',
  defenseForm: 'OFFLINE',
  location: '',
  academicYear: '',
  remark: '',
})

const publishRules = {
  sessionName: [{ required: true, message: '请输入场次名称', trigger: 'blur' }],
  defenseDate: [{ required: true, message: '请选择答辩日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  defenseForm: [{ required: true, message: '请选择答辩形式', trigger: 'change' }],
  academicYear: [{ required: true, message: '请输入学年', trigger: 'blur' }],
}

const arrangeDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentSession = ref(null)

const arrangeTable = reactive({
  current: 1,
  size: 10,
  total: 0,
  records: [],
})

async function loadStudentDefense() {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      academicYear: studentQuery.academicYear || undefined,
    }
    const res = await fetchStudentDefensePage(params)
    pagination.records = res?.records || []
    pagination.total = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadDefense() {
  loading.value = true
  try {
    let res
    if (isAdmin.value) {
      res = await fetchDefensePage({
        current: pagination.current,
        size: pagination.size,
        campusName: query.campusName || undefined,
        academicYear: query.academicYear || undefined,
        keyword: query.keyword || undefined,
      })
    } else {
      res = await fetchTeacherDefensePage({
        current: pagination.current,
        size: pagination.size,
        academicYear: query.academicYear || undefined,
        keyword: query.keyword || undefined,
      })
    }
    pagination.records = res?.records || []
    pagination.total = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.campusName = ''
  query.academicYear = ''
  query.keyword = ''
  pagination.current = 1
  loadDefense()
}

async function loadArrangeDetail() {
  if (!currentSession.value) return
  arrangeLoading.value = true
  try {
    const res = await fetchDefenseArrangePage({
      current: arrangeTable.current,
      size: arrangeTable.size,
      sessionId: currentSession.value.sessionId,
    })
    arrangeTable.records = res?.records || []
    arrangeTable.total = res?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    arrangeLoading.value = false
  }
}

function openPublishDialog() {
  publishForm.sessionName = ''
  publishForm.defenseDate = ''
  publishForm.startTime = ''
  publishForm.endTime = ''
  publishForm.defenseForm = 'OFFLINE'
  publishForm.location = ''
  publishForm.academicYear = ''
  publishForm.remark = ''
  publishFile.value = null
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
  publishDialogVisible.value = true
}

function handleFileChange(file) {
  publishFile.value = file.raw
}

function handleFileRemove() {
  publishFile.value = null
}

async function handlePublish() {
  const valid = await publishFormRef.value.validate().catch(() => false)
  if (!valid) return

  if (publishForm.startTime && publishForm.endTime && publishForm.startTime >= publishForm.endTime) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }

  submitting.value = true
  try {
    const formData = new FormData()
    formData.append('sessionName', publishForm.sessionName)
    formData.append('defenseDate', publishForm.defenseDate)
    formData.append('startTime', publishForm.startTime)
    formData.append('endTime', publishForm.endTime)
    formData.append('defenseForm', publishForm.defenseForm)
    formData.append('location', publishForm.location || '')
    formData.append('academicYear', publishForm.academicYear)
    formData.append('remark', publishForm.remark || '')
    if (publishFile.value) {
      formData.append('file', publishFile.value)
    }
    await publishDefense(formData)
    ElMessage.success('发布成功，系统已通知本校全体学生')
    publishDialogVisible.value = false
    loadDefense()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

async function openArrangeDialog(row) {
  currentSession.value = row
  arrangeTable.current = 1
  arrangeTable.records = []
  arrangeTable.total = 0
  arrangeDialogVisible.value = true
  await loadArrangeDetail()
}

async function openDetailDialog(row) {
  try {
    const res = await fetchDefenseDetail(row.sessionId)
    currentSession.value = res
    detailDialogVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

async function handleDownload(fileId, fileName) {
  try {
    const res = await downloadDefenseFile(fileId)
    const blob = new Blob([res])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName || '附件'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error('下载失败')
    console.error(e)
  }
}

onMounted(() => {
  if (isStudent.value) {
    loadStudentDefense()
  } else {
    loadDefense()
  }
})
</script>

<style scoped>
.defense-page {
  padding: 20px;
}

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
  margin-bottom: 16px;
}

.pagination-bar {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.session-info-bar {
  margin-bottom: 0;
}
</style>
