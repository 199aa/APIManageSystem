import request from '@/utils/request'

// ==================== 原子API管理 ====================

// 获取API列表
export function getApiList(params) {
  return request({
    url: '/api-info/list',
    method: 'get',
    params
  })
}

// 获取API列表（不分页）
export function getApiInfoList() {
  return request({
    url: '/api-info/list',
    method: 'get'
  })
}

// 获取API详情
export function getApiDetail(id) {
  return request({
    url: `/api-info/${id}`,
    method: 'get'
  })
}

// 保存API（新增/编辑）
export function saveApi(data) {
  return request({
    url: '/api-info/save',
    method: 'post',
    data
  })
}

// 删除API
export function deleteApi(id) {
  return request({
    url: `/api-info/${id}`,
    method: 'delete'
  })
}

// 克隆API
export function cloneApi(id) {
  return request({
    url: `/api-info/${id}/clone`,
    method: 'post'
  })
}

// 测试API
export function testApi(data) {
  return request({
    url: '/api-info/test',
    method: 'post',
    data
  })
}

// ==================== 聚合接口管理 ====================

// 获取聚合接口列表
export function getAggregateList(params) {
  return request({
    url: '/aggregate/list',
    method: 'get',
    params
  })
}

// 获取聚合接口详情
export function getAggregateDetail(id) {
  return request({
    url: `/aggregate/${id}`,
    method: 'get'
  })
}

// 创建聚合接口
export function createAggregate(data) {
  return request({
    url: '/aggregate/create',
    method: 'post',
    data
  })
}

// 保存聚合接口配置
export function saveAggregate(data) {
  return request({
    url: '/aggregate/save',
    method: 'post',
    data
  })
}

// 发布聚合接口
export function publishAggregate(id) {
  return request({
    url: `/aggregate/${id}/publish`,
    method: 'post'
  })
}

// 下线聚合接口
export function offlineAggregate(id) {
  return request({
    url: `/aggregate/${id}/offline`,
    method: 'post'
  })
}

// 删除聚合接口
export function deleteAggregate(id) {
  return request({
    url: `/aggregate/${id}`,
    method: 'delete'
  })
}

// 测试执行聚合接口
export function testAggregate(id, params) {
  return request({
    url: `/aggregate/${id}/test`,
    method: 'post',
    data: params
  })
}
