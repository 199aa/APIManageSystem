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
     * 排除内部管理和监控相关的接口，只统计真正的业务API调用
     * 
     * 被排除的Controller（不统计）：
     * - DashboardController: 仪表板统计
     * - AlertController: 告警管理
     * - ApiCallLogController: 调用日志查询
     * - OperationLogController: 操作日志
     * - SystemUserController: 系统用户管理
     * - RoleController: 角色管理
     * - PermissionController: 权限管理
     * - UserController: 用户相关
     * - ApiInfoController: API元数据管理
     * - PlatformController: 平台接入管理
     * - CustomerAppController: 客户应用管理
     * - GovernanceController: 治理规则管理
     * - AggregateController: 聚合接口管理
     * 
     * 被统计的Controller（真正的业务API）：
     * - ApiController: 对外提供的业务API
     */
    @Pointcut("execution(* com.api.controller..*.*(..)) && " +
            "!execution(* com.api.controller.DashboardController.*(..)) && " +
            "!execution(* com.api.controller.AlertController.*(..)) && " +
            "!execution(* com.api.controller.ApiCallLogController.*(..)) && " +
            "!execution(* com.api.controller.OperationLogController.*(..)) && " +
            "!execution(* com.api.controller.SystemUserController.*(..)) && " +
            "!execution(* com.api.controller.RoleController.*(..)) && " +
            "!execution(* com.api.controller.PermissionController.*(..)) && " +
            "!execution(* com.api.controller.UserController.*(..)) && " +
            "!execution(* com.api.controller.ApiInfoController.*(..)) && " +
            "!execution(* com.api.controller.PlatformController.*(..)) && " +
            "!execution(* com.api.controller.CustomerAppController.*(..)) && " +
            "!execution(* com.api.controller.GovernanceController.*(..)) && " +
            "!execution(* com.api.controller.AggregateController.*(..))")
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
            String method = request.getMethod();
            log.setApiPath(path);
            log.setApiName(extractApiName(path));
            // 添加调试日志，帮助定位哪些接口被统计
            System.out.println("========================================");
            System.out.println("[API统计] 拦截到API调用");
            System.out.println("[API统计] 路径: " + path);
            System.out.println("[API统计] 方法: " + method);
            System.out.println("[API统计] Controller: " + joinPoint.getSignature().getDeclaringTypeName());
            System.out.println("[API统计] Method: " + joinPoint.getSignature().getName());
            System.out.println("========================================");
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
            System.out.println("[API统计] 调用成功，状态码: 200");
        } catch (Exception e) {
            success = false;
            log.setIsSuccess(0);
            statusCode = 500;
            errorMsg = e.getMessage();
            log.setErrorMsg(errorMsg);
            System.out.println("[API统计] 调用失败: " + errorMsg);
            throw e;
        } finally {
            // 计算响应时间
            long costTime = System.currentTimeMillis() - startTime;
            log.setResponseTime((int) costTime);
            log.setStatusCode(statusCode);
            System.out.println("[API统计] 响应时间: " + costTime + "ms");

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

            System.out.println("[Redis统计] 开始更新计数器, 日期: " + today);

            // 累计调用总数
            Long totalCalls = redisTemplate.opsForValue().increment(REDIS_KEY_TOTAL_CALLS, 1);
            System.out.println("[Redis统计] 累计调用总数: " + totalCalls);

            // 今日调用数
            String todayCallKey = REDIS_KEY_TODAY_CALLS + today;
            Long todayCalls = redisTemplate.opsForValue().increment(todayCallKey, 1);
            System.out.println("[Redis统计] 今日调用数 (Key: " + todayCallKey + "): " + todayCalls);
            // 设置过期时间为3天
            redisTemplate.expire(todayCallKey, 3, java.util.concurrent.TimeUnit.DAYS);

            // 今日成功调用数
            if (success) {
                String todaySuccessKey = REDIS_KEY_TODAY_SUCCESS + today;
                Long todaySuccess = redisTemplate.opsForValue().increment(todaySuccessKey, 1);
                System.out.println("[Redis统计] 今日成功调用数 (Key: " + todaySuccessKey + "): " + todaySuccess);
                // 设置过期时间为3天
                redisTemplate.expire(todaySuccessKey, 3, java.util.concurrent.TimeUnit.DAYS);
            }

            System.out.println("[Redis统计] 计数器更新完成");
        } catch (Exception e) {
            // Redis操作失败，不影响主流程
            System.err.println("[Redis统计] 更新计数器失败: " + e.getMessage());
            e.printStackTrace();
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
