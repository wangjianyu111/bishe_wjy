<template>
  <el-card shadow="never">
    <template #header>
      <div class="header-row">
        <span>选题发布</span>
        <div class="header-actions">
          <input
            ref="importInputRef"
            type="file"
            accept=".xlsx,.xls"
            style="display: none"
            @change="handleImportFileChange"
          />
          <el-button type="info" :loading="importing" @click="triggerImport">
            <el-icon><Upload /></el-icon>批量导入
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>批量导出
          </el-button>
          <el-button type="primary" @click="openAdd">
            <el-icon><Plus /></el-icon>新增课题
          </el-button>
        </div>
      </div>
    </template>

    <!-- 搜索栏 -->
    <el-form :inline="true" class="filter">
      <el-form-item label="学年">
        <el-input v-model="query.academicYear" placeholder="如 2024-2025" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="已发布" value="OPEN" />
          <el-option label="已禁用" value="CLOSED" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="课题名称" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table :data="table.records" v-loading="loading" border stripe>
      <el-table-column prop="topicId" label="ID" width="70" />
      <el-table-column prop="topicName" label="课题名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="academicYear" label="学年" width="110" />
      <el-table-column label="名额" width="80" align="center">
        <template #default="{ row }">
          <span :class="{ 'full-slot': row.currentCount >= row.maxStudents }">
            {{ row.currentCount }} / {{ row.maxStudents }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'OPEN' ? 'success' : 'danger'">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="简介" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
          <el-button
            :type="row.status === 'OPEN' ? 'warning' : 'success'"
            link
            size="small"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 'OPEN' ? '禁用' : '启用' }}
          </el-button>
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

  <!-- 新增/编辑弹窗 -->
  <el-dialog v-model="dialog.visible" :title="dialog.title" width="560px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100">
      <el-form-item label="课题名称" prop="topicName">
        <el-input v-model="form.topicName" placeholder="请输入课题名称" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="学年" prop="academicYear">
        <el-input v-model="form.academicYear" placeholder="如 2024-2025" maxlength="20" style="width: 100%" />
      </el-form-item>
      <el-form-item label="可选人数" prop="maxStudents">
        <el-input-number v-model="form.maxStudents" :min="1" :max="20" style="width: 100%" />
      </el-form-item>
      <el-form-item label="简介" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入课题简介（可选）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog.visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  fetchTopicPage,
  fetchTopicById,
  addTopic,
  updateTopic,
  deleteTopic,
  toggleTopicStatus,
  exportTopic,
  importTopic,
} from '@/api/project'
import { Download, Upload, Plus } from '@element-plus/icons-vue'

const store = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const importing = ref(false)
const formRef = ref(null)
const importInputRef = ref(null)

const query = reactive({
  current: 1,
  size: 10,
  academicYear: '',
  status: '',
  keyword: '',
})
const table = reactive({ records: [], total: 0 })

// ---------- 弹窗 ----------
const dialog = reactive({ visible: false, title: '新增课题', isEdit: false })
const form = ref({})
const defaultForm = () => ({
  topicName: '',
  academicYear: '',
  maxStudents: 1,
  description: '',
})
form.value = defaultForm()

const rules = {
  topicName: [{ required: true, message: '请输入课题名称', trigger: 'blur' }],
  academicYear: [{ required: true, message: '请输入学年', trigger: 'blur' }],
  maxStudents: [{ required: true, message: '请输入可选人数', trigger: 'blur' }],
}

// ---------- 工具函数 ----------
function statusLabel(s) {
  if (s === 'OPEN') return '已发布'
  if (s === 'CLOSED') return '已禁用'
  if (s === 'DRAFT') return '草稿'
  return '—'
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  const date = new Date(dateStr)
  const y = date.getFullYear()
  const mo = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const mi = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${mo}-${d} ${h}:${mi}:${s}`
}

// ---------- 数据加载 ----------
async function load() {
  loading.value = true
  try {
    const data = await fetchTopicPage({ ...query })
    table.records = data.records || []
    table.total = data.total || 0
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.current = 1
  load()
}

function handleReset() {
  query.academicYear = ''
  query.status = ''
  query.keyword = ''
  query.current = 1
  load()
}

// ---------- 新增 ----------
function openAdd() {
  form.value = defaultForm()
  dialog.title = '新增课题'
  dialog.isEdit = false
  dialog.visible = true
}

// ---------- 编辑 ----------
async function openEdit(row) {
  dialog.title = '编辑课题'
  dialog.isEdit = true
  try {
    const data = await fetchTopicById(row.topicId)
    form.value = {
      topicId: data.topicId,
      topicName: data.topicName,
      academicYear: data.academicYear,
      maxStudents: data.maxStudents,
      description: data.description || '',
    }
  } catch {
    ElMessage.error('加载课题信息失败')
    return
  }
  dialog.visible = true
}

// ---------- 提交 ----------
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (dialog.isEdit) {
      await updateTopic(form.value)
      ElMessage.success('更新成功')
    } else {
      await addTopic(form.value)
      ElMessage.success('新增成功')
    }
    dialog.visible = false
    load()
  } catch {
    // error already handled by interceptor
  } finally {
    submitting.value = false
  }
}

// ---------- 删除 ----------
async function handleDelete(row) {
  await ElMessageBox.confirm(
    `确定要删除课题「${row.topicName}」吗？删除后不可恢复。`,
    '删除确认',
    { type: 'warning' }
  )
  try {
    await deleteTopic(row.topicId)
    ElMessage.success('删除成功')
    load()
  } catch {
    // error already handled
  }
}

// ---------- 禁用/启用 ----------
async function handleToggleStatus(row) {
  const action = row.status === 'OPEN' ? '禁用' : '启用'
  await ElMessageBox.confirm(
    `确定要${action}课题「${row.topicName}」吗？禁用后学生将无法看到该课题。`,
    `${action}确认`,
    { type: 'warning' }
  )
  try {
    await toggleTopicStatus(row.topicId)
    ElMessage.success(`${action}成功`)
    load()
  } catch {
    // error already handled
  }
}

// ---------- 导出 ----------
async function handleExport() {
  try {
    const blob = await exportTopic({ ...query })
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    const filename = `课题列表_${new Date().getTime()}.xlsx`
    link.setAttribute('download', filename)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

// ---------- 导入 ----------
function triggerImport() {
  importInputRef.value?.click()
}

async function handleImportFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    await importTopic(formData)
    ElMessage.success('导入成功')
    load()
  } catch {
    // error already handled by interceptor
  } finally {
    importing.value = false
    e.target.value = ''
  }
}

// ---------- init ----------
onMounted(() => {
  load()
})
</script>

<style scoped>
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-actions {
  display: flex;
  gap: 8px;
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
.full-slot {
  color: #e6a23c;
  font-weight: 500;
}
</style>
