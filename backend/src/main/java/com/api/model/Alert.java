package com.api.model;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class Alert implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String ruleName; // 规则名称
    private String level; // 告警级别：critical, warning, info
    private String content; // 告警内容
    private String target; // 告警对象（API路径、平台名等）
    private String status; // 状态：firing-触发中, resolved-已恢复, acknowledged-已确认
    private String traceId; // 关联的TraceID
    private Long apiId; // 关联的API ID
    private Long platformId; // 关联的平台ID
    private String details; // 详细信息（JSON）
    private Date alertTime; // 告警时间
    private Date resolvedTime; // 恢复时间
    private Date acknowledgedTime; // 确认时间
    private String acknowledgedBy; // 确认人
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
