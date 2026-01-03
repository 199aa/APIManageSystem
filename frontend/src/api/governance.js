import request from '@/utils/request'

// ==================== 限流策略 ====================

// 获取限流规则列表
export function getRateLimitList() {
  return request({
    url: '/governance/rateLimit/list',
    method: 'get'
  })
}

// 保存限流规则
export function saveRateLimit(data) {
  return request({
    url: '/governance/rateLimit/save',
    method: 'post',
    data
  })
}

// 删除限流规则
export function deleteRateLimit(id) {
  return request({
    url: `/governance/rateLimit/delete/${id}`,
    method: 'delete'
  })
}

// 更新限流规则状态
export function updateRateLimitStatus(id, status) {
  return request({
    url: `/governance/rateLimit/status/${id}/${status}`,
    method: 'put'
  })
}

// ==================== 黑白名单 ====================

// 获取黑白名单
export function getBlacklist() {
  return request({
    url: '/governance/blacklist/get',
    method: 'get'
  })
}

// 保存黑名单
export function saveBlacklist(ips) {
  return request({
    url: '/governance/blacklist/saveBlacklist',
    method: 'post',
    data: ips
  })
}

// 保存白名单
export function saveWhitelist(ips) {
  return request({
    url: '/governance/blacklist/saveWhitelist',
    method: 'post',
    data: ips
  })
}

// ==================== 缓存策略 ====================

// 获取缓存规则列表
export function getCacheRuleList() {
  return request({
    url: '/governance/cache/list',
    method: 'get'
  })
}

// 保存缓存规则
export function saveCacheRule(data) {
  return request({
    url: '/governance/cache/save',
    method: 'post',
    data
  })
}

// 删除缓存规则
export function deleteCacheRule(id) {
  return request({
    url: `/governance/cache/delete/${id}`,
    method: 'delete'
  })
}

// 清除缓存
export function clearCache(id) {
  return request({
    url: `/governance/cache/clear/${id}`,
    method: 'post'
  })
}
