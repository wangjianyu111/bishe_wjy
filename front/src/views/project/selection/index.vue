<template>
  <div class="selection-page">
    <!-- 当前申请状态展示 -->
    <el-alert
      v-if="mySelection"
      :title="`您已提交过 ${mySelection.academicYear} 学年的选题申请，当前状态：${mySelection.statusLabel}`"
      :type="statusType"
      :description="statusDesc"
      show-icon
      :closable="false"
      style="margin-bottom: 16px"
    >
      <template #default>
        <div class="my-selection-info">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="学年">{{ mySelection.academicYear }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusTagType(mySelection.status)" size="small">{{ mySelection.statusLabel }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="申请学校">{{ mySelection.campusName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="指导教师">{{ mySelection.teacherName || '—' }} {{ mySelection.teacherNo ? `(${mySelection.teacherNo})` : '' }}</el-descriptions-item>
            <el-descriptions-item label="课题名称">
              {{ mySelection.isCustomTopic ? mySelection.customTopicName : (mySelection.topicName || '—') }}
            </el-descriptions-item>
            <el-descriptions-item label="申请理由">{{ mySelection.applyReason || '—' }}</el-descriptions-item>
            <el-descriptions-item v-if="mySelection.rejectReason" label="驳回原因" :span="2">
              {{ mySelection.rejectReason }}
            </el-descriptions-item>
          </el-descriptions>
          <div class="action-bar" v-if="mySelection.status === 'PENDING'">
            <el-button type="danger" plain size="small" :loading="withdrawLoading" @click="handleWithdraw">
              撤回申请
            </el-button>
          </div>
        </div>
      </template>
    </el-alert>

    <!-- 多步骤选题向导 -->
    <el-card v-if="!mySelection || mySelection.status === 'REJECTED' || mySelection.status === 'WITHDRAWN'" shadow="never">
      <template #header>
        <span>新建选题申请</span>
      </template>

      <!-- 步骤条 -->
      <el-steps :active="step" finish-status="success" align-center style="margin-bottom: 32px">
        <el-step title="选择学校" />
        <el-step title="选择指导教师" />
        <el-step title="选择/填写课题" />
        <el-step title="提交申请" />
      </el-steps>

      <!-- ===== 步骤1：填写学校 ===== -->
      <div v-show="step === 0" class="step-content">
        <div class="step-tip">
          <el-icon><InfoFilled /></el-icon>
          请输入学校名称和学年
        </div>
        <el-form :model="form" label-width="90px" style="max-width: 480px">
          <el-form-item label="学校名称" required>
            <el-input v-model="form.campusName" placeholder="请输入学校名称" clearable />
          </el-form-item>
          <el-form-item label="学年" required>
            <el-input v-model="form.academicYear" placeholder="如：2024-2025" clearable style="width: 200px" />
          </el-form-item>
        </el-form>
        <div class="step-footer">
          <el-button type="primary" :disabled="!form.campusName?.trim()" @click="nextStep">下一步</el-button>
        </div>
      </div>

      <!-- ===== 步骤2：选择指导教师 ===== -->
      <div v-show="step === 1" class="step-content">
        <div class="step-tip">
          <el-icon><InfoFilled /></el-icon>
          已选学校：<strong>{{ form.campusName }}</strong>，学年：<strong>{{ form.academicYear }}</strong>，请选择指导教师
        </div>
        <div v-loading="teacherLoading" class="teacher-list">
          <el-empty v-if="!teacherLoading && teacherList.length === 0" description="该学校暂无指导教师" />
          <el-row :gutter="12">
            <el-col :xs="24" :sm="12" :md="8" v-for="teacher in teacherList" :key="teacher.userId">
              <div
                class="teacher-card"
                :class="{ selected: form.teacherId === teacher.userId }"
                @click="selectTeacher(teacher)"
              >
                <div class="teacher-avatar">
                  <el-avatar :size="40">{{ teacher.realName?.charAt(0) }}</el-avatar>
                </div>
                <div class="teacher-info">
                  <div class="teacher-name">{{ teacher.realName }}</div>
                  <div class="teacher-no">{{ teacher.teacherNo || '无工号' }}</div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
        <div class="step-footer">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" :disabled="!form.teacherId" @click="nextStep">下一步</el-button>
        </div>
      </div>

      <!-- ===== 步骤3：选择/填写课题 ===== -->
      <div v-show="step === 2" class="step-content">
        <div class="step-tip">
          <el-icon><InfoFilled /></el-icon>
          学年：<strong>{{ form.academicYear }}</strong>，已选教师：<strong>{{ selectedTeacherName }}</strong>，请选择课题来源
        </div>

        <!-- 题目库 / 自填切换 -->
        <el-radio-group v-model="topicSource" style="margin-bottom: 20px">
          <el-radio-button value="bank">从题目库选择</el-radio-button>
          <el-radio-button value="custom">自拟课题</el-radio-button>
        </el-radio-group>

        <!-- 题目库 -->
        <div v-if="topicSource === 'bank'">
          <div class="topic-filter">
            <el-input
              v-model="topicKeyword"
              placeholder="搜索课题名称"
              clearable
              style="width: 260px"
              @input="filterTopics"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
          <el-table
            :data="filteredTopicList"
            v-loading="topicLoading"
            border
            stripe
            size="small"
            style="width: 100%; border-radius: 8px; overflow: hidden"
            @row-click="selectTopic"
            :row-class-name="topicRowClass"
          >
            <el-table-column label="课题名称" min-width="200" align="center" header-align="center">
              <template #default="{ row }">
                <el-tooltip :content="row.topicName" placement="top" :show-after="300" :enterable="false">
                  <div class="topic-name-cell">
                    <span class="topic-name">{{ row.topicName }}</span>
                  </div>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column label="名额" width="100" align="center" header-align="center">
              <template #default="{ row }">
                <span :class="{ full: row.currentCount >= row.maxStudents }">
                  {{ row.currentCount }} / {{ row.maxStudents }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center" header-align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === 'OPEN' ? 'success' : 'danger'" effect="light">
                  {{ row.status === 'OPEN' ? '开放' : '关闭' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!topicLoading && filteredTopicList.length === 0 && topicSource === 'bank'" class="empty-topics">
            <el-empty description="该教师暂无题目库课题，或所有课题名额已满" />
          </div>
        </div>

        <!-- 自填课题 -->
        <div v-else class="custom-topic-form">
          <el-form :model="form" label-width="100px" style="max-width: 600px">
            <el-form-item label="课题名称" required>
              <el-input v-model="form.customTopicName" placeholder="请输入课题名称" />
            </el-form-item>
            <el-form-item label="课题简介">
              <el-input v-model="form.customTopicDescription" type="textarea" :rows="4" placeholder="请简述课题内容、研究方向等" />
            </el-form-item>
          </el-form>
        </div>

        <div class="step-footer">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" :disabled="!canNextStep3" @click="nextStep">下一步</el-button>
        </div>
      </div>

      <!-- ===== 步骤4：确认并提交 ===== -->
      <div v-show="step === 3" class="step-content">
        <div class="step-tip">
          <el-icon><InfoFilled /></el-icon>
          请确认以下信息，提交后将进入审批流程
        </div>
        <el-descriptions title="选题申请信息" :column="2" border style="max-width: 700px">
          <el-descriptions-item label="申请学校">{{ form.campusName }}</el-descriptions-item>
          <el-descriptions-item label="学年">{{ form.academicYear }}</el-descriptions-item>
          <el-descriptions-item label="指导教师">
            {{ selectedTeacherName }} {{ selectedTeacherNo ? `(${selectedTeacherNo})` : '' }}
          </el-descriptions-item>
          <el-descriptions-item label="课题来源">
            {{ topicSource === 'bank' ? '题目库' : '自拟课题' }}
          </el-descriptions-item>
          <el-descriptions-item label="课题名称" :span="2">
            {{ topicSource === 'bank' ? selectedTopicName : form.customTopicName }}
          </el-descriptions-item>
          <el-descriptions-item label="课题简介" :span="2">
            {{ topicSource === 'bank' ? (selectedTopicDesc || '—') : (form.customTopicDescription || '—') }}
          </el-descriptions-item>
          <el-descriptions-item label="申请理由" :span="2">
            <el-input v-model="form.applyReason" type="textarea" :rows="3" placeholder="简要说明选择该课题的原因（选填）" />
          </el-descriptions-item>
        </el-descriptions>
        <div class="step-footer">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="success" :loading="submitLoading" @click="submitApplication">确认提交</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled, Search } from '@element-plus/icons-vue'
import {
  fetchTeachers,
  fetchTopicBank,
  fetchMySelection,
  applySelection,
  withdrawSelection,
} from '@/api/project'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// ===== 状态 =====
const step = ref(0)
const teacherList = ref([])
const topicList = ref([])
const mySelection = ref(null)
const topicSource = ref('bank')
const topicKeyword = ref('')
const topicLoading = ref(false)
const teacherLoading = ref(false)
const submitLoading = ref(false)
const withdrawLoading = ref(false)

// ===== 表单 =====
const form = reactive({
  campusName: '',
  teacherId: null,
  topicId: null,
  academicYear: '',
  applyReason: '',
  customTopicName: '',
  customTopicDescription: '',
})

const selectedTeacherName = computed(() => teacherList.value.find(t => t.userId === form.teacherId)?.realName || '')
const selectedTeacherNo = computed(() => teacherList.value.find(t => t.userId === form.teacherId)?.teacherNo || '')
const selectedTopicName = computed(() => topicList.value.find(t => t.topicId === form.topicId)?.topicName || '')
const selectedTopicDesc = computed(() => topicList.value.find(t => t.topicId === form.topicId)?.description || '')

const filteredTopicList = computed(() => {
  if (!topicKeyword.value) return topicList.value
  const kw = topicKeyword.value.toLowerCase()
  return topicList.value.filter(t =>
    t.topicName?.toLowerCase().includes(kw)
  )
})

const canNextStep3 = computed(() => {
  if (topicSource.value === 'bank') {
    return form.topicId !== null
  }
  return form.customTopicName && form.customTopicName.trim().length > 0
})

const statusType = computed(() => {
  const s = mySelection.value?.status
  if (s === 'APPROVED') return 'success'
  if (s === 'REJECTED') return 'error'
  if (s === 'WITHDRAWN') return 'warning'
  return 'info'
})

const statusDesc = computed(() => {
  const s = mySelection.value
  if (!s) return ''
  if (s.status === 'PENDING') return '请等待指导教师或管理员审批'
  if (s.status === 'APPROVED') return '恭喜！您的选题申请已通过审批'
  if (s.status === 'REJECTED') return `驳回原因：${s.rejectReason || '无'}`
  if (s.status === 'WITHDRAWN') return '您已撤回本次申请'
  return ''
})

function statusTagType(status) {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'WITHDRAWN') return 'warning'
  return 'info'
}

// ===== 学年选项 =====
const academicYearOptions = computed(() => {
  const y = new Date().getFullYear()
  return [`${y - 1}-${y}`, `${y}-${y + 1}`, `${y + 1}-${y + 2}`]
})

// ===== 方法 =====

async function loadTeachers(campusId) {
  teacherLoading.value = true
  try {
    // 优先按学校名称模糊查询，fallback 到 campusId
    if (form.campusName?.trim()) {
      teacherList.value = await fetchTeachers({ campusName: form.campusName.trim() })
    } else {
      teacherList.value = await fetchTeachers({ campusId })
    }
  } finally {
    teacherLoading.value = false
  }
}

async function loadTopicBank() {
  topicLoading.value = true
  try {
    topicList.value = await fetchTopicBank({
      teacherId: form.teacherId,
      academicYear: form.academicYear || undefined,
    })
  } finally {
    topicLoading.value = false
  }
}

async function loadMySelection() {
  try {
    mySelection.value = await fetchMySelection()
  } catch {
    mySelection.value = null
  }
}

function selectCampus() {
  form.teacherId = null
  form.topicId = null
  teacherList.value = []
  topicList.value = []
  if (form.campusName?.trim()) {
    loadTeachers(null)
  }
}

function selectTeacher(teacher) {
  form.teacherId = teacher.userId
  form.topicId = null
  topicList.value = []
  loadTopicBank()
}

function selectTopic(row) {
  if (row.currentCount >= row.maxStudents) {
    ElMessage.warning('该课题名额已满，请选择其他课题')
    return
  }
  form.topicId = row.topicId
}

function topicRowClass({ row }) {
  if (row.topicId === form.topicId) return 'selected-row'
  if (row.currentCount >= row.maxStudents) return 'full-row'
  return ''
}

function nextStep() {
  if (step.value === 0 && !form.campusName?.trim()) {
    ElMessage.warning('请先输入学校名称')
    return
  }
  if (step.value === 0 && !form.academicYear?.trim()) {
    ElMessage.warning('请先输入学年')
    return
  }
  if (step.value === 1 && !form.teacherId) {
    ElMessage.warning('请先选择指导教师')
    return
  }
  step.value++
}

function prevStep() {
  step.value--
}

function filterTopics() {
  // 题目筛选由 computed 处理
}

async function submitApplication() {
  if (!form.academicYear) {
    ElMessage.warning('请选择学年')
    return
  }
  if (!canNextStep3.value) {
    ElMessage.warning(topicSource.value === 'bank' ? '请选择课题' : '请填写课题名称')
    return
  }
  submitLoading.value = true
  try {
    await applySelection({
      campusName: form.campusName,
      teacherId: form.teacherId,
      topicId: topicSource.value === 'bank' ? form.topicId : null,
      customTopicName: topicSource.value === 'custom' ? form.customTopicName : null,
      customTopicDescription: topicSource.value === 'custom' ? form.customTopicDescription : null,
      academicYear: form.academicYear,
      applyReason: form.applyReason || null,
    })
    ElMessage.success('选题申请已提交，请等待审批')
    step.value = 0
    resetForm()
    await loadMySelection()
  } finally {
    submitLoading.value = false
  }
}

async function handleWithdraw() {
  try {
    await ElMessageBox.confirm('确定要撤回本次选题申请吗？撤回后可重新申请。', '确认撤回', {
      confirmButtonText: '确定撤回',
      cancelButtonText: '取消',
      type: 'warning',
    })
    withdrawLoading.value = true
    await withdrawSelection(mySelection.value.selectionId)
    ElMessage.success('已撤回申请')
    await loadMySelection()
  } catch {
    // 用户取消
  } finally {
    withdrawLoading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    campusName: '',
    teacherId: null,
    topicId: null,
    academicYear: '',
    applyReason: '',
    customTopicName: '',
    customTopicDescription: '',
  })
  topicSource.value = 'bank'
  topicList.value = []
  teacherList.value = []
}

onMounted(async () => {
  await loadMySelection()
})

watch(() => form.campusName, (val) => {
  if (val?.trim()) {
    teacherList.value = []
    form.teacherId = null
    loadTeachers(null) // loadTeachers 内部会根据 form.campusName 过滤
  }
})

watch(() => form.academicYear, (val) => {
  if (val?.trim() && form.teacherId) {
    loadTopicBank()
  }
})
</script>

<style scoped>
.selection-page {
  padding: 16px;
}

.my-selection-info {
  margin-top: 12px;
}

.action-bar {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.step-content {
  min-height: 300px;
}

.step-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 14px;
  margin-bottom: 20px;
  padding: 10px 14px;
  background: #f5f7fa;
  border-radius: 4px;
}

.campus-card,
.teacher-card {
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 12px;
  background: #fff;
}

.campus-card:hover,
.teacher-card:hover {
  border-color: #409eff;
  background: #f0f7ff;
}

.campus-card.selected,
.teacher-card.selected {
  border-color: #409eff;
  background: #ecf5ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.campus-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.campus-code {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.teacher-card {
  display: flex;
  align-items: center;
  gap: 12px;
}

.teacher-info {
  flex: 1;
}

.teacher-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.teacher-no {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.topic-filter {
  margin-bottom: 12px;
}

.topic-filter :deep(.el-input__wrapper) {
  box-shadow: none;
  border: 1px solid #dcdfe6;
}

.full {
  color: #f56c6c;
  font-weight: 600;
}

.topic-name-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  width: 100%;
}

.topic-name {
  font-weight: 600;
  color: #303133;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
}

.custom-topic-form {
  padding: 8px 0;
}

.step-footer {
  margin-top: 28px;
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

:deep(.selected-row) {
  background-color: #ecf5ff !important;
}

:deep(.full-row) {
  opacity: 0.5;
}
</style>
