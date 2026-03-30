<template>
  <el-card shadow="never">
    <template #header>
      <div class="header-row">
        <span>权限管理</span>
        <el-button type="primary" @click="openAdd">新增权限</el-button>
      </div>
    </template>

    <!-- 搜索栏 -->
    <el-form :inline="true" class="filter">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="名称/权限码" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="query.permType" placeholder="全部" clearable style="width: 140px">
          <el-option label="菜单" :value="1" />
          <el-option label="按钮" :value="2" />
          <el-option label="接口" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table :data="table.records" v-loading="loading" border stripe>
      <el-table-column prop="permId" label="ID" width="70" />
      <el-table-column prop="permName" label="权限名称" min-width="150" />
      <el-table-column prop="permCode" label="权限码" min-width="180">
        <template #default="{ row }">
          <el-tag v-if="row.permCode" type="info" size="small">{{ row.permCode }}</el-tag>
          <span v-else class="text-muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="permType" label="类型" width="90">
        <template #default="{ row }">
          <el-tag :type="permTypeTag(row.permType)" size="small">{{ permTypeLabel(row.permType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路由路径" min-width="150">
        <template #default="{ row }">
          <span v-if="row.path">{{ row.path }}</span>
          <span v-else class="text-muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="component" label="组件路径" min-width="160">
        <template #default="{ row }">
          <span v-if="row.component">{{ row.component }}</span>
          <span v-else class="text-muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
      <el-table-column prop="visible" label="可见" width="70" align="center">
        <template #default="{ row }">
          <el-tag :type="row.visible === 1 ? 'success' : 'info'" size="small">
            {{ row.visible === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
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

  <!-- 新增/编辑弹窗 -->
  <el-dialog v-model="dialog.visible" :title="dialog.title" width="560px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100">
      <el-form-item label="上级权限" prop="parentId">
        <el-tree-select
          v-model="form.parentId"
          :data="treeData"
          :props="{ label: 'permName', value: 'permId', children: 'children', checkStrictly: true }"
          placeholder="选择上级权限（不选为顶级）"
          clearable
          check-strictly
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="权限名称" prop="permName">
        <el-input v-model="form.permName" placeholder="请输入权限名称" />
      </el-form-item>
      <el-form-item label="权限码" prop="permCode">
        <el-input v-model="form.permCode" placeholder="如 sys:user:list" />
      </el-form-item>
      <el-form-item label="权限类型" prop="permType">
        <el-radio-group v-model="form.permType">
          <el-radio :value="1">菜单</el-radio>
          <el-radio :value="2">按钮</el-radio>
          <el-radio :value="3">接口</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="路由路径" prop="path">
        <el-input v-model="form.path" placeholder="如 /system/users" />
      </el-form-item>
      <el-form-item label="组件路径" prop="component">
        <el-input v-model="form.component" placeholder="如 system/user/index.vue" />
      </el-form-item>
      <el-form-item label="图标" prop="icon">
        <el-input v-model="form.icon" placeholder="Element Plus 图标名，如 User" />
      </el-form-item>
      <el-form-item label="排序" prop="sortOrder">
        <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
      </el-form-item>
      <el-form-item label="是否显示" prop="visible">
        <el-radio-group v-model="form.visible">
          <el-radio :value="1">显示</el-radio>
          <el-radio :value="0">隐藏</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog.visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchPermissionPage,
  fetchPermissionTree,
  addPermission,
  updatePermission,
  deletePermission,
} from '@/api/system'

const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)

// ---------- 分页查询 ----------
const query = reactive({
  current: 1,
  size: 10,
  keyword: '',
  permType: null,
})
const table = reactive({ records: [], total: 0 })

// ---------- 树形下拉数据 ----------
const treeData = ref([])

// ---------- 弹窗 ----------
const dialog = reactive({ visible: false, title: '新增权限', isEdit: false })
const form = ref({})
const defaultForm = () => ({
  permId: null,
  parentId: null,
  permName: '',
  permCode: '',
  permType: 1,
  path: '',
  component: '',
  icon: '',
  sortOrder: 0,
  visible: 1,
})
form.value = defaultForm()

const rules = {
  permName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  permType: [{ required: true, message: '请选择权限类型', trigger: 'change' }],
}

// ---------- 工具函数 ----------
function permTypeLabel(t) {
  return { 1: '菜单', 2: '按钮', 3: '接口' }[t] ?? '—'
}
function permTypeTag(t) {
  return { 1: '', 2: 'warning', 3: 'danger' }[t] ?? 'info'
}

// ---------- 数据加载 ----------
async function load() {
  loading.value = true
  try {
    const params = { ...query }
    if (params.permType === '') params.permType = null
    const data = await fetchPermissionPage(params)
    table.records = data.records || []
    table.total = data.total || 0
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function loadTree() {
  treeData.value = await fetchPermissionTree()
}

// ---------- 搜索 ----------
function handleSearch() {
  query.current = 1
  load()
}
function handleReset() {
  query.keyword = ''
  query.permType = null
  query.current = 1
  load()
}

// ---------- 新增 ----------
async function openAdd() {
  form.value = defaultForm()
  dialog.title = '新增权限'
  dialog.isEdit = false
  await loadTree()
  dialog.visible = true
}

// ---------- 编辑 ----------
async function openEdit(row) {
  dialog.title = '编辑权限'
  dialog.isEdit = true
  await loadTree()
  form.value = {
    permId: row.permId,
    parentId: row.parentId === 0 ? null : row.parentId,
    permName: row.permName,
    permCode: row.permCode,
    permType: row.permType,
    path: row.path || '',
    component: row.component || '',
    icon: row.icon || '',
    sortOrder: row.sortOrder || 0,
    visible: row.visible == null ? 1 : row.visible,
  }
  dialog.visible = true
}

// ---------- 提交 ----------
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = { ...form.value }
    if (payload.parentId === null) payload.parentId = 0
    if (dialog.isEdit) {
      await updatePermission(payload)
      ElMessage.success('更新成功')
    } else {
      await addPermission(payload)
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
    await ElMessageBox.confirm(`确定删除权限「${row.permName}」？`, '提示', { type: 'warning' })
    await deletePermission(row.permId)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => {
  load()
  loadTree()
})
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
.text-muted {
  color: #aaa;
  font-size: 12px;
}
</style>
