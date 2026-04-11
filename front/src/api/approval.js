import http from './http'

// ---------- 审批意见管理 ----------
// 管理员分页查询
export function fetchAdminApprovalPage(params) {
  return http.get('/achievement/approval/admin/page', { params })
}

// 教师分页查询
export function fetchTeacherApprovalPage(params) {
  return http.get('/achievement/approval/teacher/page', { params })
}

// 学生分页查询
export function fetchStudentApprovalPage(params) {
  return http.get('/achievement/approval/student/page', { params })
}

// 提交审批意见
export function submitApproval(formData) {
  return http.post('/achievement/approval', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 更新审批意见状态（通过/驳回）
export function updateApprovalStatus(data) {
  return http.put('/achievement/approval/status', data)
}

// 获取审批意见详情
export function fetchApprovalDetail(opinionId) {
  return http.get(`/achievement/approval/${opinionId}`)
}

// 删除审批意见
export function deleteApproval(opinionId) {
  return http.delete(`/achievement/approval/${opinionId}`)
}

// 下载审批意见附件
export function downloadApprovalFile(fileId) {
  return http.get(`/doc/file/download/${fileId}`, { responseType: 'blob' })
}
