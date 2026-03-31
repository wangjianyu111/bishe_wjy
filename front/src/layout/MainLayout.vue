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
          <el-badge :value="store.unreadNoticeCount" :hidden="store.unreadNoticeCount === 0" :max="99">
            <el-icon class="header-icon" @click="openNoticePanel">
              <Bell />
            </el-icon>
          </el-badge>

          <!-- 消息通知下拉面板 -->
          <el-popover
            :visible="noticeVisible"
            placement="bottom-end"
            :width="380"
            trigger="click"
            @update:visible="(v) => (noticeVisible = v)"
          >
            <template #reference>
              <span></span>
            </template>
            <template #default>
              <div class="notice-panel">
                <div class="notice-header">
                  <span class="notice-title">我的消息</span>
                  <span class="notice-count" v-if="store.unreadNoticeCount > 0">
                    未读 {{ store.unreadNoticeCount }} 条
                  </span>
                </div>
                <el-scrollbar class="notice-scroll" v-loading="noticeLoading">
                  <div v-if="noticeList.length === 0" class="notice-empty">
                    <el-icon size="32" color="#dcdfe6"><Bell /></el-icon>
                    <p>暂无消息通知</p>
                  </div>
                  <div
                    v-for="notice in noticeList"
                    :key="notice.noticeId"
                    class="notice-item"
                    @click="openNoticeDetail(notice)"
                  >
                    <div class="notice-item-header">
                      <el-tag type="info" size="small">{{ typeLabel(notice.noticeType) }}</el-tag>
                      <span class="notice-item-time">{{ formatDate(notice.createTime) }}</span>
                    </div>
                    <div class="notice-item-title">{{ notice.title }}</div>
                    <div class="notice-item-content">{{ notice.content }}</div>
                  </div>
                </el-scrollbar>
                <div class="notice-footer" v-if="noticeList.length > 0">
                  <el-button type="primary" link size="small" @click="handleMarkAllRead">全部已读</el-button>
                </div>
              </div>
            </template>
          </el-popover>
          <div class="header-avatar">{{ userInitial }}</div>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>

    <!-- 消息详情弹窗 -->
    <el-dialog v-model="detailVisible" title="消息详情" width="500px" destroy-on-close>
      <div v-if="currentNotice" class="notice-detail">
        <div class="detail-row">
          <span class="detail-label">消息类型</span>
          <el-tag type="info" size="small">{{ typeLabel(currentNotice.noticeType) }}</el-tag>
        </div>
        <div class="detail-row">
          <span class="detail-label">发送时间</span>
          <span>{{ formatDate(currentNotice.createTime) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">发件人</span>
          <span>{{ currentNotice.senderName || '系统' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">消息标题</span>
          <span>{{ currentNotice.title }}</span>
        </div>
        <div class="detail-content">
          <div class="detail-label">消息内容</div>
          <div class="detail-text">{{ currentNotice.content }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeFilled, Search, Bell } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import SideMenu from './SideMenu.vue'
import { fetchUnreadCount, fetchUserNotificationList, markRead, markAllRead } from '@/api/notification'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const active = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '首页')

const userInitial = computed(() => {
  const n = store.user?.realName || store.user?.userName || ''
  return n ? String(n).slice(0, 1) : '用'
})

// ---------- 消息通知 ----------
const noticeVisible = ref(false)
const noticeList = ref([])
const noticeLoading = ref(false)
const detailVisible = ref(false)
const currentNotice = ref(null)

async function loadUnreadCount() {
  if (!store.token) return
  try {
    const count = await fetchUnreadCount()
    store.setUnreadCount(count)
  } catch { /* 静默失败 */ }
}

async function loadNoticeList() {
  noticeLoading.value = true
  try {
    noticeList.value = await fetchUserNotificationList()
  } catch { /* 静默失败 */ }
  finally { noticeLoading.value = false }
}

async function openNoticePanel() {
  noticeVisible.value = !noticeVisible.value
  if (noticeVisible.value) {
    await loadNoticeList()
  }
}

async function openNoticeDetail(notice) {
  currentNotice.value = notice
  detailVisible.value = true
  if (notice.isRead === 0) {
    await markRead(notice.noticeId)
    notice.isRead = 1
    store.decrementUnread()
  }
}

async function handleMarkAllRead() {
  await markAllRead()
  store.setUnreadCount(0)
  await loadNoticeList()
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

function typeLabel(t) {
  if (t === 1) return '系统通知'
  if (t === 2) return '活动通知'
  if (t === 3) return '审核通知'
  return '通知'
}

// ---------- 登出 ----------
function logout() {
  store.clear()
  router.push({ name: 'Login' })
}

onMounted(() => {
  loadUnreadCount()
  const interval = setInterval(loadUnreadCount, 30000)
  // 清理需要在组件卸载时处理，此处简化处理
})
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

/* 消息通知面板 */
.notice-panel {
  margin: -12px;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f3f4f6;
}

.notice-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.notice-count {
  font-size: 12px;
  color: #f56c6c;
  font-weight: 500;
}

.notice-scroll {
  max-height: 400px;
}

.notice-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #909399;
  gap: 8px;
}

.notice-empty p {
  margin: 0;
  font-size: 13px;
}

.notice-item {
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f9fafb;
  transition: background 0.2s;
}

.notice-item:hover {
  background: #f5f7fa;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.notice-item-time {
  font-size: 11px;
  color: #c0c4cc;
}

.notice-item-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notice-item-content {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.notice-footer {
  padding: 10px 16px;
  text-align: center;
  border-top: 1px solid #f3f4f6;
}

.notice-detail {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.detail-label {
  color: #909399;
  flex-shrink: 0;
  width: 70px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.8;
  padding: 10px 12px;
  background: #f9fafb;
  border-radius: 8px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
