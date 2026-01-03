package com.api.model;

import lombok.Data;
import java.util.Date;

@Data
public class ApiCallLog {
  private Long id;
  private Long apiId;
  private String apiName;
  private String apiPath;
  private Long platformId;
  private String platformName;
  private Integer statusCode;
  private Integer responseTime;
  private Integer isSuccess;
  private String errorMsg;
  private String requestParams;
  private String responseData;
  private Date callTime;
}
