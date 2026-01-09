package com.api.service;

import com.api.mapper.ApiInfoMapper;
import com.api.model.ApiInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;

/**
 * 聚合接口执行服务
 * 负责聚合接口的动态编排执行、参数映射、结果聚合等
 */
@Service
public class AggregateExecutionService {

    @Autowired
    private ApiInfoMapper apiInfoMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlatformProxyService platformProxyService;

    /**
     * 执行聚合接口
     * 
     * @param aggregateApi 聚合接口信息
     * @param inputParams  输入参数
     * @return 执行结果
     */
    public Map<String, Object> execute(ApiInfo aggregateApi, Map<String, Object> inputParams) throws Exception {
        if (aggregateApi.getAggregateConfig() == null || aggregateApi.getAggregateConfig().isEmpty()) {
            throw new RuntimeException("聚合接口配置不能为空");
        }

        // 解析配置
        JsonNode config = objectMapper.readTree(aggregateApi.getAggregateConfig());
        String executeMode = config.has("executeMode") ? config.get("executeMode").asText() : "serial";
        int totalTimeout = config.has("timeout") ? config.get("timeout").asInt() : 30000;
        JsonNode nodesConfig = config.get("nodes");

        if (nodesConfig == null || !nodesConfig.isArray()) {
            throw new RuntimeException("聚合接口节点配置不能为空");
        }

        // 执行节点
        Map<String, Object> context = new HashMap<>();
        context.put("input", inputParams);
        context.put("results", new HashMap<String, Object>());

        long startTime = System.currentTimeMillis();

        if ("serial".equals(executeMode)) {
            // 串行执行
            return executeSerial(nodesConfig, context, totalTimeout);
        } else {
            // 并行执行
            return executeParallel(nodesConfig, context, totalTimeout);
        }
    }

    /**
     * 串行执行
     */
    private Map<String, Object> executeSerial(JsonNode nodesConfig, Map<String, Object> context, int timeout)
            throws Exception {
        Map<String, Object> results = (Map<String, Object>) context.get("results");

        for (JsonNode nodeConfig : nodesConfig) {
            try {
                Map<String, Object> nodeResult = executeNode(nodeConfig, context);
                String responsePath = nodeConfig.has("responsePath") ? nodeConfig.get("responsePath").asText()
                        : "result";
                results.put(responsePath, nodeResult);

            } catch (Exception e) {
                String onError = nodeConfig.has("onError") ? nodeConfig.get("onError").asText() : "abort";
                if ("abort".equals(onError)) {
                    throw new RuntimeException("节点执行失败: " + e.getMessage(), e);
                } else if ("continue".equals(onError)) {
                    String responsePath = nodeConfig.has("responsePath") ? nodeConfig.get("responsePath").asText()
                            : "result";
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("error", true);
                    errorResult.put("message", e.getMessage());
                    results.put(responsePath, errorResult);
                }
            }
        }

        return buildResponse(results);
    }

    /**
     * 并行执行
     */
    private Map<String, Object> executeParallel(JsonNode nodesConfig, Map<String, Object> context, int timeout)
            throws Exception {
        Map<String, Object> results = (Map<String, Object>) context.get("results");
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<NodeExecutionResult>> futures = new ArrayList<>();

        // 提交所有任务
        for (JsonNode nodeConfig : nodesConfig) {
            Future<NodeExecutionResult> future = executor.submit(() -> {
                try {
                    Map<String, Object> nodeResult = executeNode(nodeConfig, context);
                    String responsePath = nodeConfig.has("responsePath") ? nodeConfig.get("responsePath").asText()
                            : "result";
                    return new NodeExecutionResult(responsePath, nodeResult, null);
                } catch (Exception e) {
                    String responsePath = nodeConfig.has("responsePath") ? nodeConfig.get("responsePath").asText()
                            : "result";
                    return new NodeExecutionResult(responsePath, null, e);
                }
            });
            futures.add(future);
        }

        // 收集结果
        for (Future<NodeExecutionResult> future : futures) {
            try {
                NodeExecutionResult result = future.get(timeout, TimeUnit.MILLISECONDS);
                if (result.error != null) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("error", true);
                    errorResult.put("message", result.error.getMessage());
                    results.put(result.path, errorResult);
                } else {
                    results.put(result.path, result.data);
                }
            } catch (TimeoutException e) {
                future.cancel(true);
                throw new RuntimeException("节点执行超时");
            }
        }

