package com.api.controller;

import com.api.common.Result;
import com.api.mapper.ApiCallLogMapper;
import com.api.model.ApiCallLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/monitor/api-log")
@CrossOrigin
public class ApiCallLogController {

  @Autowired
  private ApiCallLogMapper apiCallLogMapper;

  @GetMapping("/list")
  public Result<Map<String, Object>> getLogs(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) String traceId,
      @RequestParam(required = false) String apiPath,
      @RequestParam(required = false) Integer statusCode,
      @RequestParam(required = false) Integer minTime,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {

    Map<String, Object> params = new HashMap<>();
    params.put("apiPath", apiPath);
    params.put("statusCode", statusCode);
    params.put("minTime", minTime);
    params.put("startDate", startDate);
    params.put("endDate", endDate);

    int offset = (page - 1) * pageSize;
    params.put("offset", offset);
    params.put("limit", pageSize);

    List<ApiCallLog> list = apiCallLogMapper.selectList(params);
    int total = apiCallLogMapper.countByCondition(params);

    Map<String, Object> result = new HashMap<>();
    result.put("list", list);
    result.put("total", total);
    result.put("page", page);
    result.put("pageSize", pageSize);

    return Result.success(result);
  }
}
