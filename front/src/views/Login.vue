<template>
  <div class="login-page">
    <!-- 左侧登录区域 -->
    <div class="login-left">
      <!-- Logo -->
      <div class="logo">
        <svg class="logo-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span class="logo-text">大学生毕设审批一体化平台</span>
      </div>

      <!-- 登录表单区域 -->
      <div class="form-container">
        <div class="form-header">
          <h1>登录</h1>
          <p class="subtitle">登录您的账号以继续使用</p>
        </div>

        <!-- 登录方式切换 -->
        <div class="login-method-toggle">
          <button 
            :class="['toggle-btn', { active: !isEmailLogin }]" 
            type="button"
            @click="switchLoginMethod"
          >
            账号密码登录
          </button>
          <button 
            :class="['toggle-btn', { active: isEmailLogin }]" 
            type="button"
            @click="switchLoginMethod"
          >
            邮箱登录
          </button>
        </div>

        <!-- 邮箱登录表单 -->
        <div v-if="isEmailLogin" class="email-login-form">
          <el-form :model="emailForm" :rules="emailRules" ref="emailFormRef" @submit.prevent class="login-form">
            <div class="form-label">邮箱</div>
            <el-form-item prop="email">
              <el-input v-model="emailForm.email" placeholder="请输入邮箱地址" clearable>
                <template #prefix>
                  <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M4 4H20C21.1 4 22 4.9 22 6V18C22 19.1 21.1 20 20 20H4C2.9 20 2 19.1 2 18V6C2 4.9 2.9 4 4 4Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M22 6L12 13L2 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </template>
              </el-input>
            </el-form-item>

            <div class="form-label">验证码</div>
            <el-form-item prop="verificationCode">
              <div class="verification-code-wrapper">
                <el-input 
                  v-model="emailForm.verificationCode" 
                  type="text" 
                  placeholder="请输入验证码" 
                  maxlength="6"
                  clearable
                >
                  <template #prefix>
                    <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M12 2C6.477 2 2 6.477 2 12s4.477 10 10 10 10-4.477 10-10S17.523 2 12 2zm0 18c-4.411 0-8-3.589-8-8s3.589-8 8-8 8 3.589 8 8-3.589 8-8 8zm-1-13h2v6h-2V7zm0 8h2v2h-2v-2z" fill="currentColor"/>
                    </svg>
                  </template>
                </el-input>
                <el-button
                  type="primary"
                  :disabled="countdown > 0"
                  @click="sendCode"
                  class="send-code-btn"
                >
                  {{ countdown > 0 ? `${countdown}秒后重发` : '发送验证码' }}
                </el-button>
              </div>
            </el-form-item>

            <el-button type="primary" class="btn" :loading="emailLoading" @click="onSubmit">邮箱登录</el-button>
          </el-form>
        </div>

        <!-- 用户名密码登录表单 -->
        <div v-else class="username-password-form">
          <!-- 分隔线 -->
          <div class="divider">
            <span>使用账号密码登录</span>
          </div>

          <!-- 登录表单 -->
          <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent class="login-form">
            <div class="form-label">用户名</div>
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" clearable>
                <template #prefix>
                  <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </template>
              </el-input>
            </el-form-item>

            <div class="form-label">密码</div>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password clearable>
                <template #prefix>
                  <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </template>
              </el-input>
            </el-form-item>

            <!-- 记住我和忘记密码 -->
            <div class="form-options">
              <label class="remember-me">
                <input type="checkbox" />
                <span>记住我</span>
              </label>
              <a href="#" class="forgot-password">忘记密码</a>
            </div>

            <el-button type="primary" class="btn" :loading="loading" @click="onSubmit">登录</el-button>
          </el-form>
        </div>

        <!-- 注册链接 -->
        <p class="signup-link">还没有账号？<router-link to="/register" class="signup-link-text">立即注册</router-link></p>
      </div>
    </div>

    <!-- 右侧宣传区域 -->
    <div class="login-right">
      <div class="right-content">
        <div class="brand-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h2 class="promo-title">高效管理<br/>您的毕业设计</h2>
        
        <!-- 装饰性卡片 -->
        <div class="promo-cards">
          <div class="promo-card card-1">
            <div class="card-header">
              <span class="card-dot"></span>
              <span class="card-dot"></span>
              <span class="card-dot"></span>
            </div>
            <div class="card-content">
              <div class="card-bar bar-1"></div>
              <div class="card-bar bar-2"></div>
              <div class="card-bar bar-3"></div>
            </div>
          </div>
          <div class="promo-card card-2">
            <div class="card-header">
              <span class="card-dot"></span>
              <span class="card-dot"></span>
              <span class="card-dot"></span>
            </div>
            <div class="card-chart">
              <div class="chart-bar"></div>
              <div class="chart-bar"></div>
              <div class="chart-bar"></div>
              <div class="chart-bar"></div>
            </div>
          </div>
        </div>

        <p class="promo-desc">大学生毕业设计审批一体化平台，让选题、开题、中期检查、答辩等流程更加便捷高效。</p>
      </div>

      <!-- 波浪背景 -->
      <div class="wave-bg"></div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElButton } from 'element-plus'
