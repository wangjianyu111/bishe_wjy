<template>
  <el-card shadow="never" class="profile-card">
    <template #header>
      <div class="header-row">
        <span>个人资料</span>
      </div>
    </template>

    <div v-loading="loading" class="profile-body">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100"
        class="profile-form"
      >
        <div class="avatar-section">
          <div class="avatar-large">{{ userInitial }}</div>
          <p class="avatar-hint">点击头像可自定义（暂不支持）</p>
        </div>

        <el-divider content-position="left">基本信息</el-divider>

        <el-form-item label="登录账号">
          <el-input :model-value="form.userName" disabled />
        </el-form-item>

        <el-form-item label="用户类型">
          <el-input :model-value="typeLabel(form.userType)" disabled />
        </el-form-item>

        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" maxlength="50" />
        </el-form-item>

        <el-form-item v-if="form.userType === 1" label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入学号" maxlength="30" />
        </el-form-item>

        <el-form-item v-if="form.userType === 2" label="工号" prop="teacherNo">
          <el-input v-model="form.teacherNo" placeholder="请输入工号" maxlength="30" />
        </el-form-item>

        <el-divider content-position="left">联系方式</el-divider>

        <el-form-item label="手机号" prop="userPhone">
          <el-input v-model="form.userPhone" placeholder="请输入手机号" maxlength="20" />
        </el-form-item>

        <el-form-item label="邮箱" prop="userEmail">
          <el-input v-model="form.userEmail" placeholder="请输入邮箱" maxlength="100" />
        </el-form-item>

        <el-divider content-position="left">修改密码</el-divider>

        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="form.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
            maxlength="50"
          >
            <template #append>
              <el-button :loading="verifying" @click="handleVerifyOld">验证</el-button>
            </template>
          </el-input>
          <div v-if="oldPasswordVerified" class="verify-success">
            <el-icon color="#67c23a"><CircleCheckFilled /></el-icon>
            <span>原密码验证通过</span>
          </div>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
            maxlength="50"
            :disabled="!oldPasswordVerified"
          />
        </el-form-item>
      </el-form>

      <div class="form-actions">
        <el-button type="primary" :loading="submitting" @click="handleSave">保存修改</el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import { fetchUserById, updateUser } from '@/api/system'
import { changePassword, updateProfile } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const store = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const verifying = ref(false)
const oldPasswordVerified = ref(false)
const formRef = ref(null)

const form = ref({
  userId: null,
  userName: '',
  realName: '',
  userType: null,
  studentNo: '',
  teacherNo: '',
  userPhone: '',
  userEmail: '',
  oldPassword: '',
  newPassword: '',
})

const rules = {
  realName: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
  userPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  userEmail: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
}

const userInitial = computed(() => {
  const n = store.user?.realName || store.user?.userName || ''
  return n ? String(n).slice(0, 1) : '用'
})

function typeLabel(t) {
  if (t === 1) return '学生'
  if (t === 2) return '教师'
  if (t === 3) return '管理员'
  return '—'
}

async function load() {
  const userId = store.user?.userId
  if (!userId) return
  loading.value = true
  try {
    const data = await fetchUserById(userId)
    form.value = {
      userId: data.userId,
      userName: data.userName || '',
      realName: data.realName || '',
      userType: data.userType,
      studentNo: data.studentNo || '',
      teacherNo: data.teacherNo || '',
      userPhone: data.userPhone || '',
      userEmail: data.userEmail || '',
      oldPassword: '',
      newPassword: '',
    }
    oldPasswordVerified.value = false
  } catch {
    ElMessage.error('加载个人资料失败')
  } finally {
    loading.value = false
  }
}

async function handleVerifyOld() {
  if (!form.value.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  verifying.value = true
  try {
    await changePassword({ oldPassword: form.value.oldPassword, newPassword: null })
    ElMessage.success('原密码验证通过')
    oldPasswordVerified.value = true
  } catch (e) {
    ElMessage.error(e.message || '原密码错误')
    oldPasswordVerified.value = false
  } finally {
    verifying.value = false
  }
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      realName: form.value.realName,
      userPhone: form.value.userPhone || null,
      userEmail: form.value.userEmail || null,
      studentNo: form.value.studentNo || null,
      teacherNo: form.value.teacherNo || null,
    }
    await updateProfile(payload)

    if (form.value.newPassword) {
      await changePassword({
        oldPassword: form.value.oldPassword,
        newPassword: form.value.newPassword,
      })
      ElMessage.success('个人信息和密码均已保存')
    } else {
      ElMessage.success('保存成功')
    }

    if (store.user) {
      store.user.realName = form.value.realName
      store.user.userPhone = form.value.userPhone
      store.user.userEmail = form.value.userEmail
      store.user.studentNo = form.value.studentNo
      store.user.teacherNo = form.value.teacherNo
    }

    form.value.oldPassword = ''
    form.value.newPassword = ''
    oldPasswordVerified.value = false
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.profile-card {
  max-width: 680px;
  margin: 0 auto;
}

.header-row {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.profile-body {
  padding: 8px 0;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 16px 0 8px;
}

.avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: linear-gradient(135deg, #6366f1, #a5b4fc);
  color: #fff;
  font-weight: 700;
  font-size: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.3);
}

.avatar-hint {
  margin: 0;
  font-size: 12px;
  color: #c0c4cc;
}

.profile-form {
  max-width: 480px;
  margin: 0 auto;
}

.verify-success {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 6px;
  font-size: 12px;
  color: #67c23a;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
  padding-bottom: 8px;
}
</style>
