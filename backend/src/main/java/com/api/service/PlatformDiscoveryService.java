package com.api.service;

import com.api.mapper.ApiInfoMapper;
import com.api.model.ApiInfo;
import com.api.model.Platform;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * API自动发现服务
 * 负责从Swagger/OpenAPI文档中自动解析并导入API接口信息
 */
@Service
public class PlatformDiscoveryService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiInfoMapper apiInfoMapper;

    @Autowired
    private PlatformProxyService platformProxyService;

    /**
     * 从Swagger文档同步API接口
     * 
     * @param platform 平台信息
     * @return 同步结果 {success: true/false, count: 10, message: ""}
     */
    public Map<String, Object> syncApisFromSwagger(Platform platform) {
        try {
            String swaggerUrl = platform.getSwaggerUrl();
            if (swaggerUrl == null || swaggerUrl.isEmpty()) {
                return createResult(false, 0, "Swagger地址未配置");
            }

            // 获取Swagger文档
            String swaggerJson = fetchSwaggerDoc(platform, swaggerUrl);
            if (swaggerJson == null) {
                return createResult(false, 0, "无法获取Swagger文档");
            }

            // 解析Swagger文档
            JsonNode rootNode = objectMapper.readTree(swaggerJson);

            // 判断Swagger版本
            List<ApiInfo> apis = new ArrayList<>();
            if (rootNode.has("swagger")) {
                // Swagger 2.0
                apis = parseSwaggerV2(rootNode, platform);
            } else if (rootNode.has("openapi")) {
                // OpenAPI 3.0
                apis = parseOpenApiV3(rootNode, platform);
            } else {
                return createResult(false, 0, "不支持的文档格式");
            }

            // 批量导入API
            int importCount = importApis(apis, platform);

            return createResult(true, importCount, "成功同步" + importCount + "个API接口");

        } catch (Exception e) {
            return createResult(false, 0, "同步失败: " + e.getMessage());
        }
    }

    /**
     * 获取Swagger文档
     */
    private String fetchSwaggerDoc(Platform platform, String swaggerUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    swaggerUrl,
                    HttpMethod.GET,
                    entity,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }

            return null;
        } catch (Exception e) {
            System.err.println("获取Swagger文档失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 解析Swagger 2.0文档
     */
    private List<ApiInfo> parseSwaggerV2(JsonNode rootNode, Platform platform) {
        List<ApiInfo> apis = new ArrayList<>();

        JsonNode pathsNode = rootNode.get("paths");
        if (pathsNode == null) {
            return apis;
        }

        Iterator<String> pathIterator = pathsNode.fieldNames();
        while (pathIterator.hasNext()) {
            String path = pathIterator.next();
            JsonNode pathItem = pathsNode.get(path);

            // 遍历HTTP方法
            Iterator<String> methodIterator = pathItem.fieldNames();
            while (methodIterator.hasNext()) {
                String method = methodIterator.next();
                if (!isValidHttpMethod(method)) {
                    continue;
                }

                JsonNode operation = pathItem.get(method);
                ApiInfo api = new ApiInfo();
                api.setPath(path);
                api.setMethod(method.toUpperCase());
                api.setPlatformId(platform.getId());
                api.setStatus(1);

                // 提取API信息
                if (operation.has("summary")) {
                    api.setName(operation.get("summary").asText());
                } else {
                    api.setName(method.toUpperCase() + " " + path);
                }

                if (operation.has("description")) {
                    api.setDescription(operation.get("description").asText());
                }

                // 提取标签
                if (operation.has("tags") && operation.get("tags").isArray()) {
                    JsonNode tagsNode = operation.get("tags");
                    if (tagsNode.size() > 0) {
                        String tag = tagsNode.get(0).asText();
                        // 可以将tag存储到description或其他字段
                    }
                }

                apis.add(api);
            }
        }

        return apis;
    }

    /**
     * 解析OpenAPI 3.0文档
     */
    private List<ApiInfo> parseOpenApiV3(JsonNode rootNode, Platform platform) {
        List<ApiInfo> apis = new ArrayList<>();

        JsonNode pathsNode = rootNode.get("paths");
        if (pathsNode == null) {
            return apis;
        }

        Iterator<String> pathIterator = pathsNode.fieldNames();
        while (pathIterator.hasNext()) {
            String path = pathIterator.next();
            JsonNode pathItem = pathsNode.get(path);

            Iterator<String> methodIterator = pathItem.fieldNames();
            while (methodIterator.hasNext()) {
                String method = methodIterator.next();
                if (!isValidHttpMethod(method)) {
                    continue;
                }

                JsonNode operation = pathItem.get(method);
                ApiInfo api = new ApiInfo();
                api.setPath(path);
                api.setMethod(method.toUpperCase());
                api.setPlatformId(platform.getId());
                api.setStatus(1);

                if (operation.has("summary")) {
                    api.setName(operation.get("summary").asText());
                } else {
                    api.setName(method.toUpperCase() + " " + path);
                }

                if (operation.has("description")) {
                    api.setDescription(operation.get("description").asText());
                }

                apis.add(api);
            }
        }

        return apis;
    }

    /**
     * 批量导入API
     */
    private int importApis(List<ApiInfo> apis, Platform platform) {
        int count = 0;

        for (ApiInfo api : apis) {
            try {
                // 检查是否已存在
                ApiInfo existingApi = apiInfoMapper.selectByPathAndMethod(api.getPath(), api.getMethod());
                if (existingApi == null) {
                    // 新增
                    apiInfoMapper.insert(api);
                    count++;
                } else {
                    // 可选：更新现有API信息
                    // apiInfoMapper.update(api);
                }
            } catch (Exception e) {
                System.err.println("导入API失败: " + api.getPath() + " - " + e.getMessage());
            }
        }

        return count;
    }

    /**
     * 判断是否为有效的HTTP方法
     */
    private boolean isValidHttpMethod(String method) {
        return "get".equalsIgnoreCase(method) ||
                "post".equalsIgnoreCase(method) ||
                "put".equalsIgnoreCase(method) ||
                "delete".equalsIgnoreCase(method) ||
                "patch".equalsIgnoreCase(method) ||
                "head".equalsIgnoreCase(method) ||
                "options".equalsIgnoreCase(method);
    }

    /**
     * 创建结果对象
     */
    private Map<String, Object> createResult(boolean success, int count, String message) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", success);
        result.put("count", count);
        result.put("message", message);
        return result;
    }
}
