import request from '@/utils/request'

/**
 * 获取核心统计指标
 */
export function getCoreStats() {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  })
}

/**
 * 获取调用趋势数据
 * @param {string} range - 时间范围: 24h, 7d, 30d
 */
export function getCallTrend(range = '24h') {
  return request({
    url: '/dashboard/trend',
    method: 'get',
    params: { range }
  })
}

/**
 * 获取API健康度数据
 */
export function getHealthData() {
  return request({
    url: '/dashboard/health',
    method: 'get'
  })
}

/**
 * 获取平台分布数据
 */
export function getPlatformData() {
  return request({
    url: '/dashboard/platform',
    method: 'get'
  })
}

/**
 * 获取最近异常调用日志
 * @param {number} limit - 返回条数
 */
export function getErrorLogs(limit = 5) {
  return request({
    url: '/dashboard/errorLogs',
    method: 'get',
    params: { limit }
  })
}

/**
 * 清除系统缓存
 */
export function clearCache() {
  return request({
    url: '/dashboard/clearCache',
    method: 'post'
  })
}

/**
 * 获取响应最慢的API列表
 * @param {number} limit - 返回条数
 */
export function getSlowestApis(limit = 10) {
  return request({
    url: '/dashboard/slowApis',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取错误率最高的API列表
 * @param {number} limit - 返回条数
 */
export function getHighestErrorApis(limit = 10) {
  return request({
    url: '/dashboard/errorApis',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取活跃告警列表
 * @param {number} limit - 返回条数
 */
export function getActiveAlerts(limit = 5) {
  return request({
    url: '/dashboard/alerts',
    method: 'get',
    params: { limit }
  })
}