import { login, sendVerificationCode, emailLogin } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const store = useUserStore()
const formRef = ref()
const loading = ref(false)
const emailFormRef = ref()
const emailLoading = ref(false)
const isEmailLogin = ref(false) // 控制显示邮箱登录还是用户名密码登录
const countdown = ref(0)
const countdownTimer = ref(null)

// 用户名密码登录表单
const form = reactive({
  username: '',
  password: '',
})

// 邮箱登录表单
const emailForm = reactive({
  email: '',
  verificationCode: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ]
}

async function onSubmit() {
  if (isEmailLogin.value) {
    // 邮箱登录
    const valid = await emailFormRef.value?.validate().then(() => true).catch(() => false)
    if (!valid) return
    emailLoading.value = true
    try {
      const data = await emailLogin({
        email: emailForm.email,
        verificationCode: emailForm.verificationCode
      })
      store.setSession({
        token: data.token,
        user: data.user,
        menus: data.menus,
        permissions: data.permissions,
      })
      store.dynamicRoutesAdded = false
      ElMessage.success('登录成功')
      router.replace('/dashboard')
    } finally {
      emailLoading.value = false
    }
  } else {
    // 用户名密码登录
    const valid = await formRef.value?.validate().then(() => true).catch(() => false)
    if (!valid) return
    loading.value = true
    try {
      const data = await login({ username: form.username, password: form.password })
      store.setSession({
        token: data.token,
        user: data.user,
        menus: data.menus,
        permissions: data.permissions,
      })
      store.dynamicRoutesAdded = false
      ElMessage.success('登录成功')
      router.replace('/dashboard')
    } finally {
      loading.value = false
    }
  }
}

// 发送验证码
async function sendCode() {
  const valid = await emailFormRef.value?.validateField(['email']).then(() => true).catch(() => false)
  if (!valid) return

  try {
    await sendVerificationCode({ email: emailForm.email })
    ElMessage.success('验证码已发送，请注意查收')
    
    // 开始倒计时
    countdown.value = 60
    countdownTimer.value = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer.value)
        countdownTimer.value = null
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '发送验证码失败')
  }
}

// 切换登录方式
function switchLoginMethod() {
  isEmailLogin.value = !isEmailLogin.value
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background: #f8f9fe;
}

/* 左侧登录区域 */
.login-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 2rem 3rem;
  max-width: 50%;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  margin-bottom: 2rem;
}

.logo-icon {
  width: 2rem;
  height: 2rem;
  color: #6366f1;
}

.logo-text {
  font-size: 1.125rem;
  font-weight: 700;
  color: #6366f1;
}

.form-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  max-width: 400px;
  margin: 0 auto;
  width: 100%;
}

.form-header {
  margin-bottom: 2rem;
}

.form-header h1 {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem;
}

.subtitle {
  font-size: 0.9375rem;
  color: #6b7280;
  margin: 0;
}

/* 第三方登录按钮 */
.social-login {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.social-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  width: 100%;
  padding: 0.875rem 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  background: #ffffff;
  font-size: 0.9375rem;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s ease;
}

.social-btn:hover {
  background: #f9fafb;
  border-color: #d1d5db;
}

.social-icon {
  width: 1.25rem;
  height: 1.25rem;
  color: #6366f1;
}

/* 分隔线 */
.divider {
  display: flex;
  align-items: center;
  margin: 1.5rem 0;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #e5e7eb;
}

.divider span {
  padding: 0 1rem;
  font-size: 0.8125rem;
  color: #9ca3af;
}

/* 表单标签 */
.form-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 1.25rem;
}

.login-form :deep(.el-input__wrapper) {
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  box-shadow: none !important;
  border: 1px solid #e5e7eb;
  transition: all 0.2s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: #d1d5db;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1) !important;
}

.login-form :deep(.el-input__inner) {
  font-size: 0.9375rem;
  color: #1f2937;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: #9ca3af;
}

.input-icon {
  width: 1.125rem;
  height: 1.125rem;
  color: #9ca3af;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #4b5563;
  cursor: pointer;
}

.remember-me input[type="checkbox"] {
  width: 1rem;
  height: 1rem;
  accent-color: #6366f1;
  cursor: pointer;
}

.forgot-password {
  font-size: 0.875rem;
  color: #6366f1;
  text-decoration: none;
  transition: color 0.2s;
}

.forgot-password:hover {
  color: #4f46e5;
}

/* 登录按钮 */
.btn {
  width: 100%;
  height: 48px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 0.5rem;
  background: #6366f1;
  border: none;
  color: #ffffff;
  transition: all 0.2s ease;
}

.btn:hover {
  background: #4f46e5;
}

.login-form :deep(.el-button--primary) {
  --el-button-bg-color: #6366f1;
  --el-button-border-color: #6366f1;
  --el-button-hover-bg-color: #4f46e5;
  --el-button-hover-border-color: #4f46e5;
  --el-button-active-bg-color: #4338ca;
  --el-button-active-border-color: #4338ca;
}

