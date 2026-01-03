package com.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP客户端配置
 * 用于平台健康检测和API调用
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 连接超时3秒（建立连接）
        factory.setConnectTimeout(3000);
        // 读取超时5秒（等待响应）
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }
}
