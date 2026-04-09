<template>
  <div class="empty-home">
    <div class="empty-home-card">
      <div class="welcome-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <h2 class="welcome-title">欢迎使用，大学生毕设审批一体化平台</h2>
      <p class="welcome-name">{{ store.user?.realName || store.user?.userName || '用户' }}</p>
      <div class="welcome-actions">
        <el-button type="primary" @click="goProfile">完善个人信息</el-button>
        <el-button @click="logout">退出登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const store = useUserStore()

function goProfile() {
  router.push({ name: 'Profile' })
}

function logout() {
  store.clear()
  router.push({ name: 'Login' })
}
</script>

<style scoped>
.empty-home {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 60px);
  background: #f8f9fe;
  padding: 24px;
}

.empty-home-card {
  text-align: center;
  padding: 60px 48px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(15, 23, 42, 0.08);
  max-width: 500px;
  width: 100%;
}

.welcome-icon {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #6366f1, #818cf8);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 28px;
}

.welcome-icon svg {
  width: 36px;
  height: 36px;
  color: #ffffff;
}

.welcome-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px;
  line-height: 1.4;
}

.welcome-name {
  font-size: 15px;
  color: #6366f1;
  font-weight: 600;
  margin: 0 0 16px;
}

.welcome-tip {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.7;
  margin: 0 0 36px;
}

.welcome-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>
