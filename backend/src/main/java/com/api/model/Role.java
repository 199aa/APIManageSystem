package com.api.model;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class Role implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long id;
  private String name;
  private String code;
  private String description;
  private Integer status;
  private Integer isSystem; // 是否系统角色
  private Date createTime;
  private Date updateTime;

  // 用于权限管理
  private List<Long> permissionIds;
}
