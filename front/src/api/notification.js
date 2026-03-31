import http from './http'

export function fetchNotificationPage(params) {
  return http.get('/system/notifications/page', { params })
}

export function fetchNotificationById(noticeId) {
  return http.get(`/system/notifications/${noticeId}`)
}

export function addNotification(data) {
  return http.post('/system/notifications', data)
}

export function updateNotification(data) {
  return http.put('/system/notifications', data)
}

export function deleteNotification(noticeId) {
  return http.delete(`/system/notifications/${noticeId}`)
}

export function markRead(noticeId) {
  return http.put(`/system/notifications/read/${noticeId}`)
}

export function markAllRead() {
  return http.put('/system/notifications/read-all')
}

export function fetchUserNotificationList() {
  return http.get('/system/notifications/user/list')
}

export function fetchUnreadCount() {
  return http.get('/system/notifications/unread-count')
}

export function fetchReadStatus(noticeId) {
  return http.get(`/system/notifications/read-status/${noticeId}`)
}

export function fetchReceivers(noticeId) {
  return http.get(`/system/notifications/receivers/${noticeId}`)
}
