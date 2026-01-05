package com.api.controller;

import com.api.annotation.OperationLog;
import com.api.common.Result;
import com.api.model.Alert;
import com.api.model.AlertRule;
import com.api.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/monitor/alert")
@CrossOrigin
public class AlertController {

    @Autowired
    private AlertService alertService;

    /**
     * 获取告警列表（分页）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getAlertList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        try {
            Map<String, Object> result = alertService.getAlertList(page, pageSize, level, status, startTime, endTime);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取告警列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取活跃告警
     */
    @GetMapping("/active")
    public Result<List<Alert>> getActiveAlerts(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Alert> alerts = alertService.getActiveAlerts(limit);
            return Result.success(alerts);
        } catch (Exception e) {
            return Result.error("获取活跃告警失败: " + e.getMessage());
        }
    }

    /**
     * 获取告警统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Integer>> getAlertStats() {
        try {
            Map<String, Integer> stats = alertService.getAlertStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取告警统计失败: " + e.getMessage());
        }
    }

    /**
     * 确认告警
     */
    @PostMapping("/acknowledge/{id}")
    @OperationLog(module = "告警管理", type = "确认", description = "确认告警")
    public Result<Void> acknowledgeAlert(@PathVariable Long id,
            @RequestBody(required = false) Map<String, String> data) {
        try {
            String acknowledgedBy = data != null ? data.get("acknowledgedBy") : "系统";
            alertService.acknowledgeAlert(id, acknowledgedBy);
            return Result.success();
        } catch (Exception e) {
            return Result.error("确认告警失败: " + e.getMessage());
        }
    }

    /**
     * 批量确认告警
     */
    @PostMapping("/acknowledge/batch")
    @OperationLog(module = "告警管理", type = "批量确认", description = "批量确认告警")
    public Result<Void> batchAcknowledgeAlerts(@RequestBody Map<String, Object> data) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> idsInt = (List<Integer>) data.get("ids");
            List<Long> ids = idsInt.stream().map(Long::valueOf).collect(java.util.stream.Collectors.toList());
            String acknowledgedBy = (String) data.getOrDefault("acknowledgedBy", "系统");

            alertService.batchAcknowledgeAlerts(ids, acknowledgedBy);
            return Result.success();
        } catch (Exception e) {
            return Result.error("批量确认告警失败: " + e.getMessage());
        }
    }

    /**
     * 解决告警
     */
    @PostMapping("/resolve/{id}")
    @OperationLog(module = "告警管理", type = "解决", description = "解决告警")
    public Result<Void> resolveAlert(@PathVariable Long id) {
        try {
            alertService.resolveAlert(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("解决告警失败: " + e.getMessage());
        }
    }

    // ==================== 告警规则管理 ====================

    /**
     * 获取所有告警规则
     */
    @GetMapping("/rule/list")
    public Result<List<AlertRule>> getAllAlertRules() {
        try {
            List<AlertRule> rules = alertService.getAllAlertRules();
            return Result.success(rules);
        } catch (Exception e) {
            return Result.error("获取告警规则失败: " + e.getMessage());
        }
    }

    /**
     * 创建告警规则
     */
    @PostMapping("/rule/create")
    @OperationLog(module = "告警规则", type = "创建", description = "创建告警规则")
    public Result<AlertRule> createAlertRule(@RequestBody AlertRule rule) {
        try {
            AlertRule newRule = alertService.createAlertRule(rule);
            return Result.success(newRule);
        } catch (Exception e) {
            return Result.error("创建告警规则失败: " + e.getMessage());
        }
    }

    /**
     * 更新告警规则
     */
    @PutMapping("/rule/update")
    @OperationLog(module = "告警规则", type = "更新", description = "更新告警规则")
    public Result<AlertRule> updateAlertRule(@RequestBody AlertRule rule) {
        try {
            AlertRule updatedRule = alertService.updateAlertRule(rule);
            return Result.success(updatedRule);
        } catch (Exception e) {
            return Result.error("更新告警规则失败: " + e.getMessage());
        }
    }

    /**
     * 删除告警规则
     */
    @DeleteMapping("/rule/delete/{id}")
    @OperationLog(module = "告警规则", type = "删除", description = "删除告警规则")
    public Result<Void> deleteAlertRule(@PathVariable Long id) {
        try {
            alertService.deleteAlertRule(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除告警规则失败: " + e.getMessage());
        }
    }

    /**
     * 更新规则状态
     */
    @PutMapping("/rule/status")
    @OperationLog(module = "告警规则", type = "更新状态", description = "更新告警规则状态")
    public Result<Void> updateRuleStatus(@RequestBody Map<String, Object> data) {
        try {
            Long id = Long.parseLong(data.get("id").toString());
            Integer status = Integer.parseInt(data.get("status").toString());
            alertService.updateRuleStatus(id, status);
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新规则状态失败: " + e.getMessage());
        }
    }
}
