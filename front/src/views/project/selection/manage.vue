<template>
  <div class="selection-manage-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="学校">
          <el-input v-model="query.campusName" placeholder="搜索学校名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="指导教师">
          <el-select
            v-model="query.teacherId"
            placeholder="全部教师"
            clearable
            filterable
            style="width: 160px"
          >
            <el-option v-for="t in teacherList" :key="t.userId" :label="t.realName" :value="t.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="学年">
          <el-select v-model="query.academicYear" placeholder="全部学年" clearable style="width: 160px">
            <el-option v-for="y in academicYearOptions" :key="y" :label="y" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
            <el-option label="已撤回" value="WITHDRAWN" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="学生姓名/学号/课题名" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData(1)">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">申请总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value pending">{{ stats.pending }}</div>
          <div class="stat-label">待审批</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value success">{{ stats.approved }}</div>
          <div class="stat-label">已通过</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value danger">{{ stats.rejected }}</div>
          <div class="stat-label">已驳回</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-card shadow="never">
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        size="small"
      >
        <el-table-column prop="selectionId" label="ID" width="70" />
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="studentNo" label="学号" width="120" />
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
        <el-table-column prop="academicYear" label="学年" width="120" />
        <el-table-column prop="applyReason" label="申请理由" min-width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="tagType(row.status)">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160">
          <template #default="{ row }">
            {{ row.createTime ? formatTime(row.createTime) : '—' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button type="success" size="small" @click="handleApprove(row)">通过</el-button>
              <el-button type="danger" size="small" plain @click="handleReject(row)">驳回</el-button>
            </template>
            <template v-else-if="row.status === 'REJECTED'">
              <el-tooltip v-if="row.rejectReason" :content="`驳回原因：${row.rejectReason}`" placement="top">
                <el-button type="info" size="small" plain>查看原因</el-button>
              </el-tooltip>
              <span v-else style="color:#999">—</span>
            </template>
            <span v-else style="color:#999">—</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 驳回对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回申请" width="500px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="申请ID">
          <el-input v-model="rejectForm.selectionId" disabled />
        </el-form-item>
        <el-form-item label="驳回原因" required>
          <el-input
            v-model="rejectForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入驳回原因（将通知学生）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="actionLoading" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchSelectionPage, approveSelection, rejectSelection, fetchTeachers } from '@/api/project'

const loading = ref(false)
const tableData = ref([])
const teacherList = ref([])
const actionLoading = ref(false)

const query = reactive({
  campusName: '',
  teacherId: null,
  academicYear: '',
  status: '',
  keyword: '',
})

const pagination = reactive({
  total: 0,
  current: 1,
  size: 10,
})

const stats = reactive({
  total: 0,
  pending: 0,
  approved: 0,
  rejected: 0,
})

const rejectDialogVisible = ref(false)
const rejectForm = reactive({
  selectionId: null,
  comment: '',
})

const academicYearOptions = computed(() => {
  const y = new Date().getFullYear()
  return [`${y - 1}-${y}`, `${y}-${y + 1}`, `${y + 1}-${y + 2}`]
})

function tagType(status) {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'WITHDRAWN') return 'warning'
  return 'info'
}

function formatTime(value) {
  if (!value) return '—'
  const d = new Date(value)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function loadTeachers() {
  try {
    teacherList.value = await fetchTeachers({ campusName: query.campusName || undefined })
  } catch { /* handled by interceptor */ }
}

async function loadData(page = 1) {
  pagination.current = page
  loading.value = true
  try {
    const params = {
      current: page,
      size: pagination.size,
      campusName: query.campusName || undefined,
      teacherId: query.teacherId || undefined,
      academicYear: query.academicYear || undefined,
      status: query.status || undefined,
      keyword: query.keyword || undefined,
    }
    const res = await fetchSelectionPage(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0

    // 统计
    const all = tableData.value
    stats.total = res.total || 0
    stats.pending = all.filter(r => r.status === 'PENDING').length
    stats.approved = all.filter(r => r.status === 'APPROVED').length
    stats.rejected = all.filter(r => r.status === 'REJECTED').length
  } finally {
    loading.value = false
  }
}

async function resetQuery() {
  query.campusName = ''
  query.teacherId = null
  query.academicYear = ''
  query.status = ''
  query.keyword = ''
  await loadTeachers()
  loadData(1)
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(
      `确定通过学生「${row.studentName}」的选题申请？${row.isCustomTopic ? '\n（自拟课题：' + row.customTopicName + '）' : '\n（课题：' + row.topicName + '）'}`,
      '审批确认',
      { confirmButtonText: '确认通过', cancelButtonText: '取消', type: 'success' }
    )
    actionLoading.value = true
    await approveSelection({ selectionId: row.selectionId })
    ElMessage.success('已通过申请')
    loadData(pagination.current)
  } catch { /* cancel */ } finally {
    actionLoading.value = false
  }
}

function handleReject(row) {
  rejectForm.selectionId = row.selectionId
  rejectForm.comment = ''
  rejectDialogVisible.value = true
}

async function confirmReject() {
  if (!rejectForm.comment.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  actionLoading.value = true
  try {
    await rejectSelection({ selectionId: rejectForm.selectionId, comment: rejectForm.comment })
    ElMessage.success('已驳回申请')
    rejectDialogVisible.value = false
    loadData(pagination.current)
  } catch { /* handled */ } finally {
    actionLoading.value = false
  }
}

onMounted(async () => {
  await loadTeachers()
  await loadData()
})
</script>

<style scoped>
.selection-manage-page {
  padding: 16px;
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

.stat-value.pending { color: #409eff; }
.stat-value.success { color: #67c23a; }
.stat-value.danger { color: #f56c6c; }

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
</style>
