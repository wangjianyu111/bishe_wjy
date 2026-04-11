<template>
  <div class="progress-page">
    <!-- ===== 学生端视图 ===== -->
    <template v-if="isStudent">
      <el-card shadow="never" class="intro-card">
        <div class="intro-title">
          <el-icon color="#409eff" size="18"><TrendCharts /></el-icon>
          <span>我的毕设进度</span>
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
            <el-descriptions-item label="整体进度">
              <el-progress :percentage="totalProgress" :color="progressColor(totalProgress)" style="width:200px"/>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <el-empty v-else description="您还没有审批通过的选题" />
      </el-card>

      <el-card v-if="mySelection" shadow="never" class="timeline-card">
        <template #header>
          <div class="card-header">
            <span>进度时间线</span>
            <el-button v-if="mySelection.status === 'APPROVED'" type="primary" size="small" @click="handleAdd">
              <el-icon><Plus /></el-icon> 填写进度
            </el-button>
          </div>
        </template>

        <el-timeline v-loading="loading" v-if="studentProgressList.length > 0">
          <el-timeline-item
            v-for="item in studentProgressList"
            :key="item.progressId"
            :timestamp="formatDate(item.submitTime)"
            :color="item.percentDone >= 100 ? '#67c23a' : '#409eff'"
            placement="top"
          >
            <el-card shadow="never" class="timeline-card-item">
              <div class="progress-header">
                <span class="phase-tag">{{ item.phase }}</span>
                <el-progress :percentage="item.percentDone || 0" :color="progressColor(item.percentDone)" style="width:200px"/>
              </div>
              <div class="progress-content" v-if="item.content">{{ item.content }}</div>
              <div class="progress-dates">
                <span>计划完成：{{ formatDate(item.planDate) }}</span>
                <span v-if="item.actualDate">实际完成：{{ formatDate(item.actualDate) }}</span>
              </div>
              <div class="progress-actions" v-if="mySelection.status === 'APPROVED'">
                <el-button type="primary" size="small" plain @click="handleEdit(item)">编辑</el-button>
                <el-button type="danger" size="small" plain @click="handleDelete(item)">删除</el-button>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无进度记录，请点击右上角按钮添加" />
      </el-card>
    </template>

    <!-- ===== 管理员/教师端视图 ===== -->
    <template v-else>
      <el-card shadow="never" class="search-card">
        <el-form :inline="true" :model="query">
          <el-form-item label="学校">
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
          <el-form-item>
            <el-button type="primary" @click="loadData(1)">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-row :gutter="16" class="stat-row">
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">选题总数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-value info">{{ stats.hasProgress }}</div>
            <div class="stat-label">已填进度</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-value success">{{ stats.avgProgress }}%</div>
            <div class="stat-label">平均进度</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-value warning">{{ stats.notStarted }}</div>
            <div class="stat-label">未开始</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never">
        <el-table :data="tableData" v-loading="loading" border stripe size="small">
          <el-table-column prop="studentName" label="学生姓名" width="100" fixed />
          <el-table-column prop="studentNo" label="学号" width="110" />
          <el-table-column prop="campusName" label="学校" width="130" />
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
          <el-table-column prop="phase" label="最新阶段" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.phase" size="small" type="primary">{{ row.phase }}</el-tag>
              <span v-else style="color:#c0c4cc">暂无</span>
            </template>
          </el-table-column>
          <el-table-column prop="percentDone" label="完成度" width="130">
            <template #default="{ row }">
              <el-progress
                :percentage="row.percentDone || 0"
                :color="progressColor(row.percentDone)"
                style="width:100px"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" plain @click="openDetail(row)">查看详情</el-button>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" :title="`进度详情 - ${currentRow?.studentName || ''}`" width="800px">
      <div v-if="currentRow" class="detail-panel">
        <el-descriptions :column="3" border size="small" style="margin-bottom:20px">
          <el-descriptions-item label="学生姓名">{{ currentRow.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentRow.studentNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="学校">{{ currentRow.campusName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="课题">{{ currentRow.isCustomTopic ? currentRow.customTopicName : currentRow.topicName }}</el-descriptions-item>
          <el-descriptions-item label="指导教师">{{ currentRow.teacherName || '—' }}{{ currentRow.teacherNo ? `(${currentRow.teacherNo})` : '' }}</el-descriptions-item>
          <el-descriptions-item label="学年">{{ currentRow.academicYear }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-section-title">进度时间线</div>
        <el-timeline v-loading="detailLoading" v-if="detailList.length > 0">
          <el-timeline-item
            v-for="item in detailList"
            :key="item.progressId"
            :timestamp="formatDate(item.submitTime)"
            :color="item.percentDone >= 100 ? '#67c23a' : '#409eff'"
            placement="top"
          >
            <el-card shadow="never" class="timeline-card-item">
              <div class="progress-header">
                <span class="phase-tag">{{ item.phase }}</span>
                <el-progress :percentage="item.percentDone || 0" :color="progressColor(item.percentDone)" style="width:200px"/>
              </div>
              <div class="progress-content" v-if="item.content">{{ item.content }}</div>
              <div class="progress-dates">
                <span>计划完成：{{ formatDate(item.planDate) }}</span>
                <span v-if="item.actualDate">实际完成：{{ formatDate(item.actualDate) }}</span>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无进度记录" />
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑进度对话框（学生端） -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="阶段名称" prop="phase">
          <el-select v-model="form.phase" placeholder="请选择阶段" style="width:100%">
            <el-option label="选题确认" value="选题确认" />
            <el-option label="开题报告" value="开题报告" />
            <el-option label="需求分析" value="需求分析" />
            <el-option label="系统设计" value="系统设计" />
            <el-option label="��码实现" value="编码实现" />
            <el-option label="中期检查" value="中期检查" />
            <el-option label="论文撰写" value="论文撰写" />
            <el-option label="答辩准备" value="答辩准备" />
            <el-option label="正式答辩" value="正式答辩" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="进度说明" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请描述当前阶段的完成情况" />
        </el-form-item>
        <el-form-item label="计划完成日期" prop="planDate">
          <el-date-picker v-model="form.planDate" type="date" placeholder="选择计划完成日期" style="width:100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="完成进度" prop="percentDone">
          <el-slider v-model="form.percentDone" show-input :min="0" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { formatDate } from '@/utils/timeFormat'
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { fetchMySelection, fetchProgressBySelection, addProgress, updateProgress, deleteProgress, fetchAdminProgressPage, fetchTeacherProgressPage } from '@/api/project'

const store = useUserStore()

const isStudent = computed(() => store.user?.userType === 1)

// ======== 学生端数据 ========
const mySelection = ref(null)
const studentProgressList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加进度')
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  progressId: null,
  selectionId: null,
  phase: '',
  content: '',
  planDate: '',
  percentDone: 0,
})

const formRules = {
  phase: [{ required: true, message: '请选择阶段名称', trigger: 'change' }],
  content: [{ required: true, message: '请填写进度说明', trigger: 'blur' }],
}

const totalProgress = computed(() => {
  if (studentProgressList.value.length === 0) return 0
  const total = studentProgressList.value.reduce((sum, p) => sum + (p.percentDone || 0), 0)
  return Math.round(total / studentProgressList.value.length)
})

async function loadMySelection() {
  try {
    mySelection.value = await fetchMySelection()
  } catch { /* handled by interceptor */ }
}

async function loadStudentProgress() {
  if (!mySelection.value) return
  try {
    studentProgressList.value = await fetchProgressBySelection(mySelection.value.selectionId)
  } catch { /* handled by interceptor */ }
}

function handleAdd() {
  dialogTitle.value = '添加进度'
  form.progressId = null
  form.selectionId = mySelection.value.selectionId
  form.phase = ''
  form.content = ''
  form.planDate = ''
  form.percentDone = 0
  dialogVisible.value = true
}

function handleEdit(item) {
  dialogTitle.value = '编辑进度'
  form.progressId = item.progressId
  form.selectionId = item.selectionId
  form.phase = item.phase
  form.content = item.content
  form.planDate = item.planDate ? formatDate(item.planDate) : ''
  form.percentDone = item.percentDone || 0
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (form.progressId) {
      await updateProgress({
        progressId: form.progressId,
        phase: form.phase,
        content: form.content,
        planDate: form.planDate || null,
        percentDone: form.percentDone,
      })
      ElMessage.success('更新成功')
    } else {
      await addProgress({
        selectionId: form.selectionId,
        phase: form.phase,
        content: form.content,
        planDate: form.planDate || null,
      })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    await loadStudentProgress()
  } catch { /* handled */ } finally {
    submitLoading.value = false
  }
}

async function handleDelete(item) {
  try {
    await ElMessageBox.confirm(`确定删除「${item.phase}」这条进度记录吗？`, '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteProgress(item.progressId)
    ElMessage.success('删除成功')
    await loadStudentProgress()
  } catch { /* cancel */ }
}

// ======== 管理员/教师端数据 ========
const loading = ref(false)
const tableData = ref([])
const detailLoading = ref(false)
const detailList = ref([])
const detailVisible = ref(false)
const currentRow = ref(null)

const query = reactive({
  campusName: '',
  keyword: '',
  academicYear: '',
})

const pagination = reactive({
  total: 0,
  current: 1,
  size: 10,
})

const stats = reactive({
  total: 0,
  hasProgress: 0,
  avgProgress: 0,
  notStarted: 0,
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
      campusName: query.campusName || undefined,
      keyword: query.keyword || undefined,
      academicYear: query.academicYear || undefined,
    }
    let res
    if (store.user?.userType === 3) {
      res = await fetchAdminProgressPage(params)
    } else {
      res = await fetchTeacherProgressPage(params)
    }
    tableData.value = res.records || []
    pagination.total = res.total || 0

    const all = tableData.value
    stats.total = res.total || 0
    stats.hasProgress = all.filter(r => r.phase).length
    stats.notStarted = all.filter(r => !r.phase).length
    const progressVals = all.filter(r => r.percentDone).map(r => r.percentDone)
    stats.avgProgress = progressVals.length > 0
      ? Math.round(progressVals.reduce((a, b) => a + b, 0) / progressVals.length)
      : 0
  } catch { /* handled by interceptor */ } finally {
    loading.value = false
  }
}

async function resetQuery() {
  query.campusName = ''
  query.keyword = ''
  query.academicYear = ''
  loadData(1)
}

async function openDetail(row) {
  currentRow.value = row
  detailVisible.value = true
  detailLoading.value = true
  try {
    detailList.value = await fetchProgressBySelection(row.selectionId)
  } catch { /* handled */ } finally {
    detailLoading.value = false
  }
}

// ======== 公共函数 ========
function selectionTagType(status) {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'WITHDRAWN') return 'warning'
  return 'info'
}

function progressColor(val) {
  val = val || 0
  if (val < 30) return '#909399'
  if (val < 60) return '#409eff'
  if (val < 90) return '#e6a23c'
  return '#67c23a'
}

// ======== 初始化 ========
onMounted(async () => {
  if (isStudent.value) {
    await loadMySelection()
    if (mySelection.value) {
      await loadStudentProgress()
    }
  } else {
    await loadData()
  }
})
</script>

<style scoped>
.progress-page {
  padding: 16px;
}

.intro-card, .timeline-card {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-card {
  margin-bottom: 16px;
}

.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
  padding: 8px 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-value.info { color: #409eff; }
.stat-value.success { color: #67c23a; }
.stat-value.warning { color: #e6a23c; }

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
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

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.detail-panel {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.timeline-card-item {
  font-size: 14px;
}

.progress-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 10px;
}

.phase-tag {
  background: #f0f9eb;
  color: #67c23a;
  border: 1px solid #e1f3d8;
  border-radius: 4px;
  padding: 2px 10px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.progress-content {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 10px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

.progress-dates {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 12px;
}

.progress-actions {
  display: flex;
  gap: 8px;
}
</style>
