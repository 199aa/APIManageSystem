package com.api.model;

import lombok.Data;
import java.util.Date;

@Data
public class Platform {
  private Long id;
  private String name;
  private String code;
  private String icon;
  private String baseUrl;
  private String description;
  private String contact;
  private String phone;

  // 认证配置
  private String authType; // NO_AUTH, API_KEY, BEARER_TOKEN, BASIC_AUTH
  private String authConfig; // JSON格式的认证配置（加密存储）

  // 健康状态
  private String healthStatus; // ONLINE, OFFLINE, UNKNOWN
  private Date lastCheckTime; // 最后检测时间

  // API发现
  private String swaggerUrl; // Swagger文档地址

  // 监控配置
  private Integer checkInterval; // 检测频率（秒）
  private String environment; // DEV, TEST, PROD
  private Integer timeout; // 超时时间（毫秒）
  private Integer retryCount; // 重试次数

  // 环境配置
  private String envConfig; // 多环境配置（JSON格式）

  private Integer status; // 启用状态：1-启用，0-禁用
  private Date createTime;
  private Date updateTime;
}
