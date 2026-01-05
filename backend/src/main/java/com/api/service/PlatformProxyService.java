package com.api.service;

import com.api.model.Platform;
import com.api.util.AesUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 平台代理服务
 * 负责处理不同认证方式的HTTP请求构造和健康检测
 */
@Service
public class PlatformProxyService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String REDIS_KEY_PLATFORM_STATUS = "platform:status:";
    private static final int DEFAULT_TIMEOUT = 5000;

    /**
     * 健康检测 - 测试平台连通性
     * 
     * @param platform 平台信息
     * @return 检测结果 {success: true/false, message: "", responseTime: 123}
     */
    public Map<String, Object> healthCheck(Platform platform) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            // 构造请求头
            HttpHeaders headers = buildHeaders(platform);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 确定健康检查URL
            String healthUrl = determineHealthUrl(platform);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    healthUrl,
                    HttpMethod.GET,
                    entity,
                    String.class);

            long responseTime = System.currentTimeMillis() - startTime;

            // 判断是否成功（200-399视为成功）
            boolean success = response.getStatusCode().value() < 400;
            result.put("success", success);
            result.put("statusCode", response.getStatusCode().value());
            result.put("responseTime", responseTime);
            result.put("message", success ? "连接正常" : "HTTP " + response.getStatusCode().value());

            // 更新Redis缓存
            updateStatusCache(platform.getId(), success ? "ONLINE" : "OFFLINE");

            return result;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // 4xx错误 - 客户端错误（但说明连接成功）
            long responseTime = System.currentTimeMillis() - startTime;
            int statusCode = e.getRawStatusCode();
            boolean success = statusCode == 404; // 404说明服务在线，只是路径不存在

            result.put("success", success);
            result.put("statusCode", statusCode);
            result.put("responseTime", responseTime);
            result.put("message", success ? "服务在线（路径未配置）" : "HTTP " + statusCode);

            updateStatusCache(platform.getId(), success ? "ONLINE" : "OFFLINE");
            return result;

        } catch (org.springframework.web.client.HttpServerErrorException e) {
            // 5xx错误 - 服务器错误（但说明连接成功）
            long responseTime = System.currentTimeMillis() - startTime;
            result.put("success", false);
            result.put("statusCode", e.getRawStatusCode());
            result.put("responseTime", responseTime);
            result.put("message", "服务器错误 HTTP " + e.getRawStatusCode());

            updateStatusCache(platform.getId(), "OFFLINE");
            return result;

        } catch (org.springframework.web.client.ResourceAccessException e) {
            // 连接超时或网络不可达
            long responseTime = System.currentTimeMillis() - startTime;
            result.put("success", false);
            result.put("responseTime", responseTime);

            String message = "网络不可达";
            if (e.getMessage().contains("timeout")) {
                message = "连接超时";
            } else if (e.getMessage().contains("UnknownHost")) {
                message = "域名解析失败";
            }
            result.put("message", message);

            updateStatusCache(platform.getId(), "OFFLINE");
            return result;

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            result.put("success", false);
            result.put("responseTime", responseTime);
            result.put("message", "检测失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());

            updateStatusCache(platform.getId(), "OFFLINE");
            return result;
        }
    }

    /**
     * 执行带认证的HTTP请求
     * 
     * @param platform 平台信息
     * @param path     请求路径
     * @param method   HTTP方法
     * @param body     请求体
     * @return 响应结果
     */
    public ResponseEntity<String> executeRequest(Platform platform, String path, HttpMethod method, String body) {
        try {
            // 构造完整URL
            String fullUrl = platform.getBaseUrl() + path;

            // 构造请求头
            HttpHeaders headers = buildHeaders(platform);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            // 发送请求
            return restTemplate.exchange(fullUrl, method, entity, String.class);

        } catch (Exception e) {
            throw new RuntimeException("请求平台失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据认证类型构造请求头
     */
    private HttpHeaders buildHeaders(Platform platform) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String authType = platform.getAuthType();
        if (authType == null || "NO_AUTH".equals(authType)) {
            return headers;
        }

        // 解密认证配置
        String authConfig = platform.getAuthConfig();
        if (authConfig == null || authConfig.isEmpty()) {
            return headers;
        }

        // 解密并解析认证配置
        String decryptedConfig = AesUtil.decrypt(authConfig);
        JsonNode configNode = objectMapper.readTree(decryptedConfig);

        switch (authType) {
            case "API_KEY":
                handleApiKeyAuth(headers, configNode);
                break;

            case "BEARER_TOKEN":
                handleBearerTokenAuth(headers, configNode);
                break;

            case "BASIC_AUTH":
                handleBasicAuth(headers, configNode);
                break;

            default:
                break;
        }

        return headers;
    }

    /**
     * 处理API Key认证
     */
    private void handleApiKeyAuth(HttpHeaders headers, JsonNode config) {
        String keyName = config.has("keyName") ? config.get("keyName").asText() : "X-API-Key";
        String keyValue = config.has("keyValue") ? config.get("keyValue").asText() : "";
        String location = config.has("location") ? config.get("location").asText() : "header";

        if ("header".equals(location)) {
            headers.set(keyName, keyValue);
        }
        // 如果是query参数，需要在URL中添加，这里暂不处理
    }

    /**
     * 处理Bearer Token认证
     */
    private void handleBearerTokenAuth(HttpHeaders headers, JsonNode config) {
        String token = config.has("token") ? config.get("token").asText() : "";
        headers.setBearerAuth(token);
    }

    /**
     * 处理Basic认证
     */
    private void handleBasicAuth(HttpHeaders headers, JsonNode config) {
        String username = config.has("username") ? config.get("username").asText() : "";
        String password = config.has("password") ? config.get("password").asText() : "";

        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
    }

    /**
     * 确定健康检查URL
     */
    private String determineHealthUrl(Platform platform) {
        String baseUrl = platform.getBaseUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new RuntimeException("平台BaseURL未配置");
        }

        // 如果有专门的健康检查路径，使用它
        // 否则直接使用baseUrl
        return baseUrl;
    }

    /**
     * 更新状态缓存
     */
    private void updateStatusCache(Long platformId, String status) {
        try {
            String key = REDIS_KEY_PLATFORM_STATUS + platformId;
            redisTemplate.opsForValue().set(key, status, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            System.err.println("更新平台状态缓存失败: " + e.getMessage());
        }
    }

    /**
     * 从缓存获取平台状态
     */
    public String getStatusFromCache(Long platformId) {
        try {
            String key = REDIS_KEY_PLATFORM_STATUS + platformId;
            Object status = redisTemplate.opsForValue().get(key);
            return status != null ? status.toString() : "UNKNOWN";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}
