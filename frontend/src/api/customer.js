import request from '@/utils/request'

/**
 * 获取当前用户的应用列表
 */
export function getAppList() {
  return request({
    url: '/customer/app/list',
    method: 'get'
  })
}

/**
 * 获取所有应用（管理员）
 */
export function getAllApps() {
  return request({
    url: '/customer/app/all',
    method: 'get'
  })
}

/**
 * 根据ID获取应用详情
 */
export function getAppById(id) {
  return request({
    url: `/customer/app/${id}`,
    method: 'get'
  })
}

/**
 * 创建应用
 */
export function createApp(data) {
  return request({
    url: '/customer/app/create',
    method: 'post',
    data
  })
}

/**
 * 更新应用
 */
export function updateApp(data) {
  return request({
    url: '/customer/app/update',
    method: 'put',
    data
  })
}

/**
 * 删除应用
 */
export function deleteApp(id) {
  return request({
    url: `/customer/app/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 更新应用状态
 */
export function updateAppStatus(data) {
  return request({
    url: '/customer/app/status',
    method: 'put',
    data
  })
}

/**
 * 重置应用密钥
 */
export function resetAppSecret(id) {
  return request({
    url: `/customer/app/reset-secret/${id}`,
    method: 'post'
  })
}

/**
 * 获取应用已授权的API列表
 */
export function getAuthorizedApis(appId) {
  return request({
    url: `/customer/app/authorized-apis/${appId}`,
    method: 'get'
  })
}

/**
 * 保存应用的API权限
 */
export function savePermissions(data) {
  return request({
    url: '/customer/app/save-permissions',
    method: 'post',
    data
  })
}
