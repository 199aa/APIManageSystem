package com.api.model;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class ApiInfo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long id;
  private String name;
  private String path;
  private String method;
  private String description;
  private Long platformId;
  private String platformName; // 关联查询字段
  private Integer isAggregate;
  private Integer status;
  private String requestParams;
  private String responseExample;
  private String aggregateConfig; // 聚合接口配置(JSON格式)
  private Date createTime;
  private Date updateTime;
}
