import http from '@/api/http'

// ---------- 答辩安排管理 ----------

// 教师/管理员：发布答辩安排
export function publishDefense(formData) {
  return http.post('/achievement/defense', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 管理员：分页查询答辩安排
export function fetchDefensePage(params) {
  return http.get('/achievement/defense/admin/page', { params })
}

// 教师：分页查询答辩安排
export function fetchTeacherDefensePage(params) {
  return http.get('/achievement/defense/teacher/page', { params })
}

// 学生：分页查询答辩安排
export function fetchStudentDefensePage(params) {
  return http.get('/achievement/defense/student/page', { params })
}

// 管理员：查询答辩安排明细
export function fetchDefenseArrangePage(params) {
  return http.get('/achievement/defense/admin/arrangement/page', { params })
}

// 查看答辩场次详情
export function fetchDefenseDetail(sessionId) {
  return http.get(`/achievement/defense/${sessionId}`)
}

// 下载答辩安排附件
export function downloadDefenseFile(fileId) {
  return http.get(`/doc/file/download/${fileId}`, { responseType: 'blob' })
}
