package com.api.model;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class Permission {
  private Long id;
  private String name;
  private String code;
  private String type;
  private Long parentId;
  private String icon;
  private String path;
  private Integer sortOrder;
  private Integer status;
  private Date createTime;
  private Date updateTime;

  // 树形结构需要的子节点
  private List<Permission> children;
}
