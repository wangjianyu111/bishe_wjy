<template>
  <div class="excellent-page">

    <!-- ===== 教师/管理员视图 ===== -->
    <template v-if="!isStudent">

      <!-- 统计卡片 -->
      <el-row :gutter="12" class="stat-row">
        <el-col :span="8">
          <el-card shadow="never" class="stat-card">
            <div class="stat-value" style="color: #409eff">{{ stats.totalCount }}</div>
            <div class="stat-label">优秀成果总数</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never" class="stat-card">
            <div class="stat-value" style="color: #67c23a">{{ stats.qualifiedCount }}</div>
            <div class="stat-label">待认定（已达线）</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never" class="stat-card">
            <div class="stat-value" style="color: #909399">{{ thresholdCount }} 校已配置阈值</div>
            <div class="stat-label">阈值配置</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 优秀成果列表 -->
      <el-card shadow="never" class="table-card">
        <template #header>
          <div class="card-header">
            <span>优秀成果名单</span>
            <div>
              <el-button type="success" @click="handleExport">
                <el-icon><Download /></el-icon>导出名单
              </el-button>
            </div>
          </div>
        </template>

        <!-- 筛选栏 -->
        <el-form :inline="true" :model="query" style="margin-bottom: 12px">
          <el-form-item v-if="isAdmin" label="学校">
            <el-input v-model="query.campusName" placeholder="学校名称" clearable style="width: 150px" />
          </el-form-item>
          <el-form-item label="学年">
            <el-input v-model="query.academicYear" placeholder="如 2024-2025" clearable style="width: 140px" />
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="query.keyword" placeholder="学生姓名/学号" clearable style="width: 150px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData(1)">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="tableData" v-loading="loading" border stripe size="small">
          <el-table-column prop="studentName" label="学生姓名" width="100" align="center" />
          <el-table-column prop="studentNo" label="学号" width="120" align="center" />
          <el-table-column v-if="isAdmin" prop="campusName" label="学校" width="130" align="center" />
          <el-table-column label="课题" min-width="200">
            <template #default="{ row }">
              <span class="custom-tag" v-if="row.isCustomTopic">自拟</span>
              {{ row.isCustomTopic ? row.customTopicName : row.topicName }}
            </template>
          </el-table-column>
          <el-table-column prop="academicYear" label="学年" width="100" align="center" />
          <el-table-column label="最终成绩" width="110" align="center">
            <template #default="{ row }">
              <span v-if="row.gradeTotalScore != null" :class="scoreClass(row.gradeTotalScore)" style="font-weight: bold">
                {{ row.gradeTotalScore }}
              </span>
              <span v-else style="color:#999">—</span>
            </template>
          </el-table-column>
          <el-table-column label="优秀分数线" width="110" align="center">
            <template #default="{ row }">
              <span v-if="row.campusThreshold != null" style="color: #909399; font-size: 12px">
                ≥{{ row.campusThreshold }}
              </span>
              <el-tag v-else type="danger" size="small">未配置</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="认定等级" width="90" align="center">
            <template #default>
              <el-tag type="success" size="small">优秀</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="认定人" width="90" align="center">
            <template #default="{ row }">
              {{ row.approverName || '—' }}
            </template>
          </el-table-column>
          <el-table-column label="认定时间" width="160" align="center">
            <template #default="{ row }">
              {{ formatTime(row.approveTime) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'APPROVED'" type="success" size="small">已认定</el-tag>
              <el-tag v-else-if="row.status === 'REJECTED'" type="danger" size="small">已撤销</el-tag>
              <el-tag v-else type="warning" size="small">待认定</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openGradeDetail(row)">成绩详情</el-button>
              <el-button type="danger" link size="small" @click="handleRevoke(row)">撤销</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            background
            layout="total, prev, pager, next"
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            @current-change="loadData"
          />
        </div>
      </el-card>

      <!-- 待认定候选列表（成绩已达学校阈值） -->
      <el-card shadow="never" class="table-card" style="margin-top: 12px">
        <template #header>
          <div class="card-header">
            <span>待认定学生（成绩已达学校优秀分数线）</span>
            <el-button type="primary" size="small" @click="loadQualifiedGrades">
              <el-icon><Refresh /></el-icon>刷新
            </el-button>
          </div>
        </template>

        <el-alert
          v-if="qualifiedGrades.length === 0 && !loading2"
          title="暂无符合条件的待认定学生" type="info" :closable="false" show-icon style="margin-bottom: 12px"
        />

        <el-table :data="qualifiedGrades" v-loading="loading2" border stripe size="small">
          <el-table-column prop="studentName" label="学生姓名" width="100" align="center" />
          <el-table-column prop="studentNo" label="学号" width="120" align="center" />
          <el-table-column prop="campusName" label="学校" width="130" align="center" />
          <el-table-column prop="academicYear" label="学年" width="100" align="center" />
          <el-table-column label="成绩总分" width="110" align="center">
            <template #default="{ row }">
              <span :class="scoreClass(row.gradeTotalScore)" style="font-weight: bold">{{ row.gradeTotalScore }}</span>
            </template>
          </el-table-column>
          <el-table-column label="优秀分数线" width="120" align="center">
            <template #default="{ row }">
              <span style="color: #67c23a; font-weight: 600">≥{{ row.campusThreshold }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.gradeTotalScore > row.campusThreshold" type="success" size="small">已达线</el-tag>
              <el-tag v-else type="info" size="small">未达线</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleApprove(row)">认定</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 阈值管理（仅管理员可见） -->
      <el-card v-if="isAdmin" shadow="never" class="table-card" style="margin-top: 12px">
        <template #header>
          <div class="card-header">
            <span>学校优秀分数线配置</span>
            <el-button type="primary" size="small" @click="openThresholdDialog()">
              <el-icon><Plus /></el-icon>添加阈值
            </el-button>
          </div>
        </template>

        <el-table :data="thresholdList" v-loading="loading3" border stripe size="small">
          <el-table-column prop="campusName" label="学校名称" min-width="150" />
          <el-table-column prop="academicYear" label="学年" width="120" align="center" />
          <el-table-column label="优秀分数线" width="140" align="center">
            <template #default="{ row }">
              <span style="color: #67c23a; font-weight: bold; font-size: 16px">≥{{ row.excellentScore }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150" />
          <el-table-column prop="createTime" label="创建时间" width="160" align="center">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openThresholdDialog(row)">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDeleteThreshold(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>

    <!-- ===== 学生端视图 ===== -->
    <template v-else>
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <div class="intro-title">
              <el-icon color="#67c23a" size="18"><Medal /></el-icon>
              <span>优秀成果名单</span>
            </div>
            <el-tag type="info" size="small">仅可查看本校优秀成果</el-tag>
          </div>
        </template>

        <el-alert
          v-if="studentData.length === 0 && !loading"
          title="暂无优秀成果记录" type="info" :closable="false" show-icon style="margin-bottom: 16px"
        />

        <!-- 查询栏 -->
        <el-form :inline="true" :model="studentQuery" style="margin-bottom: 12px">
          <el-form-item label="姓名/学号">
            <el-input v-model="studentQuery.keyword" placeholder="输入学生姓名或学号" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadStudentData(1)">查询</el-button>
            <el-button @click="studentQuery.keyword = ''; loadStudentData(1)">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 是否有我的认定 -->
        <el-alert
          v-if="myExcellence"
          :title="`恭喜！您已被认定为优秀成果`"
          type="success"
          :closable="false"
          show-icon
          style="margin-bottom: 16px"
        >
          <template #default>
            最终成绩：<strong>{{ myExcellence.gradeTotalScore }}</strong> 分，
            认定时间：<strong>{{ formatTime(myExcellence.approveTime) }}</strong>
          </template>
        </el-alert>

        <el-table :data="studentData" v-loading="loading" border stripe>
          <el-table-column prop="studentName" label="学生姓名" width="100" align="center" />
          <el-table-column prop="studentNo" label="学号" width="120" align="center" />
          <el-table-column label="课题" min-width="200">
            <template #default="{ row }">
              <span class="custom-tag" v-if="row.isCustomTopic">自拟</span>
              {{ row.isCustomTopic ? row.customTopicName : row.topicName }}
            </template>
          </el-table-column>
          <el-table-column label="成绩" width="90" align="center">
            <template #default="{ row }">
              <span v-if="row.gradeTotalScore != null" :class="scoreClass(row.gradeTotalScore)" style="font-weight: bold">
                {{ row.gradeTotalScore }}
              </span>
              <span v-else style="color:#999">—</span>
            </template>
          </el-table-column>
          <el-table-column label="优秀分数线" width="110" align="center">
            <template #default="{ row }">
              <span v-if="row.campusThreshold != null" style="color: #909399; font-size: 12px">≥{{ row.campusThreshold }}</span>
              <el-tag v-else type="danger" size="small">未配置</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="认定时间" width="160" align="center">
            <template #default="{ row }">
              {{ formatTime(row.approveTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openGradeDetail(row)">成绩详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            background
            layout="total, prev, pager, next"
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            @current-change="loadStudentData"
          />
        </div>
      </el-card>
    </template>

    <!-- ===== 认定弹窗 ===== -->
    <el-dialog v-model="approveDialogVisible" title="认定优秀成果" width="450px" destroy-on-close>
      <el-form :model="approveForm" label-width="90px">
        <el-form-item label="学生">{{ approveForm.studentName }}</el-form-item>
        <el-form-item label="学校">{{ approveForm.campusName }}</el-form-item>
        <el-form-item label="成绩总分">
          <span :class="scoreClass(approveForm.gradeTotalScore)" style="font-weight: bold">
            {{ approveForm.gradeTotalScore }}
          </span>
          <span style="color: #909399; margin-left: 8px">（优秀分数线：≥{{ approveForm.campusThreshold }}）</span>
        </el-form-item>
        <el-form-item label="认定备注">
          <el-input v-model="approveForm.remark" type="textarea" :rows="3" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleApproveSubmit">确认认定</el-button>
      </template>
    </el-dialog>

    <!-- ===== 阈值配置弹窗 ===== -->
    <el-dialog v-if="isAdmin" v-model="thresholdDialogVisible" :title="thresholdForm.thresholdId ? '编辑阈值' : '添加阈值'" width="450px" destroy-on-close>
      <el-form :model="thresholdForm" :rules="thresholdRules" ref="thresholdFormRef" label-width="100px">
        <el-form-item label="学校名称" prop="campusName">
          <el-input v-model="thresholdForm.campusName" placeholder="如：计算机学院" />
        </el-form-item>
        <el-form-item label="学年" prop="academicYear">
          <el-input v-model="thresholdForm.academicYear" placeholder="如：2024-2025" />
        </el-form-item>
        <el-form-item label="优秀分数线" prop="excellentScore">
          <el-input-number v-model="thresholdForm.excellentScore" :min="0" :max="100" :precision="0" style="width: 100%" />
          <div style="color: #909399; font-size: 12px; margin-top: 4px">成绩总分必须大于此分数才能被认定为优秀（0~100分）</div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="thresholdForm.remark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="thresholdDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSaveThreshold">保存</el-button>
      </template>
    </el-dialog>

    <!-- ===== 成绩详情弹窗 ===== -->
    <el-dialog v-model="detailDialogVisible" title="成绩详情" width="700px" destroy-on-close>
      <el-descriptions v-if="currentGrade" :column="2" border size="default">
        <el-descriptions-item label="学生姓名">{{ currentGrade.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentGrade.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="课题" :span="2">
          <span class="custom-tag" v-if="currentGrade.isCustomTopic">自拟</span>
          {{ currentGrade.isCustomTopic ? currentGrade.customTopicName : currentGrade.topicName }}
        </el-descriptions-item>
        <el-descriptions-item label="学校">{{ currentGrade.campusName }}</el-descriptions-item>
        <el-descriptions-item label="学年">{{ currentGrade.academicYear }}</el-descriptions-item>
        <el-descriptions-item label="指导教师">{{ currentGrade.teacherName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="参与评分教师">{{ currentGrade.evaluatorCount ?? 0 }} 位</el-descriptions-item>
        <el-descriptions-item label="优秀分数线">
          <span v-if="currentGrade.campusThreshold != null" style="color: #67c23a">≥{{ currentGrade.campusThreshold }}</span>
          <el-tag v-else type="danger" size="small">未配置</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="最终成绩" :span="2">
          <span v-if="currentGrade.gradeTotalScore != null" :class="scoreClass(currentGrade.gradeTotalScore)" style="font-weight: bold; font-size: 16px">
            {{ currentGrade.gradeTotalScore }}
          </span>
          <span v-else style="color:#999">—</span>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider v-if="currentGrade && currentGrade.avgRegularScore != null" />
      <div v-if="currentGrade && currentGrade.avgRegularScore != null" style="margin-top: 12px">
        <div style="font-weight: 600; margin-bottom: 8px; color: #303133">▎ 各维度均分</div>
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="平时均分">{{ currentGrade.avgRegularScore }}</el-descriptions-item>
          <el-descriptions-item label="论文均分">{{ currentGrade.avgThesisScore }}</el-descriptions-item>
          <el-descriptions-item label="答辩均分">{{ currentGrade.avgDefenseScore }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-divider v-if="currentGrade && currentGrade.excellentId" />
      <el-descriptions v-if="currentGrade && currentGrade.excellentId" :column="2" border size="default" style="margin-top: 12px">
        <el-descriptions-item label="认定等级"><el-tag type="success" size="small">优秀</el-tag></el-descriptions-item>
        <el-descriptions-item label="认定人">{{ currentGrade.approverName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="认定时间" :span="2">{{ formatTime(currentGrade.approveTime) }}</el-descriptions-item>
        <el-descriptions-item label="认定备注" :span="2">{{ currentGrade.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { formatDateTime as formatTime, formatDate } from '@/utils/timeFormat'
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  fetchAdminExcellentPage,
  fetchTeacherExcellentPage,
  fetchStudentExcellentPage,
  fetchStudentExcellentSearch,
  approveExcellent,
  revokeExcellent,
  fetchThresholdList,
  saveThreshold,
  deleteThreshold,
  fetchQualifiedGrades,
  exportExcellent,
} from '@/api/excellent'

const userStore = useUserStore()

const isAdmin = computed(() => userStore.user?.userType === 3)
const isStudent = computed(() => userStore.user?.userType === 1)

// ==================== 状态 ====================
const loading = ref(false)
const loading2 = ref(false)
const loading3 = ref(false)
const submitting = ref(false)
const tableData = ref([])
const studentData = ref([])
const qualifiedGrades = ref([])
const thresholdList = ref([])
const pagination = reactive({ current: 1, size: 10, total: 0 })
const stats = reactive({ totalCount: 0, qualifiedCount: 0 })
const thresholdCount = ref(0)

const query = reactive({ campusName: '', academicYear: '', keyword: '' })
const studentQuery = reactive({ keyword: '' })

// 认定弹窗
const approveDialogVisible = ref(false)
const approveForm = reactive({
  summaryId: null, studentName: '', campusName: '',
  gradeTotalScore: null, campusThreshold: null, remark: '',
})

// 阈值配置弹窗
const thresholdDialogVisible = ref(false)
const thresholdFormRef = ref()
const thresholdForm = reactive({
  thresholdId: null, campusName: '', academicYear: '',
  excellentScore: 90, remark: '',
})
const thresholdRules = {
  campusName: [{ required: true, message: '请输入学校名称', trigger: 'blur' }],
  academicYear: [{ required: true, message: '请输入学年', trigger: 'blur' }],
  excellentScore: [{ required: true, message: '请输入优秀分数线', trigger: 'blur' }],
}

// 详情弹窗
const detailDialogVisible = ref(false)
const currentGrade = ref(null)

// ==================== 数据加载 ====================
async function loadData(page = 1) {
  pagination.current = page
  loading.value = true
  try {
    const params = {
      current: pagination.current, size: pagination.size,
      campusName: query.campusName || undefined,
      academicYear: query.academicYear || undefined,
      keyword: query.keyword || undefined,
    }
    const res = isAdmin.value
      ? await fetchAdminExcellentPage(params)
      : await fetchTeacherExcellentPage(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
    stats.totalCount = pagination.total
  } catch { /* 错误已在拦截器处理 */ }
  finally { loading.value = false }
}

async function loadStudentData(page = 1) {
  pagination.current = page
  loading.value = true
  try {
    let res
    if (studentQuery.keyword) {
      res = await fetchStudentExcellentSearch({
        current: pagination.current, size: pagination.size, keyword: studentQuery.keyword,
      })
    } else {
      res = await fetchStudentExcellentPage({ current: pagination.current, size: pagination.size })
    }
    studentData.value = res.records || []
    pagination.total = res.total || 0
  } catch { /* 错误已在拦截器处理 */ }
  finally { loading.value = false }
}

async function loadQualifiedGrades() {
  loading2.value = true
  try {
    const params = isAdmin.value ? {} : { campusName: userStore.user?.campusName }
    const res = await fetchQualifiedGrades(params)
    qualifiedGrades.value = res || []
    stats.qualifiedCount = qualifiedGrades.value.length
  } catch { /* 错误已在拦截器处理 */ }
  finally { loading2.value = false }
}

async function loadThresholds() {
  if (!isAdmin.value) return
  loading3.value = true
  try {
    const res = await fetchThresholdList({})
    thresholdList.value = res || []
    thresholdCount.value = thresholdList.value.length
  } catch { /* 错误已在拦截器处理 */ }
  finally { loading3.value = false }
}

const myExcellence = computed(() =>
  studentData.value.find(r => r.studentId === userStore.user?.userId && r.excellentId) || null
)

// ==================== 认定 ====================
function handleApprove(row) {
  approveForm.summaryId = row.summaryId
  approveForm.studentName = row.studentName
  approveForm.campusName = row.campusName
  approveForm.gradeTotalScore = row.gradeTotalScore
  approveForm.campusThreshold = row.campusThreshold
  approveForm.remark = ''
  approveDialogVisible.value = true
}

async function handleApproveSubmit() {
  submitting.value = true
  try {
    await approveExcellent({ gradeSummaryId: approveForm.summaryId, remark: approveForm.remark })
    ElMessage.success('认定成功')
    approveDialogVisible.value = false
    loadData(pagination.current)
    loadQualifiedGrades()
  } catch { /* 错误已在拦截器处理 */ }
  finally { submitting.value = false }
}

// ==================== 撤销 ====================
async function handleRevoke(row) {
  try {
    await ElMessageBox.confirm(`确定撤销学生「${row.studentName}」的优秀成果认定吗？`, '确认撤销', { type: 'warning', confirmButtonText: '确认撤销' })
    await revokeExcellent(row.excellentId)
    ElMessage.success('已撤销')
    loadData(pagination.current)
  } catch (err) {
    if (err !== 'cancel') { /* 错误已在拦截器处理 */ }
  }
}

// ==================== 阈值配置 ====================
function openThresholdDialog(row = null) {
  if (row) {
    thresholdForm.thresholdId = row.thresholdId
    thresholdForm.campusName = row.campusName
    thresholdForm.academicYear = row.academicYear
    thresholdForm.excellentScore = Number(row.excellentScore)
    thresholdForm.remark = row.remark || ''
  } else {
    thresholdForm.thresholdId = null
    thresholdForm.campusName = ''
    thresholdForm.academicYear = ''
    thresholdForm.excellentScore = 90
    thresholdForm.remark = ''
  }
  thresholdDialogVisible.value = true
}

async function handleSaveThreshold() {
  try {
    await thresholdFormRef.value.validate()
  } catch { return }
  submitting.value = true
  try {
    await saveThreshold({ ...thresholdForm })
    ElMessage.success('保存成功')
    thresholdDialogVisible.value = false
    loadThresholds()
  } catch { /* 错误已在拦截器处理 */ }
  finally { submitting.value = false }
}

async function handleDeleteThreshold(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.campusName}」的优秀分数线配置吗？`, '确认删除', { type: 'warning', confirmButtonText: '确认删除' })
    await deleteThreshold(row.thresholdId)
    ElMessage.success('删除成功')
    loadThresholds()
  } catch (err) {
    if (err !== 'cancel') { /* 错误已在拦截器处理 */ }
  }
}

// ==================== 详情 ====================
async function openGradeDetail(row) {
  currentGrade.value = { ...row }
  detailDialogVisible.value = true
}

// ==================== 导出 ====================
async function handleExport() {
  loading.value = true
  try {
    const params = {
      campusName: query.campusName || undefined,
      academicYear: query.academicYear || undefined,
    }
    const blob = await exportExcellent(params)
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    link.download = `优秀成果名单_${Date.now()}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch { /* 错误已在拦截器处理 */ }
  finally { loading.value = false }
}

// ==================== 重置 ====================
function resetQuery() {
  query.campusName = ''; query.academicYear = ''; query.keyword = ''
  loadData(1)
}

// ==================== 辅助函数 ====================
function scoreClass(score) {
  if (score == null) return ''
  const s = Number(score)
  if (s >= 90) return 'score-high'
  if (s >= 70) return 'score-mid'
  return 'score-low'
}

// ==================== 初始化 ====================
onMounted(() => {
  if (isStudent.value) {
    loadStudentData()
  } else {
    loadData()
    loadQualifiedGrades()
    loadThresholds()
  }
})
</script>

<style scoped>
.excellent-page { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.intro-title { display: flex; align-items: center; gap: 6px; font-size: 15px; font-weight: 600; color: #303133; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 14px; }
.custom-tag { display: inline-block; background: #ecf5ff; color: #409eff; border-radius: 4px; padding: 0 4px; font-size: 11px; margin-right: 4px; }
.score-high { color: #67c23a; }
.score-mid { color: #e6a23c; }
.score-low { color: #f56c6c; }
.stat-row { margin-bottom: 12px; }
.stat-card { text-align: center; }
.stat-value { font-size: 28px; font-weight: bold; line-height: 1.2; }
.stat-label { font-size: 12px; color: #909399; margin-top: 4px; }
.table-card :deep(.el-table) { font-size: 13px; }
</style>
