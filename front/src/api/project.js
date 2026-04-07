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

// ---------- 选题 ----------
export function applySelection(data) {
  return http.post('/project/selections/apply', data)
}
