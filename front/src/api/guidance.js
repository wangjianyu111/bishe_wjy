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

// ---------- 质量预警管理 ----------

// 教师添加预警
export function addQualityWarning(data) {
  return http.post('/guidance/warning', data)
}

// 管理员分页查询
export function fetchAdminWarningPage(params) {
  return http.get('/guidance/warning/admin/page', { params })
}

// 教师分页查询
export function fetchTeacherWarningPage(params) {
  return http.get('/guidance/warning/teacher/page', { params })
}

// 管理员关闭预警
export function handleQualityWarning(data) {
  return http.put('/guidance/warning/handle', data)
}

// 管理员删除预警
export function deleteQualityWarning(warnId) {
  return http.delete(`/guidance/warning/${warnId}`)
}

// 管理员统计
export function fetchAdminWarningStats(params) {
  return http.get('/guidance/warning/stats/admin', { params })
}

// 教师统计
export function fetchTeacherWarningStats(params) {
  return http.get('/guidance/warning/stats/teacher', { params })
}

// ---------- 指导关系管理 ----------

// 管理员分页查询
export function fetchAdminRelationPage(params) {
  return http.get('/guidance/relation/admin/page', { params })
}

// 教师分页查询
export function fetchTeacherRelationPage(params) {
  return http.get('/guidance/relation/teacher/page', { params })
}

// 学生分页查询
export function fetchStudentRelationPage(params) {
  return http.get('/guidance/relation/student/page', { params })
}

// 添加指导关系
export function addGuidanceRelation(data) {
  return http.post('/guidance/relation', data)
}

// 更新指导关系
export function updateGuidanceRelation(data) {
  return http.put('/guidance/relation', data)
}

// 解除指导关系
export function deleteGuidanceRelation(relationId) {
  return http.delete(`/guidance/relation/${relationId}`)
}

// 获取指导关系详情
export function fetchRelationDetail(relationId) {
  return http.get(`/guidance/relation/${relationId}`)
}

// 获取教师指导的学生列表
export function fetchTeacherStudents(teacherId, academicYear) {
  return http.get('/guidance/relation/teacher/students', { params: { teacherId, academicYear } })
}

// ====== 教师仪表盘 ======
export function fetchTeacherStudentPage(params) {
  return http.get('/guidance/relation/teacher/dashboard/students', { params })
}

export function fetchTeacherCreatedGroups(academicYear) {
  return http.get('/guidance/relation/teacher/dashboard/groups/created', { params: { academicYear } })
}

export function fetchTeacherJoinedGroups(academicYear) {
  return http.get('/guidance/relation/teacher/dashboard/groups/joined', { params: { academicYear } })
}

// ====== 学生仪表盘 ======
export function fetchStudentRelation(academicYear) {
  return http.get('/guidance/relation/student/dashboard/relation', { params: { academicYear } })
}

export function fetchStudentGroups(academicYear) {
  return http.get('/guidance/relation/student/dashboard/groups', { params: { academicYear } })
}

// ====== 申请流程 ======
export function sendRelationApply(data) {
  return http.post('/guidance/relation/apply', data)
}

export function fetchReceivedApplies(params) {
  return http.get('/guidance/relation/apply/received', { params })
}

export function fetchSentApplies(params) {
  return http.get('/guidance/relation/apply/sent', { params })
}

export function handleRelationApply(data) {
  return http.put('/guidance/relation/apply/handle', data)
}

export function cancelRelationApply(applyId) {
  return http.delete(`/guidance/relation/apply/${applyId}`)
}

// ---------- 答辩小组管理 ----------

// 教师分页查询答辩小组
export function fetchTeacherDefenseGroupPage(params) {
  return http.get('/guidance/defense-group/teacher/page', { params })
}

// 创建答辩小组
export function createDefenseGroup(data) {
  return http.post('/guidance/defense-group', data)
}

// 更新答辩小组
export function updateDefenseGroup(data) {
  return http.put('/guidance/defense-group', data)
}

// 删除答辩小组
export function deleteDefenseGroup(groupId) {
  return http.delete(`/guidance/defense-group/${groupId}`)
}

// 获取答辩小组详情
export function fetchDefenseGroupDetail(groupId) {
  return http.get(`/guidance/defense-group/${groupId}`)
}

// 获取我参与的答辩小组
export function fetchMyDefenseGroups(academicYear) {
  return http.get('/guidance/defense-group/my', { params: { academicYear } })
}
