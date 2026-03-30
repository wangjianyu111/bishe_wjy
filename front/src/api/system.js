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

export function addPermission(data) {
  return http.post('/system/permissions', data)
}

export function updatePermission(data) {
  return http.put('/system/permissions', data)
}

export function deletePermission(permId) {
  return http.delete(`/system/permissions/${permId}`)
}
