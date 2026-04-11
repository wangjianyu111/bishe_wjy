import http from './http'

// ---------- 成绩评审管理 ----------

// 管理员分页查询
export function fetchAdminGradePage(params) {
  return http.get('/achievement/grade/admin/page', { params })
}

// 教师分页查询
export function fetchTeacherGradePage(params) {
  return http.get('/achievement/grade/teacher/page', { params })
}

// 学生分页查询
export function fetchStudentGradePage(params) {
  return http.get('/achievement/grade/student/page', { params })
}

// 教师录入/更新评分
export function inputGrade(data) {
  return http.post('/achievement/grade', data)
}

// 管理员调整成绩
export function adjustGrade(summaryId, adjustedTotalScore, adjustedGradeLevel, remark) {
  return http.put('/achievement/grade/adjust', null, {
    params: { summaryId, adjustedTotalScore, adjustedGradeLevel, remark },
  })
}

// 管理员锁定成绩
export function lockGrade(summaryId) {
  return http.put(`/achievement/grade/lock/${summaryId}`)
}

// 管理员解锁成绩
export function unlockGrade(summaryId) {
  return http.put(`/achievement/grade/unlock/${summaryId}`)
}

// 获取成绩详情（含所有教师评分明细）
export function fetchGradeDetail(summaryId) {
  return http.get(`/achievement/grade/${summaryId}`)
}

// 删除单条教师评分记录
export function deleteGradeRecord(gradeId) {
  return http.delete(`/achievement/grade/record/${gradeId}`)
}
