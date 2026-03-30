<template>
  <el-card shadow="never" class="welcome">
    <div class="welcome-accent" aria-hidden="true" />
    <h2>欢迎使用大学生毕业设计审批一体化平台</h2>
    <p class="role-line">当前角色：{{ (store.user?.roles || []).join('、') || '—' }}</p>
    <p class="hint">请从左侧菜单进入各业务模块。后续可在后端按功能结构图继续扩展接口与页面。</p>
  </el-card>
</template>

<script setup>
import { useUserStore } from '@/stores/user'

const store = useUserStore()
</script>

<style scoped>
.welcome {
  position: relative;
  overflow: hidden;
}

.welcome-accent {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #6366f1, #a5b4fc, #2dd4bf);
  opacity: 0.95;
}

.welcome :deep(.el-card__body) {
  padding-top: 28px !important;
}

.welcome h2 {
  margin: 0 0 16px;
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: -0.02em;
  line-height: 1.35;
}

.role-line {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 500;
  color: #374151;
  line-height: 1.7;
}

.hint {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
  line-height: 1.85;
  max-width: 52rem;
}
</style>
