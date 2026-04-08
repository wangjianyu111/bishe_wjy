<template>
  <div class="template-page">
    <el-card shadow="never">
      <!-- 头部：查询 + 上传 -->
      <div class="toolbar">
        <el-form :inline="true" :model="query">
          <el-form-item label="毕设阶段">
            <el-select
              v-model="query.phase"
              placeholder="全部阶段"
              clearable
              filterable
              style="width: 180px"
            >
              <el-option v-for="p in phaseOptions" :key="p" :label="p" :value="p" />
            </el-select>
          </el-form-item>
          <!-- 教师/管理员可见学校筛选，学生隐藏（后端已自动按本校过滤） -->
          <el-form-item v-if="!isStudent" label="学校名称">
            <el-input
              v-model="query.campusName"
              placeholder="搜索学校名称"
              clearable
              style="width: 180px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 上传按钮仅教师/管理员可见 -->
        <div v-if="!isStudent" class="toolbar-right">
          <el-button type="primary" @click="openUpload">
            <el-icon><Upload /></el-icon> 上传模板
          </el-button>
        </div>
      </div>

      <!-- 学生提示 -->
      <div v-if="isStudent && userCampusName" class="school-tip">
        <el-icon><School /></el-icon>
        您正在查看「{{ userCampusName }}」的模板，仅可下载，无法上传或删除
      </div>
      <div v-if="isStudent && !userCampusName" class="school-tip school-tip--warn">
        <el-icon><Warning /></el-icon>
        您当前未关联学校，无法查看模板，请联系管理员完善个人信息
      </div>

      <!-- 列表 -->
      <el-table :data="table.records" v-loading="loading" border stripe>
        <el-table-column prop="phase" label="毕设阶段" width="130">
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.phase }}</el-tag>
          </template>
        </el-table-column>
        <!-- 教师/管理员可见学校列，学生隐藏 -->
        <el-table-column v-if="!isStudent" prop="campusName" label="学校名称" min-width="150" />
        <el-table-column prop="originalName" label="文件名称" min-width="220">
          <template #default="{ row }">
            <div class="file-name-cell">
              <el-icon class="file-icon"><Document /></el-icon>
              <span class="file-name-text">{{ row.originalName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="fileType" label="格式" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ (row.fileType || '').toUpperCase() }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="大小" width="100" align="center">
          <template #default="{ row }">
            {{ formatSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploaderName" label="上传人" width="100" />
        <el-table-column prop="uploadTime" label="上传时间" width="170">
          <template #default="{ row }">
            {{ formatDate(row.uploadTime) }}
          </template>
        </el-table-column>
        <!-- 操作列：学生只显示下载，教师/管理员显示下载+删除 -->
        <el-table-column label="操作" :width="isStudent ? 100 : 160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleDownload(row)">
              <el-icon><Download /></el-icon> 下载
            </el-button>
            <el-button
              v-if="!isStudent"
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pager">
        <el-pagination
          :current-page="pagination.current"
          :page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="load"
          @current-change="load"
        />
      </div>
    </el-card>

    <!-- 上传弹窗（仅教师/管理员） -->
    <el-dialog
      v-model="uploadDialog.visible"
      title="上传模板文件"
      width="560px"
      destroy-on-close
    >
      <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="100px">
        <el-form-item label="毕设阶段" prop="phase">
          <el-select
            v-model="uploadForm.phase"
            placeholder="请选择毕设阶段"
            style="width: 100%"
          >
            <el-option v-for="p in phaseOptions" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="学校名称" prop="campusName">
          <el-input
            v-model="uploadForm.campusName"
            placeholder="请输入学校名称"
            clearable
          />
        </el-form-item>
        <el-form-item label="选择文件">
          <div class="upload-area">
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :limit="1"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              :file-list="fileList"
              accept=".txt,.doc,.docx,.pdf,.xls,.xlsx,.ppt,.pptx,.zip,.rar"
              action="#"
            >
              <el-button type="primary" plain>
                <el-icon><Upload /></el-icon> 选择文件
              </el-button>
            </el-upload>
            <div class="upload-tip">
              支持 txt/doc/docx/pdf/xls/xlsx/ppt/pptx/zip/rar，单个文件不超过 50MB
            </div>
            <div v-if="fileList.length > 0" class="file-preview">
              <el-icon><Document /></el-icon>
              <span>{{ fileList[0].name }}</span>
              <el-button type="danger" size="small" link @click="handleFileRemove">移除</el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="uploadLoading" @click="handleUpload">
          确定上传
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Document, Download, School, Warning } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  fetchTemplatePage,
  uploadTemplate,
  deleteTemplate,
  downloadTemplate,
} from '@/api/project'

const userStore = useUserStore()

const isStudent = computed(() => userStore.user?.userType === 1)
const userCampusName = computed(() => userStore.user?.campusName || '')

const loading = ref(false)
const uploadLoading = ref(false)
const uploadFormRef = ref(null)
const uploadRef = ref(null)
const fileList = ref([])
const uploadFile = ref(null)

const phaseOptions = [
  '选题阶段', '开题报告', '需求分析', '系统设计',
  '编码实现', '中期检查', '论文撰写', '答辩准备', '正式答辩',
]

const query = reactive({
  phase: '',
  campusName: '',
})
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
})
const table = reactive({ records: [] })

