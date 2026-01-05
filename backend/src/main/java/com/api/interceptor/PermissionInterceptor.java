package com.api.interceptor;

import com.api.model.Permission;
import com.api.model.User;
import com.api.service.PermissionService;
import com.api.service.UserService;
import com.api.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限拦截器
 * 验证用户是否有访问接口的权限
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只拦截Controller方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 获取请求路径
        String requestUri = request.getRequestURI();
        
        // 从请求头获取token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendJsonResponse(response, 401, "未登录");
            return false;
        }

        String token = authHeader.substring(7);
        String userId = jwtUtil.getUserIdFromToken(token);
        
        // 获取用户信息
        User user = userService.getUserById(Long.parseLong(userId));
        if (user == null) {
            sendJsonResponse(response, 401, "用户不存在");
            return false;
        }

        // 超级管理员（角色ID=1）拥有所有权限
        if (user.getRoleId() == 1L) {
            return true;
        }

        // 获取用户的所有权限
        List<Permission> permissions = permissionService.getPermissionsByRoleId(user.getRoleId());
        List<String> permissionCodes = permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toList());

        // 检查是否有权限访问该路径
        boolean hasPermission = checkPermission(requestUri, request.getMethod(), permissionCodes);
        
        if (!hasPermission) {
            sendJsonResponse(response, 403, "无权限访问");
            return false;
        }

        return true;
    }

    /**
     * 检查权限
     * 权限检查策略：检查用户是否拥有模块级权限（如 api, platform, monitor）
     */
    private boolean checkPermission(String uri, String method, List<String> permissionCodes) {
        // 定义路径与权限的映射关系（使用模块级权限）
        Map<String, String> pathPermissionMap = new HashMap<>();
        
        // 平台接入 - 需要 platform 模块权限
        pathPermissionMap.put("GET:/platform/list", "platform");
        pathPermissionMap.put("GET:/platform/", "platform");
        pathPermissionMap.put("POST:/platform", "platform");
        pathPermissionMap.put("PUT:/platform", "platform");
        pathPermissionMap.put("DELETE:/platform/", "platform");
        
        // API管理 - 需要 api 模块权限
        pathPermissionMap.put("GET:/api-info/list", "api");
        pathPermissionMap.put("GET:/api-info/", "api");
        pathPermissionMap.put("POST:/api-info", "api");
        pathPermissionMap.put("PUT:/api-info", "api");
        pathPermissionMap.put("DELETE:/api-info/", "api");
        pathPermissionMap.put("POST:/api-info/test", "api");
        
        // 客户应用 - 需要 customer 模块权限
        pathPermissionMap.put("GET:/customer/app/list", "customer");
        pathPermissionMap.put("GET:/customer/app/", "customer");
        pathPermissionMap.put("POST:/customer/app", "customer");
        pathPermissionMap.put("PUT:/customer/app", "customer");
        pathPermissionMap.put("DELETE:/customer/app/", "customer");
        
        // 编排设计 - 需要 orchestration 模块权限
        pathPermissionMap.put("GET:/aggregate/list", "orchestration");
        pathPermissionMap.put("GET:/aggregate/", "orchestration");
        pathPermissionMap.put("POST:/aggregate", "orchestration");
        pathPermissionMap.put("PUT:/aggregate", "orchestration");
        pathPermissionMap.put("DELETE:/aggregate/", "orchestration");
        
        // 治理中心 - 需要 governance 模块权限
        pathPermissionMap.put("GET:/governance/rate-limit/list", "governance");
        pathPermissionMap.put("POST:/governance/rate-limit", "governance");
        pathPermissionMap.put("PUT:/governance/rate-limit", "governance");
        pathPermissionMap.put("DELETE:/governance/rate-limit/", "governance");
        pathPermissionMap.put("GET:/governance/blacklist", "governance");
        pathPermissionMap.put("POST:/governance/blacklist", "governance");
        pathPermissionMap.put("PUT:/governance/blacklist", "governance");
        pathPermissionMap.put("DELETE:/governance/blacklist/", "governance");
        pathPermissionMap.put("GET:/governance/cache", "governance");
        pathPermissionMap.put("POST:/governance/cache", "governance");
        
        // 监控中心 - 需要 monitor 模块权限
        pathPermissionMap.put("GET:/monitor/api-log/list", "monitor");
        pathPermissionMap.put("GET:/monitor/alert/list", "monitor");
        pathPermissionMap.put("GET:/monitor/alert/", "monitor");
        pathPermissionMap.put("POST:/monitor/alert", "monitor");
        
        // 系统管理 - 需要具体的系统权限
        pathPermissionMap.put("GET:/system/user/list", "system:user:list");
        pathPermissionMap.put("POST:/system/user", "system:user:create");
        pathPermissionMap.put("PUT:/system/user", "system:user:update");
        pathPermissionMap.put("DELETE:/system/user/", "system:user:delete");
        
        pathPermissionMap.put("GET:/system/role/list", "system:role:list");
        pathPermissionMap.put("GET:/system/role/all", "system:role:list");
        pathPermissionMap.put("POST:/system/role", "system:role:create");
        pathPermissionMap.put("PUT:/system/role", "system:role:update");
        pathPermissionMap.put("DELETE:/system/role/", "system:role:delete");
        
        pathPermissionMap.put("GET:/system/permission", "system:role:list");
        pathPermissionMap.put("POST:/system/permission/role/", "system:role:update");
        
        pathPermissionMap.put("GET:/system/log/list", "system:log:list");

        // 检查路径权限
        String key = method + ":" + uri;
        for (Map.Entry<String, String> entry : pathPermissionMap.entrySet()) {
            String pattern = entry.getKey();
            String requiredPermission = entry.getValue();
            
            if (matchPath(key, pattern)) {
                return permissionCodes.contains(requiredPermission);
            }
        }

        // 默认允许访问（未配置的路径）
        return true;
    }

    /**
     * 路径匹配
     */
    private boolean matchPath(String path, String pattern) {
        if (pattern.endsWith("/")) {
            return path.startsWith(pattern);
        }
        return path.equals(pattern) || path.startsWith(pattern + "/");
    }

    /**
     * 发送JSON响应
     */
    private void sendJsonResponse(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        result.put("data", null);
        
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
