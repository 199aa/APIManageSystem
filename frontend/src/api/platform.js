import request from '@/utils/request'

// 获取平台列表
export function getPlatformList(params) {
  return request({
    url: '/platform/list',
    method: 'get',
    params
  })
}

// 获取所有平台（下拉选择用）
export function getAllPlatforms() {
  return request({
    url: '/platform/all',
    method: 'get'
  })
}

// 获取平台详情
export function getPlatformDetail(id) {
  return request({
    url: `/platform/${id}`,
    method: 'get'
  })
}

// 保存平台（新增/编辑）
export function savePlatform(data) {
  return request({
    url: '/platform/save',
    method: 'post',
    data
  })
}

// 删除平台
export function deletePlatform(id) {
  return request({
    url: `/platform/${id}`,
    method: 'delete'
  })
}

// 切换平台状态
export function togglePlatformStatus(id, status) {
  return request({
    url: `/platform/${id}/status`,
    method: 'put',
    params: { status }
  })
}
