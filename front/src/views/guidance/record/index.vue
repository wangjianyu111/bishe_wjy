<template>
  <div class="guidance-record-page">

    <!-- ====== 教师/管理员视图 ====== -->
    <template v-if="!isStudent">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>指导记录管理</span>
            <div class="header-right">
              <span v-if="pagination.total > 0" class="record-count">共 {{ pagination.total }} 条</span>
              <el-button v-if="hasPermission('guidance:record:add')" type="primary" @click="openAddDialog">
                <el-icon><Plus /></el-icon>添加记录
              </el-button>
            </div>
          </div>
        </template>

        <!-- 搜索栏 -->
        <el-form :inline="true" class="filter-bar">
          <el-form-item v-if="isAdmin" label="学校">
            <el-input v-model="query.campusName" placeholder="学校名称" clearable style="width:150px" />
          </el-form-item>
          <el-form-item label="学年">
            <el-input v-model="query.academicYear" placeholder="如 2024" clearable style="width:120px" />
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="query.keyword" placeholder="学生姓名/学号/教师" clearable style="width:160px" />
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
        >
          <el-table-column prop="teacherName" label="指导教师" width="100" show-overflow-tooltip />
          <el-table-column prop="studentName" label="学生" width="100" />
          <el-table-column prop="studentNo" label="学号" width="110" />
          <el-table-column v-if="isAdmin" label="学校" width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ row.campusName || '—' }}</template>
          </el-table-column>
          <el-table-column label="课题" min-width="180">
            <template #default="{ row }">
              <span v-if="row.topicName">{{ row.topicName }}</span>
              <span v-else style="color:#909399">—</span>
            </template>
          </el-table-column>
          <el-table-column label="指导方式" width="90">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ row.guidanceTypeLabel || row.guidanceType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="guideTime" label="指导时间" width="170" />
          <el-table-column prop="place" label="地点" width="120" show-overflow-tooltip />
          <el-table-column prop="durationMinutes" label="时长(分)" width="80" align="center">
            <template #default="{ row }">{{ row.durationMinutes ? row.durationMinutes + '分钟' : '—' }}</template>
          </el-table-column>
          <el-table-column prop="content" label="指导内容" min-width="200" show-overflow-tooltip />
          <el-table-column label="学生反馈" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.studentFeedback" style="color:#67c23a">{{ row.studentFeedback }}</span>
              <span v-else style="color:#c0c4cc">暂无反馈</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
              <el-button v-if="hasPermission('guidance:record:edit')" type="primary" link size="small" @click="openEditDialog(row)">编辑</el-button>
              <el-button v-if="hasPermission('guidance:record:del')" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- ====== 学生视图 ====== -->
    <template v-else>
      <el-row :gutter="16" class="stat-row">
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">累计指导次数</div>
              <div class="stat-value">{{ stats.totalCount || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">本学年指导</div>
              <div class="stat-value">{{ stats.yearlyCount || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">最近指导</div>
              <div class="stat-value" style="font-size:16px">{{ stats.lastGuideTime || '—' }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>我的指导记录</span>
            <div class="header-right">
              <span v-if="pagination.total > 0" class="record-count">共 {{ pagination.total }} 条</span>
            </div>
          </div>
        </template>

        <!-- 搜索栏 -->
        <el-form :inline="true" class="filter-bar">
          <el-form-item label="学年">
            <el-input v-model="query.academicYear" placeholder="如 2024" clearable style="width:120px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table
          :data="tableData"
          border
          stripe
          v-loading="loading"
          size="small"
        >
          <el-table-column prop="teacherName" label="指导教师" width="110" />
          <el-table-column label="课题" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.topicName">{{ row.topicName }}</span>
              <span v-else style="color:#909399">—</span>
            </template>
          </el-table-column>
          <el-table-column label="指导方式" width="90">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ row.guidanceTypeLabel || row.guidanceType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="guideTime" label="指导时间" width="170" />
          <el-table-column prop="place" label="地点" width="120" show-overflow-tooltip />
          <el-table-column prop="durationMinutes" label="时长" width="80" align="center">
            <template #default="{ row }">{{ row.durationMinutes ? row.durationMinutes + '分钟' : '—' }}</template>
          </el-table-column>
          <el-table-column prop="content" label="指导内容" min-width="220" show-overflow-tooltip />
          <el-table-column label="我的反馈" min-width="160">
            <template #default="{ row }">
              <div v-if="row.studentFeedback">
                <div style="color:#67c23a">{{ row.studentFeedback }}</div>
                <div style="font-size:11px;color:#909399">{{ row.feedbackTime }}</div>
              </div>
              <span v-else style="color:#c0c4cc">待填写反馈</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
              <el-button v-if="!row.studentFeedback" type="success" link size="small" @click="openFeedbackDialog(row)">反馈</el-button>
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

    <!-- ====== 添加/编辑对话框 ====== -->
    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑指导记录' : '添加指导记录'"
      width="620px"
      destroy-on-close
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="选择选题" prop="selectionId">
          <el-select
            v-model="form.selectionId"
            placeholder="请选择已审批通过的选题"
            filterable
            style="width:100%"
            :disabled="isEdit"
            @change="onSelectionChange"
          >
            <el-option
              v-for="s in selectionList"
              :key="s.selectionId"
              :label="(s.isCustomTopic ? s.customTopicName : s.topicName) + ' — ' + s.studentName"
              :value="s.selectionId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="学生">
          <el-input :value="selectedSelection?.studentName || '—'" disabled />
        </el-form-item>

        <el-form-item label="指导时间" prop="guideTime">
          <el-date-picker
            v-model="form.guideTime"
            type="datetime"
            placeholder="请选择指导时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width:100%"
          />
        </el-form-item>

        <el-form-item label="指导方式" prop="guidanceType">
          <el-select v-model="form.guidanceType" placeholder="请选择指导方式" style="width:100%">
            <el-option label="面授指导" value="GUIDANCE" />
            <el-option label="走访指导" value="VISIT" />
            <el-option label="集体座谈" value="GROUP" />
            <el-option label="线上指导" value="ONLINE" />
            <el-option label="其他方式" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="指导地点">
          <el-input v-model="form.place" placeholder="请输入指导地点（可选）" />
        </el-form-item>

        <el-form-item label="指导时长">
          <el-input-number v-model="form.durationMinutes" :min="5" :max="480" :step="5" placeholder="分钟" />
          <span style="margin-left:8px;color:#909399">分钟</span>
        </el-form-item>

        <el-form-item label="指导内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="5"
            placeholder="请详细记录本次指导的内容，包括学生完成情况、发现的问题、下一步计划等"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- ====== 详情对话框 ====== -->
    <el-dialog v-model="detailVisible" title="指导记录详情" width="640px" destroy-on-close>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="指导教师">{{ currentRow?.teacherName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ currentRow?.studentName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentRow?.studentNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="学校">{{ currentRow?.campusName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="课题" :span="2">
          {{ currentRow?.topicName || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="指导方式">{{ currentRow?.guidanceTypeLabel || currentRow?.guidanceType }}</el-descriptions-item>
        <el-descriptions-item label="指导时长">
          {{ currentRow?.durationMinutes ? currentRow.durationMinutes + '分钟' : '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="指导时间">{{ currentRow?.guideTime }}</el-descriptions-item>
        <el-descriptions-item label="指导地点">{{ currentRow?.place || '—' }}</el-descriptions-item>
        <el-descriptions-item label="累计指导" :span="2">
          该学生共 {{ currentRow?.totalGuidesForStudent || 0 }} 次指导
        </el-descriptions-item>
        <el-descriptions-item label="指导内容" :span="2">
          <div style="white-space:pre-wrap;line-height:1.6">{{ currentRow?.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="学生反馈" :span="2">
          <template v-if="currentRow?.studentFeedback">
            <div style="color:#67c23a">{{ currentRow.studentFeedback }}</div>
            <div style="font-size:11px;color:#909399;margin-top:4px">{{ currentRow.feedbackTime }}</div>
          </template>
          <span v-else style="color:#c0c4cc">暂无反馈</span>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <template v-if="isStudent && !currentRow?.studentFeedback && hasPermission('guidance:record:feedback')">
          <el-button type="success" @click="openFeedbackDialog(currentRow)">填写反馈</el-button>
        </template>
      </template>
    </el-dialog>

    <!-- ====== 反馈对话框 ====== -->
    <el-dialog v-model="feedbackDialogVisible" title="填写反馈" width="520px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="记录信息">
          <div style="color:#606266">{{ currentRow?.guideTime }} · {{ currentRow?.teacherName }} 老师的指导</div>
        </el-form-item>
        <el-form-item label="您的反馈">
          <el-input
            v-model="feedbackContent"
            type="textarea"
            :rows="4"
            placeholder="请写下对本次指导的反馈，如：是否解决了问题、还需哪些帮助等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="feedbackLoading" @click="handleFeedback">提交反馈</el-button>
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
  fetchAdminGuidancePage,
  fetchTeacherGuidancePage,
  fetchStudentGuidancePage,
  fetchSelectionsForGuidance,
  addGuidanceRecord,
  updateGuidanceRecord,
  deleteGuidanceRecord,
  addGuidanceFeedback,
  fetchGuidanceDetail,
  fetchGuidanceStats,
} from '@/api/guidance'

const store = useUserStore()

const isStudent = computed(() => store.user?.userType === 1)
const isAdmin = computed(() => store.user?.userType === 3)

function hasPermission(code) {
  return store.hasPerm(code)
}

// ====== 统计数据 ======
const stats = ref({})

async function loadStats() {
  if (!isStudent.value) return
  try {
    stats.value = await fetchGuidanceStats()
  } catch {
    stats.value = {}
  }
}

// ====== 列表数据 ======
const loading = ref(false)
const tableRef = ref(null)
const tableData = ref([])
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
    let res
    if (isAdmin.value) {
      res = await fetchAdminGuidancePage(params)
    } else {
      res = await fetchTeacherGuidancePage(params)
    }
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

async function loadStudentData(page) {
  loading.value = true
  try {
    const params = { current: page || pagination.current, size: pagination.size, ...query }
    const res = await fetchStudentGuidancePage(params)
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
  if (isStudent.value) {
    loadStudentData(1)
  } else {
    loadData(1)
  }
}

function handleReset() {
  query.campusName = ''
  query.academicYear = ''
  query.keyword = ''
  pagination.current = 1
  if (isStudent.value) {
    loadStudentData(1)
  } else {
    loadData(1)
  }
}

// ====== 表单 ======
const formDialogVisible = ref(false)
const formLoading = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const form = ref({
  guideId: null,
  selectionId: null,
  guideTime: '',
  place: '',
  durationMinutes: null,
  guidanceType: 'GUIDANCE',
  content: '',
})
const formRules = {
  selectionId: [{ required: true, message: '请选择选题', trigger: 'change' }],
  guideTime: [{ required: true, message: '请选择指导时间', trigger: 'change' }],
  guidanceType: [{ required: true, message: '请选择指导方式', trigger: 'change' }],
  content: [{ required: true, message: '请填写指导内容', trigger: 'blur' }],
}

const selectionList = ref([])
const selectedSelection = ref(null)

async function loadSelections() {
  try {
    const res = await fetchSelectionsForGuidance()
    selectionList.value = res || []
  } catch {
    selectionList.value = []
  }
}

function onSelectionChange(selectionId) {
  selectedSelection.value = selectionList.value.find(s => s.selectionId === selectionId)
}

function openAddDialog() {
  isEdit.value = false
  form.value = {
    guideId: null,
    selectionId: null,
    guideTime: '',
    place: '',
    durationMinutes: null,
    guidanceType: 'GUIDANCE',
    content: '',
  }
  selectedSelection.value = null
  loadSelections()
  formDialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  loadSelections().then(() => {
    selectedSelection.value = selectionList.value.find(s => s.selectionId === row.selectionId)
    form.value = {
      guideId: row.guideId,
      selectionId: row.selectionId,
      guideTime: row.guideTime,
      place: row.place || '',
      durationMinutes: row.durationMinutes,
      guidanceType: row.guidanceType,
      content: row.content,
    }
    formDialogVisible.value = true
  })
}

async function handleSubmit() {
  // 编辑模式跳过选题必填验证（选题不可改）
  if (isEdit.value) {
    formRef.value?.clearValidate('selectionId')
  }
  const valid = await formRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) return
  formLoading.value = true
  try {
    if (isEdit.value) {
      await updateGuidanceRecord(form.value)
      ElMessage.success('更新成功')
    } else {
      await addGuidanceRecord(form.value)
      ElMessage.success('添加成功')
    }
    formDialogVisible.value = false
    loadData(1)
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    formLoading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除该指导记录吗？`, '删除确认', { type: 'warning' })
  try {
    await deleteGuidanceRecord(row.guideId)
    ElMessage.success('删除成功')
    loadData(1)
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

// ====== 详情 ======
const detailVisible = ref(false)
const currentRow = ref(null)

async function openDetail(row) {
  try {
    const data = await fetchGuidanceDetail(row.guideId)
    currentRow.value = data
    detailVisible.value = true
  } catch (e) {
    ElMessage.error(e.message || '获取详情失败')
  }
}

// ====== 反馈 ======
const feedbackDialogVisible = ref(false)
const feedbackContent = ref('')
const feedbackLoading = ref(false)

function openFeedbackDialog(row) {
  currentRow.value = row
  feedbackContent.value = row.studentFeedback || ''
  feedbackDialogVisible.value = true
}

async function handleFeedback() {
  if (!feedbackContent.value.trim()) {
    ElMessage.warning('请填写反馈内容')
    return
  }
  feedbackLoading.value = true
  try {
    await addGuidanceFeedback(currentRow.value.guideId, { feedback: feedbackContent.value })
    ElMessage.success('反馈提交成功')
    feedbackDialogVisible.value = false
    loadStudentData(1)
    loadStats()
    // 更新当前行
    currentRow.value = { ...currentRow.value, studentFeedback: feedbackContent.value }
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    feedbackLoading.value = false
  }
}

// ====== 初始化 ======
onMounted(() => {
  if (isStudent.value) {
    loadStats()
    loadStudentData(1)
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
.stat-row {
  margin-bottom: 16px;
}
.stat-card {
  border-radius: 10px;
}
.stat-item {
  text-align: center;
  padding: 8px 0;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}
</style>
