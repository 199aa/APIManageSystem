package com.api.task;

import com.api.mapper.AlertMapper;
import com.api.mapper.AlertRuleMapper;
import com.api.mapper.ApiCallLogMapper;
import com.api.mapper.PlatformMapper;
import com.api.model.Alert;
import com.api.model.AlertRule;
import com.api.model.Platform;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 告警规则检查定时任务
 * 定时检查告警规则条件，自动触发告警
 */
@Component
public class AlertCheckTask {

    @Autowired
    private AlertRuleMapper alertRuleMapper;

    @Autowired
    private AlertMapper alertMapper;

    @Autowired
    private ApiCallLogMapper apiCallLogMapper;

    @Autowired
    private PlatformMapper platformMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 每分钟执行一次告警规则检查
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkAlertRules() {
        System.out.println("========================================");
        System.out.println("[告警检查] 开始检查告警规则... " + new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()));

        try {
            // 获取所有启用的告警规则
            List<AlertRule> rules = alertRuleMapper.selectActiveRules();

            if (rules == null || rules.isEmpty()) {
                System.out.println("[告警检查] 没有启用的告警规则");
                System.out.println("========================================");
                return;
            }
            
            System.out.println("[告警检查] 找到 " + rules.size() + " 条启用的规则");

            int alertCount = 0;

            for (AlertRule rule : rules) {
                try {
                    System.out.println("[告警检查] 正在检查规则: " + rule.getName() + " (类型: " + rule.getType() + ", 条件: " + rule.getCondition() + ")");
                    boolean triggered = checkRule(rule);
                    if (triggered) {
                        alertCount++;
                    }
                } catch (Exception e) {
                    System.err.println("[告警检查] 规则 [" + rule.getName() + "] 检查失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("[告警检查] 检查完成，共触发 " + alertCount + " 个告警");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("[告警检查] 任务执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 检查单个规则是否触发
     */
    private boolean checkRule(AlertRule rule) {
        String type = rule.getType();
        if (type == null) {
            System.out.println("[告警检查] 规则类型为空，跳过");
            return false;
        }

        boolean triggered = false;
        String alertContent = null;
        String targetName = null;

        try {
            // 解析条件JSON
            JsonNode condition = objectMapper.readTree(rule.getCondition());
            
            // 获取阈值（支持多种字段名和格式）
            double threshold = 0;
            if (condition.has("threshold")) {
                JsonNode thresholdNode = condition.get("threshold");
                if (thresholdNode.isNumber()) {
                    threshold = thresholdNode.asDouble();
                } else {
                    threshold = Double.parseDouble(thresholdNode.asText());
                }
            } else if (condition.has("percent")) {
                // 兼容使用 percent 作为阈值的情况
                JsonNode percentNode = condition.get("percent");
                if (percentNode.isNumber()) {
                    threshold = percentNode.asDouble();
                } else {
                    threshold = Double.parseDouble(percentNode.asText());
                }
            }
            
            System.out.println("[告警检查] 解析阈值: " + threshold);

            switch (type) {
                case "error_rate":
                    // 错误率告警
                    triggered = checkErrorRate(threshold);
                    if (triggered) {
                        alertContent = "错误率超过阈值 " + threshold + "%";
                        targetName = getTargetName(rule);
                    }
                    break;

                case "response_time":
                    // 响应时间告警
                    triggered = checkResponseTime((int) threshold);
                    if (triggered) {
                        alertContent = "平均响应时间超过 " + (int) threshold + "ms";
                        targetName = getTargetName(rule);
                    }
                    break;

                case "call_count":
                case "traffic":
                    // 流量/调用量告警
                    triggered = checkTraffic((int) threshold);
                    if (triggered) {
                        alertContent = "今日调用量超过阈值 " + (int) threshold + " 次";
                        targetName = getTargetName(rule);
                    }
                    break;

                case "success_rate":
                    // 成功率告警（成功率低于阈值时触发）
                    triggered = checkSuccessRate(threshold);
                    if (triggered) {
                        alertContent = "成功率低于阈值 " + threshold + "%";
                        targetName = getTargetName(rule);
                    }
                    break;

                case "platform_offline":
                    // 平台离线告警
                    triggered = checkPlatformOffline(rule);
                    if (triggered) {
                        alertContent = "平台离线";
                        targetName = getTargetName(rule);
                    }
                    break;

                default:
                    System.out.println("[告警检查] 未知规则类型: " + type);
                    return false;
            }

            // 如果触发了告警，创建告警记录
            if (triggered) {
                createAlert(rule, alertContent, targetName);
                System.out.println("[告警检查] 规则 [" + rule.getName() + "] 触发告警: " + alertContent);
            }

        } catch (Exception e) {
            System.err.println("[告警检查] 解析规则条件失败: " + e.getMessage());
        }

        return triggered;
    }

    /**
     * 检查错误率
     */
    private boolean checkErrorRate(double threshold) {
        try {
            // 获取今日的调用统计
            long totalCalls = apiCallLogMapper.countToday();
            long successCalls = apiCallLogMapper.countTodaySuccess();

            if (totalCalls == 0) {
                return false;
            }

            double errorRate = (1.0 - (double) successCalls / totalCalls) * 100;

            System.out.println("[告警检查] 错误率检查 - 总调用: " + totalCalls + ", 成功: " + successCalls + ", 错误率: " + String.format("%.2f", errorRate) + "%, 阈值: " + threshold + "%");

            return errorRate >= threshold;

        } catch (Exception e) {
            System.err.println("[告警检查] 错误率检查失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查成功率（成功率低于阈值时触发）
     */
    private boolean checkSuccessRate(double threshold) {
        try {
            long totalCalls = apiCallLogMapper.countToday();
            long successCalls = apiCallLogMapper.countTodaySuccess();

            if (totalCalls == 0) {
                return false;
            }

            double successRate = ((double) successCalls / totalCalls) * 100;

            System.out.println("[告警检查] 成功率检查 - 总调用: " + totalCalls + ", 成功: " + successCalls + ", 成功率: " + String.format("%.2f", successRate) + "%, 阈值: " + threshold + "%");

            return successRate < threshold;

        } catch (Exception e) {
            System.err.println("[告警检查] 成功率检查失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查响应时间
     */
    private boolean checkResponseTime(int threshold) {
        try {
            Double avgTime = apiCallLogMapper.avgResponseTime();
            if (avgTime == null) {
                return false;
            }

            System.out.println("[告警检查] 响应时间检查 - 平均响应时间: " + avgTime.intValue() + "ms, 阈值: " + threshold + "ms");

            return avgTime >= threshold;

        } catch (Exception e) {
            System.err.println("[告警检查] 响应时间检查失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查流量
     */
    private boolean checkTraffic(int threshold) {
        try {
            long todayCalls = apiCallLogMapper.countToday();

            System.out.println("[告警检查] 流量检查 - 今日调用: " + todayCalls + ", 阈值: " + threshold);

            return todayCalls >= threshold;

        } catch (Exception e) {
            System.err.println("[告警检查] 流量检查失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查平台是否离线
     */
    private boolean checkPlatformOffline(AlertRule rule) {
        try {
            if (rule.getTargetId() == null) {
                // 检查所有平台
                int offlineCount = platformMapper.countOfflinePlatforms();
                return offlineCount > 0;
            } else {
                // 检查特定平台
                Platform platform = platformMapper.selectById(rule.getTargetId());
                return platform != null && "OFFLINE".equals(platform.getHealthStatus());
            }
        } catch (Exception e) {
            System.err.println("[告警检查] 平台离线检查失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取目标名称
     */
    private String getTargetName(AlertRule rule) {
        if ("platform".equals(rule.getTarget()) && rule.getTargetId() != null) {
            try {
                Platform platform = platformMapper.selectById(rule.getTargetId());
                return platform != null ? platform.getName() : "未知平台";
            } catch (Exception e) {
                return "未知平台";
            }
        }
        return rule.getTarget() != null ? rule.getTarget() : "系统";
    }

    /**
     * 创建告警记录
     */
    private void createAlert(AlertRule rule, String content, String targetName) {
        try {
            // 检查是否已有相同的未解决告警（避免重复告警）
            List<Alert> activeAlerts = alertMapper.selectActiveAlerts(100);
            for (Alert existingAlert : activeAlerts) {
                if (rule.getName().equals(existingAlert.getRuleName()) 
                    && "firing".equals(existingAlert.getStatus())) {
                    System.out.println("[告警检查] 规则 [" + rule.getName() + "] 已有未解决的告警，跳过创建");
                    return;
                }
            }

            Alert alert = new Alert();
            alert.setRuleName(rule.getName());
            alert.setLevel(rule.getLevel());
            alert.setContent(content);
            alert.setTarget(targetName);
            alert.setStatus("firing");
            alert.setAlertTime(new Date());

            if (rule.getTargetId() != null) {
                if ("platform".equals(rule.getTarget())) {
                    alert.setPlatformId(rule.getTargetId());
                } else if ("api".equals(rule.getTarget())) {
                    alert.setApiId(rule.getTargetId());
                }
            }

            alertMapper.insert(alert);
            System.out.println("[告警检查] 成功创建告警: " + content);

        } catch (Exception e) {
            System.err.println("[告警检查] 创建告警失败: " + e.getMessage());
        }
    }

    /**
     * 启动时延迟30秒后执行一次初始化检查
     */
    @Scheduled(initialDelay = 30000, fixedDelay = Long.MAX_VALUE)
    public void initializeAlertCheck() {
        System.out.println("[告警检查] 执行初始化告警规则检查...");
        checkAlertRules();
    }
}