        executor.shutdown();
        return buildResponse(results);
    }

    /**
     * 执行单个节点
     */
    private Map<String, Object> executeNode(JsonNode nodeConfig, Map<String, Object> context) throws Exception {
        Long apiId = nodeConfig.get("apiId").asLong();
        int timeout = nodeConfig.has("timeout") ? nodeConfig.get("timeout").asInt() : 5000;

        // 获取节点配置的apiPath（可能包含占位符）
        String apiPath = nodeConfig.has("apiPath") ? nodeConfig.get("apiPath").asText() : null;

        // 查询API信息
        ApiInfo apiInfo = apiInfoMapper.selectById(apiId);
        if (apiInfo == null) {
            throw new RuntimeException("API不存在: " + apiId);
        }

        // 如果节点配置了apiPath，使用节点的配置（可能包含动态参数）
        if (apiPath != null && !apiPath.isEmpty()) {
            apiInfo.setPath(apiPath);
        }

        // 参数映射 - 从paramMappings获取
        Map<String, Object> params = mapParameters(nodeConfig.get("paramMappings"), context);

        // 合并输入参数用于占位符替换
        Map<String, Object> inputParams = (Map<String, Object>) context.get("input");
        if (inputParams != null) {
            for (Map.Entry<String, Object> entry : inputParams.entrySet()) {
                if (!params.containsKey(entry.getKey())) {
                    params.put(entry.getKey(), entry.getValue());
                }
            }
        }

        // 调用API
        String url = buildUrl(apiInfo, params);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity;
        if ("GET".equalsIgnoreCase(apiInfo.getMethod())) {
            entity = new HttpEntity<>(headers);
        } else {
            String body = objectMapper.writeValueAsString(params);
            entity = new HttpEntity<>(body, headers);
        }

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.valueOf(apiInfo.getMethod().toUpperCase()),
                entity,
                String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("API调用失败: HTTP " + response.getStatusCode());
        }

        // 解析响应
        return objectMapper.readValue(response.getBody(), Map.class);
    }

    /**
     * 参数映射
     */
    private Map<String, Object> mapParameters(JsonNode mappings, Map<String, Object> context) {
        Map<String, Object> params = new HashMap<>();
        if (mappings == null || !mappings.isArray()) {
            return params;
        }

        for (JsonNode mapping : mappings) {
            String source = mapping.get("source").asText();
            String target = mapping.get("target").asText();

            Object value = getValueFromPath(context, source);
            if (value != null) {
                params.put(target, value);
            }
        }

        return params;
    }

    /**
     * 从路径获取值 (支持 input.userId, results.user.name 等)
     */
    private Object getValueFromPath(Map<String, Object> context, String path) {
        String[] parts = path.split("\\.");
        Object current = context;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map) current).get(part);
            } else {
                return null;
            }
        }

        return current;
    }

    /**
     * 构建URL
     */
    private String buildUrl(ApiInfo apiInfo, Map<String, Object> params) {
        String path = apiInfo.getPath();

        // 处理以冒号开头的端口格式，如 :8083/api/v1/user
        if (path.startsWith(":")) {
            path = path.substring(1); // 去掉开头的冒号
        }

        // 替换路径中的占位符 {paramName}
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            if (path.contains(placeholder)) {
                path = path.replace(placeholder, String.valueOf(entry.getValue()));
            }
        }

        // 如果path已经是完整URL，直接使用
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }

        // 否则需要从平台配置获取baseUrl
        String baseUrl = "http://localhost:8083"; // 默认端口

        // 如果path是 8083/api/v1/user 格式，提取端口
        if (path.matches("^\\d+/.*")) {
            String[] parts = path.split("/", 2);
            baseUrl = "http://localhost:" + parts[0];
            path = "/" + parts[1];
        } else if (!path.startsWith("/")) {
            path = "/" + path;
        }

        return baseUrl + path;
    }

    /**
     * 构建响应
     */
    private Map<String, Object> buildResponse(Map<String, Object> results) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", results);
        return response;
    }

    /**
     * 节点执行结果
     */
    private static class NodeExecutionResult {
        String path;
        Map<String, Object> data;
        Exception error;

        public NodeExecutionResult(String path, Map<String, Object> data, Exception error) {
            this.path = path;
            this.data = data;
            this.error = error;
        }
    }
}
