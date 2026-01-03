package com.api.aspect;

import com.api.mapper.ApiCallLogMapper;
import com.api.model.ApiCallLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * API调用日志切面
 * 用于记录API调用日志到数据库，并实时更新Redis计数器
 */
@Aspect
@Component
public class ApiCallLogAspect {

    @Autowired
    private ApiCallLogMapper apiCallLogMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY_TOTAL_CALLS = "dashboard:total_calls";
    private static final String REDIS_KEY_TODAY_CALLS = "dashboard:today_calls:";
    private static final String REDIS_KEY_TODAY_SUCCESS = "dashboard:today_success:";

    /**
     * 定义切点：拦截所有Controller方法
     */
    @Pointcut("execution(* com.api.controller..*.*(..)) && " +
              "!execution(* com.api.controller.DashboardController.*(..)) && " +
              "!execution(* com.api.controller.UserController.login(..))")
    public void apiCallPointcut() {
    }

    @Around("apiCallPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 构建日志对象
        ApiCallLog log = new ApiCallLog();
        
        if (request != null) {
            String path = request.getRequestURI();
            log.setApiPath(path);
            log.setApiName(extractApiName(path));
        }

        Object result = null;
        boolean success = true;
        int statusCode = 200;
        String errorMsg = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
            log.setIsSuccess(1);
            statusCode = 200;
        } catch (Exception e) {
            success = false;
            log.setIsSuccess(0);
            statusCode = 500;
            errorMsg = e.getMessage();
            log.setErrorMsg(errorMsg);
            throw e;
        } finally {
            // 计算响应时间
            long costTime = System.currentTimeMillis() - startTime;
            log.setResponseTime((int) costTime);
            log.setStatusCode(statusCode);

            // 异步记录日志到数据库（避免阻塞主流程）
            recordLogAsync(log);

            // 更新Redis计数器
            updateRedisCounters(success);
        }

        return result;
    }

    /**
     * 异步记录日志到数据库
     */
    private void recordLogAsync(ApiCallLog log) {
        new Thread(() -> {
            try {
                apiCallLogMapper.insert(log);
            } catch (Exception e) {
                // 记录日志失败，不影响主流程
                System.err.println("记录API调用日志失败: " + e.getMessage());
            }
        }).start();
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
            // 设置过期时间为3天
            redisTemplate.expire(todayCallKey, 3, java.util.concurrent.TimeUnit.DAYS);
            
            // 今日成功调用数
            if (success) {
                String todaySuccessKey = REDIS_KEY_TODAY_SUCCESS + today;
                redisTemplate.opsForValue().increment(todaySuccessKey, 1);
                // 设置过期时间为3天
                redisTemplate.expire(todaySuccessKey, 3, java.util.concurrent.TimeUnit.DAYS);
            }
        } catch (Exception e) {
            // Redis操作失败，不影响主流程
            System.err.println("更新Redis计数器失败: " + e.getMessage());
        }
    }

    /**
     * 从路径提取API名称
     */
    private String extractApiName(String path) {
        if (path == null || path.isEmpty()) {
            return "Unknown";
        }
        String[] parts = path.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : "Unknown";
    }
}
