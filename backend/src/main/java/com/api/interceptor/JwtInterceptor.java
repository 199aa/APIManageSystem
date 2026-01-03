package com.api.interceptor;

import com.api.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtInterceptor implements HandlerInterceptor {

  @Autowired
  private JwtUtil jwtUtil;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    // 处理OPTIONS请求
    if ("OPTIONS".equals(request.getMethod())) {
      return true;
    }

    // 从请求头获取token
    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      sendJsonResponse(response, 401, "未授权，请先登录");
      return false;
    }

    String token = authHeader.substring(7);

    try {
      if (!jwtUtil.validateToken(token)) {
        sendJsonResponse(response, 401, "Token无效或已过期");
        return false;
      }

      // 将用户ID存入request attribute，方便controller使用
      String userId = jwtUtil.getUserIdFromToken(token);
      request.setAttribute("userId", userId);

      return true;
    } catch (Exception e) {
      sendJsonResponse(response, 401, "Token验证失败");
      return false;
    }
  }

  private void sendJsonResponse(HttpServletResponse response, int code, String message) throws Exception {
    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(code);

    Map<String, Object> result = new HashMap<>();
    result.put("code", code);
    result.put("message", message);

    ObjectMapper mapper = new ObjectMapper();
    response.getWriter().write(mapper.writeValueAsString(result));
  }
}
