package com.api.controller;

import com.api.common.Result;
import com.api.model.ApiInfo;
import com.api.model.ApiCallLog;
import com.api.mapper.ApiInfoMapper;
import com.api.mapper.PlatformMapper;
import com.api.mapper.ApiCallLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api-info")
public class ApiInfoController {

  @Autowired
  private ApiInfoMapper apiInfoMapper;

  @Autowired
  private PlatformMapper platformMapper;

  @Autowired
  private ApiCallLogMapper apiCallLogMapper;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private RedisTemplate<String, Object> redisTemplate;

  // Redis key 常量
  private static final String REDIS_KEY_TOTAL_CALLS = "dashboard:total_calls";
  private static final String REDIS_KEY_TODAY_CALLS = "dashboard:today_calls:";
  private static final String REDIS_KEY_TODAY_SUCCESS = "dashboard:today_success:";

  /**
   * 获取API列表（分页）
   */
  @GetMapping("/list")
  public Result list(@RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer pageSize,
      @RequestParam(required = false) Long platformId,
      @RequestParam(required = false) String method,
      @RequestParam(required = false) String name) {
    int offset = (page - 1) * pageSize;
    List<ApiInfo> list = apiInfoMapper.selectPage(offset, pageSize, platformId, method, name);
    int total = apiInfoMapper.countByCondition(platformId, method, name);

    Map<String, Object> data = new HashMap<>();
    data.put("list", list);
    data.put("total", total);
    data.put("page", page);
    data.put("pageSize", pageSize);

    return Result.success(data);
  }

  /**
   * 获取API详情
   */
  @GetMapping("/{id}")
  public Result detail(@PathVariable Long id) {
    ApiInfo apiInfo = apiInfoMapper.selectById(id);
    if (apiInfo == null) {
      return Result.error("接口不存在");
    }
    return Result.success(apiInfo);
  }

  /**
   * 保存API（新增/编辑）
   */
  @com.api.annotation.OperationLog(type = "SAVE", module = "api", description = "保存API")
  @PostMapping("/save")
  public Result save(@RequestBody ApiInfo apiInfo) {
    if (apiInfo.getId() == null) {
      apiInfoMapper.insert(apiInfo);
    } else {
      apiInfoMapper.update(apiInfo);
    }
    return Result.success("保存成功");
  }

  /**
   * 删除API
   */
  @com.api.annotation.OperationLog(type = "DELETE", module = "api", description = "删除API")
  @DeleteMapping("/{id}")
  public Result delete(@PathVariable Long id) {
    apiInfoMapper.deleteById(id);
    return Result.success("删除成功");
  }

  /**
   * 克隆API
   */
  @com.api.annotation.OperationLog(type = "CLONE", module = "api", description = "克隆API")
  @PostMapping("/{id}/clone")
  public Result clone(@PathVariable Long id) {
    ApiInfo original = apiInfoMapper.selectById(id);
    if (original == null) {
      return Result.error("原接口不存在");
    }
    original.setId(null);
    original.setName(original.getName() + "_copy");
    apiInfoMapper.insert(original);
    return Result.success("克隆成功");
  }

