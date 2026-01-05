package com.api.config;

import com.api.interceptor.JwtInterceptor;
import com.api.interceptor.PermissionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Autowired
  private JwtInterceptor jwtInterceptor;

  @Autowired
  private PermissionInterceptor permissionInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // JWT认证拦截器
    registry.addInterceptor(jwtInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/user/login",
            "/user/register",
            "/dashboard/**",
            "/error");
    
    // 权限拦截器（在JWT之后执行）
    registry.addInterceptor(permissionInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/user/login",
            "/user/register",
            "/dashboard/**",
            "/error");
  }
}
