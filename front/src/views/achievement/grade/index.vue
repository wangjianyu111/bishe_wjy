<template>
  <div class="grade-page">

    <!-- ===== 学生端视图 ===== -->
    <template v-if="isStudent">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <div class="intro-title">
              <el-icon color="#409eff" size="18"><TrendCharts /></el-icon>
              <span>我的成绩</span>
            </div>
            <el-tag type="info" size="small">仅可查看，不可操作</el-tag>
          </div>
        </template>

        <el-alert
          v-if="studentData.length === 0 && !loading"
          title="暂无成绩记录" type="info" :closable="false" show-icon style="margin-bottom: 16px"
        />

        <el-table :data="studentData" v-loading="loading" border stripe>
          <el-table-column label="课题信息" min-width="220">
            <template #default="{ row }">
              <div>
                <span class="custom-tag" v-if="row.isCustomTopic">自拟</span>
                {{ row.isCustomTopic ? row.customTopicName : row.topicName }}
              </div>
              <div style="font-size: 12px; color: #999; margin-top: 2px">
                学年：{{ row.academicYear }} | 学校：{{ row.campusName }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="最终成绩" width="180" align="center">
            <template #default="{ row }">
              <div v-if="row.finalScore != null">
                <span :class="scoreClass(row.finalScore)" style="font-size: 20px; font-weight: bold">
                  {{ row.finalScore }}
                </span>
                <span style="color:#666; font-size: 12px"> 分</span>
              </div>
              <span v-else style="color:#999">暂未评分</span>
            </template>
          </el-table-column>
          <el-table-column label="成绩等级" width="90" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.finalGradeLevel" :type="gradeLevelTagType(row.finalGradeLevel)" size="small">
                {{ row.finalGradeLevel }}
              </el-tag>
              <span v-else style="color:#999">—</span>
            </template>
          </el-table-column>
          <el-table-column label="参与评分教师" width="130" align="center">
            <template #default="{ row }">
              {{ row.evaluatorCount ?? 0 }} 位
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.isLocked" type="danger" size="small">
                <el-icon><Lock /></el-icon>已锁定
              </el-tag>
              <el-tag v-else type="success" size="small">录入中</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openStudentDetail(row)">查看详情</el-button>
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
      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stat-row">
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-value" style="color: #409eff">{{ stats.totalCount }}</div>
            <div class="stat-label">总记录数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-value" style="color: #67c23a">{{ stats.avgScore }}</div>
            <div class="stat-label">平均分</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-value" style="color: #e6a23c">{{ stats.lockedCount }}</div>
            <div class="stat-label">已锁定</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-value" style="color: #f56c6c">{{ stats.adjustedCount }}</div>
            <div class="stat-label">已调整</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 筛选栏 -->
      <el-card shadow="never" class="search-card">
        <el-form :inline="true" :model="query">
          <el-form-item v-if="isAdmin" label="学校">
            <el-input v-model="query.campusName" placeholder="学校名称" clearable style="width: 160px" />
          </el-form-item>
          <el-form-item label="学年">
            <el-input v-model="query.academicYear" placeholder="如 2024-2025" clearable style="width: 140px" />
          </el-form-item>
          <el-form-item label="成绩等级">
            <el-select v-model="query.gradeLevel" placeholder="全部" clearable style="width: 120px">
              <el-option label="优秀" value="优秀" />
              <el-option label="良好" value="良好" />
              <el-option label="中等" value="中等" />
              <el-option label="及格" value="及格" />
              <el-option label="不及格" value="不及格" />
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

      <!-- 主表格 -->
      <el-card shadow="never" class="table-card">
        <template #header>
          <div class="card-header">
            <span>{{ isAdmin ? '成绩评审管理' : '我的评分' }}</span>
            <div>
              <el-button v-if="isAdmin" type="success" @click="handleExport">
                <el-icon><Download /></el-icon>导出成绩单
              </el-button>
            </div>
          </div>
        </template>

        <el-table :data="tableData" v-loading="loading" border stripe size="small">
          <el-table-column prop="studentName" label="学生姓名" width="100" />
          <el-table-column prop="studentNo" label="学号" width="110" />
          <el-table-column v-if="isAdmin" prop="campusName" label="学校" width="130" />
          <el-table-column label="课题" min-width="180">
            <template #default="{ row }">
              <span class="custom-tag" v-if="row.isCustomTopic">自拟</span>
              {{ row.isCustomTopic ? row.customTopicName : row.topicName }}
            </template>
          </el-table-column>
          <el-table-column prop="academicYear" label="学年" width="100" align="center" />
          <!-- 平时 -->
          <el-table-column label="平时成绩" width="90" align="center">
            <template #default="{ row }">
              <span v-if="row.avgRegularScore != null">{{ row.avgRegularScore }}</span>
              <span v-else style="color:#999">—</span>
            </template>
          </el-table-column>
          <!-- 论文 -->
          <el-table-column label="论文成绩" width="90" align="center">
            <template #default="{ row }">
              <span v-if="row.avgThesisScore != null">{{ row.avgThesisScore }}</span>
              <span v-else style="color:#999">—</span>
            </template>
          </el-table-column>
          <!-- 答辩 -->
          <el-table-column label="答辩成绩" width="90" align="center">
            <template #default="{ row }">
              <span v-if="row.avgDefenseScore != null">{{ row.avgDefenseScore }}</span>
              <span v-else style="color:#999">—</span>
            </template>
          </el-table-column>
          <!-- 总分 -->
          <el-table-column label="总分" width="100" align="center">
            <template #default="{ row }">
              <div v-if="row.finalScore != null">
                <span :class="scoreClass(row.finalScore)" style="font-weight: bold; font-size: 14px">
                  {{ row.finalScore }}
                </span>
                <span v-if="row.isAdjusted" style="font-size: 10px; color:#e6a23c">（调）</span>
              </div>
              <span v-else style="color:#999">—</span>
            </template>
          </el-table-column>
          <!-- 等级 -->
          <el-table-column label="等级" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.finalGradeLevel" :type="gradeLevelTagType(row.finalGradeLevel)" size="small">
                {{ row.finalGradeLevel }}
              </el-tag>
              <span v-else style="color:#999">—</span>
            </template>
          </el-table-column>
          <!-- 评分教师数 -->
          <el-table-column label="评分教师" width="90" align="center">
            <template #default="{ row }">
              <el-tooltip v-if="row.detailList && row.detailList.length > 0"
                :content="row.detailList.map(d => d.evaluatorName).join('、')" placement="top">
                <el-button type="info" link size="small">{{ row.evaluatorCount ?? 0 }} 位</el-button>
              </el-tooltip>
              <span v-else style="color:#999">0 位</span>
            </template>
          </el-table-column>
          <!-- 锁定状态 -->
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.isLocked" type="danger" size="small">
                <el-icon><Lock /></el-icon>已锁定
              </el-tag>
              <el-tag v-else type="success" size="small">录入中</el-tag>
            </template>
          </el-table-column>
          <!-- 操作列 -->
          <el-table-column label="操作" width="240" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
              <el-button v-if="!row.isLocked" type="success" link size="small" @click="openInputDialog(row)">录入</el-button>
              <el-button v-if="isAdmin && !row.isLocked" type="warning" link size="small" @click="openAdjustDialog(row)">调整</el-button>
              <el-button v-if="isAdmin && !row.isLocked" type="info" link size="small" @click="handleLock(row)">锁定</el-button>
              <el-button v-if="isAdmin && row.isLocked" type="success" link size="small" @click="handleUnlock(row)">解锁</el-button>
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

    <!-- ===== 教师录入成绩弹窗 ===== -->
    <el-dialog v-model="inputDialogVisible" title="录入成绩" width="560px" destroy-on-close>
      <el-form ref="inputFormRef" :model="inputForm" label-width="100px" :rules="inputRules">
        <el-descriptions :column="1" border size="small" style="margin-bottom: 16px">
          <el-descriptions-item label="学生姓名">{{ inputForm.studentName }}</el-descriptions-item>
          <el-descriptions-item label="课题">{{ inputForm.topicName }}</el-descriptions-item>
        </el-descriptions>
        <el-form-item label="平时成绩" prop="regularScore">
          <el-input-number v-model="inputForm.regularScore" :min="0" :max="100" :precision="1" :step="1" style="width: 160px" />
          <span style="margin-left: 8px; color: #999; font-size: 12px">（权重 30%）</span>
        </el-form-item>
        <el-form-item label="论文成绩" prop="thesisScore">
          <el-input-number v-model="inputForm.thesisScore" :min="0" :max="100" :precision="1" :step="1" style="width: 160px" />
          <span style="margin-left: 8px; color: #999; font-size: 12px">（权重 40%）</span>
        </el-form-item>
        <el-form-item label="答辩成绩" prop="defenseScore">
          <el-input-number v-model="inputForm.defenseScore" :min="0" :max="100" :precision="1" :step="1" style="width: 160px" />
          <span style="margin-left: 8px; color: #999; font-size: 12px">（权重 30%）</span>
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="inputForm.comment" type="textarea" :rows="4" placeholder="请输入评语（可选）" maxlength="500" show-word-limit />
        </el-form-item>
        <el-alert
          v-if="inputForm.regularScore != null || inputForm.thesisScore != null || inputForm.defenseScore != null"
          :title="`预览总分：${calcPreviewTotal()} 分（平时×30% + 论文×40% + 答辩×30%）`"
          type="info" :closable="false" show-icon style="margin-top: 8px"
        />
      </el-form>
      <template #footer>
        <el-button @click="inputDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleInputSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- ===== 管理员调整成绩弹窗 ===== -->
    <el-dialog v-model="adjustDialogVisible" title="调整成绩" width="480px" destroy-on-close>
      <el-form ref="adjustFormRef" :model="adjustForm" label-width="100px">
        <el-descriptions :column="1" border size="small" style="margin-bottom: 16px">
          <el-descriptions-item label="学生姓名">{{ adjustForm.studentName }}</el-descriptions-item>
          <el-descriptions-item label="当前系统分">{{ adjustForm.originalScore ?? '—' }} 分</el-descriptions-item>
          <el-descriptions-item label="当前系统等级">{{ adjustForm.originalGradeLevel ?? '—' }}</el-descriptions-item>
        </el-descriptions>
        <el-form-item label="调整后总分" required>
          <el-input-number v-model="adjustForm.adjustedTotalScore" :min="0" :max="100" :precision="1" :step="1" style="width: 160px" />
        </el-form-item>
        <el-form-item label="调整后等级">
          <el-select v-model="adjustForm.adjustedGradeLevel" placeholder="请选择等级" style="width: 160px">
            <el-option label="优秀" value="优秀" />
            <el-option label="良好" value="良好" />
            <el-option label="中等" value="中等" />
            <el-option label="及格" value="及格" />
            <el-option label="不及格" value="不及格" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整备注">
          <el-input v-model="adjustForm.remark" type="textarea" :rows="3" placeholder="请说明调整原因（必填）" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAdjustSubmit">确认调整</el-button>
      </template>
    </el-dialog>

    <!-- ===== 详情弹窗 ===== -->
    <el-dialog v-model="detailDialogVisible" :title="isStudent ? '我的成绩详情' : '成绩详情'" width="700px" destroy-on-close>
      <el-descriptions v-if="currentGrade" :column="1" border size="default">
        <el-descriptions-item label="学生姓名">{{ currentGrade.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentGrade.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="课题">{{ currentGrade.isCustomTopic ? currentGrade.customTopicName : currentGrade.topicName }}</el-descriptions-item>
        <el-descriptions-item label="学校">{{ currentGrade.campusName }}</el-descriptions-item>
        <el-descriptions-item label="学年">{{ currentGrade.academicYear }}</el-descriptions-item>
        <el-descriptions-item label="参与评分教师">{{ currentGrade.evaluatorCount ?? 0 }} 位</el-descriptions-item>
      </el-descriptions>

      <!-- 教师评分明细表格 -->
      <div v-if="currentGrade && currentGrade.detailList && currentGrade.detailList.length > 0" style="margin-top: 20px">
        <div style="font-weight: 600; margin-bottom: 8px; color: #303133">▎ 教师评分明细</div>
        <el-table :data="currentGrade.detailList" border stripe size="small">
          <el-table-column prop="evaluatorName" label="评分教师" width="120" align="center"></el-table-column>
          <el-table-column label="平时成绩" width="100" align="center">
            <template #default="{ row }">
              <span v-if="row.regularScore != null">{{ row.regularScore }}</span>
              <span v-else style="color:#999">—</span>
            </template>
          </el-table-column>
          <el-table-column label="论文成绩" width="100" align="center">
            <template #default="{ row }">
              <span v-if="row.thesisScore != null">{{ row.thesisScore }}</span>
              <span v-else style="color:#999">—</span>
            </template>
          </el-table-column>
          <el-table-column label="答辩成绩" width="100" align="center">
            <template #default="{ row }">
              <span v-if="row.defenseScore != null">{{ row.defenseScore }}</span>
              <span v-else style="color:#999">—</span>
            </template>
          </el-table-column>
          <el-table-column label="本次总分" width="100" align="center">
            <template #default="{ row }">
              <span v-if="row.totalScore != null" :class="scoreClass(row.totalScore)" style="font-weight: bold">
                {{ row.totalScore }}
              </span>
              <span v-else style="color:#999">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="comment" label="评语" min-width="150" show-overflow-tooltip></el-table-column>
          <el-table-column label="时间" width="160" align="center">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 汇总成绩展示 -->
      <el-divider v-if="currentGrade" />
      <el-descriptions v-if="currentGrade" :column="2" border size="default">
        <el-descriptions-item label="各维度均分">
          平时 {{ currentGrade.avgRegularScore ?? '—' }} | 论文 {{ currentGrade.avgThesisScore ?? '—' }} | 答辩 {{ currentGrade.avgDefenseScore ?? '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="系统计算总分">{{ currentGrade.totalScore ?? '—' }} 分</el-descriptions-item>
        <el-descriptions-item label="最终总分">
          <span v-if="currentGrade.finalScore != null" :class="scoreClass(currentGrade.finalScore)" style="font-weight: bold; font-size: 16px">
            {{ currentGrade.finalScore }} 分
          </span>
          <span v-else style="color:#999">—</span>
        </el-descriptions-item>
        <el-descriptions-item label="最终等级">
          <el-tag v-if="currentGrade.finalGradeLevel" :type="gradeLevelTagType(currentGrade.finalGradeLevel)" size="small">
            {{ currentGrade.finalGradeLevel }}
          </el-tag>
          <span v-else style="color:#999">—</span>
        </el-descriptions-item>
        <el-descriptions-item label="是否已调整">
          <el-tag v-if="currentGrade.isAdjusted" type="warning" size="small">是</el-tag>
          <el-tag v-else type="info" size="small">否</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="锁定状态">
          <el-tag v-if="currentGrade.isLocked" type="danger" size="small">
            <el-icon><Lock /></el-icon>已锁定
          </el-tag>
          <el-tag v-else type="success" size="small">未锁定</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentGrade.remark" label="调整备注" :span="2">{{ currentGrade.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  fetchAdminGradePage,
  fetchTeacherGradePage,
  fetchStudentGradePage,
  inputGrade,
  adjustGrade,
  lockGrade,
  unlockGrade,
  fetchGradeDetail,
} from '@/api/grade'

const userStore = useUserStore()

const isAdmin = computed(() => userStore.user?.userType === 3)
const isStudent = computed(() => userStore.user?.userType === 1)

// ==================== 状态 ====================
const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const studentData = ref([])
const pagination = reactive({ current: 1, size: 10, total: 0 })
const stats = reactive({ totalCount: 0, avgScore: '—', lockedCount: 0, adjustedCount: 0 })

const query = reactive({
  campusName: '',
  academicYear: '',
  keyword: '',
  gradeLevel: '',
})
const studentQuery = reactive({
  academicYear: '',
})

// ==================== 录入表单 ====================
const inputDialogVisible = ref(false)
const inputFormRef = ref()
const inputForm = reactive({
  summaryId: null,
  selectionId: null,
  studentId: null,
  studentName: '',
  topicName: '',
  regularScore: null,
  thesisScore: null,
  defenseScore: null,
  comment: '',
})
const inputRules = {
  regularScore: [{ type: 'number', message: '请输入有效的平时成绩' }],
  thesisScore: [{ type: 'number', message: '请输入有效的论文成绩' }],
  defenseScore: [{ type: 'number', message: '请输入有效的答辩成绩' }],
}

// ==================== 调整表单 ====================
const adjustDialogVisible = ref(false)
const adjustFormRef = ref()
const adjustForm = reactive({
  summaryId: null,
  studentName: '',
  originalScore: null,
  originalGradeLevel: '',
  adjustedTotalScore: null,
  adjustedGradeLevel: '',
  remark: '',
})

// ==================== 详情 ====================
const detailDialogVisible = ref(false)
const currentGrade = ref(null)

// ==================== 加载数据 ====================
async function loadData(page = 1) {
  pagination.current = page
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      campusName: query.campusName || undefined,
      academicYear: query.academicYear || undefined,
      keyword: query.keyword || undefined,
      gradeLevel: query.gradeLevel || undefined,
    }
    const res = isAdmin.value
      ? await fetchAdminGradePage(params)
      : await fetchTeacherGradePage(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
    computeStats()
  } catch {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

async function loadStudentData(page = 1) {
  pagination.current = page
  loading.value = true
  try {
    const res = await fetchStudentGradePage({
      current: pagination.current,
      size: pagination.size,
    })
    studentData.value = res.records || []
    pagination.total = res.total || 0
  } catch {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

function computeStats() {
  stats.totalCount = pagination.total
  const scored = tableData.value.filter(r => r.finalScore != null)
  if (scored.length > 0) {
    const sum = scored.reduce((acc, r) => acc + Number(r.finalScore), 0)
    stats.avgScore = (sum / scored.length).toFixed(1)
  } else {
    stats.avgScore = '—'
  }
  stats.lockedCount = tableData.value.filter(r => r.isLocked).length
  stats.adjustedCount = tableData.value.filter(r => r.isAdjusted).length
}

function resetQuery() {
  query.campusName = ''
  query.academicYear = ''
  query.keyword = ''
  query.gradeLevel = ''
  loadData(1)
}

// ==================== 录入 ====================
function openInputDialog(row) {
  inputForm.summaryId = row.summaryId
  inputForm.selectionId = row.selectionId
  inputForm.studentId = row.studentId
  inputForm.studentName = row.studentName
  inputForm.topicName = row.isCustomTopic ? row.customTopicName : row.topicName
  inputForm.regularScore = null
  inputForm.thesisScore = null
  inputForm.defenseScore = null
  inputForm.comment = ''
  // 预填已有的评分数据（如果有的话，教师可能已评分过）
  const myRecord = row.detailList?.find(d => d.evaluatorId === userStore.user?.userId)
  if (myRecord) {
    inputForm.regularScore = myRecord.regularScore != null ? Number(myRecord.regularScore) : null
    inputForm.thesisScore = myRecord.thesisScore != null ? Number(myRecord.thesisScore) : null
    inputForm.defenseScore = myRecord.defenseScore != null ? Number(myRecord.defenseScore) : null
    inputForm.comment = myRecord.comment || ''
  }
  inputDialogVisible.value = true
}

function calcPreviewTotal() {
  const r = inputForm.regularScore ?? 0
  const t = inputForm.thesisScore ?? 0
  const d = inputForm.defenseScore ?? 0
  if (inputForm.regularScore == null && inputForm.thesisScore == null && inputForm.defenseScore == null) {
    return '—'
  }
  return (r * 0.3 + t * 0.4 + d * 0.3).toFixed(1)
}

async function handleInputSubmit() {
  if (inputForm.regularScore == null && inputForm.thesisScore == null && inputForm.defenseScore == null) {
    ElMessage.warning('请至少录入一项成绩')
    return
  }
  submitting.value = true
  try {
    await inputGrade({
      selectionId: inputForm.selectionId,
      studentId: inputForm.studentId,
      regularScore: inputForm.regularScore,
      thesisScore: inputForm.thesisScore,
      defenseScore: inputForm.defenseScore,
      comment: inputForm.comment,
    })
    ElMessage.success('成绩录入成功')
    inputDialogVisible.value = false
    loadData(pagination.current)
  } catch {
    ElMessage.error('录入失败')
  } finally {
    submitting.value = false
  }
}

// ==================== 调整 ====================
function openAdjustDialog(row) {
  adjustForm.summaryId = row.summaryId
  adjustForm.studentName = row.studentName
  adjustForm.originalScore = row.totalScore
  adjustForm.originalGradeLevel = row.gradeLevel
  adjustForm.adjustedTotalScore = row.totalScore != null ? Number(row.totalScore) : null
  adjustForm.adjustedGradeLevel = row.gradeLevel || ''
  adjustForm.remark = ''
  adjustDialogVisible.value = true
}

async function handleAdjustSubmit() {
  if (!adjustForm.adjustedTotalScore) {
    ElMessage.warning('请输入调整后的总分')
    return
  }
  if (!adjustForm.remark.trim()) {
    ElMessage.warning('请填写调整原因')
    return
  }
  submitting.value = true
  try {
    await adjustGrade(
      adjustForm.summaryId,
      adjustForm.adjustedTotalScore,
      adjustForm.adjustedGradeLevel || undefined,
      adjustForm.remark,
    )
    ElMessage.success('成绩调整成功')
    adjustDialogVisible.value = false
    loadData(pagination.current)
  } catch {
    ElMessage.error('调整失败')
  } finally {
    submitting.value = false
  }
}

// ==================== 锁定/解锁 ====================
async function handleLock(row) {
  try {
    await ElMessageBox.confirm(
      `确定锁定学生「${row.studentName}」的成绩吗？锁定后教师将无法再修改该成绩。`,
      '确认锁定', { type: 'warning', confirmButtonText: '确认锁定' }
    )
    await lockGrade(row.summaryId)
    ElMessage.success('已锁定')
    loadData(pagination.current)
  } catch (err) {
    if (err !== 'cancel') ElMessage.error('操作失败')
  }
}

async function handleUnlock(row) {
  try {
    await ElMessageBox.confirm(
      `确定解锁学生「${row.studentName}」的成绩吗？解锁后教师可以继续修改。`,
      '确认解锁', { type: 'info', confirmButtonText: '确认解锁' }
    )
    await unlockGrade(row.summaryId)
    ElMessage.success('已解锁')
    loadData(pagination.current)
  } catch (err) {
    if (err !== 'cancel') ElMessage.error('操作失败')
  }
}

// ==================== 详情 ====================
async function openDetail(row) {
  try {
    currentGrade.value = await fetchGradeDetail(row.summaryId)
    detailDialogVisible.value = true
  } catch {
    ElMessage.error('加载详情失败')
  }
}

async function openStudentDetail(row) {
  try {
    currentGrade.value = await fetchGradeDetail(row.summaryId)
    detailDialogVisible.value = true
  } catch {
    ElMessage.error('加载详情失败')
  }
}

// ==================== 导出 ====================
function handleExport() {
  ElMessage.info('导出功能开发中，请使用查询条件筛选后手动记录')
}

// ==================== 辅助函数 ====================
function scoreClass(score) {
  if (score == null) return ''
  const s = Number(score)
  if (s >= 90) return 'score-high'
  if (s >= 70) return 'score-mid'
  return 'score-low'
}

function gradeLevelTagType(level) {
  switch (level) {
    case '优秀': return 'success'
    case '良好': return 'primary'
    case '中等': return 'warning'
    case '及格': return 'info'
    case '不及格': return 'danger'
    default: return 'info'
  }
}

function formatTime(value) {
  if (!value) return '—'
  const d = new Date(value)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// ==================== 初始化 ====================
onMounted(() => {
  if (isStudent.value) {
    loadStudentData()
  } else {
    loadData()
  }
})
</script>

<style scoped>
.grade-page {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.intro-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.filter-bar {
  margin-bottom: 8px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.custom-tag {
  display: inline-block;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
  padding: 0 4px;
  font-size: 11px;
  margin-right: 4px;
}

.score-high { color: #67c23a; }
.score-mid { color: #e6a23c; }
.score-low { color: #f56c6c; }

.stat-row {
  margin-bottom: 12px;
}
.stat-card {
  text-align: center;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.table-card :deep(.el-table) {
  font-size: 13px;
}
</style>
