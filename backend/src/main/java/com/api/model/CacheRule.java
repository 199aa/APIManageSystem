package com.api.model;

import lombok.Data;
import java.util.Date;

@Data
public class CacheRule {
    private Long id;
    private Long apiId;
    private String apiName;
    private String cacheKey;
    private Integer ttl; // 缓存时长（秒）
    private Double hitRate; // 命中率
    private String cacheSize; // 缓存大小
    private Date createTime;
    private Date updateTime;
}
