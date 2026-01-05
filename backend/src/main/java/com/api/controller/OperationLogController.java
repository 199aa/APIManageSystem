package com.api.controller;

import com.api.common.Result;
import com.api.model.OperationLog;
import com.api.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/log")
@CrossOrigin
public class OperationLogController {

  @Autowired
  private OperationLogService operationLogService;

  /**
   * 分页查询操作日志
   */
  @GetMapping("/list")
  public Result<Map<String, Object>> getLogsByPage(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String operType,
      @RequestParam(required = false) String module,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("page", page);
      params.put("pageSize", pageSize);
      params.put("username", username);
      params.put("operType", operType);
      params.put("module", module);
      params.put("status", status);
      params.put("startDate", startDate);
      params.put("endDate", endDate);

      Map<String, Object> result = operationLogService.getLogsByPage(params);
      return Result.success(result);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 根据ID查询日志详情
   */
  @GetMapping("/{id}")
  public Result<OperationLog> getLogById(@PathVariable Long id) {
    try {
      OperationLog log = operationLogService.getLogById(id);
      if (log == null) {
        return Result.error("日志不存在");
      }
      return Result.success(log);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 批量删除日志
   */
  @DeleteMapping("/batch")
  public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
    try {
      operationLogService.deleteBatch(ids);
      return Result.success(null);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 清理过期日志
   */
  @DeleteMapping("/clean")
  public Result<Void> cleanExpiredLogs(@RequestParam(defaultValue = "90") int days) {
    try {
      operationLogService.cleanExpiredLogs(days);
      return Result.success(null);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 导出操作日志
   */
  @GetMapping("/export")
  public Result<String> exportLogs(
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String operType,
      @RequestParam(required = false) String module,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("username", username);
      params.put("operType", operType);
      params.put("module", module);
      params.put("status", status);
      params.put("startDate", startDate);
      params.put("endDate", endDate);

      String filePath = operationLogService.exportLogs(params);
      return Result.success(filePath);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
}
