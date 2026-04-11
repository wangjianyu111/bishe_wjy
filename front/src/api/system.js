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
