<template>
  <div class="param-page">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <span>系统参数管理</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- 学校管理 -->
        <el-tab-pane label="学校管理" name="school">
          <el-form :inline="true" class="filter-bar">
            <el-form-item label="状态">
              <el-select v-model="schoolQuery.status" placeholder="全部" clearable style="width:120px" @change="loadSchools">
                <el-option :value="1" label="启用" />
                <el-option :value="0" label="禁用" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadSchools">查询</el-button>
              <el-button @click="schoolQuery = {}; loadSchools()">重置</el-button>
            </el-form-item>
          </el-form>

          <div class="toolbar-row">
            <el-button type="primary" @click="openAddDialog('school')">
              <el-icon><Plus /></el-icon>新增学校
            </el-button>
          </div>

          <el-table :data="schoolData" v-loading="schoolLoading" border stripe size="small">
            <el-table-column prop="schoolId" label="ID" width="80" />
            <el-table-column prop="campusName" label="学校名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="campusCode" label="学校代码" width="150" />
            <el-table-column prop="statusLabel" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-switch v-model="row.status" :active-value="1" :inactive-value="0"
                  @change="handleInlineToggle(row, 'school')" />
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditDialog('school', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('school', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination background layout="total, prev, pager, next"
              :total="schoolTotal" :current-page="schoolPage" :page-size="schoolSize"
              @current-change="loadSchools" />
          </div>
        </el-tab-pane>

        <!-- 毕业设计阶段 -->
        <el-tab-pane label="毕业设计阶段" name="designStage">
          <el-form :inline="true" class="filter-bar">
            <el-form-item label="所属学校">
              <el-select v-model="stageQuery.schoolId" placeholder="全部学校" clearable style="width:180px" @change="loadStages">
                <el-option v-for="s in schoolList" :key="s.schoolId" :value="s.schoolId" :label="s.campusName" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="stageQuery.status" placeholder="全部" clearable style="width:120px" @change="loadStages">
                <el-option :value="1" label="启用" />
                <el-option :value="0" label="禁用" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadStages">查询</el-button>
              <el-button @click="stageQuery = {}; loadStages()">重置</el-button>
            </el-form-item>
          </el-form>

          <div class="toolbar-row">
            <el-button type="primary" @click="openAddDialog('designStage')">
              <el-icon><Plus /></el-icon>新增阶段
            </el-button>
          </div>

          <el-table :data="stageData" v-loading="stageLoading" border stripe size="small">
            <el-table-column prop="stageId" label="ID" width="80" />
            <el-table-column prop="campusName" label="所属学校" width="180" />
            <el-table-column prop="stageName" label="阶段名称" min-width="180" />
            <el-table-column prop="stageOrder" label="排序" width="80" align="center" />
            <el-table-column prop="statusLabel" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-switch v-model="row.status" :active-value="1" :inactive-value="0"
                  @change="handleInlineToggle(row, 'designStage')" />
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditDialog('designStage', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('designStage', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination background layout="total, prev, pager, next"
              :total="stageTotal" :current-page="stagePage" :page-size="stageSize"
              @current-change="loadStages" />
          </div>
        </el-tab-pane>

        <!-- 年级管理 -->
        <el-tab-pane label="年级管理" name="grade">
          <el-form :inline="true" class="filter-bar">
            <el-form-item label="所属学校">
              <el-select v-model="gradeQuery.schoolId" placeholder="全部学校" clearable style="width:180px" @change="loadGrades">
                <el-option v-for="s in schoolList" :key="s.schoolId" :value="s.schoolId" :label="s.campusName" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="gradeQuery.status" placeholder="全部" clearable style="width:120px" @change="loadGrades">
                <el-option :value="1" label="启用" />
                <el-option :value="0" label="禁用" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadGrades">查询</el-button>
              <el-button @click="gradeQuery = {}; loadGrades()">重置</el-button>
            </el-form-item>
          </el-form>

          <div class="toolbar-row">
            <el-button type="primary" @click="openAddDialog('grade')">
              <el-icon><Plus /></el-icon>新增年级
            </el-button>
          </div>

          <el-table :data="gradeData" v-loading="gradeLoading" border stripe size="small">
            <el-table-column prop="gradeId" label="ID" width="80" />
            <el-table-column prop="campusName" label="所属学校" width="180" />
            <el-table-column prop="gradeName" label="年级名称" min-width="150" />
            <el-table-column prop="gradeYear" label="入学年份" width="120" align="center" />
            <el-table-column prop="statusLabel" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-switch v-model="row.status" :active-value="1" :inactive-value="0"
                  @change="handleInlineToggle(row, 'grade')" />
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditDialog('grade', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('grade', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination background layout="total, prev, pager, next"
              :total="gradeTotal" :current-page="gradePage" :page-size="gradeSize"
              @current-change="loadGrades" />
          </div>
        </el-tab-pane>

        <!-- 时间段设置 -->
        <el-tab-pane label="时间段设置" name="timeSlot">
          <el-form :inline="true" class="filter-bar">
            <el-form-item label="所属学校">
              <el-select v-model="slotQuery.schoolId" placeholder="全部学校" clearable style="width:180px" @change="loadTimeSlots">
                <el-option v-for="s in schoolList" :key="s.schoolId" :value="s.schoolId" :label="s.campusName" />
              </el-select>
            </el-form-item>
            <el-form-item label="类型">
              <el-select v-model="slotQuery.type" placeholder="全部" clearable style="width:160px" @change="loadTimeSlots">
                <el-option value="THESIS_SUBMISSION" label="论文提交" />
                <el-option value="DEFENSE" label="答辩" />
                <el-option value="SCORE" label="成绩评定" />
                <el-option value="ARCHIVE" label="归档" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="slotQuery.status" placeholder="全部" clearable style="width:120px" @change="loadTimeSlots">
                <el-option :value="1" label="启用" />
                <el-option :value="0" label="禁用" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadTimeSlots">查询</el-button>
              <el-button @click="slotQuery = {}; loadTimeSlots()">重置</el-button>
            </el-form-item>
          </el-form>

          <div class="toolbar-row">
            <el-button type="primary" @click="openAddDialog('timeSlot')">
              <el-icon><Plus /></el-icon>新增时间段
            </el-button>
          </div>

          <el-table :data="slotData" v-loading="slotLoading" border stripe size="small">
            <el-table-column prop="slotId" label="ID" width="80" />
            <el-table-column prop="campusName" label="所属学校" width="180" />
            <el-table-column prop="slotName" label="时间段名称" min-width="160" />
            <el-table-column prop="slotTypeLabel" label="类型" width="120" align="center" />
            <el-table-column prop="startDate" label="开始日期" width="130" align="center" />
            <el-table-column prop="endDate" label="结束日期" width="130" align="center" />
            <el-table-column prop="slotOrder" label="排序" width="80" align="center" />
            <el-table-column prop="statusLabel" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.statusLabel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditDialog('timeSlot', row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete('timeSlot', row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination background layout="total, prev, pager, next"
              :total="slotTotal" :current-page="slotPage" :page-size="slotSize"
              @current-change="loadTimeSlots" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 新增/编辑对话框 - 学校 -->
    <el-dialog v-model="schoolDialogVisible" :title="schoolEditMode === 'add' ? '新增学校' : '编辑学校'" width="480px" destroy-on-close>
      <el-form ref="schoolFormRef" :model="schoolForm" :rules="schoolRules" label-width="100px">
        <el-form-item prop="name" label="学校名称">
          <el-input v-model="schoolForm.name" placeholder="请输入学校名称" />
        </el-form-item>
        <el-form-item prop="code" label="学校代码">
          <el-input v-model="schoolForm.code" placeholder="请输入学校代码" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="schoolForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="schoolDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="confirmSchoolDialog">确认</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑对话框 - 毕业设计阶段 -->
    <el-dialog v-model="stageDialogVisible" :title="stageEditMode === 'add' ? '新增阶段' : '编辑阶段'" width="500px" destroy-on-close>
      <el-form ref="stageFormRef" :model="stageForm" :rules="stageRules" label-width="100px">
        <el-form-item prop="schoolId" label="所属学校">
          <el-select v-model="stageForm.schoolId" placeholder="请选择学校" style="width:100%">
            <el-option v-for="s in schoolList" :key="s.schoolId" :value="s.schoolId" :label="s.campusName" />
          </el-select>
        </el-form-item>
        <el-form-item prop="name" label="阶段名称">
          <el-input v-model="stageForm.name" placeholder="如：选题阶段、开题阶段、论文阶段..." />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="stageForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="stageForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stageDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="confirmStageDialog">确认</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑对话框 - 年级 -->
    <el-dialog v-model="gradeDialogVisible" :title="gradeEditMode === 'add' ? '新增年级' : '编辑年级'" width="500px" destroy-on-close>
      <el-form ref="gradeFormRef" :model="gradeForm" :rules="gradeRules" label-width="100px">
        <el-form-item prop="schoolId" label="所属学校">
          <el-select v-model="gradeForm.schoolId" placeholder="请选择学校" style="width:100%">
            <el-option v-for="s in schoolList" :key="s.schoolId" :value="s.schoolId" :label="s.campusName" />
          </el-select>
        </el-form-item>
        <el-form-item prop="name" label="年级名称">
          <el-input v-model="gradeForm.name" placeholder="如：2022级" />
        </el-form-item>
        <el-form-item prop="code" label="入学年份">
          <el-input-number v-model="gradeForm.code" :min="2000" :max="2099" placeholder="如：2022" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="gradeForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="gradeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="confirmGradeDialog">确认</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑对话框 - 时间段 -->
    <el-dialog v-model="slotDialogVisible" :title="slotEditMode === 'add' ? '新增时间段' : '编辑时间段'" width="520px" destroy-on-close>
      <el-form ref="slotFormRef" :model="slotForm" :rules="slotRules" label-width="100px">
        <el-form-item prop="schoolId" label="所属学校">
          <el-select v-model="slotForm.schoolId" placeholder="请选择学校" style="width:100%">
            <el-option v-for="s in schoolList" :key="s.schoolId" :value="s.schoolId" :label="s.campusName" />
          </el-select>
        </el-form-item>
        <el-form-item prop="slotName" label="时间段名称">
          <el-input v-model="slotForm.slotName" placeholder="如：2024年春季论文提交期" />
        </el-form-item>
        <el-form-item prop="slotType" label="类型">
          <el-select v-model="slotForm.slotType" style="width:100%">
            <el-option value="THESIS_SUBMISSION" label="论文提交" />
            <el-option value="DEFENSE" label="答辩" />
            <el-option value="SCORE" label="成绩评定" />
            <el-option value="ARCHIVE" label="归档" />
          </el-select>
        </el-form-item>
        <el-form-item prop="startDate" label="开始日期">
          <el-date-picker v-model="slotForm.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%"
            placeholder="选择开始日期" />
        </el-form-item>
        <el-form-item prop="endDate" label="结束日期">
          <el-date-picker v-model="slotForm.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%"
            placeholder="选择结束日期" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="slotForm.slotOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="slotForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="slotDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="confirmSlotDialog">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { formatDateTime as formatTime } from '@/utils/timeFormat'
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  fetchSchoolPage, addSchool, updateSchool, deleteSchool,
  fetchDesignStagePage, addDesignStage, updateDesignStage, deleteDesignStage,
  fetchGradePage, addGrade, updateGrade, deleteGrade,
  fetchTimeSlotPage, addTimeSlot, updateTimeSlot, deleteTimeSlot,
} from '@/api/system'

const activeTab = ref('school')
const schoolList = ref([])

// ==================== 学校 ====================
const schoolQuery = reactive({})
const schoolData = ref([])
const schoolLoading = ref(false)
const schoolPage = ref(1)
const schoolSize = ref(20)
const schoolTotal = ref(0)
const schoolDialogVisible = ref(false)
const schoolEditMode = ref('add')
const schoolFormRef = ref()
const schoolForm = ref({ name: '', code: '', status: 1 })
const schoolRules = {
  name: [{ required: true, message: '请输入学校名称', trigger: 'blur' }],
}

async function loadSchoolOptions() {
  try {
    const res = await fetchSchoolPage({ current: 1, size: 999, status: 1 })
    if (res) schoolList.value = res.records || []
  } catch {}
}

async function loadSchools(page) {
  schoolLoading.value = true
  try {
    const res = await fetchSchoolPage({ current: page || schoolPage.value, size: schoolSize.value, ...schoolQuery })
    if (res) {
      schoolData.value = res.records || []
      schoolTotal.value = res.total || 0
      schoolPage.value = res.current || 1
    }
  } catch {} finally {
    schoolLoading.value = false
  }
}

function openAddDialog(type) {
  if (type === 'school') {
    schoolEditMode.value = 'add'
    schoolForm.value = { name: '', code: '', status: 1 }
    schoolDialogVisible.value = true
  } else if (type === 'designStage') {
    stageEditMode.value = 'add'
    stageForm.value = { schoolId: null, name: '', sortOrder: 0, status: 1 }
    stageDialogVisible.value = true
  } else if (type === 'grade') {
    gradeEditMode.value = 'add'
    gradeForm.value = { schoolId: null, name: '', code: null, status: 1 }
    gradeDialogVisible.value = true
  } else if (type === 'timeSlot') {
    slotEditMode.value = 'add'
    slotForm.value = { schoolId: null, slotName: '', slotType: '', startDate: '', endDate: '', slotOrder: 0, status: 1 }
    slotDialogVisible.value = true
  }
}

function openEditDialog(type, row) {
  if (type === 'school') {
    schoolEditMode.value = 'edit'
    schoolForm.value = { id: row.schoolId, name: row.campusName, code: row.campusCode, status: row.status }
    schoolDialogVisible.value = true
  } else if (type === 'designStage') {
    stageEditMode.value = 'edit'
    stageForm.value = { id: row.stageId, schoolId: row.schoolId, name: row.stageName, sortOrder: row.stageOrder, status: row.status }
    stageDialogVisible.value = true
  } else if (type === 'grade') {
    gradeEditMode.value = 'edit'
    gradeForm.value = { id: row.gradeId, schoolId: row.schoolId, name: row.gradeName, code: row.gradeYear, status: row.status }
    gradeDialogVisible.value = true
  } else if (type === 'timeSlot') {
    slotEditMode.value = 'edit'
    slotForm.value = {
      slotId: row.slotId, schoolId: row.schoolId, slotName: row.slotName, slotType: row.slotType,
      startDate: row.startDate, endDate: row.endDate, slotOrder: row.slotOrder, status: row.status,
    }
    slotDialogVisible.value = true
  }
}

async function confirmSchoolDialog() {
  const valid = await schoolFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (schoolEditMode.value === 'add') {
      await addSchool({ name: schoolForm.value.name, code: schoolForm.value.code, type: 'SCHOOL', status: schoolForm.value.status })
    } else {
      await updateSchool({ id: schoolForm.value.id, name: schoolForm.value.name, code: schoolForm.value.code, status: schoolForm.value.status })
    }
    ElMessage.success('操作成功')
    schoolDialogVisible.value = false
    loadSchools()
    loadSchoolOptions()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleInlineToggle(row, type) {
  try {
    if (type === 'school') await updateSchool({ id: row.schoolId, name: row.campusName, code: row.campusCode, status: row.status })
    else if (type === 'designStage') await updateDesignStage({ id: row.stageId, schoolId: row.schoolId, name: row.stageName, sortOrder: row.stageOrder, status: row.status })
    else if (type === 'grade') await updateGrade({ id: row.gradeId, schoolId: row.schoolId, name: row.gradeName, code: row.gradeYear, status: row.status })
    ElMessage.success('状态更新成功')
  } catch (e) {
    ElMessage.error(e.message || '更新失败')
    if (type === 'school') loadSchools()
    else if (type === 'designStage') loadStages()
    else if (type === 'grade') loadGrades()
  }
}

async function handleDelete(type, row) {
  await ElMessageBox.confirm('确认删除该记录？', '警告', { type: 'warning' })
  try {
    if (type === 'school') { await deleteSchool(row.schoolId); loadSchools(); loadSchoolOptions() }
    else if (type === 'designStage') { await deleteDesignStage(row.stageId); loadStages() }
    else if (type === 'grade') { await deleteGrade(row.gradeId); loadGrades() }
    else if (type === 'timeSlot') { await deleteTimeSlot(row.slotId); loadTimeSlots() }
    ElMessage.success('删除成功')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

// ==================== 毕业设计阶段 ====================
const stageQuery = reactive({})
const stageData = ref([])
const stageLoading = ref(false)
const stagePage = ref(1)
const stageSize = ref(20)
const stageTotal = ref(0)
const stageDialogVisible = ref(false)
const stageEditMode = ref('add')
const stageFormRef = ref()
const stageForm = ref({ schoolId: null, name: '', sortOrder: 0, status: 1 })
const stageRules = {
  schoolId: [{ required: true, message: '请选择学校', trigger: 'change' }],
  name: [{ required: true, message: '请输入阶段名称', trigger: 'blur' }],
}

async function loadStages(page) {
  stageLoading.value = true
  try {
    const res = await fetchDesignStagePage({ current: page || stagePage.value, size: stageSize.value, ...stageQuery })
    if (res) {
      stageData.value = res.records || []
      stageTotal.value = res.total || 0
      stagePage.value = res.current || 1
    }
  } catch {} finally {
    stageLoading.value = false
  }
}

async function confirmStageDialog() {
  const valid = await stageFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (stageEditMode.value === 'add') {
      await addDesignStage({ name: stageForm.value.name, schoolId: stageForm.value.schoolId, sortOrder: stageForm.value.sortOrder, type: 'DESIGN_STAGE', status: stageForm.value.status })
    } else {
      await updateDesignStage({ id: stageForm.value.id, schoolId: stageForm.value.schoolId, name: stageForm.value.name, sortOrder: stageForm.value.sortOrder, status: stageForm.value.status })
    }
    ElMessage.success('操作成功')
    stageDialogVisible.value = false
    loadStages()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// ==================== 年级 ====================
const gradeQuery = reactive({})
const gradeData = ref([])
const gradeLoading = ref(false)
const gradePage = ref(1)
const gradeSize = ref(20)
const gradeTotal = ref(0)
const gradeDialogVisible = ref(false)
const gradeEditMode = ref('add')
const gradeFormRef = ref()
const gradeForm = ref({ schoolId: null, name: '', code: null, status: 1 })
const gradeRules = {
  schoolId: [{ required: true, message: '请选择学校', trigger: 'change' }],
  name: [{ required: true, message: '请输入年级名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入入学年份', trigger: 'blur' }],
}

async function loadGrades(page) {
  gradeLoading.value = true
  try {
    const res = await fetchGradePage({ current: page || gradePage.value, size: gradeSize.value, ...gradeQuery })
    if (res) {
      gradeData.value = res.records || []
      gradeTotal.value = res.total || 0
      gradePage.value = res.current || 1
    }
  } catch {} finally {
    gradeLoading.value = false
  }
}

async function confirmGradeDialog() {
  const valid = await gradeFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (gradeEditMode.value === 'add') {
      await addGrade({ name: gradeForm.value.name, schoolId: gradeForm.value.schoolId, code: String(gradeForm.value.code), type: 'GRADE', status: gradeForm.value.status })
    } else {
      await updateGrade({ id: gradeForm.value.id, schoolId: gradeForm.value.schoolId, name: gradeForm.value.name, code: String(gradeForm.value.code), status: gradeForm.value.status })
    }
    ElMessage.success('操作成功')
    gradeDialogVisible.value = false
    loadGrades()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// ==================== 时间段 ====================
const slotQuery = reactive({})
const slotData = ref([])
const slotLoading = ref(false)
const slotPage = ref(1)
const slotSize = ref(20)
const slotTotal = ref(0)
const slotDialogVisible = ref(false)
const slotEditMode = ref('add')
const slotFormRef = ref()
const slotForm = ref({ schoolId: null, slotName: '', slotType: '', startDate: '', endDate: '', slotOrder: 0, status: 1 })
const slotRules = {
  schoolId: [{ required: true, message: '请选择学校', trigger: 'change' }],
  slotName: [{ required: true, message: '请输入时间段名称', trigger: 'blur' }],
  slotType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
}

async function loadTimeSlots(page) {
  slotLoading.value = true
  try {
    const params = { current: page || slotPage.value, size: slotSize.value, ...slotQuery }
    if (params.type === '') delete params.type
    const res = await fetchTimeSlotPage(params)
    if (res) {
      slotData.value = res.records || []
      slotTotal.value = res.total || 0
      slotPage.value = res.current || 1
    }
  } catch {} finally {
    slotLoading.value = false
  }
}

async function confirmSlotDialog() {
  const valid = await slotFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (slotEditMode.value === 'add') {
      await addTimeSlot(slotForm.value)
    } else {
      await updateTimeSlot({ slotId: slotForm.value.slotId, ...slotForm.value })
    }
    ElMessage.success('操作成功')
    slotDialogVisible.value = false
    loadTimeSlots()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

const submitLoading = ref(false)

function onTabChange(tab) {
  if (tab === 'school') loadSchools()
  else if (tab === 'designStage') loadStages()
  else if (tab === 'grade') loadGrades()
  else if (tab === 'timeSlot') loadTimeSlots()
}

onMounted(() => {
  loadSchoolOptions()
  loadSchools()
})
</script>

<style scoped>
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.filter-bar {
  margin-bottom: 12px;
}
.toolbar-row {
  margin-bottom: 12px;
}
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
