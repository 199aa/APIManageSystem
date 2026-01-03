package com.api.aspect;

import com.api.annotation.OperationLog;
import com.api.model.User;
import com.api.service.OperationLogService;
import com.api.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
public class OperationLogAspect {

  @Autowired
  private OperationLogService operationLogService;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private ObjectMapper objectMapper;

  @Pointcut("@annotation(com.api.annotation.OperationLog)")
  public void operationLogPointcut() {
  }

  @Around("operationLogPointcut()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    long startTime = System.currentTimeMillis();

    // 获取注解信息
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    OperationLog annotation = method.getAnnotation(OperationLog.class);

    // 获取请求信息
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

    // 构建日志对象
    com.api.model.OperationLog log = new com.api.model.OperationLog();
    log.setOperType(annotation.type());
    log.setModule(annotation.module());
    log.setDescription(annotation.description());

    if (request != null) {
      log.setRequestMethod(request.getMethod());
      log.setRequestUrl(request.getRequestURI());
      log.setIp(getIpAddress(request));
      log.setBrowser(request.getHeader("User-Agent"));

      // 获取用户信息
      String token = request.getHeader("Authorization");
      if (token != null && token.startsWith("Bearer ")) {
        try {
          String jwt = token.replace("Bearer ", "");
          String userId = jwtUtil.getUserIdFromToken(jwt);
          log.setUserId(Long.parseLong(userId));
          // 这里可以查询用户名，暂时使用ID
          log.setUsername("User" + userId);
        } catch (Exception e) {
          // Token解析失败，忽略
        }
      }

      // 获取请求参数
      try {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
          log.setRequestParams(objectMapper.writeValueAsString(args[0]));
        }
      } catch (Exception e) {
        // 参数序列化失败，忽略
      }
    }

    Object result = null;
    try {
      // 执行方法
      result = joinPoint.proceed();
      log.setStatus(1);

      // 记录响应结果（可选）
      try {
        String resultStr = objectMapper.writeValueAsString(result);
        if (resultStr.length() > 1000) {
          resultStr = resultStr.substring(0, 1000) + "...";
        }
        log.setResponseResult(resultStr);
      } catch (Exception e) {
        // 结果序列化失败，忽略
      }
    } catch (Exception e) {
      log.setStatus(0);
      log.setErrorMsg(e.getMessage());
      throw e;
    } finally {
      // 计算耗时
      long endTime = System.currentTimeMillis();
      log.setCostTime((int) (endTime - startTime));

      // 异步保存日志
      try {
        operationLogService.saveLog(log);
      } catch (Exception e) {
        // 日志保存失败不影响业务
        e.printStackTrace();
      }
    }

    return result;
  }

  /**
   * 获取客户端IP地址
   */
  private String getIpAddress(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    // 处理多个IP的情况
    if (ip != null && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }
    return ip;
  }
}
