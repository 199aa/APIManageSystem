package com.api.task;

import com.api.mapper.PlatformMapper;
import com.api.model.Platform;
import com.api.service.PlatformProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 平台健康检测定时任务
 * 每5分钟自动检测所有启用平台的健康状态
 */
@Component
public class PlatformHealthCheckTask {

    @Autowired
    private PlatformMapper platformMapper;

    @Autowired
    private PlatformProxyService platformProxyService;

    /**
     * 每5分钟执行一次健康检测
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void checkAllPlatforms() {
        System.out.println("开始平台健康检测...");
        
        try {
            // 查询所有启用的平台
            List<Platform> platforms = platformMapper.selectAllEnabled();
            
            if (platforms.isEmpty()) {
                System.out.println("没有需要检测的平台");
                return;
            }

            int onlineCount = 0;
            int offlineCount = 0;

            // 遍历检测每个平台
            for (Platform platform : platforms) {
                try {
                    // 执行健康检测
                    Map<String, Object> result = platformProxyService.healthCheck(platform);
                    
                    boolean success = (Boolean) result.get("success");
                    String healthStatus = success ? "ONLINE" : "OFFLINE";
                    
                    // 更新数据库状态
                    platformMapper.updateHealthStatus(platform.getId(), healthStatus);
                    
                    if (success) {
                        onlineCount++;
                        long responseTime = result.get("responseTime") != null ? 
                            ((Number) result.get("responseTime")).longValue() : 0;
                        System.out.println("✓ 平台 [" + platform.getName() + "] 在线 (" + responseTime + "ms)");
                    } else {
                        offlineCount++;
                        System.out.println("✗ 平台 [" + platform.getName() + "] 离线: " + result.get("message"));
                    }

                    // 避免并发请求过快
                    Thread.sleep(500);

                } catch (Exception e) {
                    offlineCount++;
                    try {
                        platformMapper.updateHealthStatus(platform.getId(), "OFFLINE");
                    } catch (Exception dbError) {
                        System.err.println("更新平台状态失败: " + dbError.getMessage());
                    }
                    System.err.println("✗ 平台 [" + platform.getName() + "] 检测异常: " + e.getMessage());
                }
            }

            System.out.println("平台健康检测完成 - 在线: " + onlineCount + ", 离线: " + offlineCount);

        } catch (Exception e) {
            System.err.println("平台健康检测任务执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 启动时延迟10秒后执行一次初始化检测
     */
    @Scheduled(initialDelay = 10000, fixedDelay = Long.MAX_VALUE)
    public void initializeHealthCheck() {
        System.out.println("执行平台健康状态初始化检测...");
        checkAllPlatforms();
    }
}
