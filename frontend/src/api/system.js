import request from '@/utils/request'

/**
 * 用户管理 API
 */

// 分页查询用户
export function getUserList(params) {
  return request({
    url: '/system/user/list',
    method: 'get',
    params
  })
}

// 根据ID查询用户
export function getUserById(id) {
  return request({
    url: `/system/user/${id}`,
    method: 'get'
  })
}

// 创建用户
export function createUser(data) {
  return request({
    url: '/system/user',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(data) {
  return request({
    url: '/system/user',
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/system/user/${id}`,
    method: 'delete'
  })
}

// 批量删除用户
export function deleteUserBatch(ids) {
  return request({
    url: '/system/user/batch',
    method: 'delete',
    data: ids
  })
}

// 更新用户状态
export function updateUserStatus(id, status) {
  return request({
    url: `/system/user/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 重置用户密码
export function resetUserPassword(id, password) {
  return request({
    url: `/system/user/${id}/reset-password`,
    method: 'put',
    data: password ? { password } : {}
  })
}

/**
 * 角色管理 API
 */

// 查询所有角色
export function getAllRoles() {
  return request({
    url: '/system/role/all',
    method: 'get'
  })
}

// 分页查询角色
export function getRoleList(params) {
  return request({
    url: '/system/role/list',
    method: 'get',
    params
  })
}

// 根据ID查询角色
export function getRoleById(id) {
  return request({
    url: `/system/role/${id}`,
    method: 'get'
  })
}

// 创建角色
export function createRole(data) {
  return request({
    url: '/system/role',
    method: 'post',
    data
  })
}

// 更新角色
export function updateRole(data) {
  return request({
    url: '/system/role',
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(id) {
  return request({
    url: `/system/role/${id}`,
    method: 'delete'
  })
}

// 批量删除角色
export function deleteRoleBatch(ids) {
  return request({
    url: '/system/role/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 操作日志 API
 */

// 分页查询操作日志
export function getOperationLogList(params) {
  return request({
    url: '/system/log/list',
    method: 'get',
    params
  })
}

// 根据ID查询日志详情
export function getOperationLogById(id) {
  return request({
    url: `/system/log/${id}`,
    method: 'get'
  })
}

// 批量删除日志
export function deleteLogBatch(ids) {
  return request({
    url: '/system/log/batch',
    method: 'delete',
    data: ids
  })
}

// 清理过期日志
export function cleanExpiredLogs(days) {
  return request({
    url: '/system/log/clean',
    method: 'delete',
    params: { days }
  })
}

// 导出操作日志
export function exportOperationLogs(params) {
  return request({
    url: '/system/log/export',
    method: 'get',
    params
  })
}

/**
 * 权限管理 API
 */

// 获取权限树
export function getPermissionsTree() {
  return request({
    url: '/system/permission/tree',
    method: 'get'
  })
}

// 获取所有权限列表
export function getAllPermissions() {
  return request({
    url: '/system/permission/list',
    method: 'get'
  })
}

// 根据角色ID获取权限
export function getPermissionsByRoleId(roleId) {
  return request({
    url: `/system/permission/role/${roleId}`,
    method: 'get'
  })
}

// 保存角色权限
export function saveRolePermissions(roleId, permissionIds) {
  return request({
    url: `/system/permission/role/${roleId}`,
    method: 'post',
    data: permissionIds
  })
}

// 创建权限
export function createPermission(data) {
  return request({
    url: '/system/permission',
    method: 'post',
    data
  })
}

// 更新权限
export function updatePermission(data) {
  return request({
    url: '/system/permission',
    method: 'put',
    data
  })
}

// 删除权限
export function deletePermission(id) {
  return request({
    url: `/system/permission/${id}`,
    method: 'delete'
  })
}
