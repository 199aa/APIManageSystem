package com.api.model;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class AlertRule implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name; // 规则名称
    private String type; // 规则类型：error_rate, response_time, traffic
    private String level; // 告警级别：critical, warning, info
    private String condition; // 触发条件（JSON）
    private String target; // 监控目标：api, platform, system
    private Long targetId; // 目标ID
    private Integer status; // 状态：1-启用，0-禁用
    private String notifyChannels; // 通知渠道（JSON）
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
