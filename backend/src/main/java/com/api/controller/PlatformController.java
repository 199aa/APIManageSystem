package com.api.controller;

import com.api.common.Result;
import com.api.model.Platform;
import com.api.mapper.PlatformMapper;
import com.api.service.PlatformProxyService;
import com.api.service.PlatformDiscoveryService;
import com.api.util.AesUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/platform")
public class PlatformController {

    @Autowired
    private PlatformMapper platformMapper;

    @Autowired
    private PlatformProxyService platformProxyService;

    @Autowired
    private PlatformDiscoveryService platformDiscoveryService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取平台列表（分页）
     */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<Platform> list = platformMapper.selectPage(offset, pageSize);
        int total = platformMapper.countAll();
        
        // 脱敏处理authConfig并添加健康状态
        for (Platform platform : list) {
            maskAuthConfig(platform);
            // 从缓存获取健康状态
            String cachedStatus = platformProxyService.getStatusFromCache(platform.getId());
            if (!"UNKNOWN".equals(cachedStatus)) {
                platform.setHealthStatus(cachedStatus);
            }
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        
        return Result.success(data);
    }

    /**
     * 获取所有平台（用于下拉选择）
     */
    @GetMapping("/all")
    public Result all() {
        List<Platform> list = platformMapper.selectAll();
        return Result.success(list);
    }

    /**
     * 获取平台详情
     */
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        Platform platform = platformMapper.selectById(id);
        if (platform == null) {
            return Result.error("平台不存在");
        }
        return Result.success(platform);
    }

    /**
     * 保存平台（新增/编辑）
     */
    @com.api.annotation.OperationLog(type = "SAVE", module = "platform", description = "保存平台")
    @PostMapping("/save")
    public Result save(@RequestBody Platform platform) {
        try {
            // 加密authConfig
            encryptAuthConfig(platform);
            
            if (platform.getId() == null) {
                // 检查编码是否重复
                Platform existing = platformMapper.selectByCode(platform.getCode());
                if (existing != null) {
                    return Result.error("平台编码已存在");
                }
                platformMapper.insert(platform);
            } else {
                // 检查编码是否被其他平台使用
                Platform existing = platformMapper.selectByCode(platform.getCode());
                if (existing != null && !existing.getId().equals(platform.getId())) {
                    return Result.error("平台编码已被其他平台使用");
                }
                platformMapper.update(platform);
            }
            return Result.success("保存成功");
        } catch (Exception e) {
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 删除平台
     */
    @com.api.annotation.OperationLog(type = "DELETE", module = "platform", description = "删除平台")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        platformMapper.deleteById(id);
        return Result.success("删除成功");
    }

    /**
     * 切换平台状态
     */
    @com.api.annotation.OperationLog(type = "UPDATE", module = "platform", description = "切换平台状态")
    @PutMapping("/{id}/status")
    public Result toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        platformMapper.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    /**
     * 健康检测 - 测试平台连接
     */
    @com.api.annotation.OperationLog(type = "CHECK", module = "platform", description = "平台健康检测")
    @PostMapping("/{id}/health-check")
    public Result healthCheck(@PathVariable Long id) {
        try {
            Platform platform = platformMapper.selectById(id);
            if (platform == null) {
                return Result.error("平台不存在");
            }

            Map<String, Object> result = platformProxyService.healthCheck(platform);
            
            // 更新数据库中的健康状态
            String healthStatus = (Boolean) result.get("success") ? "ONLINE" : "OFFLINE";
            platformMapper.updateHealthStatus(id, healthStatus);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("健康检测失败: " + e.getMessage());
        }
    }

    /**
     * 同步API接口 - 从Swagger文档导入
     */
    @com.api.annotation.OperationLog(type = "SYNC", module = "platform", description = "同步平台API")
    @PostMapping("/{id}/sync-apis")
    public Result syncApis(@PathVariable Long id) {
        try {
            Platform platform = platformMapper.selectById(id);
            if (platform == null) {
                return Result.error("平台不存在");
            }

            if (platform.getSwaggerUrl() == null || platform.getSwaggerUrl().isEmpty()) {
                return Result.error("该平台未配置Swagger地址");
            }

            Map<String, Object> result = platformDiscoveryService.syncApisFromSwagger(platform);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("同步API失败: " + e.getMessage());
        }
    }

    /**
     * 获取平台统计信息
     */
    @GetMapping("/stats")
    public Result getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPlatforms", platformMapper.countAll());
            stats.put("offlinePlatforms", platformMapper.countOfflinePlatforms());
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 加密认证配置
     */
    private void encryptAuthConfig(Platform platform) throws Exception {
        String authConfig = platform.getAuthConfig();
        if (authConfig != null && !authConfig.isEmpty() && !AesUtil.isEncrypted(authConfig)) {
            platform.setAuthConfig(AesUtil.encrypt(authConfig));
        }
    }

    /**
     * 脱敏认证配置
     */
    private void maskAuthConfig(Platform platform) {
        try {
            String authConfig = platform.getAuthConfig();
            if (authConfig != null && !authConfig.isEmpty()) {
                // 解密
                String decrypted = AesUtil.decrypt(authConfig);
                // 解析JSON
                Map<String, Object> configMap = objectMapper.readValue(decrypted, Map.class);
                
                // 脱敏处理
                for (Map.Entry<String, Object> entry : configMap.entrySet()) {
                    String key = entry.getKey();
                    if (key.contains("key") || key.contains("token") || key.contains("password") || key.contains("secret")) {
                        String value = entry.getValue().toString();
                        configMap.put(key, AesUtil.mask(value));
                    }
                }
                
                // 重新序列化（已脱敏）
                platform.setAuthConfig(objectMapper.writeValueAsString(configMap));
            }
        } catch (Exception e) {
            // 脱敏失败，不影响主流程
            platform.setAuthConfig("****");
        }
    }
}
