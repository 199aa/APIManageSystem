package com.api.model;

import lombok.Data;
import java.util.Date;

@Data
public class RateLimit {
    private Long id;
    private String name;
    private String targetType;  // api, app, global
    private Long targetId;
    private String targetName;
    private Integer limitValue;  // QPS限制值
    private Integer status;  // 0-禁用, 1-启用
    private Date createTime;
    private Date updateTime;
}