  /**
   * 测试API（简单模拟）
   */
  @com.api.annotation.OperationLog(type = "TEST", module = "api", description = "测试API")
  @PostMapping("/test")
  public Result test(@RequestBody Map<String, Object> params) {
    long startTime = System.currentTimeMillis();

    // 获取API ID
    Object apiIdObj = params.get("apiId");
    Long apiId = null;
    if (apiIdObj != null) {
      if (apiIdObj instanceof Integer) {
        apiId = ((Integer) apiIdObj).longValue();
      } else if (apiIdObj instanceof Long) {
        apiId = (Long) apiIdObj;
      }
    }

    // 获取API信息
    ApiInfo apiInfo = null;
    if (apiId != null) {
      apiInfo = apiInfoMapper.selectById(apiId);
    }

    if (apiInfo == null) {
      return Result.error("API信息不存在");
    }

    Map<String, Object> response = new HashMap<>();
    int statusCode = 200;
    String statusText = "OK";
    Object responseData = null;

    try {
      // 优先使用前端传递的URL，如果没有则使用API配置的path
      String baseUrl = params.containsKey("url") ? params.get("url").toString() : apiInfo.getPath();

      // 确保URL是绝对路径
      if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
        // 如果path只是相对路径，尝试从path中提取或使用默认的http://localhost
        if (baseUrl.matches("^\\d+/.*")) {
          // 格式: 8083/api/users -> http://localhost:8083/api/users
          baseUrl = "http://localhost:" + baseUrl;
        } else if (!baseUrl.startsWith("/")) {
          // 格式: api/users -> http://localhost/api/users
          baseUrl = "http://localhost/" + baseUrl;
        } else {
          // 格式: /api/users -> http://localhost/api/users
          baseUrl = "http://localhost" + baseUrl;
        }
      }

      // 获取查询参数
      List<Map<String, String>> queryParams = (List<Map<String, String>>) params.get("queryParams");
      if (queryParams != null && !queryParams.isEmpty()) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);
        for (Map<String, String> param : queryParams) {
          String key = param.get("key");
          String value = param.get("value");
          if (key != null && !key.trim().isEmpty()) {
            builder.queryParam(key, value);
          }
        }
        baseUrl = builder.toUriString();
      }

      // 构建请求头
      HttpHeaders headers = new HttpHeaders();
      List<Map<String, String>> headerList = (List<Map<String, String>>) params.get("headers");
      if (headerList != null && !headerList.isEmpty()) {
        for (Map<String, String> header : headerList) {
          String key = header.get("key");
          String value = header.get("value");
          if (key != null && !key.trim().isEmpty()) {
            headers.add(key, value);
          }
        }
      }

      // 设置默认Content-Type
      if (headers.getContentType() == null) {
        headers.setContentType(MediaType.APPLICATION_JSON);
      }

      // 获取请求体
      Object body = params.get("body");

      // 构建请求
      HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);

      // 根据方法类型发送请求
      String method = apiInfo.getMethod().toUpperCase();
      ResponseEntity<Object> responseEntity;

      switch (method) {
        case "GET":
          responseEntity = restTemplate.exchange(baseUrl, HttpMethod.GET, requestEntity, Object.class);
          break;
        case "POST":
          responseEntity = restTemplate.exchange(baseUrl, HttpMethod.POST, requestEntity, Object.class);
          break;
        case "PUT":
          responseEntity = restTemplate.exchange(baseUrl, HttpMethod.PUT, requestEntity, Object.class);
          break;
        case "DELETE":
          responseEntity = restTemplate.exchange(baseUrl, HttpMethod.DELETE, requestEntity, Object.class);
          break;
        default:
          return Result.error("不支持的HTTP方法: " + method);
      }

      statusCode = responseEntity.getStatusCodeValue();
      statusText = responseEntity.getStatusCode().getReasonPhrase();
      responseData = responseEntity.getBody();

    } catch (Exception e) {
      statusCode = 500;
      statusText = "Error";
      Map<String, Object> errorData = new HashMap<>();
      errorData.put("error", e.getMessage());
      errorData.put("type", e.getClass().getSimpleName());
      // 添加详细的堆栈信息用于调试
      if (e.getCause() != null) {
        errorData.put("cause", e.getCause().getMessage());
      }
      responseData = errorData;
    }

    response.put("statusCode", statusCode);
    response.put("statusText", statusText);
    response.put("response", responseData);

    // 记录API调用日志
    long costTime = System.currentTimeMillis() - startTime;
    ApiCallLog log = new ApiCallLog();
    log.setApiPath(apiInfo.getPath());
    log.setApiName(apiInfo.getName());
    log.setResponseTime((int) costTime);
    log.setStatusCode(statusCode);
    boolean isSuccess = statusCode >= 200 && statusCode < 300;
    log.setIsSuccess(isSuccess ? 1 : 0);

    // 更新Redis计数器
    updateRedisCounters(isSuccess);

    // 异步插入日志，不阻塞响应
    new Thread(() -> {
      try {
        apiCallLogMapper.insert(log);
      } catch (Exception e) {
        System.err.println("插入API调用日志失败: " + e.getMessage());
      }
    }).start();

    return Result.success(response);
  }

  /**
   * 更新Redis计数器
   */
  private void updateRedisCounters(boolean success) {
    try {
      String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

      // 累计调用总数
      redisTemplate.opsForValue().increment(REDIS_KEY_TOTAL_CALLS, 1);

      // 今日调用数
      String todayCallKey = REDIS_KEY_TODAY_CALLS + today;
      redisTemplate.opsForValue().increment(todayCallKey, 1);
      redisTemplate.expire(todayCallKey, 3, TimeUnit.DAYS);

      // 今日成功调用数
      if (success) {
        String todaySuccessKey = REDIS_KEY_TODAY_SUCCESS + today;
        redisTemplate.opsForValue().increment(todaySuccessKey, 1);
        redisTemplate.expire(todaySuccessKey, 3, TimeUnit.DAYS);
      }

      System.out.println("[API测试] Redis计数器已更新, 成功: " + success);
    } catch (Exception e) {
      System.err.println("[API测试] 更新Redis计数器失败: " + e.getMessage());
    }
  }
}
