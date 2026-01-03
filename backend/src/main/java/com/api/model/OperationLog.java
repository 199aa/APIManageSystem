package com.api.model;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class OperationLog implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long id;
  private Long userId;
  private String username;
  private String operType; // LOGIN/CREATE/UPDATE/DELETE/EXPORT
  private String module;
  private String description;
  private String requestMethod;
  private String requestUrl;
  private String requestParams;
  private String responseResult;
  private String ip;
  private String browser;
  private Integer status; // 1-成功，0-失败
  private String errorMsg;
  private Integer costTime; // 耗时(ms)
  private Date operTime;
}
