<template>
  <el-container class="layout">
    <el-aside width="240px" class="aside">
      <div class="sidebar-inner">
        <div class="logo">
          <span class="logo-text">大学生毕设审批一体化平台</span>
        </div>
        <el-scrollbar class="menu-scroll">
          <el-menu :default-active="active" router class="side-menu">
            <el-menu-item index="/dashboard">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <SideMenu :menus="store.menus" />
          </el-menu>
        </el-scrollbar>
        <div class="sidebar-user">
          <div class="sidebar-user-avatar">{{ userInitial }}</div>
          <div class="sidebar-user-meta">
            <div class="sidebar-user-name">{{ store.user?.realName || store.user?.userName || '用户' }}</div>
            <el-button class="sidebar-logout" link type="primary" @click="logout">退出登录</el-button>
          </div>
        </div>
      </div>
    </el-aside>
    <el-container class="main-wrap">
      <el-header class="header">
        <span class="title">{{ currentTitle }}</span>
        <div class="header-right">
          <el-icon class="header-icon"><Search /></el-icon>
          <el-icon class="header-icon"><Bell /></el-icon>
          <div class="header-avatar">{{ userInitial }}</div>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeFilled, Search, Bell } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import SideMenu from './SideMenu.vue'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const active = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '首页')

const userInitial = computed(() => {
  const n = store.user?.realName || store.user?.userName || ''
  return n ? String(n).slice(0, 1) : '用'
})

function logout() {
  store.clear()
  router.push({ name: 'Login' })
}
</script>

<style scoped>
.layout {
  height: 100vh;
  background: var(--tf-bg-app, #f8f9fe);
}

.aside {
  background: var(--tf-bg-sidebar, #f0f1f7);
  overflow: hidden;
  border-right: 1px solid rgba(15, 23, 42, 0.06);
}

.sidebar-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 20px 16px;
  flex-shrink: 0;
}

.logo-mark {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: #fff;
  font-weight: 700;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
}

.logo-text {
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: 0.02em;
  line-height: 1.3;
}

.menu-scroll {
  flex: 1;
  min-height: 0;
  padding: 0 10px;
}

.menu-scroll :deep(.el-scrollbar) {
  height: 100%;
}

.menu-scroll :deep(.el-scrollbar__wrap) {
  overflow-x: hidden;
}

.menu-scroll :deep(.el-scrollbar__view) {
  padding-bottom: 8px;
}

.side-menu {
  border-right: none !important;
  background: transparent !important;
}

.side-menu :deep(.el-menu-item),
.side-menu :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  margin: 4px 0;
  border-radius: 8px;
  color: #6b7280;
  font-weight: 500;
}

.side-menu :deep(.el-menu-item .el-icon),
.side-menu :deep(.el-sub-menu__title .el-icon) {
  color: #9ca3af;
}

.side-menu :deep(.el-menu-item:hover),
.side-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(99, 102, 241, 0.08) !important;
  color: #374151;
}

.side-menu :deep(.el-menu-item:hover .el-icon),
.side-menu :deep(.el-sub-menu__title:hover .el-icon) {
  color: #6366f1;
}

.side-menu :deep(.el-menu-item.is-active) {
  background: #6366f1 !important;
  color: #ffffff !important;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.35);
}

.side-menu :deep(.el-menu-item.is-active .el-icon) {
  color: #ffffff !important;
}

.side-menu :deep(.el-sub-menu .el-menu) {
  background: transparent !important;
}

.side-menu :deep(.el-sub-menu .el-menu-item) {
  min-width: auto;
  padding-left: 48px !important;
}

.sidebar-user {
  flex-shrink: 0;
  margin: 12px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.06);
}

.sidebar-user-avatar {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(145deg, #e0e7ff, #c7d2fe);
  color: #4338ca;
  font-weight: 700;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.sidebar-user-meta {
  min-width: 0;
  flex: 1;
}

.sidebar-user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-logout {
  padding: 0 !important;
  height: auto !important;
  margin-top: 2px;
  font-size: 12px;
  font-weight: 500;
}

.main-wrap {
  background: var(--tf-bg-app, #f8f9fe);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px !important;
  padding: 0 28px !important;
  background: #ffffff;
  border-bottom: 1px solid #f3f4f6;
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.04);
}

.title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: -0.02em;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 20px;
  color: #9ca3af;
  padding: 8px;
  border-radius: 10px;
  cursor: default;
}

.header-icon:hover {
  color: #6366f1;
  background: rgba(99, 102, 241, 0.08);
}

.header-avatar {
  width: 40px;
  height: 40px;
  margin-left: 8px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1, #a5b4fc);
  color: #fff;
  font-weight: 600;
  font-size: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main {
  background: transparent;
  min-height: calc(100vh - 64px);
  padding: 24px !important;
}
</style>
