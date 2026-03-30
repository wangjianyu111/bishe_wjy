<template>
  <el-card shadow="never">
    <template #header>
      <span>选题申请</span>
    </template>
    <el-form :model="form" label-width="100px" style="max-width: 480px">
      <el-form-item label="课题ID" required>
        <el-input-number v-model="form.topicId" :min="1" controls-position="right" style="width: 100%" />
      </el-form-item>
      <el-form-item label="学年" required>
        <el-input v-model="form.academicYear" placeholder="如 2024-2025" />
      </el-form-item>
      <el-form-item label="申请理由">
        <el-input v-model="form.applyReason" type="textarea" rows="3" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="submit">提交申请</el-button>
      </el-form-item>
    </el-form>
    <el-alert type="info" show-icon :closable="false" title="演示环境可先浏览「选题发布」列表获取课题 ID，学号账号 student1 可申请。" />
  </el-card>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { applySelection } from '@/api/project'

const loading = ref(false)
const form = reactive({
  topicId: 1,
  academicYear: '2024-2025',
  applyReason: '',
})

async function submit() {
  if (!form.topicId || !form.academicYear) {
    ElMessage.warning('请填写课题与学年')
    return
  }
  loading.value = true
  try {
    await applySelection({ ...form })
    ElMessage.success('申请已提交')
  } finally {
    loading.value = false
  }
}
</script>
