<template>
  <el-card shadow="never">
    <template #header>
      <div class="header-row">
        <span>消息通知管理</span>
        <el-button type="primary" @click="openAdd">发送通知</el-button>
      </div>
    </template>

    <el-form :inline="true" class="filter">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="通知标题" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="table.records" v-loading="loading" border stripe>
      <el-table-column prop="noticeId" label="ID" width="70" />
      <el-table-column prop="title" label="通知标题" min-width="160" show-overflow-tooltip />
      <el-table-column prop="content" label="通知内容" min-width="200" show-overflow-tooltip />
      <el-table-column prop="noticeType" label="类型" width="100">
        <template #default="{ row }">
          {{ typeLabel(row.noticeType) }}
        </template>
      </el-table-column>
      <el-table-column prop="senderName" label="发送人" width="100" />
      <el-table-column prop="receiverNames" label="接收人" min-width="140">
        <template #default="{ row }">
          {{ formatReceivers(row.receiverNames, row.receiverCount) }}
        </template>
      </el-table-column>
      <el-table-column label="已读状态" width="100">
        <template #default="{ row }">
          <el-tag :type="(row.readCount || 0) === row.receiverCount ? 'success' : row.readCount > 0 ? 'warning' : 'info'" size="small">
            {{ row.readCount || 0 }}/{{ row.receiverCount }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发送时间" width="170">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
          <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="table.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="load"
        @current-change="load"
      />
    </div>
  </el-card>

  <el-dialog v-model="dialog.visible" :title="dialog.title" width="560px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100">
      <el-form-item label="通知标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入通知标题" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="通知类型" prop="noticeType">
        <el-select v-model="form.noticeType" placeholder="请选择" style="width: 100%">
          <el-option :value="1" label="系统通知" />
          <el-option :value="2" label="审批通知" />
          <el-option :value="3" label="预警通知" />
        </el-select>
      </el-form-item>
      <el-form-item label="接收人" prop="receiverIds">
        <el-alert type="info" :closable="false" style="margin-bottom: 8px">
          选择可以接收此通知的用户（可多选）
        </el-alert>
        <el-select
          v-model="form.receiverIds"
          multiple
          filterable
          placeholder="请选择接收人"
          style="width: 100%"
        >
          <el-option
            v-for="user in userList"
            :key="user.userId"
            :value="user.userId"
            :label="`${user.realName} (${user.userName})`"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="通知内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="5"
          placeholder="请输入通知内容"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog.visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>

  <!-- 已读状态详情弹窗 -->
  <el-dialog v-model="detailDialog.visible" title="发送详情" width="560px" destroy-on-close>
    <div v-if="detailDialog.data" class="detail-info">
      <div class="detail-row">
        <span class="detail-label">通知标题</span>
        <span>{{ detailDialog.data.title }}</span>
      </div>
      <div class="detail-row">
        <span class="detail-label">通知内容</span>
        <span>{{ detailDialog.data.content }}</span>
      </div>
      <div class="detail-row">
        <span class="detail-label">发送时间</span>
        <span>{{ detailDialog.data.createTime }}</span>
      </div>
    </div>

    <el-divider />
    <div class="status-summary">
      <el-statistic title="总接收人数" :value="detailDialog.data?.totalCount || 0" />
      <el-statistic title="已读人数" :value="detailDialog.data?.readCount || 0">
        <template #suffix><span style="color: #67c23a; font-size: 13px">人</span></template>
      </el-statistic>
      <el-statistic title="未读人数" :value="detailDialog.data?.unreadCount || 0">
        <template #suffix><span style="color: #f56c6c; font-size: 13px">人</span></template>
      </el-statistic>
    </div>

    <el-divider content-position="left">接收人列表</el-divider>
    <el-table :data="detailDialog.receivers" size="small" border stripe v-loading="detailDialog.loading">
      <el-table-column prop="receiverName" label="姓名" min-width="100" />
      <el-table-column prop="receiverUserName" label="用户名" min-width="100" />
      <el-table-column prop="noticeTime" label="发送时间" width="160" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.isRead === 1 ? 'success' : 'danger'" size="small">
            {{ row.isRead === 1 ? '已读' : '未读' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button @click="detailDialog.visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { formatFullTime as formatDate } from '@/utils/timeFormat'
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchNotificationPage,
  fetchNotificationById,
  addNotification,
  updateNotification,
  deleteNotification,
  fetchReadStatus,
  fetchReceivers,
} from '@/api/notification'
import { fetchUserPage } from '@/api/system'

const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const userList = ref([])

const query = reactive({
  current: 1,
  size: 10,
  keyword: '',
})
const table = reactive({ records: [], total: 0 })

const dialog = reactive({ visible: false, title: '发送通知', isEdit: false })
const form = ref({})
const defaultForm = () => ({
  title: '',
  content: '',
  noticeType: 1,
  receiverIds: [],
})
form.value = defaultForm()

const rules = {
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
  receiverIds: [{ required: true, message: '请选择至少一个接收人', trigger: 'change' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }],
}

// ---------- 详情弹窗 ----------
const detailDialog = reactive({
  visible: false,
  loading: false,
  data: null,
  receivers: [],
})

async function openDetail(row) {
  detailDialog.visible = true
  detailDialog.loading = true
  detailDialog.data = null
  detailDialog.receivers = []
  try {
    const [statusData, receiversData] = await Promise.all([
      fetchReadStatus(row.noticeId),
      fetchReceivers(row.noticeId),
    ])
    detailDialog.data = statusData
    detailDialog.receivers = receiversData || []
  } catch {
    ElMessage.error('加载详情失败')
  } finally {
    detailDialog.loading = false
  }
}

function typeLabel(t) {
  if (t === 1) return '系统通知'
  if (t === 2) return '审批通知'
  if (t === 3) return '预警通知'
  return '—'
}

function formatReceivers(names, count) {
  if (!names) return '—'
  const list = names.split(',')
  if (count <= 3) {
    return list.join('、')
  }
  return list.slice(0, 3).join('、') + ` (+${count - 3})`
}

async function load() {
  loading.value = true
  try {
    const data = await fetchNotificationPage({ ...query })
    table.records = data.records || []
    table.total = data.total || 0
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function loadUserList() {
  try {
    const data = await fetchUserPage({ current: 1, size: 9999 })
    userList.value = data.records || []
  } catch { /* ignore */ }
}

function handleSearch() {
  query.current = 1
  load()
}

function handleReset() {
  query.keyword = ''
  query.current = 1
  load()
}

function openAdd() {
  form.value = defaultForm()
  dialog.title = '发送通知'
  dialog.isEdit = false
  dialog.visible = true
  loadUserList()
}

async function openEdit(row) {
  dialog.title = '编辑通知'
  dialog.isEdit = true
  try {
    const data = await fetchNotificationById(row.noticeId)
    form.value = {
      noticeId: data.noticeId,
      title: data.title,
      content: data.content,
      noticeType: data.noticeType,
      receiverIds: data.receiverId ? [data.receiverId] : [],
    }
  } catch {
    ElMessage.error('加载通知信息失败')
    return
  }
  await loadUserList()
  dialog.visible = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (dialog.isEdit) {
      await updateNotification(form.value)
      ElMessage.success('更新成功')
    } else {
      await addNotification(form.value)
      ElMessage.success('发送成功')
    }
    dialog.visible = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除通知「${row.title}」？`, '提示', { type: 'warning' })
    await deleteNotification(row.noticeId)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

onMounted(load)
</script>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filter {
  margin-bottom: 12px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.detail-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.detail-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  font-size: 14px;
  line-height: 1.6;
}
.detail-label {
  color: #909399;
  flex-shrink: 0;
  width: 70px;
}
.status-summary {
  display: flex;
  gap: 24px;
  justify-content: flex-start;
  padding: 4px 0;
}
</style>
