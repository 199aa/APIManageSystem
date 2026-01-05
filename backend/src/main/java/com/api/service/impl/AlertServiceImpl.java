package com.api.service.impl;

import com.api.mapper.AlertMapper;
import com.api.mapper.AlertRuleMapper;
import com.api.model.Alert;
import com.api.model.AlertRule;
import com.api.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlertServiceImpl implements AlertService {

    @Autowired
    private AlertMapper alertMapper;

    @Autowired
    private AlertRuleMapper alertRuleMapper;

    @Override
    @Transactional
    public Alert createAlert(Alert alert) {
        if (alert.getAlertTime() == null) {
            alert.setAlertTime(new Date());
        }
        if (alert.getStatus() == null) {
            alert.setStatus("firing");
        }
        alertMapper.insert(alert);
        return alert;
    }

    @Override
    public Map<String, Object> getAlertList(int page, int pageSize, String level, String status, Date startTime,
            Date endTime) {
        int offset = (page - 1) * pageSize;
        List<Alert> list = alertMapper.selectPage(offset, pageSize, level, status, startTime, endTime);
        int total = alertMapper.countByCondition(level, status, startTime, endTime);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);

        return result;
    }

    @Override
    public List<Alert> getActiveAlerts(int limit) {
        return alertMapper.selectActiveAlerts(limit);
    }

    @Override
    public Map<String, Integer> getAlertStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("critical", alertMapper.countByLevel("critical", "firing"));
        stats.put("warning", alertMapper.countByLevel("warning", "firing"));
        stats.put("info", alertMapper.countByLevel("info", "firing"));
        stats.put("resolved", alertMapper.countByLevel(null, "resolved"));
        return stats;
    }

    @Override
    @Transactional
    public void acknowledgeAlert(Long id, String acknowledgedBy) {
        Alert alert = new Alert();
        alert.setId(id);
        alert.setStatus("acknowledged");
        alert.setAcknowledgedBy(acknowledgedBy);
        alert.setAcknowledgedTime(new Date());
        alertMapper.updateById(alert);
    }

    @Override
    @Transactional
    public void batchAcknowledgeAlerts(List<Long> ids, String acknowledgedBy) {
        if (ids != null && !ids.isEmpty()) {
            alertMapper.batchAcknowledge(ids, acknowledgedBy, new Date());
        }
    }

    @Override
    @Transactional
    public void resolveAlert(Long id) {
        Alert alert = new Alert();
        alert.setId(id);
        alert.setStatus("resolved");
        alert.setResolvedTime(new Date());
        alertMapper.updateById(alert);
    }

    @Override
    @Transactional
    public AlertRule createAlertRule(AlertRule rule) {
        if (rule.getStatus() == null) {
            rule.setStatus(1);
        }
        alertRuleMapper.insert(rule);
        return rule;
    }

    @Override
    @Transactional
    public AlertRule updateAlertRule(AlertRule rule) {
        alertRuleMapper.updateById(rule);
        return alertRuleMapper.selectById(rule.getId());
    }

    @Override
    @Transactional
    public void deleteAlertRule(Long id) {
        alertRuleMapper.deleteById(id);
    }

    @Override
    public List<AlertRule> getAllAlertRules() {
        return alertRuleMapper.selectAll();
    }

    @Override
    @Transactional
    public void updateRuleStatus(Long id, Integer status) {
        alertRuleMapper.updateStatus(id, status);
    }
}
