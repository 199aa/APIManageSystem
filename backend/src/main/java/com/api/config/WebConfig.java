package com.api.config;

import com.api.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Autowired
  private JwtInterceptor jwtInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(jwtInterceptor)
        // 拦截所有请求
        .addPathPatterns("/**")
        // 排除登录注册接口和Dashboard接口
        .excludePathPatterns(
            "/user/login",
            "/user/register",
            "/dashboard/**",
            "/error");
  }
}
