<template>
  <div class="not-found-page">
    <div class="not-found-card">
      <div class="not-found-code">404</div>
      <h2 class="not-found-title">页面不存在</h2>
      <p class="not-found-desc">抱歉，您访问的页面不存在或已被移除。</p>
      <el-button type="primary" @click="goHome">返回</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push({ name: 'Login' })
}
</script>

<style scoped>
.not-found-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f8f9fe;
}

.not-found-card {
  text-align: center;
  padding: 60px 40px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(15, 23, 42, 0.08);
  max-width: 460px;
  width: 100%;
}

.not-found-code {
  font-size: 80px;
  font-weight: 800;
  color: #e0e7ff;
  line-height: 1;
  margin-bottom: 16px;
}

.not-found-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 12px;
}

.not-found-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 32px;
}
</style>
