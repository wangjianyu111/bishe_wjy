<template>
  <el-card shadow="never">
    <template #header>
      <span>选题发布 / 浏览</span>
    </template>
    <el-form :inline="true" class="filter">
      <el-form-item label="学年">
        <el-input v-model="query.academicYear" placeholder="如 2024-2025" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="开放" value="OPEN" />
          <el-option label="关闭" value="CLOSED" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="课题名称" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="table.records" v-loading="loading" border stripe>
      <el-table-column prop="topicId" label="ID" width="70" />
      <el-table-column prop="topicName" label="课题名称" min-width="220" show-overflow-tooltip />
      <el-table-column prop="academicYear" label="学年" width="120" />
      <el-table-column prop="maxStudents" label="名额" width="80" />
      <el-table-column prop="currentCount" label="已选" width="80" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="description" label="简介" min-width="160" show-overflow-tooltip />
    </el-table> 
    <div class="pager">
      <el-pagination
      :current-page="query.current"
       :page-size="query.size"
        :total="table.total"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </div>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { fetchTopicPage } from '@/api/project'

const loading = ref(false)
const query = reactive({
  current: 1,
  size: 10,
  academicYear: '',
  status: '',
  keyword: '',
})
const table = reactive({
  records: [],
  total: 0,
})

async function load() {
  loading.value = true
  try {
    const data = await fetchTopicPage({ ...query })
    table.records = data.records || []
    table.total = data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.filter {
  margin-bottom: 12px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
