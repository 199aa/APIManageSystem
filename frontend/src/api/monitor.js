import request from '@/utils/request'

// ==================== 调用日志 ====================

// 分页查询API调用日志
export function getApiCallLogs(params) {
  return request({
    url: '/monitor/api-log/list',
    method: 'get',
    params
  })
}

// ==================== 告警管理 ====================

/**
 * 获取告警列表（分页）
 */
export function getAlertList(params) {
  return request({
    url: '/monitor/alert/list',
    method: 'get',
    params
  })
}

/**
 * 获取活跃告警
 */
export function getActiveAlerts(limit = 10) {
  return request({
    url: '/monitor/alert/active',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取告警统计
 */
export function getAlertStats() {
  return request({
    url: '/monitor/alert/stats',
    method: 'get'
  })
}

/**
 * 确认告警
 */
export function acknowledgeAlert(id, data) {
  return request({
    url: `/monitor/alert/acknowledge/${id}`,
    method: 'post',
    data
  })
}

/**
 * 批量确认告警
 */
export function batchAcknowledgeAlerts(data) {
  return request({
    url: '/monitor/alert/acknowledge/batch',
    method: 'post',
    data
  })
}

/**
 * 解决告警
 */
export function resolveAlert(id) {
  return request({
    url: `/monitor/alert/resolve/${id}`,
    method: 'post'
  })
}

// ==================== 告警规则 ====================

/**
 * 获取告警规则列表
 */
export function getAlertRules() {
  return request({
    url: '/monitor/alert/rule/list',
    method: 'get'
  })
}

/**
 * 创建告警规则
 */
export function createAlertRule(data) {
  return request({
    url: '/monitor/alert/rule/create',
    method: 'post',
    data
  })
}

/**
 * 更新告警规则
 */
export function updateAlertRule(data) {
  return request({
    url: '/monitor/alert/rule/update',
    method: 'put',
    data
  })
}

/**
 * 删除告警规则
 */
export function deleteAlertRule(id) {
  return request({
    url: `/monitor/alert/rule/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 更新规则状态
 */
export function updateRuleStatus(data) {
  return request({
    url: '/monitor/alert/rule/status',
    method: 'put',
    data
  })
}
