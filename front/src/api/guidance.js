import http from './http'

// ---------- 指导记录管理 ----------

// 教师：查询可添加指导的选题列表
export function fetchSelectionsForGuidance() {
  return http.get('/guidance/record/selections')
}

// 管理员分页
export function fetchAdminGuidancePage(params) {
  return http.get('/guidance/record/admin/page', { params })
}

// 教师分页（本校）
export function fetchTeacherGuidancePage(params) {
  return http.get('/guidance/record/teacher/page', { params })
}

// 学生分页（自己的）
export function fetchStudentGuidancePage(params) {
  return http.get('/guidance/record/student/page', { params })
}

// 添加指导记录
export function addGuidanceRecord(data) {
  return http.post('/guidance/record', data)
}

// 更新指导记录
export function updateGuidanceRecord(data) {
  return http.put('/guidance/record', data)
}

// 删除指导记录
export function deleteGuidanceRecord(guideId) {
  return http.delete(`/guidance/record/${guideId}`)
}

// 学生填写反馈
export function addGuidanceFeedback(guideId, feedback) {
  return http.post(`/guidance/record/feedback/${guideId}`, feedback)
}

// 获取详情
export function fetchGuidanceDetail(guideId) {
  return http.get(`/guidance/record/${guideId}`)
}

// 获取统计信息
export function fetchGuidanceStats(studentId) {
  return http.get('/guidance/record/stats', { params: { studentId } })
}

// ---------- 教师反馈管理 ----------

// 教师提交反馈
export function submitTeacherFeedback(data) {
  return http.post('/guidance/feedback', data)
}

// 管理员分页查询
export function fetchAdminFeedbackPage(params) {
  return http.get('/guidance/feedback/admin/page', { params })
}

// 教师分页查询
export function fetchTeacherFeedbackPage(params) {
  return http.get('/guidance/feedback/teacher/page', { params })
}

// 管理员处理反馈
export function handleTeacherFeedback(data) {
  return http.put('/guidance/feedback/handle', data)
}

// 管理员统计
export function fetchAdminFeedbackStats(params) {
  return http.get('/guidance/feedback/stats/admin', { params })
}

// 教师统计
export function fetchTeacherFeedbackStats(params) {
  return http.get('/guidance/feedback/stats/teacher', { params })
}