const uploadDialog = reactive({ visible: false })
const uploadForm = reactive({
  phase: '',
  campusName: '',
  file: null,
})
const uploadRules = {
  phase: [{ required: true, message: '请选择毕设阶段', trigger: 'change' }],
  campusName: [{ required: true, message: '请输入学校名称', trigger: 'blur' }],
}

function formatSize(bytes) {
  if (!bytes) return '—'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  const d = new Date(dateStr)
  const p = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

async function load() {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      phase: query.phase || undefined,
      // 学生用户不传 campusName，后端自动按本校过滤
      campusName: isStudent.value ? undefined : (query.campusName || undefined),
    }
    const data = await fetchTemplatePage(params)
    table.records = data.records || []
    pagination.total = data.total || 0
  } catch {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  load()
}

function handleReset() {
  query.phase = ''
  query.campusName = ''
  pagination.current = 1
  load()
}

function openUpload() {
  uploadForm.phase = ''
  uploadForm.campusName = ''
  uploadForm.file = null
  fileList.value = []
  uploadFile.value = null
  uploadDialog.visible = true
}

function handleFileChange(file, files) {
  uploadFile.value = file.raw
  fileList.value = files
}

function handleFileRemove() {
  uploadFile.value = null
  fileList.value = []
  uploadRef.value?.clearFiles()
}

async function handleUpload() {
  const valid = await uploadFormRef.value.validate().catch(() => false)
  if (!valid) return

  if (!uploadFile.value) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  const formData = new FormData()
  formData.append('phase', uploadForm.phase)
  formData.append('campusName', uploadForm.campusName)
  formData.append('file', uploadFile.value)

  uploadLoading.value = true
  try {
    await uploadTemplate(formData)
    ElMessage.success('上传成功')
    uploadDialog.visible = false
    load()
  } catch {
    ElMessage.error('上传失败')
  } finally {
    uploadLoading.value = false
  }
}

async function handleDownload(row) {
  try {
    const blob = await downloadTemplate(row.templateId)
    const url = URL.createObjectURL(new Blob([blob]))
    const a = document.createElement('a')
    a.href = url
    a.download = row.originalName
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    ElMessage.error('下载失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除模板「${row.originalName}」吗？`,
      '确认删除',
      { type: 'warning' }
    )
    await deleteTemplate(row.templateId)
    ElMessage.success('删除成功')
    load()
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.template-page {
  padding: 16px;
}

.toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-right {
  flex-shrink: 0;
}

.school-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  margin-bottom: 12px;
  background: #f0f9eb;
  border: 1px solid #e1f3d8;
  border-radius: 4px;
  color: #67c23a;
  font-size: 13px;
}

.school-tip--warn {
  background: #fdf6ec;
  border-color: #f5dab1;
  color: #e6a23c;
}

.file-name-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.file-icon {
  color: #409eff;
  flex-shrink: 0;
}

.file-name-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.upload-area {
  width: 100%;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
  line-height: 1.4;
}

.file-preview {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  padding: 8px 12px;
  background: #f0f9eb;
  border: 1px solid #e1f3d8;
  border-radius: 4px;
  color: #67c23a;
  font-size: 13px;
}
</style>
