import http from './http'

// ---------- 优秀成果管理 ----------

// 管理员分页查询
export function fetchAdminExcellentPage(params) {
  return http.get('/achievement/excellent/admin/page', { params })
}

// 教师分页查询（本校）
export function fetchTeacherExcellentPage(params) {
  return http.get('/achievement/excellent/teacher/page', { params })
}

// 学生分页查询（自己的）
export function fetchStudentExcellentPage(params) {
  return http.get('/achievement/excellent/student/page', { params })
}

// 学生自助查询（本校名单，按姓名/学号模糊搜索）
export function fetchStudentExcellentSearch(params) {
  return http.get('/achievement/excellent/student/search', { params })
}

// 认定优秀成果
export function approveExcellent(data) {
  return http.post('/achievement/excellent', data)
}

// 撤销认定
export function revokeExcellent(excellentId) {
  return http.delete(`/achievement/excellent/${excellentId}`)
}

// 查询阈值配置列表
export function fetchThresholdList(params) {
  return http.get('/achievement/excellent/threshold/list', { params })
}

// 新增/更新阈值配置
export function saveThreshold(data) {
  return http.post('/achievement/excellent/threshold', data)
}

// 删除阈值配置
export function deleteThreshold(thresholdId) {
  return http.delete(`/achievement/excellent/threshold/${thresholdId}`)
}

// 查询可认定候选成绩（总分 > 学校阈值）
export function fetchQualifiedGrades(params) {
  return http.get('/achievement/excellent/qualified', { params })
}

// 导出优秀成果名单
export function exportExcellent(params) {
  return http.get('/achievement/excellent/export', {
    params,
    responseType: 'blob',
  })
}
