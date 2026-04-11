import http from './http'

// ---------- 用户 ----------
export function fetchUserPage(params) {
  return http.get('/system/users/page', { params })
}

export function fetchUserById(userId) {
  return http.get(`/system/users/${userId}`)
}

export function addUser(data) {
  return http.post('/system/users', data)
}

export function updateUser(data) {
  return http.put('/system/users', data)
}

export function deleteUser(userId) {
  return http.delete(`/system/users/${userId}`)
}

export function assignUserRoles(data) {
  return http.post('/system/users/assign-roles', data)
}

export function toggleUserStatus(userId, status) {
  return http.put(`/system/users/status/${userId}`, null, { params: { status } })
}

// ---------- 角色 ----------
export function fetchRolePage(params) {
  return http.get('/system/roles/page', { params })
}

export function fetchRoleList() {
  return http.get('/system/roles')
}

export function fetchRoleById(roleId) {
  return http.get(`/system/roles/${roleId}`)
}

export function addRole(data) {
  return http.post('/system/roles', data)
}

export function updateRole(data) {
  return http.put('/system/roles', data)
}

export function deleteRole(roleId) {
  return http.delete(`/system/roles/${roleId}`)
}

export function fetchRolePermissionTree() {
  return http.get('/system/roles/permissions/tree')
}

export function assignRolePermissions(data) {
  return http.post('/system/roles/assign-permissions', data)
}

// ---------- 校区 ----------
export function fetchCampusList() {
  return http.get('/system/campuses')
}

// ---------- 权限 ----------
export function fetchPermissionPage(params) {
  return http.get('/system/permissions/page', { params })
}

export function fetchPermissionTree() {
  return http.get('/system/permissions/tree')
}

export function fetchPermissionList() {
  return http.get('/system/permissions/list')
}

export function fetchUserList(params) {
  return http.get('/system/users/page', { params })
}

export function addPermission(data) {
  return http.post('/system/permissions', data)
}

export function updatePermission(data) {
  return http.put('/system/permissions', data)
}

export function deletePermission(permId) {
  return http.delete(`/system/permissions/${permId}`)
}

// ---------- 操作日志 ----------
export function fetchOperationLogPage(params) {
  return http.get('/system/operation-log/page', { params })
}

export function exportOperationLog(params) {
  return http.get('/system/operation-log/export', {
    params,
    responseType: 'blob',
  })
}

export function deleteOperationLogBatch(ids) {
  return http.delete('/system/operation-log/batch', { data: ids })
}

// ---------- 版本更新 ----------
export function fetchVersionPage(params) {
  return http.get('/system/version/page', { params })
}

export function fetchVersionDetail(versionId) {
  return http.get(`/system/version/${versionId}`)
}

export function fetchCurrentVersion(appType) {
  return http.get('/system/version/current', { params: { appType } })
}

export function addVersion(data) {
  return http.post('/system/version', data)
}

export function performVersionRollout(data) {
  return http.post('/system/version/rollout', data)
}

export function deleteVersion(versionId) {
  return http.delete(`/system/version/${versionId}`)
}

// ---------- 系统监控 ----------
export function fetchMonitorDashboard() {
  return http.get('/system/monitor/dashboard')
}

export function fetchMonitorMetricsPage(params) {
  return http.get('/system/monitor/metrics/page', { params })
}

export function fetchMonitorAlertsPage(params) {
  return http.get('/system/monitor/alerts/page', { params })
}

export function fetchMonitorApiStatsPage(params) {
  return http.get('/system/monitor/api-stats/page', { params })
}

export function resolveMonitorAlert(data) {
  return http.post('/system/monitor/alerts/resolve', data)
}

export function clearMonitorHistory(days) {
  return http.delete('/system/monitor/history', { params: { days } })
}

// ---------- 系统参数 ----------
export function fetchSchoolPage(params) {
  return http.get('/system/param/school/page', { params })
}
export function addSchool(data) {
  return http.post('/system/param/school', data)
}
export function updateSchool(data) {
  return http.put('/system/param/school', data)
}
export function deleteSchool(id) {
  return http.delete(`/system/param/school/${id}`)
}

export function fetchDesignStagePage(params) {
  return http.get('/system/param/design-stage/page', { params })
}
export function addDesignStage(data) {
  return http.post('/system/param/design-stage', data)
}
export function updateDesignStage(data) {
  return http.put('/system/param/design-stage', data)
}
export function deleteDesignStage(id) {
  return http.delete(`/system/param/design-stage/${id}`)
}

export function fetchGradePage(params) {
  return http.get('/system/param/grade/page', { params })
}
export function addGrade(data) {
  return http.post('/system/param/grade', data)
}
export function updateGrade(data) {
  return http.put('/system/param/grade', data)
}
export function deleteGrade(id) {
  return http.delete(`/system/param/grade/${id}`)
}

export function fetchTimeSlotPage(params) {
  return http.get('/system/param/time-slot/page', { params })
}
export function addTimeSlot(data) {
  return http.post('/system/param/time-slot', data)
}
export function updateTimeSlot(data) {
  return http.put('/system/param/time-slot', data)
}
export function deleteTimeSlot(id) {
  return http.delete(`/system/param/time-slot/${id}`)
}

export function batchUpdateParams(data) {
  return http.post('/system/param/batch-update', data)
}
