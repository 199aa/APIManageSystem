package com.api.model;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class User implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long id;
  private String username;
  private String password;
  private String realName;
  private String email;
  private String phone;
  private String avatar;
  private Long roleId;
  private String roleName; // 角色名称，用于显示
  private Integer status;
  private Date lastLoginTime;
  private Date createTime;
  private Date updateTime;
}
