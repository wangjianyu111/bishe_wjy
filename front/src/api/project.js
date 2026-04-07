import http from './http'

// ---------- 课题 ----------
export function fetchTopicPage(params) {
  return http.get('/project/topics', { params })
}

export function fetchTopicById(topicId) {
  return http.get(`/project/topics/${topicId}`)
}

export function addTopic(data) {
  return http.post('/project/topics', data)
}

export function updateTopic(data) {
  return http.put('/project/topics', data)
}

export function deleteTopic(topicId) {
  return http.delete(`/project/topics/${topicId}`)
}

export function toggleTopicStatus(topicId) {
  return http.put(`/project/topics/toggle-status/${topicId}`)
}

export function exportTopic(params) {
  return http.get('/project/topics/export', { params, responseType: 'blob' })
}

export function importTopic(formData) {
  return http.post('/project/topics/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// ---------- 选题申请 ----------
export function fetchCampuses() {
  return http.get('/project/selections/campuses')
}

export function fetchTeachers(params) {
  return http.get('/project/selections/teachers', { params })
}

export function fetchTopicBank(params) {
  return http.get('/project/selections/topic-bank', { params })
}

export function applySelection(data) {
  return http.post('/project/selections/apply', data)
}

export function fetchMySelection() {
  return http.get('/project/selections/my')
}

export function withdrawSelection(selectionId) {
  return http.put(`/project/selections/withdraw/${selectionId}`)
}

// ---------- 选题管理 ----------
export function fetchSelectionPage(params) {
  return http.get('/project/selections', { params })
}

export function approveSelection(data) {
  return http.put('/project/selections/approve', data)
}

export function rejectSelection(data) {
  return http.put('/project/selections/reject', data)
}

export function fetchTeacherApprovals(params) {
  return http.get('/project/selections/teacher-approvals', { params })
}
