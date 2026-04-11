<template>
  <div class="approval-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="学年">
          <el-input v-model="query.academicYear" placeholder="如：2024-2025" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
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

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        size="small"
      >
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="studentNo" label="学号" width="130" />
        <el-table-column prop="campusName" label="学校" width="130" />
        <el-table-column label="课题" min-width="200">
          <template #default="{ row }">
            <span v-if="row.isCustomTopic" class="custom-tag">自拟</span>
            {{ row.isCustomTopic ? row.customTopicName : row.topicName }}
          </template>
        </el-table-column>
        <el-table-column prop="academicYear" label="学年" width="120" />
        <el-table-column prop="applyReason" label="申请理由" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType(row.status)">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="驳回原因" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.status === 'REJECTED' ? row.rejectReason : '—' }}
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button type="success" size="small" @click="handleApprove(row)">通过</el-button>
              <el-button type="danger" size="small" @click="openReject(row)">驳回</el-button>
            </template>
            <span v-else class="handled-text">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回选题申请" width="480px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="学生姓名">
          <span>{{ rejectForm.studentName }}</span>
        </el-form-item>
        <el-form-item label="课题名称">
          <span>{{ rejectForm.topicName }}</span>
        </el-form-item>
        <el-form-item label="驳回原因" required>
          <el-input
            v-model="rejectForm.rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请填写驳回原因，以便学生了解未通过的原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="confirmReject">发送并驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { formatDateTime as formatTime } from '@/utils/timeFormat'
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchTeacherApprovals, approveSelection, rejectSelection } from '@/api/project'

const loading = ref(false)
const tableData = ref([])
const actionLoading = ref(false)

const query = reactive({
  academicYear: '',
  status: '',
  keyword: '',
})

const pagination = reactive({
  total: 0,
  current: 1,
  size: 10,
})

const rejectDialogVisible = ref(false)
const rejectForm = reactive({
  selectionId: null,
  studentName: '',
  topicName: '',
  rejectReason: '',
})

function statusTagType(status) {
  if (status === 'PENDING') return 'warning'
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'info'
}

async function loadData(page = 1) {
  pagination.current = page
  loading.value = true
  try {
    const params = {
      current: page,
      size: pagination.size,
      academicYear: query.academicYear || undefined,
      status: query.status || undefined,
      keyword: query.keyword || undefined,
    }
    const res = await fetchTeacherApprovals(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch { /* handled by interceptor */ } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.academicYear = ''
  query.status = ''
  query.keyword = ''
  loadData(1)
}

function buildTopicName(row) {
  if (row.isCustomTopic) {
    return row.customTopicName || '自拟课题'
  }
  return row.topicName || '未知课题'
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(
      `确定通过学生「${row.studentName}」的选题申请吗？\n课题：${buildTopicName(row)}\n将通过后系统将自动发送通知给学生。`,
      '审批确认',
      { type: 'success', confirmButtonText: '确认通过' }
    )
    actionLoading.value = true
    await approveSelection({ selectionId: row.selectionId })
    ElMessage.success('已通过申请，系统已通知学生')
    loadData(pagination.current)
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('操作失败')
    }
  } finally {
    actionLoading.value = false
  }
}

function openReject(row) {
  rejectForm.selectionId = row.selectionId
  rejectForm.studentName = row.studentName
  rejectForm.topicName = buildTopicName(row)
  rejectForm.rejectReason = ''
  rejectDialogVisible.value = true
}

async function confirmReject() {
  if (!rejectForm.rejectReason?.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  try {
    actionLoading.value = true
    await rejectSelection({
      selectionId: rejectForm.selectionId,
      comment: rejectForm.rejectReason.trim(),
    })
    ElMessage.success('已驳回，系统已通知学生')
    rejectDialogVisible.value = false
    loadData(pagination.current)
  } catch { /* handled */ } finally {
    actionLoading.value = false
  }
}

onMounted(async () => {
  await loadData()
})
</script>

<style scoped>
.approval-page {
  padding: 16px;
}

.search-card {
  margin-bottom: 12px;
}

.table-card :deep(.el-table) {
  font-size: 13px;
}

.custom-tag {
  background-color: #e6a23c;
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  margin-right: 4px;
}

.pagination-wrap {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.handled-text {
  color: #999;
  font-size: 13px;
}
</style>
