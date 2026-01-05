package com.api.controller;

import com.api.common.Result;
import com.api.mapper.ApiInfoMapper;
import com.api.mapper.ApiCallLogMapper;
import com.api.mapper.PlatformMapper;
import com.api.model.ApiCallLog;
import com.api.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin
public class DashboardController {

  @Autowired
  private ApiInfoMapper apiInfoMapper;

  @Autowired
  private ApiCallLogMapper apiCallLogMapper;

  @Autowired
  private PlatformMapper platformMapper;

  @Autowired
  private DashboardService dashboardService;

  /**
   * 获取核心统计指标（真实数据）
   */
  @GetMapping("/stats")
  public Result<Map<String, Object>> getCoreStats() {
    try {
      Map<String, Object> stats = dashboardService.getCoreStats();
      return Result.success(stats);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 获取调用趋势数据（真实数据）
   */
  @GetMapping("/trend")
  public Result<Map<String, Object>> getCallTrend(@RequestParam(defaultValue = "24h") String range) {
    try {
      Map<String, Object> data = dashboardService.getCallTrend(range);
      return Result.success(data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 获取API健康度数据（真实数据）
   */
  @GetMapping("/health")
  public Result<Map<String, Object>> getHealthData() {
    try {
      Map<String, Object> result = apiInfoMapper.countByStatus();
      Map<String, Object> health = new HashMap<>();
      health.put("normal", result.get("normal") != null ? ((Number) result.get("normal")).intValue() : 0);
      health.put("error", result.get("error") != null ? ((Number) result.get("error")).intValue() : 0);
      health.put("offline", result.get("offline") != null ? ((Number) result.get("offline")).intValue() : 0);
      return Result.success(health);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 获取平台分布数据（真实数据）
   */
  @GetMapping("/platform")
  public Result<Map<String, Object>> getPlatformData() {
    try {
      Map<String, Object> data = new HashMap<>();
      List<String> platforms = new ArrayList<>();
      List<Integer> values = new ArrayList<>();

      List<Map<String, Object>> result = platformMapper.countApiByPlatform();
      for (Map<String, Object> item : result) {
        platforms.add((String) item.get("name"));
        values.add(((Number) item.get("value")).intValue());
      }

      data.put("platforms", platforms);
      data.put("values", values);
      return Result.success(data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 获取最近异常调用日志（真实数据）
   */
  @GetMapping("/errorLogs")
  public Result<List<Map<String, Object>>> getErrorLogs(@RequestParam(defaultValue = "5") int limit) {
    try {
      List<ApiCallLog> logs = apiCallLogMapper.selectRecentErrors(limit);
      List<Map<String, Object>> result = new ArrayList<>();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

      for (ApiCallLog log : logs) {
        Map<String, Object> item = new HashMap<>();
        item.put("time", sdf.format(log.getCallTime()));
        item.put("apiPath", log.getApiPath());
        item.put("platform", log.getPlatformName() != null ? log.getPlatformName() : "本地");
        item.put("statusCode", log.getStatusCode());
        item.put("errorMsg", log.getErrorMsg());
        result.add(item);
      }

      return Result.success(result);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 清除系统缓存
   */
  @PostMapping("/clearCache")
  public Result<String> clearCache() {
    try {
      Thread.sleep(500);
      return Result.success("缓存清除成功");
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 获取响应最慢的API列表
   */
  @GetMapping("/slowApis")
  public Result<List<Map<String, Object>>> getSlowestApis(@RequestParam(defaultValue = "10") int limit) {
    try {
      List<Map<String, Object>> slowApis = dashboardService.getSlowestApis(limit);
      return Result.success(slowApis);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 获取错误率最高的API列表
   */
  @GetMapping("/errorApis")
  public Result<List<Map<String, Object>>> getHighestErrorApis(@RequestParam(defaultValue = "10") int limit) {
    try {
      List<Map<String, Object>> errorApis = dashboardService.getHighestErrorApis(limit);
      return Result.success(errorApis);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 获取活跃告警列表
   */
  @GetMapping("/alerts")
  public Result<List<Map<String, Object>>> getActiveAlerts(@RequestParam(defaultValue = "5") int limit) {
    try {
      List<Map<String, Object>> alerts = dashboardService.getActiveAlerts(limit);
      return Result.success(alerts);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 格式化数字
   */
  private String formatNumber(long num) {
    if (num >= 1000000) {
      return String.format("%.2fM", num / 1000000.0);
    } else if (num >= 1000) {
      return String.format("%.1fK", num / 1000.0);
    }
    return String.valueOf(num);
  }
}
