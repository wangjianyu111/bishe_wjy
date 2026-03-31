<template>
  <el-card shadow="never">
    <template #header>
      <div class="header-row">
        <span>用户管理</span>
        <el-button type="primary" @click="openAdd">新增用户</el-button>
      </div>
    </template>

    <!-- 搜索栏 -->
    <el-form :inline="true" class="filter">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="用户名/姓名" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table :data="table.records" v-loading="loading" border stripe>
      <el-table-column prop="userId" label="ID" width="70" />
      <el-table-column prop="userName" label="登录名" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="userType" label="类型" width="90">
        <template #default="{ row }">
          {{ typeLabel(row.userType) }}
        </template>
      </el-table-column>
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="teacherNo" label="工号" width="120" />
      <el-table-column prop="userPhone" label="手机" width="130" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="success" link size="small" @click="openAssignRole(row)">分配角色</el-button>
          <el-button
            v-if="row.status === 1"
            type="warning"
            link
            size="small"
            @click="handleToggleStatus(row, 0)"
          >禁用</el-button>
          <el-button
            v-else
            type="success"
            link
            size="small"
            @click="handleToggleStatus(row, 1)"
          >启用</el-button>
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
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90">
      <el-form-item label="登录名" prop="userName">
        <el-input v-model="form.userName" placeholder="请输入登录名" :disabled="dialog.isEdit" />
      </el-form-item>
      <el-form-item v-if="!dialog.isEdit" label="密码" prop="userPassword">
        <el-input v-model="form.userPassword" type="password" placeholder="请输入密码" show-password />
      </el-form-item>
      <el-form-item label="姓名" prop="realName">
        <el-input v-model="form.realName" placeholder="请输入姓名" />
      </el-form-item>
      <el-form-item label="用户类型" prop="userType">
        <el-select v-model="form.userType" placeholder="请选择" style="width: 100%">
          <el-option :value="1" label="学生" />
          <el-option :value="2" label="教师" />
          <el-option :value="3" label="管理员" />
        </el-select>
      </el-form-item>
      <el-form-item label="学号" prop="studentNo">
        <el-input v-model="form.studentNo" placeholder="请输入学号" />
      </el-form-item>
      <el-form-item label="工号" prop="teacherNo">
        <el-input v-model="form.teacherNo" placeholder="请输入工号" />
      </el-form-item>
      <el-form-item label="手机" prop="userPhone">
        <el-input v-model="form.userPhone" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="邮箱" prop="userEmail">
        <el-input v-model="form.userEmail" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">正常</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog.visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>

  <!-- 分配角色弹窗 -->
  <el-dialog v-model="roleDialog.visible" title="分配角色" width="480px" destroy-on-close>
    <el-alert type="info" :closable="false" style="margin-bottom: 16px">
      为用户「<strong>{{ roleDialog.userName }}</strong>」分配角色
    </el-alert>
    <el-checkbox-group v-model="roleDialog.selectedRoles">
      <el-checkbox
        v-for="role in roleList"
        :key="role.roleId"
        :value="role.roleId"
        style="display: block; margin-bottom: 8px"
      >
        {{ role.roleName }}
        <el-tag type="info" size="small" style="margin-left: 6px">{{ role.roleCode }}</el-tag>
      </el-checkbox>
    </el-checkbox-group>
    <template #footer>
      <el-button @click="roleDialog.visible = false">取消</el-button>
      <el-button type="primary" :loading="roleSubmitting" @click="handleAssignRoleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchUserPage,
  fetchUserById,
  addUser,
  updateUser,
  deleteUser,
  assignUserRoles,
  toggleUserStatus,
  fetchRoleList,
} from '@/api/system'

const loading = ref(false)
const submitting = ref(false)
const roleSubmitting = ref(false)
const formRef = ref(null)
const roleList = ref([])

// ---------- 分页查询 ----------
const query = reactive({
  current: 1,
  size: 10,
  keyword: '',
})
const table = reactive({ records: [], total: 0 })

// ---------- 用户弹窗 ----------
const dialog = reactive({ visible: false, title: '新增用户', isEdit: false })
const form = ref({})
const defaultForm = () => ({
  userName: '',
  userPassword: '',
  realName: '',
  userType: null,
  studentNo: '',
  teacherNo: '',
  userPhone: '',
  userEmail: '',
  status: 1,
  roleIds: [],
})
form.value = defaultForm()

const rules = {
  userName: [{ required: true, message: '请输入登录名', trigger: 'blur' }],
  userPassword: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
}

// ---------- 角色分配弹窗 ----------
const roleDialog = reactive({ visible: false, userId: null, userName: '', selectedRoles: [] })

// ---------- 工具函数 ----------
function typeLabel(t) {
  if (t === 1) return '学生'
  if (t === 2) return '教师'
  if (t === 3) return '管理员'
  return '—'
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// ---------- 数据加载 ----------
async function load() {
  loading.value = true
  try {
    const data = await fetchUserPage({ ...query })
    table.records = data.records || []
    table.total = data.total || 0
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function loadRoleList() {
  if (roleList.value.length === 0) {
    roleList.value = await fetchRoleList()
  }
}

// ---------- 搜索 ----------
function handleSearch() {
  query.current = 1
  load()
}
function handleReset() {
  query.keyword = ''
  query.current = 1
  load()
}

// ---------- 新增 ----------
function openAdd() {
  form.value = defaultForm()
  dialog.title = '新增用户'
  dialog.isEdit = false
  dialog.visible = true
}

// ---------- 编辑 ----------
async function openEdit(row) {
  dialog.title = '编辑用户'
  dialog.isEdit = true
  try {
    const data = await fetchUserById(row.userId)
    form.value = {
      userId: data.userId,
      userName: data.userName,
      userPassword: '',
      realName: data.realName || '',
      userType: data.userType,
      studentNo: data.studentNo || '',
      teacherNo: data.teacherNo || '',
      userPhone: data.userPhone || '',
      userEmail: data.userEmail || '',
      status: data.status,
      roleIds: data.roleIds || [],
    }
  } catch {
    ElMessage.error('加载用户信息失败')
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
      await updateUser(form.value)
      ElMessage.success('更新成功')
    } else {
      await addUser(form.value)
      ElMessage.success('新增成功')
    }
    dialog.visible = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// ---------- 删除 ----------
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.userName}」？`, '提示', { type: 'warning' })
    await deleteUser(row.userId)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

// ---------- 切换状态 ----------
async function handleToggleStatus(row, status) {
  try {
    await ElMessageBox.confirm(
      `确定${status === 1 ? '启用' : '禁用'}用户「${row.userName}」？`,
      '提示',
      { type: 'warning' }
    )
    await toggleUserStatus(row.userId, status)
    ElMessage.success('操作成功')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

// ---------- 分配角色 ----------
async function openAssignRole(row) {
  roleDialog.userId = row.userId
  roleDialog.userName = row.userName
  roleDialog.selectedRoles = []
  roleDialog.visible = true
  await loadRoleList()
  try {
    const data = await fetchUserById(row.userId)
    roleDialog.selectedRoles = data.roleIds || []
  } catch {
    roleDialog.selectedRoles = []
  }
}

async function handleAssignRoleSubmit() {
  roleSubmitting.value = true
  try {
    await assignUserRoles({
      userId: roleDialog.userId,
      roleIds: roleDialog.selectedRoles,
    })
    ElMessage.success('角色分配成功')
    roleDialog.visible = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '分配失败')
  } finally {
    roleSubmitting.value = false
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
</style>
