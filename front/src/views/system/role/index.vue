<template>
  <el-card shadow="never">
    <template #header>
      <div class="header-row">
        <span>角色管理</span>
        <el-button type="primary" @click="openAdd">新增角色</el-button>
      </div>
    </template>

    <!-- 搜索栏 -->
    <el-form :inline="true" class="filter">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="角色名称/编码" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table :data="table.records" v-loading="loading" border stripe>
      <el-table-column prop="roleId" label="ID" width="70" />
      <el-table-column prop="roleName" label="角色名称" min-width="150" />
      <el-table-column prop="roleCode" label="角色编码" min-width="150">
        <template #default="{ row }">
          <el-tag type="info" size="small">{{ row.roleCode }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="success" link size="small" @click="openAssignPerm(row)">分配权限</el-button>
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
  <el-dialog v-model="dialog.visible" :title="dialog.title" width="480px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100">
      <el-form-item label="角色名称" prop="roleName">
        <el-input v-model="form.roleName" placeholder="请输入角色名称" />
      </el-form-item>
      <el-form-item label="角色编码" prop="roleCode">
        <el-input v-model="form.roleCode" placeholder="如 admin / teacher / student" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="可选" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog.visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>

  <!-- 权限分配弹窗 -->
  <el-dialog v-model="permDialog.visible" title="分配权限" width="640px" destroy-on-close>
    <el-alert type="info" :closable="false" style="margin-bottom: 16px">
      为角色「<strong>{{ permDialog.roleName }}</strong>」分配权限，勾选即可授权
    </el-alert>
    <el-scrollbar height="420px">
      <el-tree
        ref="permTreeRef"
        :data="permTree"
        :props="{ label: 'permName', children: 'children' }"
        node-key="permId"
        show-checkbox
        default-expand-all
        check-strictly
      >
        <template #default="{ data }">
          <span class="node-label">
            <span>{{ data.permName }}</span>
            <el-tag type="info" size="small" style="margin-left: 6px">{{ permTypeLabel(data.permType) }}</el-tag>
            <span v-if="data.permCode" class="perm-code">{{ data.permCode }}</span>
          </span>
        </template>
      </el-tree>
    </el-scrollbar>
    <template #footer>
      <el-button @click="permDialog.visible = false">取消</el-button>
      <el-button type="primary" :loading="permSubmitting" @click="handleAssignSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchRolePage,
  fetchRolePermissionTree,
  fetchRoleById,
  addRole,
  updateRole,
  deleteRole,
  assignRolePermissions,
} from '@/api/system'

const loading = ref(false)
const submitting = ref(false)
const permSubmitting = ref(false)
const formRef = ref(null)
const permTreeRef = ref(null)

// ---------- 分页查询 ----------
const query = reactive({
  current: 1,
  size: 10,
  keyword: '',
})
const table = reactive({ records: [], total: 0 })

// ---------- 角色弹窗 ----------
const dialog = reactive({ visible: false, title: '新增角色', isEdit: false })
const form = ref({})
const defaultForm = () => ({
  roleId: null,
  roleName: '',
  roleCode: '',
  remark: '',
})
form.value = defaultForm()
const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-z_]+$/, message: '角色编码只能包含小写字母和下划线', trigger: 'blur' },
  ],
}

// ---------- 权限分配弹窗 ----------
const permDialog = reactive({ visible: false, roleId: null, roleName: '' })
const permTree = ref([])

// ---------- 工具函数 ----------
function permTypeLabel(t) {
  return { 1: '菜单', 2: '按钮', 3: '接口' }[t] ?? '—'
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
    const data = await fetchRolePage({ ...query })
    table.records = data.records || []
    table.total = data.total || 0
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function loadPermTree() {
  permTree.value = await fetchRolePermissionTree()
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
  dialog.title = '新增角色'
  dialog.isEdit = false
  dialog.visible = true
}

// ---------- 编辑 ----------
async function openEdit(row) {
  dialog.title = '编辑角色'
  dialog.isEdit = true
  form.value = {
    roleId: row.roleId,
    roleName: row.roleName,
    roleCode: row.roleCode,
    remark: row.remark || '',
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
      await updateRole(form.value)
      ElMessage.success('更新成功')
    } else {
      await addRole(form.value)
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
    await ElMessageBox.confirm(`确定删除角色「${row.roleName}」？`, '提示', { type: 'warning' })
    await deleteRole(row.roleId)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

// ---------- 分配权限 ----------
async function openAssignPerm(row) {
  permDialog.roleId = row.roleId
  permDialog.roleName = row.roleName
  permDialog.visible = true
  await loadPermTree()
  try {
    const roleData = await fetchRoleById(row.roleId)
    await nextTick()
    permTreeRef.value?.setCheckedKeys(roleData.permIds || [])
  } catch {
    permTreeRef.value?.setCheckedKeys([])
  }
}

async function handleAssignSubmit() {
  const checkedNodes = permTreeRef.value?.getCheckedNodes(false, false) || []
  const permIds = checkedNodes.map(n => n.permId)
  permSubmitting.value = true
  try {
    await assignRolePermissions({ roleId: permDialog.roleId, permIds })
    ElMessage.success('权限分配成功')
    permDialog.visible = false
  } catch (e) {
    ElMessage.error(e.message || '分配失败')
  } finally {
    permSubmitting.value = false
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
.node-label {
  display: flex;
  align-items: center;
  gap: 4px;
}
.perm-code {
  font-size: 11px;
  color: #999;
  margin-left: 4px;
  font-family: monospace;
}
</style>
