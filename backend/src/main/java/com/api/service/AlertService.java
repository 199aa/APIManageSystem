package com.api.service;

import com.api.model.Alert;
import com.api.model.AlertRule;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface AlertService {
    
    /**
     * 创建告警
     */
    Alert createAlert(Alert alert);
    
    /**
     * 分页查询告警列表
     */
    Map<String, Object> getAlertList(int page, int pageSize, String level, String status, Date startTime, Date endTime);
    
    /**
     * 获取活跃告警列表
     */
    List<Alert> getActiveAlerts(int limit);
    
    /**
     * 获取告警统计
     */
    Map<String, Integer> getAlertStats();
    
    /**
     * 确认告警
     */
    void acknowledgeAlert(Long id, String acknowledgedBy);
    
    /**
     * 批量确认告警
     */
    void batchAcknowledgeAlerts(List<Long> ids, String acknowledgedBy);
    
    /**
     * 解决告警
     */
    void resolveAlert(Long id);
    
    /**
     * 创建告警规则
     */
    AlertRule createAlertRule(AlertRule rule);
    
    /**
     * 更新告警规则
     */
    AlertRule updateAlertRule(AlertRule rule);
    
    /**
     * 删除告警规则
     */
    void deleteAlertRule(Long id);
    
    /**
     * 获取所有告警规则
     */
    List<AlertRule> getAllAlertRules();
    
    /**
     * 更新规则状态
     */
    void updateRuleStatus(Long id, Integer status);
}
