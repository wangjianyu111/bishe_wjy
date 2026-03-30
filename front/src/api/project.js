import http from './http'

export function fetchTopicPage(params) {
  return http.get('/project/topics', { params })
}

export function applySelection(data) {
  return http.post('/project/selections/apply', data)
}