/* 注册链接 */
.signup-link {
  text-align: center;
  margin-top: 1.5rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.signup-link a {
  color: #6366f1;
  text-decoration: none;
  font-weight: 500;
}

.signup-link a:hover {
  color: #4f46e5;
}

.signup-link-text {
  color: #6366f1;
  text-decoration: none;
  font-weight: 500;
}

.signup-link-text:hover {
  color: #4f46e5;
}

/* 登录方式切换按钮 */
.login-method-toggle {
  display: flex;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  padding: 0.25rem;
  margin-bottom: 1.5rem;
  background: #f9fafb;
}

.toggle-btn {
  flex: 1;
  padding: 0.75rem 1rem;
  border: none;
  background: transparent;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s ease;
}

.toggle-btn.active {
  background: #ffffff;
  color: #6366f1;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  font-weight: 500;
}

.toggle-btn:first-child {
  margin-right: 0.25rem;
}

.toggle-btn:last-child {
  margin-left: 0.25rem;
}

/* 验证码输入包装器：与输入框同高，按钮文字常显 */
.verification-code-wrapper {
  display: flex;
  align-items: stretch;
  gap: 0.5rem;
}

.verification-code-wrapper :deep(.el-input) {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.verification-code-wrapper :deep(.el-input__wrapper) {
  flex: 1;
  min-height: 48px;
  box-sizing: border-box;
}

.send-code-btn {
  flex-shrink: 0;
  align-self: stretch;
  margin: 0 !important;
  padding: 0 1rem !important;
  font-size: 0.8125rem;
  font-weight: 600;
  white-space: nowrap;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  line-height: 1.25 !important;
  color: #ffffff !important;
  --el-button-text-color: #ffffff;
  --el-button-hover-text-color: #ffffff;
  --el-button-active-text-color: #ffffff;
  /* 与当前登录表单输入框（含 padding）视觉高度对齐 */
  min-height: 48px !important;
  height: 100% !important;
  box-sizing: border-box;
}

.send-code-btn :deep(span) {
  color: inherit !important;
  opacity: 1 !important;
}

/* 右侧宣传区域 */
.login-right {
  flex: 1;
  background: #6366f1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  max-width: 50%;
}

.right-content {
  position: relative;
  z-index: 2;
  text-align: center;
  padding: 3rem;
  color: #ffffff;
}

.brand-icon {
  width: 3.5rem;
  height: 3.5rem;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 2rem;
}

.brand-icon svg {
  width: 2rem;
  height: 2rem;
  color: #ffffff;
}

.promo-title {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1.3;
  margin: 0 0 2rem;
}

/* 装饰性卡片 */
.promo-cards {
  position: relative;
  height: 200px;
  margin: 2rem 0;
}

.promo-card {
  position: absolute;
  background: #ffffff;
  border-radius: 0.75rem;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  padding: 1rem;
}

.card-1 {
  width: 200px;
  left: 10%;
  top: 0;
  transform: rotate(-5deg);
}

.card-2 {
  width: 180px;
  right: 10%;
  top: 40px;
  transform: rotate(5deg);
}

.card-header {
  display: flex;
  gap: 0.375rem;
  margin-bottom: 0.75rem;
}

.card-dot {
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
  background: #e5e7eb;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.card-bar {
  height: 0.5rem;
  border-radius: 0.25rem;
  background: #e5e7eb;
}

.bar-1 { width: 100%; }
.bar-2 { width: 80%; }
.bar-3 { width: 60%; }

.card-chart {
  display: flex;
  align-items: flex-end;
  gap: 0.5rem;
  height: 60px;
}

.chart-bar {
  flex: 1;
  background: linear-gradient(to top, #6366f1, #a5b4fc);
  border-radius: 0.25rem 0.25rem 0 0;
}

.chart-bar:nth-child(1) { height: 40%; }
.chart-bar:nth-child(2) { height: 70%; }
.chart-bar:nth-child(3) { height: 55%; }
.chart-bar:nth-child(4) { height: 85%; }

.promo-desc {
  font-size: 0.9375rem;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.85);
  max-width: 320px;
  margin: 0 auto;
}

/* 波浪背景 */
.wave-bg {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40%;
  background: 
    repeating-linear-gradient(
      to right,
      transparent,
      transparent 50px,
      rgba(255, 255, 255, 0.03) 50px,
      rgba(255, 255, 255, 0.03) 100px
    );
  opacity: 0.5;
}

.wave-bg::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: -50%;
  width: 200%;
  height: 100%;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='rgba(255,255,255,0.1)' d='M0,192L48,176C96,160,192,128,288,138.7C384,149,480,203,576,208C672,213,768,171,864,149.3C960,128,1056,128,1152,149.3C1248,171,1344,213,1392,234.7L1440,256L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") repeat-x;
  background-size: 1440px 320px;
  animation: wave 15s linear infinite;
}

@keyframes wave {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

/* 响应式适配 */
@media (max-width: 1024px) {
  .login-right {
    display: none;
  }
  
  .login-left {
    max-width: 100%;
  }
}

@media (max-width: 480px) {
  .login-left {
    padding: 1.5rem;
  }
  
  .form-header h1 {
    font-size: 1.5rem;
  }
}
</style>
